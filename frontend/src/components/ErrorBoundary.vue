<template>
  <div class="error-boundary">
    <div v-if="hasError" class="error-content">
      <div class="error-icon">
        <el-icon size="64" color="#f56c6c">
          <Warning />
        </el-icon>
      </div>
      <h3 class="error-title">页面出现错误</h3>
      <p class="error-message">{{ errorMessage }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="handleReload">
          <el-icon><Refresh /></el-icon>
          重新加载
        </el-button>
        <el-button @click="handleGoHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
      </div>
      <details v-if="errorDetails" class="error-details">
        <summary>错误详情</summary>
        <pre>{{ errorDetails }}</pre>
      </details>
    </div>
    <slot v-else />
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { Warning, Refresh, HomeFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const hasError = ref(false)
const errorMessage = ref('')
const errorDetails = ref('')

// 捕获子组件错误
onErrorCaptured((error, instance, info) => {
  console.error('Error captured:', error, info)
  
  hasError.value = true
  errorMessage.value = error.message || '未知错误'
  errorDetails.value = `${error.stack}\n\nComponent: ${info}`
  
  // 发送错误报告（可选）
  reportError(error, info)
  
  return false // 阻止错误继续传播
})

// 错误报告函数
const reportError = (error, info) => {
  // 这里可以发送错误到监控服务
  console.log('Reporting error:', {
    message: error.message,
    stack: error.stack,
    info,
    url: window.location.href,
    userAgent: navigator.userAgent,
    timestamp: new Date().toISOString()
  })
}

// 重新加载页面
const handleReload = () => {
  hasError.value = false
  errorMessage.value = ''
  errorDetails.value = ''
  window.location.reload()
}

// 返回首页
const handleGoHome = () => {
  hasError.value = false
  errorMessage.value = ''
  errorDetails.value = ''
  router.push('/')
  ElMessage.success('已返回首页')
}
</script>

<style lang="scss" scoped>
.error-boundary {
  width: 100%;
  height: 100%;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
  text-align: center;

  .error-icon {
    margin-bottom: 24px;
  }

  .error-title {
    font-size: 24px;
    color: #303133;
    margin: 0 0 16px 0;
    font-weight: 600;
  }

  .error-message {
    font-size: 16px;
    color: #606266;
    margin: 0 0 32px 0;
    max-width: 500px;
    line-height: 1.5;
  }

  .error-actions {
    display: flex;
    gap: 16px;
    margin-bottom: 32px;

    .el-button {
      padding: 12px 24px;
    }
  }

  .error-details {
    width: 100%;
    max-width: 800px;
    text-align: left;

    summary {
      cursor: pointer;
      font-weight: 600;
      color: #909399;
      margin-bottom: 12px;
      
      &:hover {
        color: #409eff;
      }
    }

    pre {
      background: #f5f7fa;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      padding: 16px;
      font-size: 12px;
      color: #606266;
      overflow: auto;
      max-height: 300px;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .error-content {
    padding: 20px;
    min-height: 300px;

    .error-title {
      font-size: 20px;
    }

    .error-message {
      font-size: 14px;
    }

    .error-actions {
      flex-direction: column;
      width: 100%;
      max-width: 300px;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>
