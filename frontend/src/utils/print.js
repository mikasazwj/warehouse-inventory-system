/**
 * 打印工具函数
 */

/**
 * 打印指定元素
 * @param {string|HTMLElement} element - 要打印的元素或选择器
 * @param {Object} options - 打印选项
 */
export const printElement = (element, options = {}) => {
  const defaultOptions = {
    title: '打印文档',
    styles: '',
    beforePrint: null,
    afterPrint: null,
    removeAfterPrint: true
  }
  
  const config = { ...defaultOptions, ...options }
  
  // 获取要打印的元素
  let printElement
  if (typeof element === 'string') {
    printElement = document.querySelector(element)
  } else {
    printElement = element
  }
  
  if (!printElement) {
    console.error('打印元素未找到')
    return
  }
  
  // 执行打印前回调
  if (config.beforePrint && typeof config.beforePrint === 'function') {
    config.beforePrint()
  }
  
  // 创建打印窗口
  const printWindow = window.open('', '_blank', 'width=800,height=600')
  
  // 构建打印文档
  const printDocument = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>${config.title}</title>
      <style>
        /* 基础打印样式 */
        * {
          box-sizing: border-box;
        }
        
        body {
          margin: 0;
          padding: 20px;
          font-family: 'SimSun', '宋体', serif;
          font-size: 14px;
          line-height: 1.6;
          color: #000;
        }
        
        /* 打印时隐藏不需要的元素 */
        .no-print {
          display: none !important;
        }
        
        /* 表格样式 */
        table {
          width: 100%;
          border-collapse: collapse;
          margin: 10px 0;
        }
        
        table th,
        table td {
          border: 1px solid #000;
          padding: 8px;
          text-align: left;
        }
        
        table th {
          background-color: #f5f5f5;
          font-weight: bold;
        }
        
        /* 页面分页设置 */
        @page {
          margin: 1.5cm 1.2cm;
          size: A4;
        }

        /* 避免表格行在页面间断开 */
        tr {
          page-break-inside: avoid;
        }

        /* 避免标题和内容分离 */
        h1, h2, h3, h4, h5, h6 {
          page-break-after: avoid;
        }

        /* 避免签名区域被分页 */
        .signature-area {
          page-break-inside: avoid;
          margin-top: 30px;
        }

        /* 文档标题 */
        .document-title {
          text-align: center;
          font-size: 24px;
          font-weight: bold;
          margin: 0 0 20px 0;
          padding-bottom: 10px;
          border-bottom: 2px solid #333;
        }

        /* 页脚样式 */
        .print-footer {
          border-top: 1px solid #ddd;
          padding: 10px 0;
          margin-top: 30px;
          font-size: 10px;
          color: #666;
          text-align: center;
        }

        /* 内容区域 */
        .print-content {
          padding: 0;
          background: white;
        }

        /* 打印时的内容区域调整 */
        @media print {
          .print-content {
            padding: 0;
            margin: 0;
          }

          /* 表格分页优化 */
          .details-table {
            page-break-inside: auto;
          }

          .details-table thead {
            display: table-header-group;
          }

          .details-table tbody tr {
            page-break-inside: avoid;
            page-break-after: auto;
          }

          .details-table thead tr {
            page-break-after: avoid;
          }
        }
        
        /* 基本信息样式 */
        .basic-info {
          margin-bottom: 20px;
          font-size: 13px;
          line-height: 1.6;
        }

        .info-table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 10px;
        }

        .info-table td {
          padding: 4px 8px;
          border: 1px solid #ddd;
          vertical-align: top;
        }

        .info-label {
          font-weight: bold;
          background-color: #f5f5f5;
          width: 100px;
          text-align: right;
        }

        .info-value {
          background-color: white;
        }

        .remark-row .info-value {
          min-height: 40px;
        }
        
        /* 表格样式 */
        .details-table {
          width: 100%;
          border-collapse: collapse;
          margin: 15px 0;
          font-size: 12px;
        }

        .details-table th {
          padding: 8px 6px;
          text-align: center;
          font-weight: bold;
          background-color: #f5f5f5;
          border: 1px solid #ddd;
        }

        .details-table td {
          padding: 6px;
          border: 1px solid #ddd;
          vertical-align: middle;
        }

        .details-table tfoot td {
          padding: 8px 6px;
          font-weight: bold;
          background-color: #f9f9f9;
        }

        /* 表格列宽优化 */
        .col-index { width: 50px; text-align: center; }
        .col-name { width: 25%; }
        .col-spec { width: 20%; }
        .col-unit { width: 60px; text-align: center; }
        .col-quantity { width: 80px; text-align: right; }
        .col-price { width: 90px; text-align: right; }
        .col-amount { width: 100px; text-align: right; }

        /* 签名区域 */
        .signature-area {
          margin-top: 30px;
          display: table;
          width: 100%;
          border-collapse: collapse;
        }

        .signature-row {
          display: table-row;
        }

        .signature-item {
          display: table-cell;
          text-align: center;
          padding: 20px 10px;
          border: 1px solid #ddd;
          vertical-align: top;
          width: 25%;
        }

        /* 签名表格样式 */
        .signature-section {
          margin-top: 30px;
          page-break-inside: avoid;
        }

        .signature-table {
          width: 100%;
          border-collapse: collapse;
        }

        .signature-cell {
          text-align: center;
          padding: 20px 10px;
          border: 1px solid #333;
          vertical-align: top;
          width: 25%;
        }

        .signature-title {
          font-size: 12px;
          font-weight: bold;
          margin-bottom: 10px;
        }

        .signature-line {
          border-bottom: 1px solid #333;
          height: 40px;
          margin: 10px 0 5px 0;
        }

        .signature-label {
          font-size: 12px;
          font-weight: bold;
          margin-bottom: 5px;
        }

        .signature-date {
          font-size: 10px;
          color: #666;
          margin-top: 5px;
        }

        /* 台账表格样式 */
        .ledger-table {
          width: 100%;
          border-collapse: collapse;
          margin: 15px 0;
          font-size: 11px;
        }

        .ledger-table th {
          padding: 6px 4px;
          text-align: center;
          font-weight: bold;
          background-color: #f5f5f5;
          border: 1px solid #ddd;
          white-space: nowrap;
        }

        .ledger-table td {
          padding: 4px;
          border: 1px solid #ddd;
          vertical-align: middle;
        }

        .ledger-table .col-no { width: 40px; text-align: center; }
        .ledger-table .col-order-no { width: 120px; }
        .ledger-table .col-warehouse { width: 80px; }
        .ledger-table .col-type { width: 60px; text-align: center; }
        .ledger-table .col-creator { width: 60px; text-align: center; }
        .ledger-table .col-date { width: 80px; text-align: center; }
        .ledger-table .col-status { width: 60px; text-align: center; }
        .ledger-table .col-quantity { width: 60px; text-align: right; }
        .ledger-table .col-amount { width: 80px; text-align: right; }
        .ledger-table .col-remark { text-align: left; }

        /* 台账统计行 */
        .ledger-summary {
          margin-top: 20px;
          padding: 10px;
          background-color: #f8f9fa;
          border: 1px solid #ddd;
          font-size: 12px;
        }

        .summary-item {
          display: inline-block;
          margin-right: 30px;
          font-weight: bold;
        }
        
        /* 自定义样式 */
        ${config.styles}
      </style>
    </head>
    <body>
      ${printElement.outerHTML}

    </body>
    </html>
  `
  
  // 写入文档内容
  printWindow.document.write(printDocument)
  printWindow.document.close()
  
  // 等待内容加载完成后打印
  printWindow.onload = () => {
    setTimeout(() => {
      printWindow.print()
      
      // 执行打印后回调
      if (config.afterPrint && typeof config.afterPrint === 'function') {
        config.afterPrint()
      }
      
      // 打印完成后关闭窗口
      if (config.removeAfterPrint) {
        printWindow.onafterprint = () => {
          printWindow.close()
        }
      }
    }, 500)
  }
}

/**
 * 打印HTML内容
 * @param {string} htmlContent - HTML内容
 * @param {Object} options - 打印选项
 */
export const printHtml = (htmlContent, options = {}) => {
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = htmlContent
  printElement(tempDiv, options)
}

/**
 * 生成入库单打印内容
 * @param {Object} inboundData - 入库单数据
 * @returns {string} HTML内容
 */
export const generateInboundPrintContent = (inboundData) => {
  const currentDate = new Date().toLocaleString('zh-CN')

  // 格式化日期时间
  const formatDateTime = (dateTime) => {
    if (!dateTime) return '-'
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 格式化数字
  const formatNumber = (num, decimals = 0) => {
    if (num === null || num === undefined || isNaN(num)) return '0'
    return Number(num).toLocaleString('zh-CN', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    })
  }
  
  return `
    <div class="print-container">
      <!-- 文档标题 -->
      <div class="document-title">入库单</div>

      <!-- 主要内容区域 -->
      <div class="print-content">

        <!-- 基本信息 -->
        <div class="basic-info">
          <table class="info-table">
            <tr>
              <td class="info-label">入库单号：</td>
              <td class="info-value">${inboundData.orderNumber || '-'}</td>
              <td class="info-label">仓库：</td>
              <td class="info-value">${inboundData.warehouseName || '-'}</td>
              <td class="info-label">入库类型：</td>
              <td class="info-value">${getBusinessTypeText(inboundData.businessType)}</td>
            </tr>
            <tr>
              <td class="info-label">制单人：</td>
              <td class="info-value">${inboundData.createdBy || '-'}</td>
              <td class="info-label">计划日期：</td>
              <td class="info-value">${inboundData.plannedDate || '-'}</td>
              <td class="info-label">状态：</td>
              <td class="info-value">${getStatusText(inboundData.status)}</td>
            </tr>
            <tr>
              <td class="info-label">参考单号：</td>
              <td class="info-value">${inboundData.referenceNumber || '-'}</td>
              <td class="info-label">创建时间：</td>
              <td class="info-value">${formatDateTime(inboundData.createdTime)}</td>
              <td class="info-label">打印时间：</td>
              <td class="info-value">${currentDate}</td>
            </tr>
            ${inboundData.remark ? `
            <tr class="remark-row">
              <td class="info-label">备注：</td>
              <td class="info-value" colspan="5">${inboundData.remark}</td>
            </tr>
            ` : ''}
          </table>
        </div>
      
        <!-- 入库明细 -->
        <div class="detail-section">
          <h3 style="margin: 15px 0 10px 0; font-size: 14px; font-weight: bold;">入库明细</h3>
          <table class="details-table">
            <thead>
              <tr>
                <th class="col-index">序号</th>
                <th class="col-name">货物名称</th>
                <th class="col-spec">规格/型号</th>
                <th class="col-unit">单位</th>
                <th class="col-quantity">数量</th>
                <th class="col-price">单价</th>
                <th class="col-amount">金额</th>
              </tr>
            </thead>
            <tbody>
              ${(inboundData.details || []).map((item, index) => `
                <tr>
                  <td class="col-index">${index + 1}</td>
                  <td class="col-name">${item.goodsName || '-'}</td>
                  <td class="col-spec">${getDetailSpecificationModel(item)}</td>
                  <td class="col-unit">${item.goodsUnit || '-'}</td>
                  <td class="col-quantity">${formatNumber(item.quantity || 0)}</td>
                  <td class="col-price">¥${formatNumber(item.unitPrice || 0, 2)}</td>
                  <td class="col-amount">¥${formatNumber((item.quantity || 0) * (item.unitPrice || 0), 2)}</td>
                </tr>
              `).join('')}
            </tbody>
            <tfoot>
              <tr>
                <td colspan="4" style="text-align: right; font-weight: 600;">合计：</td>
                <td class="col-quantity" style="font-weight: 600;">${formatNumber(calculateTotalQuantity(inboundData.details))}</td>
                <td class="col-price"></td>
                <td class="col-amount" style="font-weight: 600; color: #667eea;">¥${formatNumber(calculateTotalAmount(inboundData.details), 2)}</td>
              </tr>
            </tfoot>
          </table>
        </div>
      
        <!-- 审批信息 -->
        ${inboundData.approvalRemark ? `
        <div class="basic-info" style="margin-top: 15px;">
          <h3 style="margin: 0 0 10px 0; font-size: 14px; font-weight: bold;">审批信息</h3>
          <table class="info-table">
            <tr>
              <td class="info-label">审批意见：</td>
              <td class="info-value" colspan="5">${inboundData.approvalRemark}</td>
            </tr>
            ${inboundData.approvedBy ? `
            <tr>
              <td class="info-label">审批人：</td>
              <td class="info-value">${inboundData.approvedBy}</td>
              <td class="info-label">审批时间：</td>
              <td class="info-value" colspan="3">${formatDateTime(inboundData.approvedTime)}</td>
            </tr>
            ` : ''}
          </table>
        </div>
        ` : ''}

        <!-- 签名区域 -->
        <div class="signature-area">
          <div class="signature-row">
            <div class="signature-item">
              <div class="signature-label">送货人签字</div>
              <div class="signature-line"></div>
              <div class="signature-date">日期：_______</div>
            </div>
            <div class="signature-item">
              <div class="signature-label">入库人签字</div>
              <div class="signature-line"></div>
              <div class="signature-date">日期：_______</div>
            </div>
            <div class="signature-item">
              <div class="signature-label">制单人签字</div>
              <div class="signature-line"></div>
              <div class="signature-date">日期：_______</div>
            </div>
            <div class="signature-item">
              <div class="signature-label">审批人签字</div>
              <div class="signature-line"></div>
              <div class="signature-date">日期：_______</div>
            </div>
          </div>
        </div>

        <!-- 页脚 -->
        <div class="print-footer">
          打印时间：${currentDate} | 仓库管理系统
        </div>
      </div>
    </div>
  `
}

// 辅助函数
const getBusinessTypeText = (businessType) => {
  const typeMap = {
    'PURCHASE_IN': '采购入库',
    'RETURN_IN': '归还入库',
    'TRANSFER_IN': '调拨入库',
    'INVENTORY_GAIN': '盘盈入库',
    'OTHER_IN': '其他入库'
  }
  return typeMap[businessType] || businessType || '-'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'EXECUTED': '已执行',
    'COMPLETED': '已执行',
    'CONSISTENCY': '已执行',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status || '-'
}

// 出库类型映射
const getOutboundBusinessTypeText = (businessType) => {
  const typeMap = {
    'SALE_OUT': '领用出库',
    'TRANSFER_OUT': '调拨出库',
    'DAMAGE_OUT': '借用出库',
    'OTHER_OUT': '其他出库'
  }
  return typeMap[businessType] || businessType || '-'
}

/**
 * 生成出库单台账打印内容
 */
export const generateOutboundLedgerPrintContent = (orders, options = []) => {
  const currentDate = new Date().toLocaleString('zh-CN')
  const showDetails = options.includes('showDetails')
  const showSummary = options.includes('showSummary')
  const showPageNumber = options.includes('showPageNumber')

  // 计算汇总数据
  const totalOrders = orders.length
  const totalQuantity = orders.reduce((sum, order) => sum + (order.totalQuantity || 0), 0)
  const totalAmount = orders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)

  return `
    <div class="print-container">
      <style>
        ${getCommonPrintStyles()}
        .ledger-table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 20px;
          font-size: 12px;
        }
        .ledger-table th,
        .ledger-table td {
          border: 1px solid #333;
          padding: 8px 6px;
          text-align: left;
          vertical-align: middle;
        }
        .ledger-table th {
          background-color: #f5f5f5;
          font-weight: bold;
          text-align: center;
        }
        .text-center { text-align: center; }
        .text-right { text-align: right; }
        .summary-section {
          background: #f8f9fa;
          padding: 15px;
          border-radius: 6px;
          margin-top: 20px;
        }
        .summary-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 20px;
          text-align: center;
        }
        .summary-item {
          background: white;
          padding: 12px;
          border-radius: 4px;
          border: 1px solid #ddd;
        }
        .summary-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 4px;
        }
        .summary-value {
          font-size: 18px;
          font-weight: bold;
          color: #333;
        }
      </style>

      <div class="print-content">
        <!-- 标题 -->
        <div class="print-header">
          <h1>出库单台账</h1>
          <div class="header-info">
            <span>统计期间：${orders.length > 0 ? `${orders[0].createdTime?.split(' ')[0]} 至 ${orders[orders.length - 1].createdTime?.split(' ')[0]}` : '全部'}</span>
            <span style="margin-left: 30px;">打印时间：${currentDate}</span>
          </div>
        </div>

        <!-- 台账表格 -->
        <table class="ledger-table">
          <thead>
            <tr>
              <th style="width: 8%">序号</th>
              <th style="width: 15%">出库单号</th>
              <th style="width: 12%">仓库</th>
              <th style="width: 10%">出库类型</th>
              <th style="width: 10%">领取人</th>
              <th style="width: 8%">状态</th>
              <th style="width: 10%">总数量</th>
              <th style="width: 12%">总金额(元)</th>
              <th style="width: 10%">出库日期</th>
              <th style="width: 5%">制单人</th>
            </tr>
          </thead>
          <tbody>
            ${orders.map((order, index) => `
              <tr>
                <td class="text-center">${index + 1}</td>
                <td>${order.orderNumber || '-'}</td>
                <td>${order.warehouseName || '-'}</td>
                <td class="text-center">${getOutboundBusinessTypeText(order.businessType)}</td>
                <td>${order.recipientName || '-'}</td>
                <td class="text-center">${getStatusText(order.status)}</td>
                <td class="text-right">${formatNumber(order.totalQuantity)}</td>
                <td class="text-right">${formatNumber(order.totalAmount, 2)}</td>
                <td class="text-center">${order.plannedDate || '-'}</td>
                <td>${order.createdBy || '-'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>

        ${showSummary ? `
        <!-- 汇总统计 -->
        <div class="summary-section">
          <h3 style="margin: 0 0 15px 0; text-align: center;">汇总统计</h3>
          <div class="summary-grid">
            <div class="summary-item">
              <div class="summary-label">出库单总数</div>
              <div class="summary-value">${totalOrders} 个</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">总出库数量</div>
              <div class="summary-value">${formatNumber(totalQuantity)} 件</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">总出库金额</div>
              <div class="summary-value">¥${formatNumber(totalAmount, 2)}</div>
            </div>
          </div>
        </div>
        ` : ''}

        <!-- 页脚 -->
        <div class="print-footer">
          ${showPageNumber ? `第 1 页，共 1 页 | ` : ''}打印时间：${currentDate} | 仓库管理系统
        </div>
      </div>
    </div>
  `
}



const getDetailSpecificationModel = (item) => {
  const parts = []
  if (item.goodsModel && item.goodsModel.trim()) {
    parts.push(item.goodsModel.trim())
  }
  if (item.goodsSpecification && item.goodsSpecification.trim()) {
    parts.push(item.goodsSpecification.trim())
  }
  return parts.length > 0 ? parts.join(' / ') : '-'
}

const calculateTotalQuantity = (details) => {
  if (!details || !Array.isArray(details)) return 0
  return details.reduce((sum, item) => sum + (item.quantity || 0), 0)
}

const calculateTotalAmount = (details) => {
  if (!details || !Array.isArray(details)) return 0
  return details.reduce((sum, item) => sum + ((item.quantity || 0) * (item.unitPrice || 0)), 0)
}

/**
 * 生成入库单台账打印内容
 * @param {Array} inboundOrders - 入库单列表
 * @param {Object} options - 打印选项
 * @returns {string} HTML内容
 */
export const generateInboundLedgerPrintContent = (inboundOrders, options = {}) => {
  const currentDate = new Date().toLocaleString('zh-CN')

  // 格式化日期时间
  const formatDateTime = (dateTime) => {
    if (!dateTime) return '-'
    const date = new Date(dateTime)
    return date.toLocaleDateString('zh-CN')
  }

  // 格式化数字
  const formatNumber = (num, decimals = 0) => {
    if (num === null || num === undefined || isNaN(num)) return '0'
    return Number(num).toLocaleString('zh-CN', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    })
  }

  // 计算统计数据
  const totalOrders = inboundOrders.length
  const totalQuantity = inboundOrders.reduce((sum, order) => sum + (order.totalQuantity || 0), 0)
  const totalAmount = inboundOrders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)

  return `
    <div class="print-container">
      <!-- 文档标题 -->
      <div class="document-title">入库单台账</div>

      <!-- 主要内容区域 -->
      <div class="print-content">
        <!-- 打印信息 -->
        <div class="basic-info">
          <table class="info-table">
            <tr>
              <td class="info-label">打印时间：</td>
              <td class="info-value">${currentDate}</td>
              <td class="info-label">打印范围：</td>
              <td class="info-value">${options.dateRange || '全部'}</td>
              <td class="info-label">单据数量：</td>
              <td class="info-value">${totalOrders} 张</td>
            </tr>
          </table>
        </div>

        <!-- 台账明细 -->
        <div class="detail-section">
          <table class="ledger-table">
            <thead>
              <tr>
                <th class="col-no">序号</th>
                <th class="col-order-no">入库单号</th>
                <th class="col-warehouse">仓库</th>
                <th class="col-type">类型</th>
                <th class="col-creator">制单人</th>
                <th class="col-date">计划日期</th>
                <th class="col-status">状态</th>
                <th class="col-quantity">总数量</th>
                <th class="col-amount">总金额</th>
                <th class="col-remark">备注</th>
              </tr>
            </thead>
            <tbody>
              ${inboundOrders.map((order, index) => `
                <tr>
                  <td class="col-no">${index + 1}</td>
                  <td class="col-order-no">${order.orderNumber || '-'}</td>
                  <td class="col-warehouse">${order.warehouseName || '-'}</td>
                  <td class="col-type">${getBusinessTypeText(order.businessType)}</td>
                  <td class="col-creator">${order.createdBy || '-'}</td>
                  <td class="col-date">${formatDateTime(order.plannedDate)}</td>
                  <td class="col-status">${getStatusText(order.status)}</td>
                  <td class="col-quantity">${formatNumber(order.totalQuantity || 0)}</td>
                  <td class="col-amount">¥${formatNumber(order.totalAmount || 0, 2)}</td>
                  <td class="col-remark">${order.remark || '-'}</td>
                </tr>
              `).join('')}
            </tbody>
            <tfoot>
              <tr>
                <td colspan="7" style="text-align: right; font-weight: bold;">合计：</td>
                <td class="col-quantity" style="font-weight: bold;">${formatNumber(totalQuantity)}</td>
                <td class="col-amount" style="font-weight: bold;">¥${formatNumber(totalAmount, 2)}</td>
                <td class="col-remark"></td>
              </tr>
            </tfoot>
          </table>
        </div>

        <!-- 统计信息 -->
        <div class="ledger-summary">
          <div class="summary-item">单据总数：${totalOrders} 张</div>
          <div class="summary-item">货物总数量：${formatNumber(totalQuantity)}</div>
          <div class="summary-item">总金额：¥${formatNumber(totalAmount, 2)}</div>
        </div>

        <!-- 页脚 -->
        <div class="print-footer">
          打印时间：${currentDate} | 仓库管理系统
        </div>
      </div>
    </div>
  `
}

/**
 * 生成出库单打印内容
 * @param {Object} outboundData - 出库单数据
 * @returns {string} HTML内容
 */
export const generateOutboundPrintContent = (outboundData) => {
  const currentDate = new Date().toLocaleString('zh-CN')

  // 格式化日期时间
  const formatDateTime = (dateTime) => {
    if (!dateTime) return '-'
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 格式化数字
  const formatNumber = (num, decimals = 0) => {
    if (num === null || num === undefined || isNaN(num)) return '0'
    return Number(num).toLocaleString('zh-CN', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    })
  }

  return `
    <div class="print-container">
      <!-- 文档标题 -->
      <div class="document-title">出库单</div>

      <!-- 主要内容区域 -->
      <div class="print-content">
        <!-- 基本信息 -->
        <div class="basic-info">
          <table class="info-table">
            <tr>
              <td class="info-label">出库单号：</td>
              <td class="info-value">${outboundData.orderNumber || '-'}</td>
              <td class="info-label">出库类型：</td>
              <td class="info-value">${getOutboundBusinessTypeText(outboundData.businessType)}</td>
            </tr>
            <tr>
              <td class="info-label">出库仓库：</td>
              <td class="info-value">${outboundData.warehouseName || '-'}</td>
              <td class="info-label">状态：</td>
              <td class="info-value">${outboundData.status === 'PENDING' ? '待审批' : outboundData.status === 'APPROVED' ? '已审批' : outboundData.status === 'EXECUTED' ? '已执行' : outboundData.status === 'CANCELLED' ? '已取消' : '未知'}</td>
            </tr>
            <tr>
              <td class="info-label">申请人：</td>
              <td class="info-value">${outboundData.createdBy || '-'}</td>
              <td class="info-label">申请时间：</td>
              <td class="info-value">${formatDateTime(outboundData.createdTime)}</td>
            </tr>
          </table>
        </div>

        <!-- 货物明细 -->
        <div class="goods-details">
          <div class="section-title">货物明细</div>
          <table class="details-table">
            <thead>
              <tr>
                <th style="width: 8%">序号</th>
                <th style="width: 25%">货物名称</th>
                <th style="width: 15%">规格型号</th>
                <th style="width: 10%">单位</th>
                <th style="width: 12%">出库数量</th>
                <th style="width: 15%">单价(元)</th>
                <th style="width: 15%">金额(元)</th>
              </tr>
            </thead>
            <tbody>
              ${(outboundData.details || []).map((item, index) => `
                <tr>
                  <td style="text-align: center">${index + 1}</td>
                  <td>${item.goodsName || '-'}</td>
                  <td>${item.goodsSpecification || item.specification || '-'}</td>
                  <td style="text-align: center">${item.goodsUnit || item.unit || '-'}</td>
                  <td style="text-align: right">${formatNumber(item.quantity)}</td>
                  <td style="text-align: right">${formatNumber(item.unitPrice, 2)}</td>
                  <td style="text-align: right">${formatNumber((item.quantity || 0) * (item.unitPrice || 0), 2)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>

        <!-- 汇总信息 -->
        <div class="summary-info">
          <div class="summary-row">
            <span class="summary-item">总数量：${formatNumber(outboundData.totalQuantity || 0)} 件</span>
            <span class="summary-item">总金额：¥${formatNumber(outboundData.totalAmount || 0, 2)}</span>
          </div>
        </div>

        <!-- 备注信息 -->
        ${outboundData.remark ? `
          <div class="remark-section">
            <div class="section-title">备注</div>
            <div class="remark-content">${outboundData.remark}</div>
          </div>
        ` : ''}

        <!-- 签名区域 -->
        <div class="signature-section">
          <table class="signature-table">
            <tr>
              <td class="signature-cell">
                <div class="signature-title">领取人签名：</div>
                <div class="signature-line"></div>
                <div class="signature-date">日期：___________</div>
              </td>
              <td class="signature-cell">
                <div class="signature-title">班长审批：</div>
                <div class="signature-line"></div>
                <div class="signature-date">日期：___________</div>
              </td>
              <td class="signature-cell">
                <div class="signature-title">队长审批：</div>
                <div class="signature-line"></div>
                <div class="signature-date">日期：___________</div>
              </td>
              <td class="signature-cell">
                <div class="signature-title">出库人签名：</div>
                <div class="signature-line"></div>
                <div class="signature-date">日期：___________</div>
              </td>
            </tr>
          </table>
        </div>

        <!-- 页脚信息 -->
        <div class="footer-info">
          <div class="print-info">
            <span>打印时间：${currentDate}</span>
            <span style="margin-left: 50px;">第 1 页，共 1 页</span>
          </div>
        </div>
      </div>
    </div>
  `
}


