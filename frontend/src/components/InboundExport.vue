<template>
  <div>
    <!-- 导出按钮 -->
    <el-button 
      @click="showExportDialog" 
      :size="size"
      :loading="exporting"
    >
      <el-icon><Download /></el-icon>
      导出数据
    </el-button>

    <!-- 导出设置对话框 -->
    <el-dialog
      v-model="exportDialogVisible"
      title="导出入库单数据"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="exportForm" label-width="120px">
        <!-- 导出范围 -->
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.range">
            <el-radio value="current">当前页数据</el-radio>
            <el-radio value="selected" :disabled="!canExportSelected">选中的数据</el-radio>
            <el-radio value="all">全部数据</el-radio>
            <el-radio value="filtered">按条件筛选</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 筛选条件 -->
        <div v-if="exportForm.range === 'filtered'" class="filter-section">
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="exportForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="仓库">
            <el-select
              v-model="exportForm.warehouseId"
              placeholder="请选择仓库（可选）"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="warehouse in warehouses"
                :key="warehouse.id"
                :label="warehouse.name"
                :value="warehouse.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="状态">
            <el-select
              v-model="exportForm.status"
              placeholder="请选择状态（可选）"
              clearable
              style="width: 100%"
            >
              <el-option label="待审批" value="PENDING" />
              <el-option label="已审批" value="APPROVED" />
              <el-option label="已执行" value="EXECUTED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>

          <el-form-item label="入库类型">
            <el-select
              v-model="exportForm.businessType"
              placeholder="请选择入库类型（可选）"
              clearable
              style="width: 100%"
            >
              <el-option label="采购入库" value="PURCHASE_IN" />
              <el-option label="归还入库" value="RETURN_IN" />
              <el-option label="调拨入库" value="TRANSFER_IN" />
              <el-option label="盘盈入库" value="INVENTORY_GAIN" />
              <el-option label="其他入库" value="OTHER_IN" />
            </el-select>
          </el-form-item>
        </div>

        <!-- 导出内容 -->
        <el-form-item label="导出内容">
          <el-checkbox-group v-model="exportForm.includeFields">
            <el-checkbox value="basic">基本信息</el-checkbox>
            <el-checkbox value="details">明细信息</el-checkbox>
            <el-checkbox value="approval">审批信息</el-checkbox>
            <el-checkbox value="statistics">统计信息</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 文件格式 -->
        <el-form-item label="文件格式">
          <el-radio-group v-model="exportForm.format">
            <el-radio value="xlsx">Excel文件 (.xlsx)</el-radio>
            <el-radio value="csv">CSV文件 (.csv)</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 文件名 -->
        <el-form-item label="文件名">
          <el-input
            v-model="exportForm.filename"
            placeholder="请输入文件名"
            style="width: 70%"
          />
          <span style="margin-left: 10px; color: #909399;">
            .{{ exportForm.format }}
          </span>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleExport" 
            :loading="exporting"
          >
            <el-icon><Download /></el-icon>
            开始导出
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'
import * as XLSX from 'xlsx'

const props = defineProps({
  selectedOrders: {
    type: Array,
    default: () => []
  },
  currentPageData: {
    type: Array,
    default: () => []
  },
  searchParams: {
    type: Object,
    default: () => ({})
  },
  size: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['refresh'])

const exporting = ref(false)
const exportDialogVisible = ref(false)
const warehouses = ref([])

// 导出表单
const exportForm = reactive({
  range: 'current',
  dateRange: null,
  warehouseId: null,
  status: null,
  businessType: null,
  includeFields: ['basic', 'details'],
  format: 'xlsx',
  filename: ''
})

// 计算是否可以导出选中数据
const canExportSelected = computed(() => {
  return props.selectedOrders && props.selectedOrders.length > 0
})

// 显示导出对话框
const showExportDialog = async () => {
  // 重置表单
  Object.assign(exportForm, {
    range: canExportSelected.value ? 'selected' : 'current',
    dateRange: null,
    warehouseId: null,
    status: null,
    businessType: null,
    includeFields: ['basic', 'details'],
    format: 'xlsx',
    filename: generateDefaultFilename()
  })

  // 加载仓库列表
  await loadWarehouses()
  
  exportDialogVisible.value = true
}

// 生成默认文件名
const generateDefaultFilename = () => {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const timeStr = now.toTimeString().slice(0, 8).replace(/:/g, '')
  return `入库单数据_${dateStr}_${timeStr}`
}

// 加载仓库列表
const loadWarehouses = async () => {
  try {
    const response = await request.get('/warehouses/enabled')
    if (response.success) {
      warehouses.value = response.data || []
    }
  } catch (error) {
    console.error('加载仓库列表失败:', error)
    warehouses.value = []
  }
}

// 获取导出数据
const getExportData = async () => {
  let data = []

  switch (exportForm.range) {
    case 'current':
      data = props.currentPageData
      break
      
    case 'selected':
      data = props.selectedOrders
      break
      
    case 'all':
      // 获取所有数据
      try {
        const response = await request.get('/inbound-orders', {
          page: 0,
          size: 10000,
          ...props.searchParams
        })
        if (response.success) {
          data = response.data.content || []
        }
      } catch (error) {
        throw new Error('获取全部数据失败')
      }
      break
      
    case 'filtered':
      // 根据筛选条件获取数据
      try {
        const params = {
          page: 0,
          size: 10000
        }
        
        if (exportForm.dateRange && exportForm.dateRange.length === 2) {
          params.startDate = exportForm.dateRange[0]
          params.endDate = exportForm.dateRange[1]
        }
        
        if (exportForm.warehouseId) {
          params.warehouseId = exportForm.warehouseId
        }
        
        if (exportForm.status) {
          params.status = exportForm.status
        }
        
        if (exportForm.businessType) {
          params.businessType = exportForm.businessType
        }
        
        const response = await request.get('/inbound-orders', params)
        if (response.success) {
          data = response.data.content || []
        }
      } catch (error) {
        throw new Error('获取筛选数据失败')
      }
      break
  }

  return data
}

// 处理导出
const handleExport = async () => {
  try {
    exporting.value = true

    // 验证表单
    if (!exportForm.filename.trim()) {
      ElMessage.warning('请输入文件名')
      return
    }

    if (exportForm.includeFields.length === 0) {
      ElMessage.warning('请选择至少一项导出内容')
      return
    }

    // 获取数据
    const data = await getExportData()
    
    if (data.length === 0) {
      ElMessage.warning('没有可导出的数据')
      return
    }

    // 确认导出
    await ElMessageBox.confirm(
      `确定要导出 ${data.length} 条入库单数据吗？`,
      '确认导出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 生成导出数据
    const exportData = generateExportData(data)
    
    // 导出文件
    if (exportForm.format === 'xlsx') {
      exportToExcel(exportData)
    } else {
      exportToCSV(exportData)
    }

    ElMessage.success('导出成功')
    exportDialogVisible.value = false

  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出失败:', error)
      ElMessage.error(error.message || '导出失败，请重试')
    }
  } finally {
    exporting.value = false
  }
}

// 生成导出数据
const generateExportData = (data) => {
  const result = []

  data.forEach(order => {
    if (exportForm.includeFields.includes('basic')) {
      // 基本信息
      const basicInfo = {
        '入库单号': order.orderNumber || '',
        '仓库': order.warehouseName || '',
        '入库类型': getBusinessTypeText(order.businessType),
        '制单人': order.createdBy || '',
        '计划日期': order.plannedDate || '',
        '状态': getStatusText(order.status),
        '参考单号': order.referenceNumber || '',
        '创建时间': formatDateTime(order.createdTime),
        '备注': order.remark || ''
      }

      if (exportForm.includeFields.includes('statistics')) {
        basicInfo['总数量'] = order.totalQuantity || 0
        basicInfo['总金额'] = order.totalAmount || 0
      }

      if (exportForm.includeFields.includes('approval') && order.approvalRemark) {
        basicInfo['审批意见'] = order.approvalRemark || ''
        basicInfo['审批人'] = order.approvedBy || ''
        basicInfo['审批时间'] = formatDateTime(order.approvedTime)
      }

      if (!exportForm.includeFields.includes('details')) {
        result.push(basicInfo)
      } else {
        // 包含明细信息
        if (order.details && order.details.length > 0) {
          order.details.forEach((detail, index) => {
            const row = { ...basicInfo }
            
            if (index === 0) {
              // 第一行显示基本信息
            } else {
              // 其他行清空基本信息，只显示明细
              Object.keys(row).forEach(key => {
                if (!key.includes('明细')) {
                  row[key] = ''
                }
              })
            }

            // 添加明细信息
            row['明细序号'] = index + 1
            row['货物名称'] = detail.goodsName || ''
            row['规格型号'] = getDetailSpecificationModel(detail)
            row['单位'] = detail.goodsUnit || ''
            row['数量'] = detail.quantity || 0
            row['单价'] = detail.unitPrice || 0
            row['金额'] = (detail.quantity || 0) * (detail.unitPrice || 0)

            result.push(row)
          })
        } else {
          result.push(basicInfo)
        }
      }
    }
  })

  return result
}

// 导出到Excel
const exportToExcel = (data) => {
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '入库单数据')
  
  // 设置列宽
  const colWidths = [
    { wch: 15 }, // 入库单号
    { wch: 12 }, // 仓库
    { wch: 10 }, // 入库类型
    { wch: 10 }, // 制单人
    { wch: 12 }, // 计划日期
    { wch: 8 },  // 状态
    { wch: 15 }, // 参考单号
    { wch: 18 }, // 创建时间
    { wch: 20 }  // 备注
  ]
  
  if (exportForm.includeFields.includes('details')) {
    colWidths.push(
      { wch: 8 },  // 明细序号
      { wch: 20 }, // 货物名称
      { wch: 15 }, // 规格型号
      { wch: 8 },  // 单位
      { wch: 10 }, // 数量
      { wch: 12 }, // 单价
      { wch: 12 }  // 金额
    )
  }
  
  ws['!cols'] = colWidths
  
  XLSX.writeFile(wb, `${exportForm.filename}.xlsx`)
}

// 导出到CSV
const exportToCSV = (data) => {
  const ws = XLSX.utils.json_to_sheet(data)
  const csv = XLSX.utils.sheet_to_csv(ws)
  
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', `${exportForm.filename}.csv`)
  link.style.visibility = 'hidden'
  
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 辅助函数
const getBusinessTypeText = (type) => {
  const typeMap = {
    'PURCHASE_IN': '采购入库',
    'RETURN_IN': '归还入库',
    'TRANSFER_IN': '调拨入库',
    'INVENTORY_GAIN': '盘盈入库',
    'OTHER_IN': '其他入库'
  }
  return typeMap[type] || type || '-'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'EXECUTED': '已执行',
    'COMPLETED': '已执行',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status || '-'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
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
</script>

<style lang="scss" scoped>
.filter-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  margin: 16px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
