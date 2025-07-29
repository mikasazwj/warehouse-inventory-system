<template>
  <div>
    <!-- 台账打印按钮 -->
    <el-button 
      type="primary" 
      :icon="Printer" 
      @click="showPrintDialog"
      :disabled="loading"
    >
      台账打印
    </el-button>

    <!-- 打印设置对话框 -->
    <el-dialog
      v-model="printDialogVisible"
      :width="isMobile ? '95%' : '800px'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="ledger-print-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <el-icon><Printer /></el-icon>
            </div>
            <div class="header-text">
              <h3>出库单台账打印</h3>
              <p>设置打印参数并生成台账报表</p>
            </div>
          </div>
          <el-button type="text" @click="printDialogVisible = false" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 打印设置卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Setting /></el-icon>
            <span>打印设置</span>
          </div>
          <el-form :model="printForm" label-width="100px">
            <!-- 打印范围 -->
            <el-form-item label="打印范围">
              <el-radio-group v-model="printForm.range">
                <el-radio value="selected" :disabled="!hasSelectedOrders">
                  已选择的出库单 ({{ selectedOrders.length }} 个)
                </el-radio>
                <el-radio value="current">当前页面数据 ({{ allOrders.length }} 个)</el-radio>
                <el-radio value="all">全部出库单</el-radio>
                <el-radio value="custom">自定义条件</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 自定义条件 -->
            <div v-if="printForm.range === 'custom'" class="custom-conditions">
              <el-form-item label="日期范围">
                <el-date-picker
                  v-model="printForm.dateRange"
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
                <el-select v-model="printForm.warehouseId" placeholder="请选择仓库" clearable style="width: 100%">
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
                <el-select v-model="printForm.businessType" placeholder="请选择类型" clearable style="width: 100%">
                  <el-option label="全部类型" value="" />
                  <el-option label="领用出库" value="SALE_OUT" />
                  <el-option label="调拨出库" value="TRANSFER_OUT" />
                  <el-option label="借用出库" value="DAMAGE_OUT" />
                  <el-option label="其他出库" value="OTHER_OUT" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="printForm.status" placeholder="请选择状态" clearable style="width: 100%">
                  <el-option label="全部状态" value="" />
                  <el-option label="待审批" value="PENDING" />
                  <el-option label="已审批" value="APPROVED" />
                  <el-option label="已执行" value="EXECUTED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
              </el-form-item>
            </div>

            <!-- 打印选项 -->
            <el-form-item label="打印选项">
              <el-checkbox-group v-model="printForm.options">
                <el-checkbox value="showDetails">包含货物明细</el-checkbox>
                <el-checkbox value="showSummary">显示汇总统计</el-checkbox>
                <el-checkbox value="showPageNumber">显示页码</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <!-- 排序方式 -->
            <el-form-item label="排序方式">
              <el-select v-model="printForm.sortBy" style="width: 200px">
                <el-option label="按创建时间" value="createdTime" />
                <el-option label="按出库单号" value="orderNumber" />
                <el-option label="按出库日期" value="plannedDate" />
                <el-option label="按金额" value="totalAmount" />
              </el-select>
              <el-select v-model="printForm.sortOrder" style="width: 100px; margin-left: 10px">
                <el-option label="升序" value="asc" />
                <el-option label="降序" value="desc" />
              </el-select>
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
          <el-button @click="printDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePreview" :loading="loading">
            <el-icon><View /></el-icon>
            预览
          </el-button>
          <el-button type="success" @click="handlePrint" :loading="loading">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 打印预览 -->
    <PrintPreview
      v-model="printPreviewVisible"
      :content="printContent"
      :title="printTitle"
      @print="handlePrintComplete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Printer, Close, Setting, View } from '@element-plus/icons-vue'
import { useDeviceDetection } from '@/utils/responsive'
import { generateOutboundLedgerPrintContent } from '@/utils/print'
import { request } from '@/utils/request'
import PrintPreview from '@/components/PrintPreview.vue'

// 响应式检测
const { isMobile } = useDeviceDetection()

// Props
const props = defineProps({
  selectedOrders: {
    type: Array,
    default: () => []
  },
  allOrders: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['refresh'])

// 响应式数据
const loading = ref(false)
const printDialogVisible = ref(false)
const printPreviewVisible = ref(false)
const printContent = ref('')
const printTitle = ref('')
const warehouses = ref([])
const previewData = ref([])

// 打印表单
const printForm = reactive({
  range: 'selected',
  dateRange: [],
  warehouseId: '',
  businessType: '',
  status: '',
  options: ['showDetails', 'showSummary', 'showPageNumber'],
  sortBy: 'createdTime',
  sortOrder: 'desc'
})

// 计算属性
const hasSelectedOrders = computed(() => props.selectedOrders.length > 0)

const totalQuantity = computed(() => {
  return previewData.value.reduce((sum, order) => sum + (order.totalQuantity || 0), 0)
})

const totalAmount = computed(() => {
  return previewData.value.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
})

// 监听打印范围变化
watch(() => printForm.range, async (newRange) => {
  await updatePreviewData()
}, { immediate: false })

// 监听自定义条件变化
watch([
  () => printForm.dateRange,
  () => printForm.warehouseId,
  () => printForm.businessType,
  () => printForm.status,
  () => printForm.sortBy,
  () => printForm.sortOrder
], async () => {
  if (printForm.range === 'custom') {
    await updatePreviewData()
  }
}, { deep: true })

// 方法
const showPrintDialog = async () => {
  printDialogVisible.value = true
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

    switch (printForm.range) {
      case 'selected':
        data = props.selectedOrders
        break
      case 'current':
        data = props.allOrders
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
          startDate: printForm.dateRange?.[0],
          endDate: printForm.dateRange?.[1],
          warehouseId: printForm.warehouseId || undefined,
          businessType: printForm.businessType || undefined,
          status: printForm.status || undefined
        }
        const customResponse = await request.get('/outbound-orders', customParams)
        if (customResponse.success) {
          data = customResponse.data?.content || []
        }
        break
    }

    // 排序
    data.sort((a, b) => {
      const aValue = a[printForm.sortBy]
      const bValue = b[printForm.sortBy]
      const order = printForm.sortOrder === 'asc' ? 1 : -1
      
      if (aValue < bValue) return -1 * order
      if (aValue > bValue) return 1 * order
      return 0
    })

    previewData.value = data
  } catch (error) {
    console.error('更新预览数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handlePreview = async () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可打印的数据')
    return
  }

  try {
    loading.value = true
    printContent.value = generateOutboundLedgerPrintContent(previewData.value, printForm.options)
    printTitle.value = `出库单台账-${new Date().toLocaleDateString()}`
    printPreviewVisible.value = true
    printDialogVisible.value = false
  } catch (error) {
    console.error('生成预览失败:', error)
    ElMessage.error('生成预览失败')
  } finally {
    loading.value = false
  }
}

const handlePrint = async () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可打印的数据')
    return
  }

  try {
    await ElMessageBox.confirm('确认要打印出库单台账吗？', '确认打印', {
      type: 'warning'
    })

    loading.value = true
    printContent.value = generateOutboundLedgerPrintContent(previewData.value, printForm.options)
    printTitle.value = `出库单台账-${new Date().toLocaleDateString()}`
    
    // 直接打印
    const printWindow = window.open('', '_blank')
    printWindow.document.write(printContent.value)
    printWindow.document.close()
    printWindow.print()
    
    ElMessage.success('打印任务已发送')
    printDialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('打印失败:', error)
      ElMessage.error('打印失败')
    }
  } finally {
    loading.value = false
  }
}

const handlePrintComplete = () => {
  printPreviewVisible.value = false
}
</script>

<style scoped>
.ledger-print-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
