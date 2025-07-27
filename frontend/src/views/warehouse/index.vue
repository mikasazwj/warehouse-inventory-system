<template>
  <div class="warehouse-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><House /></el-icon>
            仓库管理
          </h1>
          <p class="page-subtitle">管理系统中的所有仓库信息</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="handleAdd" size="default">
            <el-icon><Plus /></el-icon>
            新增仓库
          </el-button>
          <el-button @click="handleExport" size="default">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索和统计区域 -->
    <div class="search-section">
      <div class="search-card">
        <div class="search-header">
          <h3>筛选条件</h3>
          <div class="search-stats">
            <span class="stat-item">
              <el-icon><House /></el-icon>
              总计：{{ pagination.total }} 个仓库
            </span>
            <span class="stat-item">
              <el-icon><CircleCheck /></el-icon>
              启用：{{ enabledCount }} 个
            </span>
            <span class="stat-item">
              <el-icon><CircleClose /></el-icon>
              禁用：{{ disabledCount }} 个
            </span>
          </div>
        </div>
        <el-form :model="searchForm" class="search-form" :class="{ 'mobile-form': isMobile }">
          <div class="form-content">
            <div class="search-inputs">
              <el-form-item label="名称" class="search-item">
                <el-input
                  v-model="searchForm.keyword"
                  placeholder="请输入仓库名称或编码"
                  clearable
                  prefix-icon="Search"
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable class="search-select">
                  <el-option label="启用" :value="true">
                    <el-icon><CircleCheck /></el-icon>
                    启用
                  </el-option>
                  <el-option label="禁用" :value="false">
                    <el-icon><CircleClose /></el-icon>
                    禁用
                  </el-option>
                </el-select>
              </el-form-item>
            </div>
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                <span v-if="!isMobile">搜索</span>
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                <span v-if="!isMobile">重置</span>
              </el-button>
            </div>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <div class="table-card">
        <div class="table-header">
          <h3>仓库列表</h3>
          <div class="table-actions">
            <el-button size="small" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          class="warehouse-table"
          empty-text="暂无仓库数据"
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
        >
          <el-table-column prop="code" label="仓库编码" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="code-cell">
                <el-icon class="code-icon"><Postcard /></el-icon>
                <span class="code-text">{{ row.code }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="仓库名称" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="name-cell">
                <el-icon class="name-icon"><House /></el-icon>
                <span class="name-text">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="仓库地址" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="address-cell">
                <el-icon class="address-icon"><Location /></el-icon>
                <span class="address-text">{{ row.address || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="contactPerson" label="负责人" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="contact-cell">
                <el-icon class="contact-icon"><User /></el-icon>
                <span class="contact-text">{{ row.contactPerson || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="contactPhone" label="联系电话" width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="phone-cell">
                <el-icon class="phone-icon"><Phone /></el-icon>
                <span class="phone-text">{{ row.contactPhone || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.enabled ? 'success' : 'danger'"
                effect="dark"
                class="status-tag"
              >
                <div class="status-content">
                  <el-icon><component :is="row.enabled ? 'CircleCheck' : 'CircleClose'" /></el-icon>
                  <span>{{ row.enabled ? '启用' : '禁用' }}</span>
                </div>
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon class="time-icon"><Clock /></el-icon>
                <span class="time-text">{{ formatDateTime(row.createdTime) }}</span>
              </div>
            </template>
          </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 80 : 320" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 桌面端按钮组 -->
            <div class="action-buttons desktop-actions">
              <el-button type="primary" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button type="warning" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                :type="row.enabled ? 'danger' : 'success'"
                size="small"
                @click="handleToggleStatus(row)"
              >
                <el-icon><Switch /></el-icon>
                {{ row.enabled ? '禁用' : '启用' }}
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
                :disabled="!row.enabled"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>

            <!-- 移动端下拉菜单 -->
            <div class="mobile-actions">
              <el-dropdown trigger="click" @command="(command) => handleMobileAction(command, row)">
                <el-button type="primary" size="small">
                  操作
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="view">
                      <el-icon><View /></el-icon>
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>
                      编辑信息
                    </el-dropdown-item>
                    <el-dropdown-item command="toggle">
                      <el-icon><Switch /></el-icon>
                      {{ row.enabled ? '禁用仓库' : '启用仓库' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" :disabled="!row.enabled" divided>
                      <el-icon><Delete /></el-icon>
                      删除仓库
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="isMobile ? [5, 10, 20] : [10, 20, 50, 100]"
            :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
            :small="isMobile"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="warehouse-pagination"
          />
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :width="isMobile ? '95%' : '700px'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="warehouse-dialog modern-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <el-icon><component :is="form.id ? 'Edit' : 'Plus'" /></el-icon>
            </div>
            <div class="header-text">
              <h3>{{ dialogTitle }}</h3>
              <p>{{ form.id ? '修改仓库信息' : '创建新的仓库' }}</p>
            </div>
          </div>
          <el-button type="text" @click="dialogVisible = false" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="dialog-body">
        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          :label-width="isMobile ? '80px' : '120px'"
          :label-position="isMobile ? 'top' : 'right'"
          class="warehouse-form modern-form"
        >
          <!-- 基本信息 -->
          <div class="form-section">
            <div class="section-title">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="仓库编码" prop="code">
                  <el-input
                    v-model="form.code"
                    placeholder="请输入仓库编码"
                    :disabled="!!form.id"
                    prefix-icon="Postcard"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="仓库名称" prop="name">
                  <el-input
                    v-model="form.name"
                    placeholder="请输入仓库名称"
                    prefix-icon="House"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="仓库地址" prop="address">
              <el-input
                v-model="form.address"
                placeholder="请输入仓库地址"
                prefix-icon="Location"
              />
            </el-form-item>
          </div>

          <!-- 联系信息 -->
          <div class="form-section">
            <div class="section-title">
              <el-icon><User /></el-icon>
              <span>联系信息</span>
            </div>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="负责人" prop="contactPerson">
                  <el-input
                    v-model="form.contactPerson"
                    placeholder="请输入负责人"
                    prefix-icon="Avatar"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input
                    v-model="form.contactPhone"
                    placeholder="请输入联系电话"
                    prefix-icon="Phone"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 其他设置 -->
          <div class="form-section">
            <div class="section-title">
              <el-icon><Setting /></el-icon>
              <span>其他设置</span>
            </div>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="状态" prop="enabled">
                  <el-switch
                    v-model="form.enabled"
                    active-text="启用"
                    inactive-text="禁用"
                    active-color="#67c23a"
                    inactive-color="#f56c6c"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息"
                show-word-limit
                maxlength="200"
              />
            </el-form-item>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" size="large">
            <el-icon><component :is="submitLoading ? 'Loading' : 'Check'" /></el-icon>
            {{ form.id ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :width="isMobile ? '95%' : '700px'"
      :fullscreen="isMobile"
      class="warehouse-view-dialog modern-dialog"
      :show-close="false"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon view-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="header-text">
              <h3>仓库详情</h3>
              <p>查看仓库的详细信息</p>
            </div>
          </div>
          <el-button type="text" @click="viewDialogVisible = false" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="detail-content" v-if="viewData">
          <!-- 基本信息卡片 -->
          <div class="info-card">
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
            <div class="card-content">
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><Postcard /></el-icon>
                    仓库编码
                  </div>
                  <div class="info-value">{{ viewData.code }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><House /></el-icon>
                    仓库名称
                  </div>
                  <div class="info-value">{{ viewData.name }}</div>
                </div>
                <div class="info-item full-width">
                  <div class="info-label">
                    <el-icon><Location /></el-icon>
                    仓库地址
                  </div>
                  <div class="info-value">{{ viewData.address || '-' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 联系信息卡片 -->
          <div class="info-card">
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>联系信息</span>
            </div>
            <div class="card-content">
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><Avatar /></el-icon>
                    负责人
                  </div>
                  <div class="info-value">{{ viewData.contactPerson || '-' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><Phone /></el-icon>
                    联系电话
                  </div>
                  <div class="info-value">{{ viewData.contactPhone || '-' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 状态和其他信息 -->
          <div class="info-card">
            <div class="card-header">
              <el-icon><Setting /></el-icon>
              <span>状态信息</span>
            </div>
            <div class="card-content">
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><Switch /></el-icon>
                    状态
                  </div>
                  <div class="info-value">
                    <el-tag :type="viewData.enabled ? 'success' : 'danger'" effect="dark">
                      <el-icon><component :is="viewData.enabled ? 'CircleCheck' : 'CircleClose'" /></el-icon>
                      {{ viewData.enabled ? '启用' : '禁用' }}
                    </el-tag>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><Clock /></el-icon>
                    创建时间
                  </div>
                  <div class="info-value">{{ formatDateTime(viewData.createdTime) }}</div>
                </div>
                <div class="info-item full-width" v-if="viewData.remark">
                  <div class="info-label">
                    <el-icon><Document /></el-icon>
                    备注
                  </div>
                  <div class="info-value">{{ viewData.remark }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import { useDeviceDetection } from '@/utils/responsive'

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const formRef = ref()

// 统计数据
const enabledCount = computed(() => tableData.value.filter(item => item.enabled).length)
const disabledCount = computed(() => tableData.value.filter(item => !item.enabled).length)

// 搜索表单
const searchForm = reactive({
  keyword: '',
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
  code: '',
  name: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  enabled: true,
  remark: ''
})

// 表单验证规则
const formRules = {
  code: [
    { required: true, message: '请输入仓库编码', trigger: 'blur' },
    { min: 2, max: 20, message: '编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入仓库地址', trigger: 'blur' },
    { max: 200, message: '地址长度不能超过 200 个字符', trigger: 'blur' }
  ],
  contactPerson: [
    { max: 20, message: '负责人长度不能超过 20 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    {
      validator: (rule, value, callback) => {
        if (value && !/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑仓库' : '新增仓库'
})

// 方法
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      enabled: searchForm.enabled
    }
    
    const response = await request.get('/warehouses', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载仓库数据失败:', error)
    // 清空数据
    tableData.value = []
    pagination.total = 0
    if (error.code !== 'ERR_CONNECTION_REFUSED') {
      ElMessage.error('加载仓库数据失败')
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
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
  // 处理字段名映射
  Object.assign(form, {
    ...row,
    contactPerson: row.contactPerson || row.contact_person || '',
    contactPhone: row.contactPhone || row.contact_phone || ''
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
    await ElMessageBox.confirm(`确定要${action}该仓库吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.put(`/warehouses/${row.id}/toggle-status`)
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

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除仓库"${row.name}"吗？删除后不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const response = await request.delete(`/warehouses/${row.id}`)
    if (response.success) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitLoading.value = true
    const url = form.id ? `/warehouses/${form.id}` : '/warehouses'
    const method = form.id ? 'put' : 'post'

    // 准备提交数据，过滤掉不需要的字段
    const submitData = { ...form }
    if (form.id) {
      // 编辑时移除 id 和 code 字段
      delete submitData.id
      delete submitData.code
    }

    console.log('提交数据:', submitData)
    console.log('请求URL:', url)
    console.log('请求方法:', method)

    const response = await request[method](url, submitData)
    console.log('响应数据:', response)

    if (response.success) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    address: '',
    contactPerson: '',
    contactPhone: '',
    enabled: true,
    remark: ''
  })
  formRef.value?.resetFields()
}

const handleExport = async () => {
  try {
    ElMessage.info('正在导出数据...')

    // 如果没有数据，提示用户
    if (tableData.value.length === 0) {
      ElMessage.warning('暂无数据可导出')
      return
    }

    // 尝试后端导出
    try {
      const params = {
        keyword: searchForm.keyword || undefined,
        enabled: searchForm.enabled
      }

      const response = await request.download('/warehouses/export', params)

      // 创建下载链接
      const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `仓库数据_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      ElMessage.success('导出成功')
    } catch (apiError) {
      // 如果后端接口不存在，使用前端CSV导出
      console.error('后端导出接口错误:', apiError)
      console.log('错误详情:', {
        message: apiError.message,
        status: apiError.response?.status,
        statusText: apiError.response?.statusText,
        url: apiError.config?.url
      })
      console.log('后端导出接口不可用，使用前端导出')
      exportToCSV()
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 前端CSV导出功能
const exportToCSV = () => {
  try {
    const headers = ['仓库编码', '仓库名称', '仓库地址', '负责人', '联系电话', '状态', '创建时间', '备注']
    const csvContent = [
      headers.join(','),
      ...tableData.value.map(row => [
        row.code || '',
        row.name || '',
        row.address || '',
        row.contactPerson || '',
        row.contactPhone || '',
        row.enabled ? '启用' : '禁用',
        formatDateTime(row.createdTime) || '',
        row.remark || ''
      ].map(field => `"${field}"`).join(','))
    ].join('\n')

    // 添加BOM以支持中文
    const BOM = '\uFEFF'
    const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `仓库数据_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('CSV导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const handleRefresh = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

const handleMobileAction = (command, row) => {
  switch (command) {
    case 'view':
      handleView(row)
      break
    case 'edit':
      handleEdit(row)
      break
    case 'toggle':
      handleToggleStatus(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.warehouse-management {
  padding: 24px;
  background: #f8f9fa;
  min-height: calc(100vh - 60px);

  // 页面头部样式
  .page-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 32px;
    margin-bottom: 24px;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      width: 200px;
      height: 200px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      transform: translate(50%, -50%);
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      position: relative;
      z-index: 1;

      .header-left {
        .page-title {
          margin: 0 0 8px 0;
          font-size: 28px;
          font-weight: 700;
          color: white;
          display: flex;
          align-items: center;
          gap: 12px;

          .title-icon {
            font-size: 32px;
          }
        }

        .page-subtitle {
          margin: 0;
          font-size: 16px;
          color: rgba(255, 255, 255, 0.8);
        }
      }

      .header-actions {
        display: flex;
        gap: 12px;
        margin-left: auto;
        padding-left: 40px;

        .el-button {
          border: 2px solid rgba(255, 255, 255, 0.3);
          background: rgba(255, 255, 255, 0.1);
          color: white;
          backdrop-filter: blur(10px);
          transition: all 0.3s;
          white-space: nowrap;

          &:hover {
            background: rgba(255, 255, 255, 0.2);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
          }

          &.el-button--primary {
            background: rgba(255, 255, 255, 0.2);
            border-color: rgba(255, 255, 255, 0.5);
          }
        }
      }
    }
  }

  // 搜索区域样式
  .search-section {
    margin-bottom: 24px;

    .search-card {
      background: white;
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      border: 1px solid #e6e8eb;

      .search-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 1px solid #f0f0f0;

        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #303133;
        }

        .search-stats {
          display: flex;
          gap: 24px;

          .stat-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: #606266;

            .el-icon {
              font-size: 16px;
            }
          }
        }
      }

      .search-form {
        .form-content {
          display: flex;
          justify-content: space-between;
          align-items: flex-end;
          gap: 20px;
          flex-wrap: wrap;
        }

        .search-inputs {
          display: flex;
          gap: 20px;
          flex-wrap: wrap;
          flex: 1;

          .search-item {
            margin-bottom: 0;

            .search-input {
              width: 220px;
            }

            .search-select {
              width: 140px;
            }
          }
        }

        .search-actions {
          display: flex;
          gap: 12px;
          flex-shrink: 0;
        }

        // 移动端样式
        &.mobile-form {
          .form-content {
            flex-direction: column;
            gap: 16px;
          }

          .search-inputs {
            flex-direction: column;
            gap: 16px;
            width: 100%;

            .search-item {
              width: 100%;

              .search-input, .search-select {
                width: 100% !important;
              }
            }
          }

          .search-actions {
            width: 100%;
            justify-content: center;

            .el-button {
              flex: 1;
              max-width: 100px;
            }
          }
        }
      }
    }
  }

  // 表格区域样式
  .table-section {
    .table-card {
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      border: 1px solid #e6e8eb;
      overflow: hidden;

      .table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 24px;
        border-bottom: 1px solid #f0f0f0;
        background: #fafbfc;

        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #303133;
        }

        .table-actions {
          .el-button {
            border: 1px solid #dcdfe6;

            &:hover {
              color: #409eff;
              border-color: #409eff;
            }
          }
        }
      }

      .warehouse-table {
        // 表格自适应
        width: 100%;
        table-layout: auto;

        // 桌面端操作按钮
        .desktop-actions {
          display: flex;
          gap: 4px;
          justify-content: center;
          flex-wrap: nowrap;
          align-items: center;
          white-space: nowrap;
          min-width: 320px;

          .el-button {
            min-width: 50px;
            flex-shrink: 0;

            &.el-button--small {
              padding: 4px 6px;
              font-size: 11px;

              .el-icon {
                margin-right: 2px;
                font-size: 12px;
              }
            }
          }
        }

        // 移动端操作下拉菜单
        .mobile-actions {
          display: none;
        }

        // 状态标签样式
        .status-tag {
          .status-content {
            display: flex;
            align-items: center;
            gap: 4px;

            .el-icon {
              font-size: 14px;
            }

            span {
              line-height: 1;
            }
          }
        }

        // 表格单元格样式
        .code-cell, .name-cell, .address-cell, .contact-cell, .phone-cell, .time-cell {
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            font-size: 14px;
            color: #909399;
          }
        }

        .status-tag {
          .el-icon {
            margin-right: 4px;
          }
        }
      }
    }
  }

  // 分页样式
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 24px;
    padding: 20px 24px;
    background: #fafbfc;
    border-top: 1px solid #f0f0f0;
  }

  // 现代化弹窗样式
  .modern-dialog {
    .el-dialog__header {
      padding: 0;
      border-bottom: 1px solid #f0f0f0;
    }

    .el-dialog__body {
      padding: 0;
    }

    .el-dialog__footer {
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

          &.view-icon {
            background: rgba(255, 255, 255, 0.15);
          }

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
            .el-input__wrapper {
              border-radius: 8px;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
              transition: all 0.3s;

              &:hover {
                box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
              }

              &.is-focus {
                box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
              }
            }
          }

          .el-textarea {
            .el-textarea__inner {
              border-radius: 8px;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
              transition: all 0.3s;

              &:hover {
                box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
              }

              &:focus {
                box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
              }
            }
          }

          .el-switch {
            .el-switch__core {
              border-radius: 20px;
            }
          }
        }
      }

      // 详情卡片样式
      .info-card {
        background: white;
        border-radius: 12px;
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        border: 1px solid #e6e8eb;
        overflow: hidden;

        &:last-child {
          margin-bottom: 0;
        }

        .card-header {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 16px 24px;
          background: #f8f9fa;
          border-bottom: 1px solid #e6e8eb;
          font-size: 16px;
          font-weight: 600;
          color: #303133;

          .el-icon {
            font-size: 18px;
            color: #667eea;
          }
        }

        .card-content {
          padding: 24px;

          .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;

            .info-item {
              &.full-width {
                grid-column: 1 / -1;
              }

              .info-label {
                display: flex;
                align-items: center;
                gap: 8px;
                font-size: 14px;
                color: #909399;
                margin-bottom: 8px;

                .el-icon {
                  font-size: 16px;
                }
              }

              .info-value {
                font-size: 15px;
                color: #303133;
                font-weight: 500;
                word-break: break-all;

                .el-tag {
                  .el-icon {
                    margin-right: 4px;
                  }
                }
              }
            }
          }
        }
      }
    }

    // 弹窗底部
    .dialog-footer {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      padding: 20px 32px;
      background: white;

      .el-button {
        border-radius: 8px;
        padding: 12px 24px;
        font-weight: 500;

        .el-icon {
          margin-right: 6px;
        }

        &.el-button--primary {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border: none;

          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
          }
        }
      }
    }
  }

  // 分页样式
  .warehouse-pagination {
    .el-pagination__total {
      margin-right: auto;
    }
  }

  // 详情弹窗样式
  .detail-content {
    padding: 20px 0;

    .el-descriptions {
      .el-descriptions__label {
        font-weight: 600;
        color: #303133;
      }
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  .warehouse-management {
    padding: 16px;

    .page-header {
      padding: 20px;
      margin-bottom: 16px;

      .header-content {
        flex-direction: column;
        gap: 16px;
        text-align: center;

        .header-actions {
          width: 100%;
          justify-content: center;
        }
      }
    }

    .search-section {
      .search-card {
        padding: 16px;

        .search-header {
          flex-direction: column;
          gap: 12px;
          align-items: flex-start;

          .search-stats {
            flex-direction: column;
            gap: 8px;
            width: 100%;
          }
        }

        // 分页移动端适配
        .pagination-container {
          padding: 16px;

          .warehouse-pagination {
            .el-pagination__sizes,
            .el-pagination__total,
            .el-pagination__jump {
              display: none;
            }
          }
        }
      }
    }

    .table-section {
      .table-card {
        .table-header {
          flex-direction: column;
          gap: 12px;
          text-align: center;
        }

        .warehouse-table {
          // 隐藏桌面端操作按钮
          .desktop-actions {
            display: none;
          }

          // 显示移动端下拉菜单
          .mobile-actions {
            display: block;

            .el-dropdown {
              width: 100%;

              .el-button {
                width: 100%;
                justify-content: space-between;
              }
            }
          }
        }
      }
    }

    // 弹窗移动端适配
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

        .form-section, .info-card {
          padding: 16px;
          margin-bottom: 16px;

          .section-title, .card-header {
            font-size: 14px;
            padding: 12px 0;
          }

          .card-content {
            padding: 16px 0 0 0;

            .info-grid {
              grid-template-columns: 1fr;
              gap: 16px;

              .info-item {
                &.full-width {
                  grid-column: 1;
                }
              }
            }
          }
        }

        .el-form-item {
          margin-bottom: 16px;

          .el-form-item__label {
            font-size: 14px;
          }
        }
      }

      .dialog-footer {
        padding: 16px 20px;
        flex-direction: column;

        .el-button {
          width: 100%;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }
        }
      }
    }
  }
}

// 小屏手机适配
@media (max-width: 480px) {
  .warehouse-management {
    .pagination-container {
      padding: 12px;

      .warehouse-pagination {
        .el-pager li {
          min-width: 28px;
          height: 28px;
          line-height: 28px;
          font-size: 12px;
        }

        .btn-prev, .btn-next {
          min-width: 28px;
          height: 28px;
          line-height: 28px;
        }
      }
    }
  }
}

// 小屏手机适配
@media (max-width: 480px) {
  .warehouse-management {
    padding: 12px;

    .page-header {
      padding: 16px;

      .header-content {
        .header-left {
          .page-title {
            font-size: 22px;
          }

          .page-subtitle {
            font-size: 14px;
          }
        }

        .header-actions {
          .el-button {
            padding: 8px 12px;
            font-size: 12px;
          }
        }
      }
    }

    .search-section {
      .search-card {
        padding: 12px;
      }
    }

    .table-section {
      .table-card {
        .table-header {
          padding: 16px;
        }
      }
    }
  }
}
</style>
