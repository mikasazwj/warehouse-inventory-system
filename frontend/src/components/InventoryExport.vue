<template>
  <el-dialog
    v-model="dialogVisible"
    title="导出库存数据"
    :width="isMobile ? '95%' : '900px'"
    :fullscreen="isMobile"
    class="export-dialog modern-dialog"
    :show-close="false"
    append-to-body
    :z-index="3000"
    destroy-on-close
  >
    <template #header>
      <div class="dialog-header export-header">
        <div class="header-content">
          <div class="dialog-title">
            <div class="title-icon">
              <el-icon><Download /></el-icon>
            </div>
            <div class="title-content">
              <h2>导出库存数据</h2>
              <p>选择导出格式和字段，生成库存报表</p>
            </div>
          </div>
          <el-button @click="handleClose" class="dialog-close" text>
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
    </template>

    <div class="export-content">
      <div class="export-sections">
        <!-- 基本设置卡片 -->
        <div class="export-card">
          <div class="card-header">
            <div class="card-icon settings">
              <el-icon><Setting /></el-icon>
            </div>
            <h3 class="card-title">基本设置</h3>
          </div>
          <div class="card-content">
            <el-form :model="exportForm" class="export-form">
              <div class="form-grid">
                <div class="form-item">
                  <label class="form-label">导出格式</label>
                  <el-radio-group v-model="exportForm.format" class="radio-group">
                    <el-radio label="excel" class="radio-item">
                      <div class="radio-content">
                        <el-icon><Document /></el-icon>
                        <span>Excel格式</span>
                      </div>
                    </el-radio>
                    <el-radio label="csv" class="radio-item">
                      <div class="radio-content">
                        <el-icon><Tickets /></el-icon>
                        <span>CSV格式</span>
                      </div>
                    </el-radio>
                  </el-radio-group>
                </div>
                <div class="form-item">
                  <label class="form-label">导出范围</label>
                  <el-radio-group v-model="exportForm.range" class="radio-group">
                    <el-radio label="all" class="radio-item">
                      <div class="radio-content">
                        <el-icon><Grid /></el-icon>
                        <span>全部数据</span>
                      </div>
                    </el-radio>
                    <el-radio label="current" class="radio-item">
                      <div class="radio-content">
                        <el-icon><List /></el-icon>
                        <span>当前页面</span>
                      </div>
                    </el-radio>
                  </el-radio-group>
                </div>
              </div>
            </el-form>
          </div>
        </div>

        <!-- 字段选择卡片 -->
        <div class="export-card">
          <div class="card-header">
            <div class="card-icon fields">
              <el-icon><Menu /></el-icon>
            </div>
            <h3 class="card-title">选择导出字段</h3>
            <div class="card-actions">
              <el-button size="small" text @click="selectAllFields">全选</el-button>
              <el-button size="small" text @click="clearAllFields">清空</el-button>
            </div>
          </div>
          <div class="card-content">
            <el-checkbox-group v-model="exportForm.fields" class="fields-grid">
              <div class="field-category">
                <h4 class="category-title">基本信息</h4>
                <div class="field-items">
                  <el-checkbox label="warehouseName" class="field-item">
                    <div class="field-content">
                      <el-icon><Box /></el-icon>
                      <span>仓库</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="goodsCode" class="field-item">
                    <div class="field-content">
                      <el-icon><Document /></el-icon>
                      <span>货物编码</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="goodsName" class="field-item">
                    <div class="field-content">
                      <el-icon><Box /></el-icon>
                      <span>货物名称</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="categoryName" class="field-item">
                    <div class="field-content">
                      <el-icon><Menu /></el-icon>
                      <span>分类</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="specification" class="field-item">
                    <div class="field-content">
                      <el-icon><List /></el-icon>
                      <span>规格型号</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="goodsUnit" class="field-item">
                    <div class="field-content">
                      <el-icon><Menu /></el-icon>
                      <span>单位</span>
                    </div>
                  </el-checkbox>
                </div>
              </div>

              <div class="field-category">
                <h4 class="category-title">库存信息</h4>
                <div class="field-items">
                  <el-checkbox label="quantity" class="field-item">
                    <div class="field-content">
                      <el-icon><TrendCharts /></el-icon>
                      <span>当前库存</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="availableQuantity" class="field-item">
                    <div class="field-content">
                      <el-icon><CircleCheck /></el-icon>
                      <span>可用库存</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="lockedQuantity" class="field-item">
                    <div class="field-content">
                      <el-icon><Lock /></el-icon>
                      <span>锁定库存</span>
                    </div>
                  </el-checkbox>
                </div>
              </div>

              <div class="field-category">
                <h4 class="category-title">价格信息</h4>
                <div class="field-items">
                  <el-checkbox label="costPrice" class="field-item">
                    <div class="field-content">
                      <el-icon>¥</el-icon>
                      <span>单价</span>
                    </div>
                  </el-checkbox>
                  <el-checkbox label="totalValue" class="field-item">
                    <div class="field-content">
                      <el-icon>¥</el-icon>
                      <span>库存价值</span>
                    </div>
                  </el-checkbox>
                </div>
              </div>

              <div class="field-category">
                <h4 class="category-title">时间信息</h4>
                <div class="field-items">
                  <el-checkbox label="updatedTime" class="field-item">
                    <div class="field-content">
                      <el-icon><Clock /></el-icon>
                      <span>更新时间</span>
                    </div>
                  </el-checkbox>
                </div>
              </div>
            </el-checkbox-group>
          </div>
        </div>

        <!-- 文件名设置卡片 -->
        <div class="export-card">
          <div class="card-header">
            <div class="card-icon filename">
              <el-icon><Document /></el-icon>
            </div>
            <h3 class="card-title">文件名设置</h3>
          </div>
          <div class="card-content">
            <div class="filename-input">
              <el-input
                v-model="exportForm.filename"
                placeholder="请输入文件名称（不含扩展名）"
                size="large"
                class="filename-field"
              >
                <template #prepend>
                  <el-icon><Folder /></el-icon>
                </template>
                <template #append>
                  <span class="file-extension">.{{ exportForm.format === 'excel' ? 'xlsx' : 'csv' }}</span>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer export-footer">
        <div class="footer-actions">
          <el-button @click="handleClose" size="large" class="cancel-btn">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="handleExport" :loading="exporting" size="large" class="submit-btn">
            <el-icon><Download /></el-icon>
            {{ exporting ? '导出中...' : '开始导出' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useDeviceDetection } from '@/utils/responsive'
import {
  Download,
  Close,
  Setting,
  Document,
  Tickets,
  Grid,
  List,
  Menu,
  Box,
  TrendCharts,
  CircleCheck,
  Lock,
  Clock,
  Folder
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 响应式检测
const { isMobile } = useDeviceDetection()

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['close', 'export'])

// 响应式数据
const exporting = ref(false)

// 弹窗显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => {
    if (!value) {
      emit('close')
    }
  }
})

// 导出表单
const exportForm = reactive({
  format: 'excel',
  range: 'all',
  fields: [
    'warehouseName',
    'goodsCode',
    'goodsName',
    'categoryName',
    'specification',
    'goodsUnit',
    'quantity',
    'availableQuantity',
    'lockedQuantity',
    'costPrice',
    'totalValue',
    'updatedTime'
  ],
  filename: ''
})

// 监听弹窗显示状态，重置表单
watch(() => props.visible, (visible) => {
  if (visible) {
    // 生成默认文件名
    exportForm.filename = `库存数据_${dayjs().format('YYYY-MM-DD_HH-mm')}`
  }
})

// 全选字段
const selectAllFields = () => {
  exportForm.fields = [
    'warehouseName',
    'goodsCode',
    'goodsName',
    'categoryName',
    'specification',
    'goodsUnit',
    'quantity',
    'availableQuantity',
    'lockedQuantity',
    'costPrice',
    'totalValue',
    'updatedTime'
  ]
}

// 清空字段
const clearAllFields = () => {
  exportForm.fields = []
}

// 关闭弹窗
const handleClose = () => {
  emit('close')
}

// 导出数据
const handleExport = async () => {
  if (exportForm.fields.length === 0) {
    ElMessage.warning('请至少选择一个导出字段')
    return
  }

  if (!exportForm.filename.trim()) {
    ElMessage.warning('请输入文件名称')
    return
  }

  try {
    exporting.value = true
    
    // 发送导出配置给父组件
    emit('export', {
      format: exportForm.format,
      range: exportForm.range,
      fields: exportForm.fields,
      filename: exportForm.filename.trim()
    })
    
    // 关闭弹窗
    handleClose()
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exporting.value = false
  }
}
</script>

<style lang="scss" scoped>
/* 导出对话框现代化样式 */
.export-header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 24px 32px;
  margin: -20px -24px 0 -24px;
  border-radius: 12px 12px 0 0;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .dialog-title {
    display: flex;
    align-items: center;
    gap: 16px;

    .title-icon {
      width: 48px;
      height: 48px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(10px);

      .el-icon {
        font-size: 24px;
        color: white;
      }
    }

    .title-content {
      h2 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: white;
      }

      p {
        margin: 4px 0 0 0;
        font-size: 14px;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .dialog-close {
    color: white;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    flex-shrink: 0;
    margin-left: auto;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.export-content {
  padding: 32px 0;
  max-height: 70vh;
  overflow-y: auto;
}

.export-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.export-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }
}

.card-header {
  padding: 20px 24px 16px 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 12px;

  .card-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;

    &.settings {
      background: linear-gradient(135deg, #3b82f6, #1d4ed8);
      color: white;
    }

    &.fields {
      background: linear-gradient(135deg, #8b5cf6, #7c3aed);
      color: white;
    }

    &.filename {
      background: linear-gradient(135deg, #f59e0b, #d97706);
      color: white;
    }

    .el-icon {
      font-size: 20px;
    }
  }

  .card-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    flex: 1;
  }

  .card-actions {
    display: flex;
    gap: 8px;
  }
}

.card-content {
  padding: 24px;
}

/* 表单样式 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.form-item {
  .form-label {
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
    display: block;
  }

  .radio-group {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .radio-item {
      margin: 0;
      padding: 16px;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        border-color: #3b82f6;
        background: #f8fafc;
      }

      &.is-checked {
        border-color: #3b82f6;
        background: linear-gradient(135deg, #eff6ff, #dbeafe);
      }

      .radio-content {
        display: flex;
        align-items: center;
        gap: 12px;

        .el-icon {
          font-size: 20px;
          color: #6b7280;
        }

        span {
          font-weight: 500;
          color: #374151;
        }
      }
    }
  }
}

/* 字段选择样式 */
.fields-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.field-category {
  .category-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 16px 0;
    padding-bottom: 8px;
    border-bottom: 2px solid #e5e7eb;
    display: flex;
    align-items: center;
    gap: 8px;

    &::before {
      content: '';
      width: 4px;
      height: 16px;
      background: #3b82f6;
      border-radius: 2px;
    }
  }

  .field-items {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;

    .field-item {
      margin: 0;
      padding: 12px 16px;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        border-color: #3b82f6;
        background: #f8fafc;
      }

      &.is-checked {
        border-color: #3b82f6;
        background: linear-gradient(135deg, #eff6ff, #dbeafe);
      }

      .field-content {
        display: flex;
        align-items: center;
        gap: 8px;

        .el-icon {
          font-size: 16px;
          color: #6b7280;
        }

        span {
          font-size: 14px;
          font-weight: 500;
          color: #374151;
        }
      }
    }
  }
}

/* 文件名输入样式 */
.filename-input {
  .filename-field {
    .el-input__inner {
      font-size: 16px;
      font-weight: 500;
    }

    .el-input-group__prepend {
      background: #f3f4f6;
      border-color: #d1d5db;
    }

    .el-input-group__append {
      background: #f3f4f6;
      border-color: #d1d5db;

      .file-extension {
        font-weight: 600;
        color: #6b7280;
      }
    }
  }
}

/* 底部按钮样式 */
.export-footer {
  padding: 24px 32px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
  margin: 0 -24px -20px -24px;

  .footer-actions {
    display: flex;
    justify-content: center;
    gap: 16px;

    .cancel-btn {
      min-width: 120px;
    }

    .submit-btn {
      min-width: 140px;
    }
  }
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .export-header {
    padding: 20px 16px;
    margin: -20px -16px 0 -16px;

    .title-icon {
      width: 40px;
      height: 40px;

      .el-icon {
        font-size: 20px;
      }
    }

    .title-content h2 {
      font-size: 20px;
    }
  }

  .export-content {
    padding: 24px 0;
  }

  .export-sections {
    gap: 16px;
  }

  .card-content {
    padding: 16px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .field-items {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .export-footer {
    padding: 16px;
    margin: 0 -16px -20px -16px;

    .footer-actions {
      flex-direction: column;
      gap: 12px;

      .cancel-btn,
      .submit-btn {
        width: 100%;
      }
    }
  }
}
</style>
