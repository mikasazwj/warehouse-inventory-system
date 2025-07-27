<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-image">
        <div class="error-icon">
          <el-icon size="120" color="#409EFF">
            <Warning />
          </el-icon>
        </div>
      </div>
      <div class="error-info">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">页面不存在</h2>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被删除。
        </p>
        <div class="error-actions">
          <el-button type="primary" @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          <el-button @click="goBack">
            <el-icon><Back /></el-icon>
            返回上页
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { House, Back, Warning } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  .error-content {
    display: flex;
    align-items: center;
    background: white;
    border-radius: 16px;
    padding: 60px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    max-width: 800px;
    width: 100%;

    .error-image {
      flex: 1;
      text-align: center;
      margin-right: 60px;

      img {
        max-width: 300px;
        width: 100%;
        height: auto;
      }
    }

    .error-info {
      flex: 1;

      .error-code {
        font-size: 120px;
        font-weight: bold;
        color: #409eff;
        margin: 0;
        line-height: 1;
      }

      .error-title {
        font-size: 32px;
        font-weight: 600;
        color: #303133;
        margin: 20px 0 16px 0;
      }

      .error-description {
        font-size: 16px;
        color: #606266;
        margin: 0 0 40px 0;
        line-height: 1.6;
      }

      .error-actions {
        display: flex;
        gap: 16px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .error-page {
    .error-content {
      flex-direction: column;
      text-align: center;
      padding: 40px 20px;

      .error-image {
        margin-right: 0;
        margin-bottom: 40px;

        img {
          max-width: 200px;
        }
      }

      .error-info {
        .error-code {
          font-size: 80px;
        }

        .error-title {
          font-size: 24px;
        }

        .error-actions {
          justify-content: center;
          flex-wrap: wrap;
        }
      }
    }
  }
}
</style>
