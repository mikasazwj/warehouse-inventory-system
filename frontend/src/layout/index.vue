<template>
  <div class="layout-container" :class="{ mobile: isMobile }">
    <!-- 移动端遮罩层 -->
    <div
      v-if="isMobile && !isCollapsed"
      class="mobile-overlay"
      @click="toggleSidebar"
    ></div>

    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: isCollapsed, mobile: isMobile }">
      <div class="logo">
        <img src="/warehouse-icon.svg" alt="Logo" class="logo-icon" />
        <span v-show="!isCollapsed" class="logo-text">泰盛能源</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :unique-opened="true"
        router
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item
            v-if="!route.meta?.hidden && hasPermission(route)"
            :index="route.path"
          >
            <el-icon><component :is="route.meta.icon" /></el-icon>
            <template #title>{{ route.meta.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-button
            type="text"
            @click="toggleSidebar"
            class="collapse-btn"
          >
            <el-icon><Expand v-if="isCollapsed" /><Fold v-else /></el-icon>
          </el-button>
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="navbar-right">
          <!-- 全屏按钮 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-button type="text" @click="toggleFullscreen" class="navbar-btn">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 用户菜单 -->
          <el-dropdown @command="handleUserCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.realName || userStore.username }}</span>
              <el-icon class="arrow-down"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="changePassword">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="page-content">
        <ErrorBoundary>
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </ErrorBoundary>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="changePasswordVisible"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import ErrorBoundary from '@/components/ErrorBoundary.vue'
import { useDeviceDetection } from '@/utils/responsive'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 响应式数据
const isCollapsed = ref(false)
const changePasswordVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

// 菜单路由
const menuRoutes = computed(() => {
  const routes = router.getRoutes()
  return routes.find(r => r.path === '/')?.children || []
})

// 权限检查
const hasPermission = (route) => {
  if (!route.meta?.roles) return true

  // 特殊处理仓库管理：只有系统管理员可见
  if (route.path === '/warehouses') {
    return userStore.role === 'ROLE_ADMIN'
  }

  return route.meta.roles.includes(userStore.role)
}

// 方法
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 处理菜单选择（移动端自动关闭侧边栏）
const handleMenuSelect = () => {
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

// 监听移动端变化，自动调整侧边栏状态
watch(isMobile, (newVal) => {
  if (newVal) {
    // 移动端默认收起侧边栏
    isCollapsed.value = true
  }
}, { immediate: true })

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'changePassword':
      changePasswordVisible.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    
    passwordLoading.value = true
    await userStore.changePassword(passwordForm.value)
    
    changePasswordVisible.value = false
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 监听路由变化，重置修改密码表单
watch(changePasswordVisible, (visible) => {
  if (!visible) {
    passwordFormRef.value?.resetFields()
  }
})
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  height: 100vh;
  width: 100vw;
}

.sidebar {
  width: 250px;
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;

  &.collapsed {
    width: 64px;
  }

  .logo {
    display: flex;
    align-items: center;
    padding: 20px;
    color: white;
    font-size: 18px;
    font-weight: bold;

    .logo-icon {
      width: 32px;
      height: 32px;
      margin-right: 12px;
    }

    .logo-text {
      white-space: nowrap;
    }
  }

  .sidebar-menu {
    border: none;
    background: transparent;

    :deep(.el-menu-item) {
      color: #bfcbd9;
      
      &:hover {
        background: #263445;
        color: #409eff;
      }

      &.is-active {
        background: #409eff;
        color: white;
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  background: white;
  border-bottom: 1px solid #e6e6e6;

  .navbar-left {
    display: flex;
    align-items: center;

    .collapse-btn {
      margin-right: 20px;
      font-size: 18px;
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;

    .navbar-btn {
      margin-right: 10px;
      font-size: 16px;
    }

    .user-dropdown {
      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        padding: 5px 10px;
        border-radius: 4px;
        transition: background-color 0.3s;

        &:hover {
          background: #f5f5f5;
        }

        .username {
          margin: 0 8px;
          font-size: 14px;
        }

        .arrow-down {
          font-size: 12px;
        }
      }
    }
  }
}

.page-content {
  flex: 1;
  padding: 20px;
  background: #f0f2f5;
  overflow: auto;
}

// 页面切换动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .layout-container.mobile {
    .sidebar {
      position: fixed;
      top: 0;
      left: 0;
      height: 100vh;
      z-index: 1000;
      transform: translateX(-100%);
      transition: transform 0.3s ease;

      &:not(.collapsed) {
        transform: translateX(0);
      }

      &.mobile {
        width: 280px;
      }
    }

    .mobile-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(0, 0, 0, 0.5);
      z-index: 999;
    }

    .main-content {
      margin-left: 0;
      width: 100%;

      .navbar {
        padding: 0 15px;

        .navbar-left {
          .collapse-btn {
            display: block;
          }
        }

        .navbar-right {
          .user-info {
            .user-name {
              display: none;
            }
          }
        }
      }

      .content-area {
        padding: 15px;
      }
    }
  }
}

/* 平板端样式 */
@media (min-width: 769px) and (max-width: 1024px) {
  .sidebar {
    width: 200px;

    &.collapsed {
      width: 64px;
    }
  }

  .main-content {
    .navbar {
      padding: 0 20px;
    }

    .content-area {
      padding: 20px;
    }
  }
}
</style>
