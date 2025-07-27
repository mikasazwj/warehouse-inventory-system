<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img src="/warehouse-icon.svg" alt="Logo" class="logo" />
        <h1 class="title">泰盛能源库房管理系统</h1>
        <p class="subtitle">现代化的库房管理解决方案</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>


    </div>

    <div class="login-bg">
      <div class="bg-content">
        <h2>功能特色</h2>
        <ul class="feature-list">
          <li><el-icon><Check /></el-icon>完整的入库、出库、调拨、盘点业务流程</li>
          <li><el-icon><Check /></el-icon>实时库存管理和预警机制</li>
          <li><el-icon><Check /></el-icon>多仓库、多角色权限管理</li>
          <li><el-icon><Check /></el-icon>丰富的统计报表和数据分析</li>
          <li><el-icon><Check /></el-icon>现代化的用户界面和交互体验</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const loginFormRef = ref()

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}



// 方法
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()

    loading.value = true
    await userStore.login(loginForm)

    ElMessage.success('登录成功')

    // 根据用户角色跳转到合适的页面
    const role = userStore.role
    let defaultPage = '/dashboard'

    switch (role) {
      case 'ROLE_USER':
      case 'SQUAD_LEADER':
      case 'TEAM_LEADER':
        defaultPage = '/outbound'
        break
      case 'WAREHOUSE_ADMIN':
      case 'ROLE_ADMIN':
      default:
        defaultPage = '/dashboard'
        break
    }

    router.push(defaultPage)
  } catch (error) {
    // 登录失败，不输出敏感信息到控制台
  } finally {
    loading.value = false
  }
}



// 生命周期
onMounted(() => {
  // 如果已经登录，直接跳转到首页
  if (userStore.isLoggedIn) {
    router.push('/dashboard')
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  background: white;
  max-width: 500px;

  .login-header {
    text-align: center;
    margin-bottom: 40px;

    .logo {
      width: 80px;
      height: 80px;
      margin-bottom: 20px;
    }

    .title {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin: 0 0 10px 0;
    }

    .subtitle {
      font-size: 16px;
      color: #909399;
      margin: 0;
    }
  }

  .login-form {
    width: 100%;
    max-width: 400px;

    .login-btn {
      width: 100%;
      margin-top: 10px;
    }
  }


}

.login-bg {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  padding: 40px;

  .bg-content {
    max-width: 500px;

    h2 {
      font-size: 32px;
      margin-bottom: 30px;
      text-align: center;
    }

    .feature-list {
      list-style: none;
      padding: 0;

      li {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        font-size: 18px;

        .el-icon {
          margin-right: 15px;
          color: #67c23a;
          font-size: 20px;
        }
      }
    }
  }
}

// 移动端响应式设计
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    padding: 20px;
  }

  .login-box {
    max-width: none;
    padding: 30px 20px;

    .login-header {
      margin-bottom: 30px;

      .logo {
        width: 60px;
        height: 60px;
        margin-bottom: 15px;
      }

      .title {
        font-size: 24px;
        margin-bottom: 8px;
      }

      .subtitle {
        font-size: 14px;
      }
    }

    .login-form {
      .el-form-item {
        margin-bottom: 20px;
      }
    }
  }

  .login-bg {
    display: none;
  }
}

// 小屏手机优化
@media (max-width: 480px) {
  .login-container {
    padding: 15px;
  }

  .login-box {
    padding: 20px 15px;

    .login-header {
      .title {
        font-size: 20px;
      }
    }
  }
}
</style>
