<template>
  <div class="test-print-container">
    <div class="header">
      <h2>打印功能测试</h2>
      <div class="button-group">
        <el-button type="primary" @click="testPrint">
          <el-icon><Printer /></el-icon>
          测试打印入库单
        </el-button>
        <el-button type="success" @click="testLedgerPrint">
          <el-icon><Printer /></el-icon>
          测试台账打印
        </el-button>
        <el-button type="warning" @click="testOutboundPrint">
          <el-icon><Printer /></el-icon>
          测试打印出库单
        </el-button>
        <InboundLedgerPrint
          :selected-orders="[]"
          :all-orders="testLedgerData"
          @refresh="() => {}"
        />
        <InboundExport
          :selected-orders="[]"
          :current-page-data="testLedgerData"
          :search-params="{}"
          @refresh="() => {}"
        />
      </div>
    </div>

    <!-- 打印预览 -->
    <PrintPreview
      v-model="printPreviewVisible"
      :content="printContent"
      :title="printTitle"
      @print="handlePrintComplete"
      @close="handlePrintPreviewClose"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Printer } from '@element-plus/icons-vue'
import { generateInboundPrintContent, generateInboundLedgerPrintContent, generateOutboundPrintContent } from '@/utils/print'
import PrintPreview from '@/components/PrintPreview.vue'
import InboundLedgerPrint from '@/components/InboundLedgerPrint.vue'
import InboundExport from '@/components/InboundExport.vue'
import { ElMessage } from 'element-plus'

const printPreviewVisible = ref(false)
const printContent = ref('')
const printTitle = ref('')

// 测试数据
const testInboundData = {
  orderNumber: 'IN20241201001',
  warehouseName: '主仓库',
  businessType: 'PURCHASE',
  createdBy: '张三',
  plannedDate: '2024-12-01',
  status: 'APPROVED',
  referenceNumber: 'PO20241201001',
  createdTime: '2024-12-01 10:30:00',
  remark: '这是一个测试入库单，用于验证打印功能的多页显示效果。',
  approvalRemark: '审批通过，可以入库。',
  approvedBy: '李四',
  approvedTime: '2024-12-01 11:00:00',
  details: [
    {
      goodsName: '苹果手机',
      goodsSpecification: 'iPhone 15 Pro',
      goodsModel: '256GB',
      goodsUnit: '台',
      quantity: 10,
      unitPrice: 8999.00
    },
    {
      goodsName: '华为手机',
      goodsSpecification: 'Mate 60 Pro',
      goodsModel: '512GB',
      goodsUnit: '台',
      quantity: 5,
      unitPrice: 6999.00
    },
    {
      goodsName: '小米手机',
      goodsSpecification: 'Mi 14 Ultra',
      goodsModel: '1TB',
      goodsUnit: '台',
      quantity: 8,
      unitPrice: 5999.00
    }
  ]
}

// 测试台账数据
const testLedgerData = [
  {
    orderNumber: 'IN20241201001',
    warehouseName: '主仓库',
    businessType: 'PURCHASE_IN',
    createdBy: '张三',
    plannedDate: '2024-12-01',
    status: 'EXECUTED',
    totalQuantity: 23,
    totalAmount: 189970.00,
    remark: '采购入库',
    createdTime: '2024-12-01 10:30:00',
    approvalRemark: '审批通过',
    approvedBy: '李经理',
    approvedTime: '2024-12-01 11:00:00',
    details: [
      {
        goodsName: '苹果手机',
        goodsSpecification: 'iPhone 15 Pro',
        goodsModel: '256GB',
        goodsUnit: '台',
        quantity: 10,
        unitPrice: 8999.00
      },
      {
        goodsName: '华为手机',
        goodsSpecification: 'Mate 60 Pro',
        goodsModel: '512GB',
        goodsUnit: '台',
        quantity: 5,
        unitPrice: 6999.00
      },
      {
        goodsName: '小米手机',
        goodsSpecification: 'Mi 14 Ultra',
        goodsModel: '1TB',
        goodsUnit: '台',
        quantity: 8,
        unitPrice: 5999.00
      }
    ]
  },
  {
    orderNumber: 'IN20241201002',
    warehouseName: '分仓库',
    businessType: 'RETURN_IN',
    createdBy: '李四',
    plannedDate: '2024-12-01',
    status: 'APPROVED',
    totalQuantity: 15,
    totalAmount: 89985.00,
    remark: '退货入库',
    createdTime: '2024-12-01 14:20:00',
    details: [
      {
        goodsName: 'iPad平板',
        goodsSpecification: 'iPad Pro 12.9',
        goodsModel: '2TB/5G',
        goodsUnit: '台',
        quantity: 8,
        unitPrice: 16999.00
      },
      {
        goodsName: '显示器',
        goodsSpecification: 'Dell UltraSharp 32',
        goodsModel: '4K/HDR',
        goodsUnit: '台',
        quantity: 7,
        unitPrice: 3999.00
      }
    ]
  },
  {
    orderNumber: 'IN20241202001',
    warehouseName: '主仓库',
    businessType: 'TRANSFER_IN',
    createdBy: '王五',
    plannedDate: '2024-12-02',
    status: 'EXECUTED',
    totalQuantity: 8,
    totalAmount: 47992.00,
    remark: '调拨入库',
    createdTime: '2024-12-02 09:15:00',
    details: [
      {
        goodsName: '键盘',
        goodsSpecification: '罗技MX Keys',
        goodsModel: '无线机械',
        goodsUnit: '个',
        quantity: 20,
        unitPrice: 699.00
      },
      {
        goodsName: '鼠标',
        goodsSpecification: '罗技MX Master 3S',
        goodsModel: '无线激光',
        goodsUnit: '个',
        quantity: 15,
        unitPrice: 599.00
      }
    ]
  }
]

const testPrint = () => {
  try {
    // 生成打印内容
    printContent.value = generateInboundPrintContent(testInboundData)
    printTitle.value = `入库单-${testInboundData.orderNumber}`
    
    // 显示打印预览
    printPreviewVisible.value = true
    
    ElMessage.success('打印预览已生成')
  } catch (error) {
    console.error('生成打印内容失败:', error)
    ElMessage.error('生成打印内容失败，请重试')
  }
}

const handlePrintComplete = () => {
  printPreviewVisible.value = false
  ElMessage.success('打印任务已发送')
}

const testLedgerPrint = () => {
  try {
    // 生成台账打印内容
    printContent.value = generateInboundLedgerPrintContent(testLedgerData, {
      dateRange: '2024-12-01 至 2024-12-02'
    })
    printTitle.value = `入库单台账-${new Date().toLocaleDateString()}`

    // 显示打印预览
    printPreviewVisible.value = true

    ElMessage.success('台账打印预览已生成')
  } catch (error) {
    console.error('生成台账打印内容失败:', error)
    ElMessage.error('生成台账打印内容失败，请重试')
  }
}

const testOutboundPrint = () => {
  try {
    // 测试出库单数据
    const testOutboundData = {
      id: 1,
      orderNumber: 'OUT202412010001',
      warehouseName: '主仓库',
      businessType: 'SALE_OUT',
      status: 'EXECUTED',
      recipientName: '张三',
      createdBy: '申请人张三',
      approvedBy: '审批人李四',
      operatedBy: '执行人王五',
      createdTime: '2024-12-01 10:30:00',
      approvalTime: '2024-12-01 11:00:00',
      operationTime: '2024-12-01 14:30:00',
      totalQuantity: 150,
      totalAmount: 15000,
      remark: '其他出库，测试打印功能',
      details: [
        {
          id: 1,
          goodsCode: 'G001',
          goodsName: '笔记本电脑',
          goodsSpecification: 'ThinkPad X1 Carbon',
          goodsUnit: '台',
          quantity: 5,
          unitPrice: 8000
        },
        {
          id: 2,
          goodsCode: 'G002',
          goodsName: '无线鼠标',
          goodsSpecification: '罗技MX Master 3',
          goodsUnit: '个',
          quantity: 10,
          unitPrice: 500
        },
        {
          id: 3,
          goodsCode: 'G003',
          goodsName: 'USB数据线',
          goodsSpecification: 'Type-C 1米',
          goodsUnit: '根',
          quantity: 20,
          unitPrice: 25
        }
      ]
    }

    // 生成打印内容
    printContent.value = generateOutboundPrintContent(testOutboundData)
    printTitle.value = `出库单-${testOutboundData.orderNumber}`

    // 显示打印预览
    printPreviewVisible.value = true

    ElMessage.success('出库单打印预览已生成')
  } catch (error) {
    console.error('生成出库单打印内容失败:', error)
    ElMessage.error('生成出库单打印内容失败，请重试')
  }
}

const handlePrintPreviewClose = () => {
  printContent.value = ''
  printTitle.value = ''
}
</script>

<style lang="scss" scoped>
.test-print-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;

  h2 {
    margin: 0;
    color: #303133;
  }

  .button-group {
    display: flex;
    gap: 12px;
  }
}
</style>
