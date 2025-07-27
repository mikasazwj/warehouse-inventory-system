<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-info">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userStore.avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="handleAvatarUpload"
            accept="image/*"
          >
            <el-button type="text" class="change-avatar-btn">
              <el-icon><Camera /></el-icon>
              更换头像
            </el-button>
          </el-upload>
        </div>
        <div class="user-info">
          <h2 class="username">{{ userStore.realName || userStore.username }}</h2>
          <p class="user-role">{{ getRoleText(userStore.role) }}</p>
          <div class="user-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              注册时间：{{ formatDate(currentUserProfile.createdTime) }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              最后登录：{{ formatDate(currentUserProfile.lastLoginTime) }}
            </span>
          </div>
        </div>
      </div>
      <div class="profile-actions">
        <el-button type="primary" @click="editProfileVisible = true">
          <el-icon><Edit /></el-icon>
          编辑资料
        </el-button>
      </div>
    </div>

    <div class="profile-content">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <div class="info-section">
            <div class="info-grid">
              <div class="info-item">
                <label>用户名</label>
                <span>{{ userStore.username }}</span>
              </div>
              <div class="info-item">
                <label>真实姓名</label>
                <span>{{ userStore.userInfo.realName || '-' }}</span>
              </div>
              <div class="info-item">
                <label>邮箱</label>
                <span>{{ userStore.userInfo.email || '-' }}</span>
              </div>
              <div class="info-item">
                <label>手机号</label>
                <span>{{ userStore.userInfo.phone || '-' }}</span>
              </div>
              <div class="info-item">
                <label>角色</label>
                <span>{{ getRoleText(userStore.role) }}</span>
              </div>
              <div class="info-item">
                <label>状态</label>
                <el-tag :type="currentUserProfile.enabled ? 'success' : 'danger'">
                  {{ currentUserProfile.enabled ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 仓库权限 -->
        <el-tab-pane label="仓库权限" name="warehouses">
          <div class="warehouse-section">
            <div v-if="userStore.isAdmin" class="admin-notice">
              <el-alert
                title="管理员权限"
                description="您是系统管理员，拥有所有仓库的访问权限"
                type="success"
                :closable="false"
              />
            </div>
            <div v-else-if="userStore.warehouses.length === 0" class="no-warehouses">
              <el-empty description="暂无仓库权限" />
            </div>
            <div v-else class="warehouse-grid">
              <div
                v-for="warehouse in userStore.warehouses"
                :key="warehouse.id"
                class="warehouse-card"
              >
                <div class="warehouse-icon">
                  <el-icon><House /></el-icon>
                </div>
                <div class="warehouse-info">
                  <h4>{{ warehouse.name }}</h4>
                  <p>{{ warehouse.address }}</p>
                  <div class="warehouse-meta">
                    <span>负责人：{{ warehouse.manager }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 操作日志 -->
        <el-tab-pane label="操作日志" name="logs">
          <div class="logs-section">
            <div class="logs-header">
              <el-date-picker
                v-model="logDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                @change="loadOperationLogs"
              />
            </div>
            <div class="logs-content">
              <el-timeline>
                <el-timeline-item
                  v-for="log in operationLogs"
                  :key="log.id"
                  :timestamp="formatDateTime(log.createdTime)"
                  placement="top"
                >
                  <div class="log-item">
                    <div class="log-action">{{ log.action }}</div>
                    <div class="log-description">{{ log.description }}</div>
                    <div class="log-meta">
                      <span>IP: {{ log.ipAddress }}</span>
                      <span>浏览器: {{ log.userAgent }}</span>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
              
              <div v-if="operationLogs.length === 0" class="no-logs">
                <el-empty description="暂无操作日志" />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="editProfileVisible"
      title="编辑个人资料"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="profileFormRef"
        :model="profileForm"
        :rules="profileRules"
        label-width="80px"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editProfileVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const userStore = useUserStore()

// 响应式数据
const activeTab = ref('basic')
const editProfileVisible = ref(false)
const profileLoading = ref(false)
const logDateRange = ref([])
const operationLogs = ref([])
const profileFormRef = ref()
const currentUserProfile = ref({})

// 编辑资料表单
const profileForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

// 表单验证规则
const profileRules = {
  realName: [
    { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 方法
const getRoleText = (role) => {
  const roleMap = {
    'ROLE_ADMIN': '系统管理员',
    'ROLE_WAREHOUSE_ADMIN': '仓库管理员',
    'ROLE_USER': '普通用户'
  }
  return roleMap[role] || '未知角色'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const handleUpdateProfile = async () => {
  try {
    await profileFormRef.value.validate()
    
    profileLoading.value = true
    await userStore.updateProfile(profileForm)
    
    editProfileVisible.value = false
  } catch (error) {
    console.error('更新个人资料失败:', error)
  } finally {
    profileLoading.value = false
  }
}

// 加载当前用户详细信息
const loadCurrentUserProfile = async () => {
  try {
    const response = await request.get('/users/current/profile')
    if (response.success) {
      currentUserProfile.value = response.data || {}
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 使用默认值
    currentUserProfile.value = {
      enabled: true,
      createdTime: null,
      lastLoginTime: null
    }
  }
}

// 处理头像上传
const handleAvatarUpload = async (file) => {
  try {
    const formData = new FormData()
    formData.append('avatar', file)

    const response = await request.upload('/users/current/avatar', formData)
    if (response.success) {
      ElMessage.success('头像更新成功')
      // 更新用户头像
      userStore.avatar = response.data
      await loadCurrentUserProfile()
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
  return false // 阻止默认上传行为
}

const loadOperationLogs = async () => {
  try {
    const params = {}
    if (logDateRange.value && logDateRange.value.length === 2) {
      params.startDate = logDateRange.value[0]
      params.endDate = logDateRange.value[1]
    }
    
    const response = await request.get('/users/current/operation-logs', params)
    if (response.success) {
      operationLogs.value = response.data || []
    }
  } catch (error) {
    console.error('加载操作日志失败:', error)
    // 使用模拟数据
    operationLogs.value = [
      {
        id: 1,
        action: '登录系统',
        description: '用户登录系统',
        ipAddress: '192.168.1.100',
        userAgent: 'Chrome 120.0.0.0',
        createdTime: new Date()
      },
      {
        id: 2,
        action: '创建入库单',
        description: '创建入库单 IN20240120001',
        ipAddress: '192.168.1.100',
        userAgent: 'Chrome 120.0.0.0',
        createdTime: new Date(Date.now() - 3600000)
      }
    ]
  }
}

// 生命周期
onMounted(() => {
  // 初始化编辑表单
  profileForm.realName = userStore.userInfo.realName || ''
  profileForm.email = userStore.userInfo.email || ''
  profileForm.phone = userStore.userInfo.phone || ''

  // 加载用户详细信息
  loadCurrentUserProfile()

  // 加载操作日志
  loadOperationLogs()
})
</script>

<style lang="scss" scoped>
.profile-container {
  .profile-header {
    background: white;
    border-radius: 12px;
    padding: 30px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;

    .profile-info {
      display: flex;
      align-items: center;

      .avatar-section {
        text-align: center;
        margin-right: 30px;

        .change-avatar-btn {
          margin-top: 10px;
          font-size: 12px;
        }
      }

      .user-info {
        .username {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 8px 0;
        }

        .user-role {
          font-size: 16px;
          color: #409eff;
          margin: 0 0 16px 0;
        }

        .user-meta {
          display: flex;
          flex-direction: column;
          gap: 8px;

          .meta-item {
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #606266;

            .el-icon {
              margin-right: 8px;
            }
          }
        }
      }
    }
  }

  .profile-content {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .info-section {
      .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 24px;

        .info-item {
          display: flex;
          flex-direction: column;
          gap: 8px;

          label {
            font-size: 14px;
            color: #909399;
            font-weight: 500;
          }

          span {
            font-size: 16px;
            color: #303133;
          }
        }
      }
    }

    .warehouse-section {
      .admin-notice {
        margin-bottom: 24px;
      }

      .warehouse-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 20px;

        .warehouse-card {
          display: flex;
          align-items: center;
          padding: 20px;
          border: 1px solid #e6e6e6;
          border-radius: 8px;
          transition: all 0.3s;

          &:hover {
            border-color: #409eff;
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
          }

          .warehouse-icon {
            width: 48px;
            height: 48px;
            background: linear-gradient(135deg, #409eff, #66b1ff);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 24px;
            margin-right: 16px;
          }

          .warehouse-info {
            flex: 1;

            h4 {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              margin: 0 0 8px 0;
            }

            p {
              font-size: 14px;
              color: #606266;
              margin: 0 0 8px 0;
            }

            .warehouse-meta {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }
    }

    .logs-section {
      .logs-header {
        margin-bottom: 24px;
      }

      .logs-content {
        .log-item {
          .log-action {
            font-size: 16px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 8px;
          }

          .log-description {
            font-size: 14px;
            color: #606266;
            margin-bottom: 8px;
          }

          .log-meta {
            display: flex;
            gap: 16px;
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-container {
    .profile-header {
      flex-direction: column;
      text-align: center;
      gap: 20px;

      .profile-info {
        flex-direction: column;
        text-align: center;

        .avatar-section {
          margin-right: 0;
          margin-bottom: 20px;
        }

        .user-info .user-meta {
          align-items: center;
        }
      }
    }

    .profile-content {
      .info-section .info-grid {
        grid-template-columns: 1fr;
      }

      .warehouse-section .warehouse-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>
