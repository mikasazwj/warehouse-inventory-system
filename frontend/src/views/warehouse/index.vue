<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">仓库管理</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增仓库
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="仓库名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入仓库名称或编码"
            clearable
            style="width: 200px"
          />
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
        <el-table-column prop="code" label="仓库编码" width="120" />
        <el-table-column prop="name" label="仓库名称" min-width="150" />
        <el-table-column prop="address" label="仓库地址" min-width="200" />
        <el-table-column prop="contactPerson" label="负责人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div style="display: flex; gap: 4px; justify-content: center; flex-wrap: nowrap;">
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
            <el-form-item label="仓库编码" prop="code">
              <el-input v-model="form.code" placeholder="请输入仓库编码" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入仓库名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="仓库地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入仓库地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="enabled">
              <el-switch
                v-model="form.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 预留空间 -->
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
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
      title="仓库详情"
      width="600px"
    >
      <div class="detail-content" v-if="viewData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="仓库编码">{{ viewData.code }}</el-descriptions-item>
          <el-descriptions-item label="仓库名称">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="仓库地址" :span="2">{{ viewData.address }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ viewData.contactPerson || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ viewData.contactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewData.enabled ? 'success' : 'danger'">
              {{ viewData.enabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>

          <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(viewData.createdTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">{{ formatDateTime(viewData.updatedTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ viewData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const formRef = ref()

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

// 生命周期
onMounted(() => {
  loadData()
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

/* 确保按钮在同一行显示 */
.el-table .el-button {
  margin: 0 2px;
}

.el-table .el-button + .el-button {
  margin-left: 4px;
}
</style>
