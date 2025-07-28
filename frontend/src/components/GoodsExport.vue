<template>
  <div>
    <!-- 导出按钮 -->
    <el-button
      @click="showExportDialog"
      :size="size"
      :loading="exporting"
      class="goods-export-btn"
    >
      <el-icon><Download /></el-icon>
      导出数据
    </el-button>

    <!-- 导出设置对话框 -->
    <el-dialog
      v-model="exportDialogVisible"
      :width="isMobile ? '95%' : '1000px'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="goods-export-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header goods-export-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Download /></el-icon>
              </div>
              <div class="title-content">
                <h2>导出货物数据</h2>
                <p>设置导出参数并生成数据文件</p>
              </div>
            </div>
            <el-button @click="exportDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 导出设置卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Setting /></el-icon>
            <span>导出设置</span>
          </div>
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
              <el-form-item label="分类">
                <el-select
                  v-model="exportForm.categoryId"
                  placeholder="请选择分类（可选）"
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="状态">
                <el-select
                  v-model="exportForm.enabled"
                  placeholder="请选择状态（可选）"
                  clearable
                  style="width: 100%"
                >
                  <el-option label="启用" :value="true" />
                  <el-option label="禁用" :value="false" />
                </el-select>
              </el-form-item>

              <el-form-item label="关键词">
                <el-input
                  v-model="exportForm.keyword"
                  placeholder="货物编码、名称、规格等"
                  clearable
                />
              </el-form-item>
            </div>

            <!-- 导出格式 -->
            <el-form-item label="导出格式">
              <el-radio-group v-model="exportForm.format">
                <el-radio value="xlsx">Excel格式 (.xlsx)</el-radio>
                <el-radio value="csv">CSV格式 (.csv)</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 导出字段 -->
            <el-form-item label="导出字段">
              <el-checkbox-group v-model="exportForm.fields">
                <el-checkbox value="code">货物编码</el-checkbox>
                <el-checkbox value="name">货物名称</el-checkbox>
                <el-checkbox value="categoryName">分类</el-checkbox>
                <el-checkbox value="unit">单位</el-checkbox>
                <el-checkbox value="specification">规格/型号</el-checkbox>
                <el-checkbox value="minStock">最小库存</el-checkbox>
                <el-checkbox value="maxStock">最大库存</el-checkbox>
                <el-checkbox value="enabled">状态</el-checkbox>
                <el-checkbox value="createdTime">创建时间</el-checkbox>
                <el-checkbox value="description">描述</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </div>

        <!-- 导出预览卡片 -->
        <div class="form-section" v-if="previewData.length > 0">
          <div class="section-title">
            <el-icon><View /></el-icon>
            <span>数据预览</span>
          </div>
          <div class="preview-info">
            <p>预计导出 <strong>{{ previewData.length }}</strong> 条记录</p>
          </div>
          <el-table :data="previewData.slice(0, 5)" size="small" max-height="200">
            <el-table-column prop="code" label="货物编码" width="120" />
            <el-table-column prop="name" label="货物名称" min-width="150" />
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
          </el-table>
          <div v-if="previewData.length > 5" class="preview-more">
            还有 {{ previewData.length - 5 }} 条记录...
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button @click="previewExport" :loading="previewing">预览数据</el-button>
          <el-button type="primary" @click="handleExport" :loading="exporting">
            导出
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { Download, Close, Setting, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import { useDeviceDetection } from '@/utils/responsive'
import * as XLSX from 'xlsx'

// 响应式检测
const { isMobile } = useDeviceDetection()

const props = defineProps({
  selectedGoods: {
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
  categories: {
    type: Array,
    default: () => []
  },
  size: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['refresh'])

const exportDialogVisible = ref(false)
const exporting = ref(false)
const previewing = ref(false)
const previewData = ref([])

const exportForm = reactive({
  range: 'current',
  format: 'xlsx',
  fields: ['code', 'name', 'categoryName', 'unit', 'specification', 'enabled', 'createdTime'],
  categoryId: '',
  enabled: null,
  keyword: ''
})

// 计算属性
const canExportSelected = computed(() => {
  return props.selectedGoods && props.selectedGoods.length > 0
})

// 显示导出对话框
const showExportDialog = () => {
  exportDialogVisible.value = true
  // 重置表单
  exportForm.range = 'current'
  exportForm.format = 'xlsx'
  exportForm.fields = ['code', 'name', 'categoryName', 'unit', 'specification', 'enabled', 'createdTime']
  exportForm.categoryId = ''
  exportForm.enabled = null
  exportForm.keyword = ''
  previewData.value = []
}

// 预览数据
const previewExport = async () => {
  try {
    previewing.value = true
    
    let data = []
    
    switch (exportForm.range) {
      case 'current':
        data = props.currentPageData
        break
      case 'selected':
        data = props.selectedGoods
        break
      case 'all':
        // 获取所有数据
        const allResponse = await request.get('/goods', { 
          params: { 
            page: 1, 
            size: 10000,
            ...props.searchParams 
          } 
        })
        data = allResponse.data.records || []
        break
      case 'filtered':
        // 按条件筛选
        const params = {
          page: 1,
          size: 10000,
          categoryId: exportForm.categoryId || undefined,
          enabled: exportForm.enabled,
          keyword: exportForm.keyword || undefined
        }
        const filteredResponse = await request.get('/goods', { params })
        data = filteredResponse.data.records || []
        break
    }
    
    previewData.value = data
    ElMessage.success(`预览成功，共 ${data.length} 条记录`)
  } catch (error) {
    console.error('预览失败:', error)
    ElMessage.error('预览失败')
  } finally {
    previewing.value = false
  }
}

// 导出数据
const handleExport = async () => {
  try {
    if (exportForm.fields.length === 0) {
      ElMessage.warning('请至少选择一个导出字段')
      return
    }
    
    exporting.value = true
    
    // 如果没有预览数据，先获取数据
    if (previewData.value.length === 0) {
      await previewExport()
    }
    
    if (previewData.value.length === 0) {
      ElMessage.warning('没有数据可导出')
      return
    }
    
    // 根据格式导出
    if (exportForm.format === 'xlsx') {
      exportToExcel()
    } else {
      exportToCSV()
    }
    
    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 导出到Excel
const exportToExcel = () => {
  const fieldMap = {
    code: '货物编码',
    name: '货物名称',
    categoryName: '分类',
    unit: '单位',
    specification: '规格/型号',
    minStock: '最小库存',
    maxStock: '最大库存',
    enabled: '状态',
    createdTime: '创建时间',
    description: '描述'
  }
  
  const headers = exportForm.fields.map(field => fieldMap[field])
  const data = previewData.value.map(item => {
    return exportForm.fields.map(field => {
      if (field === 'enabled') {
        return item[field] ? '启用' : '禁用'
      }
      if (field === 'createdTime') {
        return item[field] ? new Date(item[field]).toLocaleString() : ''
      }
      return item[field] || ''
    })
  })
  
  const ws = XLSX.utils.aoa_to_sheet([headers, ...data])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '货物数据')
  
  const fileName = `货物数据_${new Date().toISOString().slice(0, 10)}.xlsx`
  XLSX.writeFile(wb, fileName)
}

// 导出到CSV
const exportToCSV = () => {
  const fieldMap = {
    code: '货物编码',
    name: '货物名称',
    categoryName: '分类',
    unit: '单位',
    specification: '规格/型号',
    minStock: '最小库存',
    maxStock: '最大库存',
    enabled: '状态',
    createdTime: '创建时间',
    description: '描述'
  }
  
  const headers = exportForm.fields.map(field => fieldMap[field])
  const csvContent = [
    headers.join(','),
    ...previewData.value.map(item => {
      return exportForm.fields.map(field => {
        let value = ''
        if (field === 'enabled') {
          value = item[field] ? '启用' : '禁用'
        } else if (field === 'createdTime') {
          value = item[field] ? new Date(item[field]).toLocaleString() : ''
        } else {
          value = item[field] || ''
        }
        // CSV转义
        if (typeof value === 'string' && (value.includes(',') || value.includes('"') || value.includes('\n'))) {
          value = `"${value.replace(/"/g, '""')}"`
        }
        return value
      }).join(',')
    })
  ].join('\n')
  
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `货物数据_${new Date().toISOString().slice(0, 10)}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
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

        .el-input, .el-select {
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

        .el-checkbox-group {
          .el-checkbox {
            margin-right: 20px;
            margin-bottom: 8px;

            :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
              background-color: #667eea;
              border-color: #667eea;
            }
          }
        }
      }

      .filter-section {
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #f0f0f0;
      }

      .preview-info {
        margin-bottom: 16px;
        padding: 12px;
        background: #f0f7ff;
        border-radius: 8px;
        border-left: 4px solid #667eea;

        p {
          margin: 0;
          color: #606266;
        }

        strong {
          color: #667eea;
        }
      }

      .preview-more {
        text-align: center;
        padding: 8px;
        color: #909399;
        font-size: 12px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
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

          .el-checkbox-group {
            .el-checkbox {
              margin-right: 16px;
              margin-bottom: 8px;
              display: block;
            }
          }
        }
      }
    }
  }

  .dialog-footer {
    padding: 12px 16px;
    flex-direction: column;
    gap: 8px;

    .el-button {
      width: 100%;
    }
  }
}

/* 确保导出按钮与其他按钮样式一致 */
.goods-export-btn {
  white-space: nowrap;
  transition: all 0.3s ease;

  /* 在父容器的header-actions中时，继承统一样式 */
  .el-icon {
    margin-right: 6px;
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .goods-export-btn {
    width: 100%;
    justify-content: center;

    &.el-button--small {
      height: 44px;
      padding: 12px 16px;
      font-size: 14px;
    }
  }
}

@media (max-width: 768px) and (min-width: 481px) {
  .goods-export-btn {
    height: 36px;
    padding: 8px 12px;
    font-size: 13px;

    &.el-button--small {
      height: 36px;
      padding: 8px 12px;
      font-size: 13px;
    }
  }
}
</style>

<!-- 全局样式 - 货物导出对话框 -->
<style>
/* 货物导出对话框样式 */
.goods-export-dialog .goods-export-header {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
  position: relative !important;
}

.goods-export-dialog .goods-export-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.goods-export-dialog .goods-export-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.goods-export-dialog .goods-export-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.goods-export-dialog .goods-export-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.goods-export-dialog .goods-export-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.goods-export-dialog .goods-export-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.goods-export-dialog .goods-export-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.goods-export-dialog .goods-export-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .goods-export-dialog .goods-export-header {
    padding: 20px 16px !important;
    margin: -20px -16px 0 -16px !important;
  }

  .goods-export-dialog .goods-export-header .title-icon {
    width: 40px !important;
    height: 40px !important;
  }

  .goods-export-dialog .goods-export-header .title-icon .el-icon {
    font-size: 20px !important;
  }

  .goods-export-dialog .goods-export-header .title-content h2 {
    font-size: 20px !important;
  }
}
</style>
