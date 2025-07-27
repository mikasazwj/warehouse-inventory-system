import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import Cookies from 'js-cookie'

const TOKEN_KEY = 'warehouse_token'
const USER_INFO_KEY = 'warehouse_user_info'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: Cookies.get(TOKEN_KEY) || '',
    userInfo: JSON.parse(localStorage.getItem(USER_INFO_KEY) || '{}'),
    permissions: [],
    warehouses: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo.username || '',
    realName: (state) => state.userInfo.realName || '',
    role: (state) => state.userInfo.role || '',
    isAdmin: (state) => state.userInfo.role === 'ROLE_ADMIN',
    isWarehouseAdmin: (state) => ['ROLE_ADMIN', 'WAREHOUSE_ADMIN'].includes(state.userInfo.role),
    isTeamLeader: (state) => state.userInfo.role === 'TEAM_LEADER',
    isSquadLeader: (state) => state.userInfo.role === 'SQUAD_LEADER',
    isUser: (state) => state.userInfo.role === 'ROLE_USER',
    // 判断是否有审批权限
    hasApprovalPermission: (state) => ['ROLE_ADMIN', 'WAREHOUSE_ADMIN', 'TEAM_LEADER', 'SQUAD_LEADER'].includes(state.userInfo.role),
    // 判断是否有管理权限
    hasManagePermission: (state) => ['ROLE_ADMIN', 'WAREHOUSE_ADMIN'].includes(state.userInfo.role),
    avatar: (state) => state.userInfo.avatar || '',
    
    // 权限检查
    hasPermission: (state) => (permission) => {
      if (state.userInfo.role === 'ROLE_ADMIN') return true
      return state.permissions.includes(permission)
    },
    
    // 仓库权限检查
    hasWarehouseAccess: (state) => (warehouseId) => {
      if (state.userInfo.role === 'ROLE_ADMIN') return true
      return state.warehouses.some(w => w.id === warehouseId)
    }
  },

  actions: {
    // 登录
    async login(loginForm) {
      try {
        // 调用真实的后端API，禁用重试
        const response = await request.post('/auth/login', loginForm, { retry: false })

        if (response.success) {
          const { accessToken, user } = response.data



          // 保存token和用户信息
          this.token = accessToken
          this.userInfo = user

          // 设置token过期时间为1天，而不是7天
          Cookies.set(TOKEN_KEY, accessToken, { expires: 1 })
          localStorage.setItem(USER_INFO_KEY, JSON.stringify(user))

          // 获取用户权限和仓库信息（传递token确保请求头正确设置）
          await this.getUserPermissions(accessToken)
          await this.getUserWarehouses(accessToken)

          ElMessage.success('登录成功')
          return response
        } else {
          throw new Error(response.message || '登录失败')
        }
      } catch (error) {
        throw error
      }
    },

    // 登出
    async logout() {
      try {
        // 只有在有有效token时才调用后端登出接口
        if (this.token) {
          await request.post('/auth/logout')
        }
      } catch (error) {
        // 忽略登出请求错误
        console.warn('登出请求失败:', error)
      } finally {
        // 清除本地数据
        this.clearUserData()
        ElMessage.success('已退出登录')
      }
    },

    // 检查登录状态
    async checkLoginStatus() {
      if (!this.token) {
        // 清除可能残留的用户信息
        this.clearUserData()
        return false
      }

      try {
        const response = await request.get('/auth/user')
        if (response.success) {
          this.userInfo = response.data
          localStorage.setItem(USER_INFO_KEY, JSON.stringify(response.data))

          // 更新权限和仓库信息
          await this.getUserPermissions()
          await this.getUserWarehouses()

          return true
        }

        // token无效，清除数据
        this.clearUserData()
        return false
      } catch (error) {
        // token过期或无效，清除数据
        this.clearUserData()
        return false
      }
    },

    // 清除用户数据
    clearUserData() {
      this.token = ''
      this.userInfo = {}
      this.permissions = []
      this.warehouses = []

      Cookies.remove(TOKEN_KEY)
      localStorage.removeItem(USER_INFO_KEY)
    },

    // 获取用户权限
    async getUserPermissions(token = null) {
      try {
        const config = token ? {
          headers: { Authorization: `Bearer ${token}` },
          retry: false  // 禁用重试
        } : { retry: false }
        const response = await request.get('/auth/permissions', {}, config)
        if (response.success) {
          this.permissions = response.data || []
        }
      } catch (error) {
        // 忽略权限获取错误
      }
    },

    // 获取用户仓库权限
    async getUserWarehouses(token = null) {
      try {
        const config = token ? {
          headers: { Authorization: `Bearer ${token}` },
          retry: false  // 禁用重试
        } : { retry: false }
        const response = await request.get('/auth/warehouses', {}, config)
        if (response.success) {
          this.warehouses = response.data || []
        }
      } catch (error) {
        // 忽略仓库权限获取错误
      }
    },

    // 修改密码
    async changePassword(passwordForm) {
      try {
        const response = await request.put('/users/current/change-password', passwordForm)
        if (response.success) {
          ElMessage.success('密码修改成功')
          return response
        }
      } catch (error) {
        throw error
      }
    },

    // 更新用户信息
    async updateProfile(profileForm) {
      try {
        const response = await request.put('/users/profile', profileForm)
        if (response.success) {
          this.userInfo = { ...this.userInfo, ...response.data }
          localStorage.setItem(USER_INFO_KEY, JSON.stringify(this.userInfo))
          ElMessage.success('个人信息更新成功')
          return response
        }
      } catch (error) {
        throw error
      }
    }
  }
})
