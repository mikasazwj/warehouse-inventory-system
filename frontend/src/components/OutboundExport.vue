<template>
  <div>
    <!-- 导出按钮 -->
    <el-button 
      type="success" 
      :icon="Download" 
      @click="showExportDialog"
      :disabled="loading"
    >
      导出数据
    </el-button>

    <!-- 导出设置对话框 -->
    <el-dialog
      v-model="exportDialogVisible"
      :width="isMobile ? '95%' : '700px'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="export-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <el-icon><Download /></el-icon>
            </div>
            <div class="header-text">
              <h3>出库单数据导出</h3>
              <p>选择导出格式和数据范围</p>
            </div>
          </div>
          <el-button type="text" @click="exportDialogVisible = false" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 导出设置 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Setting /></el-icon>
            <span>导出设置</span>
          </div>
          <el-form :model="exportForm" label-width="100px">
            <!-- 导出格式 -->
            <el-form-item label="导出格式">
              <el-radio-group v-model="exportForm.format">
                <el-radio value="excel">Excel (.xlsx)</el-radio>
                <el-radio value="csv">CSV (.csv)</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 导出范围 -->
            <el-form-item label="导出范围">
              <el-radio-group v-model="exportForm.range">
                <el-radio value="selected" :disabled="!hasSelectedOrders">
                  已选择的出库单 ({{ selectedOrders.length }} 个)
                </el-radio>
                <el-radio value="current">当前页面数据 ({{ currentPageData.length }} 个)</el-radio>
                <el-radio value="all">全部出库单</el-radio>
                <el-radio value="custom">自定义条件</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 自定义条件 -->
            <div v-if="exportForm.range === 'custom'" class="custom-conditions">
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
                <el-select v-model="exportForm.warehouseId" placeholder="请选择仓库" clearable style="width: 100%">
                  <el-option label="全部仓库" value="" />
                  <el-option
                    v-for="warehouse in warehouses"
                    :key="warehouse.id"
                    :label="warehouse.name"
                    :value="warehouse.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="出库类型">
                <el-select v-model="exportForm.businessType" placeholder="请选择类型" clearable style="width: 100%">
                  <el-option label="全部类型" value="" />
                  <el-option label="领用出库" value="SALE_OUT" />
                  <el-option label="调拨出库" value="TRANSFER_OUT" />
                  <el-option label="借用出库" value="DAMAGE_OUT" />
                  <el-option label="其他出库" value="OTHER_OUT" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="exportForm.status" placeholder="请选择状态" clearable style="width: 100%">
                  <el-option label="全部状态" value="" />
                  <el-option label="待审批" value="PENDING" />
                  <el-option label="已审批" value="APPROVED" />
                  <el-option label="已执行" value="EXECUTED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
              </el-form-item>
            </div>

            <!-- 导出内容 -->
            <el-form-item label="导出内容">
              <el-checkbox-group v-model="exportForm.includeFields">
                <el-checkbox value="basic">基本信息</el-checkbox>
                <el-checkbox value="details">货物明细</el-checkbox>
                <el-checkbox value="approval">审批信息</el-checkbox>
                <el-checkbox value="execution">执行信息</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <!-- 文件名 -->
            <el-form-item label="文件名">
              <el-input 
                v-model="exportForm.filename" 
                placeholder="请输入文件名"
                style="width: 300px"
              >
                <template #suffix>
                  .{{ exportForm.format === 'excel' ? 'xlsx' : 'csv' }}
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>

        <!-- 预览信息 -->
        <div class="preview-section" v-if="previewData.length > 0">
          <div class="section-title">
            <el-icon><View /></el-icon>
            <span>预览信息</span>
          </div>
          <div class="preview-stats">
            <div class="stat-item">
              <span class="stat-label">出库单数量：</span>
              <span class="stat-value">{{ previewData.length }} 个</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">总出库数量：</span>
              <span class="stat-value">{{ totalQuantity }} 件</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">总出库金额：</span>
              <span class="stat-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePreview" :loading="loading">
            <el-icon><View /></el-icon>
            预览
          </el-button>
          <el-button type="success" @click="handleExport" :loading="loading">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Close, Setting, View } from '@element-plus/icons-vue'
import { useDeviceDetection } from '@/utils/responsive'
import { request } from '@/utils/request'
import * as XLSX from 'xlsx'

// 响应式检测
const { isMobile } = useDeviceDetection()

// Props
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
  }
})

// Emits
const emit = defineEmits(['refresh'])

// 响应式数据
const loading = ref(false)
const exportDialogVisible = ref(false)
const warehouses = ref([])
const previewData = ref([])

// 导出表单
const exportForm = reactive({
  format: 'excel',
  range: 'selected',
  dateRange: [],
  warehouseId: '',
  businessType: '',
  status: '',
  includeFields: ['basic', 'details'],
  filename: `出库单数据_${new Date().toLocaleDateString().replace(/\//g, '')}`
})

// 计算属性
const hasSelectedOrders = computed(() => props.selectedOrders.length > 0)

const totalQuantity = computed(() => {
  return previewData.value.reduce((sum, order) => sum + (order.totalQuantity || 0), 0)
})

const totalAmount = computed(() => {
  return previewData.value.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
})

// 监听导出范围变化
watch(() => exportForm.range, async (newRange) => {
  await updatePreviewData()
}, { immediate: false })

// 监听自定义条件变化
watch([
  () => exportForm.dateRange,
  () => exportForm.warehouseId,
  () => exportForm.businessType,
  () => exportForm.status
], async () => {
  if (exportForm.range === 'custom') {
    await updatePreviewData()
  }
}, { deep: true })

// 方法
const showExportDialog = async () => {
  exportDialogVisible.value = true
  await loadWarehouses()
  await updatePreviewData()
}

const loadWarehouses = async () => {
  try {
    const response = await request.get('/warehouses/enabled')
    if (response.success) {
      warehouses.value = response.data || []
    }
  } catch (error) {
    console.error('加载仓库列表失败:', error)
  }
}

const updatePreviewData = async () => {
  try {
    loading.value = true
    let data = []

    switch (exportForm.range) {
      case 'selected':
        data = props.selectedOrders
        break
      case 'current':
        data = props.currentPageData
        break
      case 'all':
        const allResponse = await request.get('/outbound-orders', { page: 1, size: 10000 })
        if (allResponse.success) {
          data = allResponse.data?.content || []
        }
        break
      case 'custom':
        const customParams = {
          page: 1,
          size: 10000,
          startDate: exportForm.dateRange?.[0],
          endDate: exportForm.dateRange?.[1],
          warehouseId: exportForm.warehouseId || undefined,
          businessType: exportForm.businessType || undefined,
          status: exportForm.status || undefined
        }
        const customResponse = await request.get('/outbound-orders', customParams)
        if (customResponse.success) {
          data = customResponse.data?.content || []
        }
        break
    }

    previewData.value = data
  } catch (error) {
    console.error('更新预览数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handlePreview = () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  const data = prepareExportData()
  console.log('预览数据:', data.slice(0, 5)) // 显示前5条数据
  ElMessage.success(`预览成功，共 ${data.length} 条数据`)
}

const handleExport = async () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  try {
    await ElMessageBox.confirm('确认要导出出库单数据吗？', '确认导出', {
      type: 'warning'
    })

    loading.value = true
    const data = prepareExportData()

    if (exportForm.format === 'excel') {
      exportToExcel(data)
    } else {
      exportToCSV(data)
    }

    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出失败:', error)
      ElMessage.error('导出失败')
    }
  } finally {
    loading.value = false
  }
}

const prepareExportData = () => {
  return previewData.value.map(order => {
    const row = {}

    // 基本信息
    if (exportForm.includeFields.includes('basic')) {
      row['出库单号'] = order.orderNumber
      row['仓库'] = order.warehouseName
      row['出库类型'] = getBusinessTypeText(order.businessType)
      row['领取人'] = order.recipientName
      row['状态'] = getStatusText(order.status)
      row['出库日期'] = order.plannedDate
      row['总数量'] = order.totalQuantity
      row['总金额'] = order.totalAmount
      row['制单人'] = order.createdBy
      row['创建时间'] = order.createdTime
    }

    // 审批信息
    if (exportForm.includeFields.includes('approval')) {
      row['审批人'] = order.approvedBy || '-'
      row['审批时间'] = order.approvalTime || '-'
      row['审批意见'] = order.approvalComment || '-'
    }

    // 执行信息
    if (exportForm.includeFields.includes('execution')) {
      row['执行人'] = order.operatedBy || '-'
      row['执行时间'] = order.operationTime || '-'
      row['实际出库日期'] = order.actualDate || '-'
    }

    return row
  })
}

// 导出到Excel
const exportToExcel = (data) => {
  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '出库单数据')
  
  // 设置列宽
  const colWidths = Object.keys(data[0] || {}).map(() => ({ wch: 15 }))
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
    'SALE_OUT': '领用出库',
    'TRANSFER_OUT': '调拨出库',
    'DAMAGE_OUT': '借用出库',
    'OTHER_OUT': '其他出库'
  }
  return typeMap[type] || type || '-'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'EXECUTED': '已执行',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status || '-'
}
</script>

<style scoped>
.export-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  color: white;
  margin: -20px -24px 20px -24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.header-text h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-text p {
  margin: 4px 0 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.close-btn {
  color: white;
  font-size: 18px;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.dialog-body {
  padding: 0 4px;
}

.form-section, .preview-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e9ecef;
}

.custom-conditions {
  background: white;
  border-radius: 6px;
  padding: 16px;
  margin-top: 12px;
  border: 1px solid #e9ecef;
}

.preview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-item {
  background: white;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 0 0 0;
  border-top: 1px solid #e9ecef;
}

@media (max-width: 768px) {
  .dialog-header {
    padding: 16px 20px;
    margin: -20px -20px 16px -20px;
  }
  
  .header-text h3 {
    font-size: 16px;
  }
  
  .header-text p {
    font-size: 13px;
  }
  
  .preview-stats {
    grid-template-columns: 1fr;
  }
  
  .dialog-footer {
    flex-direction: column;
  }
}
</style>
