<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">货物管理</h1>
      <div class="page-actions">
        <el-button @click="categoryDialogVisible = true">
          <el-icon><Collection /></el-icon>
          分类管理
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增货物
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="货物名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入货物名称或编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width: 150px">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
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
        <el-table-column prop="code" label="货物编码" min-width="120" />
        <el-table-column prop="name" label="货物名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" min-width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="specification" label="规格/型号" min-width="120" />

        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
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
      width="700px"
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
            <el-form-item label="货物编码" prop="code">
              <el-input v-model="form.code" placeholder="请输入货物编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="货物名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入货物名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格/型号" prop="specification">
              <el-input v-model="form.specification" placeholder="请输入规格/型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如：个、箱、kg" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="enabled">
              <el-switch
                v-model="form.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低库存" prop="minStock">
              <el-input-number v-model="form.minStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高库存" prop="maxStock">
              <el-input-number v-model="form.maxStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入货物描述"
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
      title="货物详情"
      width="600px"
    >
      <div class="detail-content" v-if="viewData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="货物编码">{{ viewData.code }}</el-descriptions-item>
          <el-descriptions-item label="货物名称">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ viewData.categoryName }}</el-descriptions-item>

          <el-descriptions-item label="单位">{{ viewData.unit }}</el-descriptions-item>
          <el-descriptions-item label="规格/型号">{{ viewData.specification }}</el-descriptions-item>
          <el-descriptions-item label="最低库存">{{ viewData.minStock }}</el-descriptions-item>
          <el-descriptions-item label="最高库存">{{ viewData.maxStock }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewData.enabled ? 'success' : 'danger'">
              {{ viewData.enabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(viewData.createdTime) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      title="分类管理"
      width="500px"
    >
      <div class="category-management">
        <div class="category-header">
          <el-button type="primary" size="small" @click="handleAddCategory">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
        <el-table :data="categories" size="small">
          <el-table-column prop="name" label="分类名称" />
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <div style="display: flex; gap: 8px; justify-content: center;">
                <el-button type="primary" size="small" @click="handleEditCategory(row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteCategory(row)">
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="categoryFormVisible"
      :title="categoryForm.id ? '编辑分类' : '新增分类'"
      width="400px"
    >
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="80px">
        <el-form-item label="分类编码" prop="code">
          <el-input v-model="categoryForm.code" placeholder="请输入分类编码" :disabled="!!categoryForm.id" />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const categoryFormVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const categories = ref([])

const formRef = ref()
const categoryFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: null,
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
  categoryId: null,
  unit: '',
  specification: '',
  minStock: 0,
  maxStock: 0,
  enabled: true,
  description: ''
})

// 分类表单
const categoryForm = reactive({
  id: null,
  code: '',
  name: '',
  description: ''
})

// 表单验证规则
const formRules = {
  code: [
    { required: true, message: '请输入货物编码', trigger: 'blur' },
    { min: 2, max: 20, message: '编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入货物名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}

const categoryRules = {
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑货物' : '新增货物'
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
      categoryId: searchForm.categoryId,
      enabled: searchForm.enabled
    }
    
    const response = await request.get('/goods', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载货物数据失败:', error)
    // 清空数据
    tableData.value = []
    pagination.total = 0
    if (error.code !== 'ERR_CONNECTION_REFUSED') {
      ElMessage.error('加载货物数据失败')
    }
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await request.get('/goods-categories')
    if (response.success) {
      categories.value = response.data || []
    }
  } catch (error) {
    console.error('加载分类数据失败:', error)
    categories.value = [
      { id: 1, name: '电子产品', description: '电子设备类产品' },
      { id: 2, name: '办公用品', description: '办公相关用品' }
    ]
  }
}



const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
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
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}该货物吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.put(`/goods/${row.id}/toggle-status`)
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
    const url = form.id ? `/goods/${form.id}` : '/goods'
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
    code: '',
    name: '',
    categoryId: null,

    unit: '',
    specification: '',
    minStock: 0,
    maxStock: 0,
    enabled: true,
    description: ''
  })
  formRef.value?.resetFields()
}

// 分类管理方法
const handleAddCategory = () => {
  categoryForm.id = null
  categoryForm.code = ''
  categoryForm.name = ''
  categoryForm.description = ''
  categoryFormVisible.value = true
}

const handleEditCategory = (row) => {
  Object.assign(categoryForm, row)
  categoryFormVisible.value = true
}

const handleDeleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/goods-categories/${row.id}`)
    if (response.success) {
      ElMessage.success('删除成功')
      loadCategories()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
    }
  }
}

const handleSubmitCategory = async () => {
  try {
    await categoryFormRef.value.validate()
    
    const url = categoryForm.id ? `/goods-categories/${categoryForm.id}` : '/goods-categories'
    const method = categoryForm.id ? 'put' : 'post'
    
    const response = await request[method](url, categoryForm)
    if (response.success) {
      ElMessage.success(categoryForm.id ? '更新成功' : '创建成功')
      categoryFormVisible.value = false
      loadCategories()
    }
  } catch (error) {
    console.error('提交分类失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadData()
  loadCategories()

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

.category-management {
  .category-header {
    margin-bottom: 16px;
  }
}

/* 确保按钮在同一行显示 */
.el-table .el-button {
  margin: 0 2px;
}

.el-table .el-button + .el-button {
  margin-left: 4px;
}
</style>
