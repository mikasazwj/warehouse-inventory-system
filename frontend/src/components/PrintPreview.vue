<template>
  <el-dialog
    v-model="visible"
    title="打印预览"
    width="90%"
    :close-on-click-modal="false"
    class="print-preview-dialog"
  >
    <div class="print-preview-container">
      <!-- 工具栏 -->
      <div class="preview-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handlePrint">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
          <el-button @click="handleClose">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button-group>
            <el-button :class="{ active: scale === 0.5 }" @click="setScale(0.5)">50%</el-button>
            <el-button :class="{ active: scale === 0.75 }" @click="setScale(0.75)">75%</el-button>
            <el-button :class="{ active: scale === 1 }" @click="setScale(1)">100%</el-button>
            <el-button :class="{ active: scale === 1.25 }" @click="setScale(1.25)">125%</el-button>
          </el-button-group>
        </div>
      </div>

      <!-- 预览内容 -->
      <div class="preview-content" :style="{ transform: `scale(${scale})`, transformOrigin: 'top center' }">
        <div class="print-page" v-html="content"></div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Printer, Close } from '@element-plus/icons-vue'
import { printHtml } from '@/utils/print'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  content: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: '打印文档'
  }
})

const emit = defineEmits(['update:modelValue', 'print', 'close'])

const visible = ref(false)
const scale = ref(1)

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

// 监听 visible 变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 设置缩放比例
const setScale = (newScale) => {
  scale.value = newScale
}

// 处理打印
const handlePrint = () => {
  printHtml(props.content, {
    title: props.title,
    beforePrint: () => {
      ElMessage.info('正在准备打印...')
    },
    afterPrint: () => {
      ElMessage.success('打印任务已发送')
      emit('print')
    }
  })
}

// 处理关闭
const handleClose = () => {
  visible.value = false
  emit('close')
}
</script>

<style lang="scss" scoped>

.print-preview-dialog {
  :deep(.el-dialog) {
    margin: 20px auto;
    max-height: calc(100vh - 40px);
  }

  :deep(.el-dialog__body) {
    padding: 0;
    height: calc(100vh - 200px);
    overflow: hidden;
  }
}

.print-preview-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.preview-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;

  .toolbar-left {
    display: flex;
    gap: 10px;
  }

  .toolbar-right {
    .el-button-group {
      .el-button {
        &.active {
          background: #409eff;
          color: white;
          border-color: #409eff;
        }
      }
    }
  }
}

.preview-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  transition: transform 0.3s ease;
}

.print-page {
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-height: 297mm; // A4 高度
  width: 210mm; // A4 宽度
  padding: 15mm;
  margin: 0 auto;
  position: relative;

  // 打印样式
  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 10px 0;

    th,
    td {
      border: 1px solid #ddd;
      padding: 8px;
      text-align: left;
    }

    th {
      background-color: #f5f5f5;
      font-weight: bold;
    }
  }

  :deep(.print-title) {
    text-align: center;
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 20px;
    border-bottom: 2px solid #333;
    padding-bottom: 10px;
  }

  :deep(.info-row) {
    display: flex;
    justify-content: space-between;
    margin: 10px 0;
    padding: 5px 0;
  }

  :deep(.info-item) {
    flex: 1;
  }

  :deep(.signature-area) {
    margin-top: 40px;
    display: flex;
    justify-content: space-between;
  }

  :deep(.signature-item) {
    text-align: center;
    width: 150px;
  }

  :deep(.signature-line) {
    border-bottom: 1px solid #333;
    height: 40px;
    margin-bottom: 5px;
  }

  // 打印预览中的页头页脚样式
  :deep(.print-header) {
    background: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    margin-bottom: 15px;
    position: relative;
  }

  :deep(.print-footer) {
    background: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    margin-top: 15px;
    position: relative;
  }

  // 预览模式下的分页提示
  :deep(.page-break-indicator) {
    border-top: 2px dashed #007bff;
    margin: 20px 0;
    padding: 10px 0;
    text-align: center;
    color: #007bff;
    font-size: 12px;
    background: rgba(0, 123, 255, 0.1);
  }

  // 表格在预览中的样式优化
  :deep(.details-table) {
    font-size: 12px;

    th, td {
      padding: 6px 8px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .print-preview-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 10px auto;
      max-height: calc(100vh - 20px);
    }
  }

  .preview-toolbar {
    flex-direction: column;
    gap: 10px;
    padding: 10px;

    .toolbar-left,
    .toolbar-right {
      width: 100%;
      justify-content: center;
    }
  }

  .print-page {
    width: 100%;
    min-height: auto;
    padding: 15px;
  }
}


</style>
