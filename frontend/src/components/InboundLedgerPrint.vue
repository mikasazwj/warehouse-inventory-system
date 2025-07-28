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
              <h3>入库单台账打印</h3>
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
                <el-radio value="selected">选中的单据</el-radio>
                <el-radio value="all">全部单据</el-radio>
                <el-radio value="date">按日期范围</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 日期范围选择 -->
            <el-form-item
              v-if="printForm.range === 'date'"
              label="日期范围"
            >
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

            <!-- 仓库筛选 -->
            <el-form-item label="仓库筛选">
              <el-select
                v-model="printForm.warehouseId"
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

            <!-- 状态筛选 -->
            <el-form-item label="状态筛选">
              <el-select
                v-model="printForm.status"
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

            <!-- 排序方式 -->
            <el-form-item label="排序方式">
              <el-select v-model="printForm.sortBy" style="width: 100%">
                <el-option label="按单号排序" value="orderNumber" />
                <el-option label="按创建时间排序" value="createdTime" />
                <el-option label="按计划日期排序" value="plannedDate" />
                <el-option label="按金额排序" value="totalAmount" />
              </el-select>
            </el-form-item>

            <!-- 排序顺序 -->
            <el-form-item label="排序顺序">
              <el-radio-group v-model="printForm.sortOrder">
                <el-radio value="asc">升序</el-radio>
                <el-radio value="desc">降序</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="printDialogVisible = false">取消</el-button>
          <el-button @click="previewPrint" :loading="loading">预览</el-button>
          <el-button type="primary" @click="handlePrint" :loading="loading">
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
      @close="handlePrintPreviewClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Printer, Close, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { generateInboundLedgerPrintContent, printHtml } from '@/utils/print'
import PrintPreview from '@/components/PrintPreview.vue'
import { request } from '@/utils/request'
import { useDeviceDetection } from '@/utils/responsive'

// 响应式检测
const { isMobile } = useDeviceDetection()

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

const emit = defineEmits(['refresh'])

const loading = ref(false)
const printDialogVisible = ref(false)
const printPreviewVisible = ref(false)
const printContent = ref('')
const printTitle = ref('')
const warehouses = ref([])

// 打印表单
const printForm = reactive({
  range: 'selected',
  dateRange: null,
  warehouseId: null,
  status: null,
  sortBy: 'createdTime',
  sortOrder: 'desc'
})

// 计算可用的打印范围
const canPrintSelected = computed(() => {
  return props.selectedOrders && props.selectedOrders.length > 0
})

// 显示打印对话框
const showPrintDialog = async () => {
  if (!canPrintSelected.value) {
    printForm.range = 'all'
  }
  
  // 加载仓库列表
  await loadWarehouses()
  
  printDialogVisible.value = true
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
    // 提供默认数据
    warehouses.value = [
      { id: 1, name: '主仓库' },
      { id: 2, name: '分仓库A' }
    ]
  }
}

// 获取要打印的订单数据
const getPrintOrders = async () => {
  let orders = []

  if (printForm.range === 'selected') {
    if (!canPrintSelected.value) {
      throw new Error('没有选中的单据')
    }
    orders = props.selectedOrders
  } else if (printForm.range === 'all') {
    orders = props.allOrders
  } else if (printForm.range === 'date') {
    if (!printForm.dateRange || printForm.dateRange.length !== 2) {
      throw new Error('请选择日期范围')
    }
    
    // 根据条件查询订单
    const params = {
      startDate: printForm.dateRange[0],
      endDate: printForm.dateRange[1],
      warehouseId: printForm.warehouseId,
      status: printForm.status,
      page: 0, // 后端使用0开始的页码
      size: 1000 // 获取大量数据用于打印
    }

    const response = await request.get('/inbound-orders', params)
    if (response.success) {
      orders = response.data.content || []
    }
  }

  // 筛选数据
  if (printForm.warehouseId && printForm.range !== 'date') {
    orders = orders.filter(order => order.warehouseId === printForm.warehouseId)
  }
  
  if (printForm.status && printForm.range !== 'date') {
    orders = orders.filter(order => order.status === printForm.status)
  }

  // 排序
  orders.sort((a, b) => {
    const aValue = a[printForm.sortBy] || ''
    const bValue = b[printForm.sortBy] || ''
    
    if (printForm.sortOrder === 'asc') {
      return aValue > bValue ? 1 : -1
    } else {
      return aValue < bValue ? 1 : -1
    }
  })

  return orders
}

// 预览打印
const previewPrint = async () => {
  try {
    loading.value = true
    
    const orders = await getPrintOrders()
    
    if (orders.length === 0) {
      ElMessage.warning('没有符合条件的单据')
      return
    }

    // 生成打印内容
    const options = {
      dateRange: printForm.dateRange ? 
        `${printForm.dateRange[0]} 至 ${printForm.dateRange[1]}` : 
        '全部'
    }
    
    printContent.value = generateInboundLedgerPrintContent(orders, options)
    printTitle.value = `入库单台账-${new Date().toLocaleDateString()}`
    
    // 显示预览
    printPreviewVisible.value = true
    printDialogVisible.value = false
    
  } catch (error) {
    console.error('预览失败:', error)
    ElMessage.error(error.message || '预览失败，请重试')
  } finally {
    loading.value = false
  }
}

// 直接打印
const handlePrint = async () => {
  try {
    loading.value = true
    
    const orders = await getPrintOrders()
    
    if (orders.length === 0) {
      ElMessage.warning('没有符合条件的单据')
      return
    }

    // 确认打印
    await ElMessageBox.confirm(
      `确定要打印 ${orders.length} 张入库单的台账吗？`,
      '确认打印',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 生成打印内容
    const options = {
      dateRange: printForm.dateRange ? 
        `${printForm.dateRange[0]} 至 ${printForm.dateRange[1]}` : 
        '全部'
    }
    
    const content = generateInboundLedgerPrintContent(orders, options)
    
    // 直接打印
    printHtml(content, {
      title: `入库单台账-${new Date().toLocaleDateString()}`,
      beforePrint: () => {
        ElMessage.info('正在准备打印...')
      },
      afterPrint: () => {
        ElMessage.success('打印任务已发送')
        printDialogVisible.value = false
      }
    })
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('打印失败:', error)
      ElMessage.error(error.message || '打印失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 打印完成
const handlePrintComplete = () => {
  printPreviewVisible.value = false
  ElMessage.success('打印任务已发送')
}

// 关闭预览
const handlePrintPreviewClose = () => {
  printContent.value = ''
  printTitle.value = ''
}
</script>

<style lang="scss" scoped>
// 现代化弹窗样式
.modern-dialog {
  :deep(.el-dialog) {
    z-index: 3000 !important;
  }

  :deep(.el-overlay) {
    z-index: 2999 !important;
  }

  :deep(.el-dialog__header) {
    padding: 0;
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 0;
    border-top: 1px solid #f0f0f0;
  }

  // 弹窗头部
  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;

    .header-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .header-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(10px);

        .el-icon {
          font-size: 24px;
        }
      }

      .header-text {
        h3 {
          margin: 0 0 4px 0;
          font-size: 20px;
          font-weight: 600;
        }

        p {
          margin: 0;
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }

    .close-btn {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      color: white;
      transition: all 0.3s;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
        transform: scale(1.05);
      }

      .el-icon {
        font-size: 18px;
      }
    }
  }

  // 弹窗主体
  .dialog-body {
    padding: 32px;
    background: #fafbfc;
    min-height: 400px;

    // 表单分组样式
    .form-section {
      background: white;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      border: 1px solid #e6e8eb;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;

        .el-icon {
          font-size: 18px;
          color: #667eea;
        }
      }

      .el-form-item {
        margin-bottom: 20px;

        .el-form-item__label {
          font-weight: 500;
          color: #606266;
        }

        .el-input, .el-select, .el-date-picker {
          :deep(.el-input__wrapper),
          :deep(.el-select__wrapper) {
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;

            &:hover {
              box-shadow: 0 2px 6px rgba(102, 126, 234, 0.15);
            }

            &.is-focus {
              box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }
          }
        }

        .el-radio-group {
          .el-radio {
            margin-right: 20px;

            :deep(.el-radio__input.is-checked .el-radio__inner) {
              background-color: #667eea;
              border-color: #667eea;
            }
          }
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .modern-dialog {
    .dialog-header {
      padding: 16px 20px;

      .header-content {
        gap: 12px;

        .header-icon {
          width: 40px;
          height: 40px;

          .el-icon {
            font-size: 20px;
          }
        }

        .header-text {
          h3 {
            font-size: 18px;
          }

          p {
            font-size: 13px;
          }
        }
      }

      .close-btn {
        width: 36px;
        height: 36px;
      }
    }

    .dialog-body {
      padding: 16px;

      .form-section {
        padding: 16px;
        margin-bottom: 16px;

        .section-title {
          font-size: 14px;
          padding-bottom: 8px;
          margin-bottom: 16px;
        }

        .el-form-item {
          margin-bottom: 16px;

          .el-form-item__label {
            width: auto !important;
            margin-bottom: 6px;
            text-align: left !important;
            font-size: 14px;
          }

          .el-form-item__content {
            margin-left: 0 !important;
          }

          .el-radio-group {
            .el-radio {
              margin-right: 16px;
              margin-bottom: 8px;
              display: block;
            }
          }
        }
      }
    }
  }
}
</style>
