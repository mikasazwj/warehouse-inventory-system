<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入用户名或姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable style="width: 150px">
            <el-option label="系统管理员" value="ROLE_ADMIN" />
            <el-option label="仓库管理员" value="WAREHOUSE_ADMIN" />
            <el-option label="队长" value="TEAM_LEADER" />
            <el-option label="班长" value="SQUAD_LEADER" />
            <el-option label="普通用户" value="ROLE_USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="data-table">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />

        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button type="text" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="text" size="small" @click="handleAssignWarehouse(row)">
                <el-icon><House /></el-icon>
                分配仓库
              </el-button>
              <el-button type="text" size="small" @click="handleResetPassword(row)">
                <el-icon><Lock /></el-icon>
                重置密码
              </el-button>
              <el-button
                type="text"
                size="small"
                :class="row.enabled ? 'text-danger' : 'text-success'"
                @click="handleToggleStatus(row)"
              >
                <el-icon><Switch /></el-icon>
                {{ row.enabled ? '禁用' : '启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="!form.id">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
                <el-option
                  v-for="role in availableRoles"
                  :key="role.value"
                  :label="role.label"
                  :value="role.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="enabled">
              <el-switch
                v-model="form.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="用户详情"
      width="600px"
    >
      <div class="detail-content" v-if="viewData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ viewData.username }}</el-descriptions-item>
          <el-descriptions-item label="真实姓名">{{ viewData.realName }}</el-descriptions-item>

          <el-descriptions-item label="角色">
            <el-tag :type="getRoleType(viewData.role)">
              {{ getRoleText(viewData.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewData.enabled ? 'success' : 'danger'">
              {{ viewData.enabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后登录时间" :span="2">{{ formatDateTime(viewData.lastLoginTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(viewData.createdTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(viewData.updatedTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 分配仓库对话框 -->
    <el-dialog
      v-model="warehouseDialogVisible"
      title="分配仓库权限"
      width="500px"
    >
      <div class="warehouse-assignment" v-if="currentUser">
        <div class="user-info">
          <p><strong>用户：</strong>{{ currentUser.realName }} ({{ currentUser.username }})</p>
          <p><strong>角色：</strong>{{ getRoleText(currentUser.role) }}</p>
        </div>
        <div class="warehouse-list">
          <div v-if="availableWarehouses.length === 0" class="no-warehouses">
            暂无可分配的仓库
          </div>
          <el-checkbox-group v-model="selectedWarehouses" v-else>
            <div v-for="warehouse in availableWarehouses" :key="warehouse.id" class="warehouse-item">
              <el-checkbox :label="warehouse.id">
                {{ warehouse.name || '未知仓库' }} - {{ warehouse.address || '地址未知' }}
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <template #footer>
        <el-button @click="warehouseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitWarehouse" :loading="warehouseLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const warehouseLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const warehouseDialogVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const currentUser = ref(null)
const warehouses = ref([])
const selectedWarehouses = ref([])
const formRef = ref()

// 用户store
const userStore = useUserStore()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  enabled: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: null,
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',

  role: 'ROLE_USER',
  enabled: true
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 20, message: '姓名长度不能超过 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],

  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑用户' : '新增用户'
})

// 根据当前用户角色获取可选择的角色选项
const availableRoles = computed(() => {
  const allRoles = [
    { label: '系统管理员', value: 'ROLE_ADMIN' },
    { label: '仓库管理员', value: 'WAREHOUSE_ADMIN' },
    { label: '队长', value: 'TEAM_LEADER' },
    { label: '班长', value: 'SQUAD_LEADER' },
    { label: '普通用户', value: 'ROLE_USER' }
  ]

  // 如果是超级管理员，可以选择所有角色
  if (userStore.isAdmin) {
    return allRoles
  }

  // 如果是库房管理员，只能选择班长、队长、普通用户
  if (userStore.role === 'WAREHOUSE_ADMIN') {
    return allRoles.filter(role =>
      ['TEAM_LEADER', 'SQUAD_LEADER', 'ROLE_USER'].includes(role.value)
    )
  }

  // 其他角色不能创建用户
  return []
})

// 根据当前用户角色获取可分配的仓库
const availableWarehouses = computed(() => {
  // 如果是超级管理员，可以分配所有仓库
  if (userStore.isAdmin) {
    return warehouses.value
  }

  // 如果是库房管理员，只能分配自己拥有的仓库
  if (userStore.role === 'WAREHOUSE_ADMIN') {
    return userStore.warehouses
  }

  return []
})

// 方法
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const getRoleText = (role) => {
  const roleMap = {
    'ROLE_ADMIN': '系统管理员',
    'WAREHOUSE_ADMIN': '仓库管理员',
    'TEAM_LEADER': '队长',
    'SQUAD_LEADER': '班长',
    'ROLE_USER': '普通用户'
  }
  return roleMap[role] || '未知角色'
}

const getRoleType = (role) => {
  const typeMap = {
    'ROLE_ADMIN': 'danger',
    'WAREHOUSE_ADMIN': 'warning',
    'TEAM_LEADER': 'primary',
    'SQUAD_LEADER': 'success',
    'ROLE_USER': 'info'
  }
  return typeMap[role] || 'info'
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      role: searchForm.role,
      enabled: searchForm.enabled
    }
    
    const response = await request.get('/users', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    // 清空数据，不使用模拟数据
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const loadWarehouses = async () => {
  try {
    const response = await request.get('/warehouses/enabled')
    if (response.success && response.data && response.data.length > 0) {
      warehouses.value = response.data
      console.log('加载仓库数据成功:', warehouses.value)
    } else {
      console.log('后端返回空数据')
      warehouses.value = []
    }
  } catch (error) {
    console.error('加载仓库数据失败:', error)
    // 清空数据，不使用模拟数据
    warehouses.value = []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.enabled = null
  pagination.page = 1
  loadData()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, {
    ...row,
    password: '',
    confirmPassword: ''
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const endpoint = row.enabled ? `/users/${row.id}/disable` : `/users/${row.id}/enable`
    const response = await request.put(endpoint)
    if (response.success) {
      ElMessage.success(`${action}成功`)
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
    }
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重置该用户的密码吗？重置后密码为：123456', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await request.put(`/users/${row.id}/reset-password`, {
      newPassword: '123456'
    })
    if (response.success) {
      ElMessage.success('密码重置成功，新密码为：123456')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
    }
  }
}

const handleAssignWarehouse = async (row) => {
  currentUser.value = row

  try {
    // 先加载所有仓库列表
    await loadWarehouses()

    // 再加载用户当前的仓库权限
    const response = await request.get(`/users/${row.id}/warehouses`)
    if (response.success) {
      selectedWarehouses.value = response.data.map(w => w.id) || []
    }
  } catch (error) {
    console.error('加载用户仓库权限失败:', error)
    selectedWarehouses.value = []
  }

  warehouseDialogVisible.value = true
}

const handleSubmitWarehouse = async () => {
  try {
    warehouseLoading.value = true
    const response = await request.put(`/users/${currentUser.value.id}/assign-warehouses`, {
      warehouseIds: selectedWarehouses.value
    })
    if (response.success) {
      ElMessage.success('仓库权限分配成功')
      warehouseDialogVisible.value = false
    }
  } catch (error) {
    console.error('分配仓库权限失败:', error)
    ElMessage.error('分配仓库权限失败')
  } finally {
    warehouseLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    const url = form.id ? `/users/${form.id}` : '/users'
    const method = form.id ? 'put' : 'post'
    
    const response = await request[method](url, form)
    if (response.success) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    username: '',
    realName: '',
    password: '',
    confirmPassword: '',

    role: 'ROLE_USER',
    enabled: true
  })
  formRef.value?.resetFields()
}

// 生命周期
onMounted(() => {
  loadData()
  loadWarehouses()
})
</script>

<style lang="scss" scoped>
.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.detail-content {
  padding: 20px 0;
}

.warehouse-assignment {
  .user-info {
    background: #f8f9fa;
    padding: 16px;
    border-radius: 6px;
    margin-bottom: 20px;

    p {
      margin: 0 0 8px 0;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .warehouse-list {
    max-height: 300px;
    overflow-y: auto;

    .warehouse-item {
      margin-bottom: 12px;
      padding: 8px;
      border: 1px solid #ebeef5;
      border-radius: 4px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .no-warehouses {
      text-align: center;
      color: #909399;
      padding: 20px;
    }
  }
}
</style>
