<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">库存管理</h1>
      <div class="page-actions">
        <el-button @click="handleAdjust">
          <el-icon><EditPen /></el-icon>
          库存调整
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出库存
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="仓库" v-if="userStore.userInfo?.role === 'ROLE_ADMIN'">
          <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable style="width: 150px">
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" v-else>
          <el-input
            :value="getCurrentWarehouseName()"
            disabled
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="货物">
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
        <el-form-item label="库存状态">
          <el-select v-model="searchForm.stockStatus" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="正常" value="normal" />
            <el-option label="不足" value="low" />
            <el-option label="超量" value="high" />
            <el-option label="零库存" value="zero" />
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

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon primary">
          <el-icon><Box /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalGoods }}</div>
          <div class="stat-label">货物种类</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><List /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalQuantity }}</div>
          <div class="stat-label">库存总量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.lowStockCount }}</div>
          <div class="stat-label">库存不足</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">
          <el-icon><CircleClose /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.zeroStockCount }}</div>
          <div class="stat-label">零库存</div>
        </div>
      </div>
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
        <template #empty>
          <div class="empty-state">
            <el-icon size="48" color="#c0c4cc"><Box /></el-icon>
            <p>暂无库存数据</p>
            <p class="empty-tip">请检查网络连接或联系管理员</p>
          </div>
        </template>
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column prop="goodsCode" label="货物编码" width="120" />
        <el-table-column prop="goodsName" label="货物名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="specification" label="规格/型号" min-width="120" />
        <el-table-column prop="goodsUnit" label="单位" width="80" />
        <el-table-column prop="quantity" label="当前库存" width="100" align="right">
          <template #default="{ row }">
            <span :class="getQuantityClass(row)">{{ formatQuantity(row.quantity) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="可用库存" width="100" align="right">
          <template #default="{ row }">
            {{ formatQuantity(row.availableQuantity) }}
          </template>
        </el-table-column>
        <el-table-column label="锁定库存" width="100" align="right">
          <template #default="{ row }">
            {{ formatQuantity(row.lockedQuantity) }}
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStockStatusType(row)">
              {{ getStockStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="updatedTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleViewHistory(row)">
                <el-icon><Clock /></el-icon>
                历史
              </el-button>
              <el-button type="warning" size="small" @click="handleAdjustItem(row)">
                <el-icon><EditPen /></el-icon>
                调整
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

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="库存调整"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select
                v-model="adjustForm.warehouseId"
                placeholder="请选择仓库"
                :disabled="adjustForm.isWarehouseLocked"
                style="width: 100%"
              >
                <el-option
                  v-for="warehouse in warehouses"
                  :key="warehouse.id"
                  :label="warehouse.name"
                  :value="warehouse.id"
                />
              </el-select>
              <div v-if="adjustForm.isWarehouseLocked" class="field-lock-tip">
                <el-icon><Lock /></el-icon>
                <span>从库存记录调整，仓库已锁定</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="货物" prop="goodsId">
              <el-select
                v-model="adjustForm.goodsId"
                placeholder="请选择货物"
                filterable
                :disabled="adjustForm.isGoodsLocked"
                style="width: 100%"
              >
                <el-option
                  v-for="goods in goodsList"
                  :key="goods.id"
                  :label="`${goods.code} - ${goods.name}`"
                  :value="goods.id"
                />
              </el-select>
              <div v-if="adjustForm.isGoodsLocked" class="field-lock-tip">
                <el-icon><Lock /></el-icon>
                <span>从库存记录调整，货物已锁定</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调整类型" prop="adjustType">
              <el-radio-group v-model="adjustForm.adjustType">
                <el-radio label="increase">增加</el-radio>
                <el-radio label="decrease">减少</el-radio>
                <el-radio label="set">设置</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调整数量" prop="quantity">
              <el-input-number
                v-model="adjustForm.quantity"
                :min="0"
                :precision="0"
                :step="1"
                size="default"
                placeholder="请输入调整数量"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="adjustForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdjust" :loading="adjustLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 库存历史对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="库存变动历史"
      width="800px"
      class="history-dialog"
    >
      <el-table
        :data="historyData"
        v-loading="historyLoading"
        :max-height="400"
        style="width: 100%"
      >
        <el-table-column prop="operationType" label="操作类型" width="100" />
        <el-table-column prop="quantity" label="变动数量" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.quantity > 0 ? 'text-success' : 'text-danger'">
              {{ row.quantity > 0 ? '+' : '' }}{{ formatQuantity(row.quantity) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变动前" width="100" align="right">
          <template #default="{ row }">
            {{ formatQuantity(row.beforeQuantity) }}
          </template>
        </el-table-column>
        <el-table-column label="变动后" width="100" align="right">
          <template #default="{ row }">
            {{ formatQuantity(row.afterQuantity) }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="150" />
        <el-table-column prop="operatedBy" label="操作人" width="100" />
        <el-table-column prop="operatedTime" label="操作时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.operatedTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { useDeviceDetection, mobileOptimizations } from '@/utils/responsive'

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 响应式数据
const userStore = useUserStore()
const loading = ref(false)
const adjustLoading = ref(false)
const historyLoading = ref(false)
const adjustDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const tableData = ref([])
const historyData = ref([])
const warehouses = ref([])
const categories = ref([])
const goodsList = ref([])
const adjustFormRef = ref()

// 统计数据
const stats = reactive({
  totalGoods: 0,
  totalQuantity: 0,
  lowStockCount: 0,
  zeroStockCount: 0
})

// 搜索表单
const searchForm = reactive({
  warehouseId: null,
  keyword: '',
  categoryId: null,
  stockStatus: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 调整表单
const adjustForm = reactive({
  warehouseId: null,
  goodsId: null,
  adjustType: 'increase',
  quantity: null,
  reason: '',
  isWarehouseLocked: false,
  isGoodsLocked: false
})

// 表单验证规则
const adjustRules = {
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  goodsId: [
    { required: true, message: '请选择货物', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '调整数量不能为空', trigger: 'blur' },
    { type: 'number', min: 0, message: '调整数量不能小于0', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入调整原因', trigger: 'blur' }
  ]
}

// 方法
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

// 格式化数量为整数
const formatQuantity = (quantity) => {
  if (quantity === null || quantity === undefined) return 0
  return Math.round(Number(quantity))
}

const getQuantityClass = (row) => {
  if (row.quantity === 0) return 'text-danger'
  if (row.quantity <= row.minStock) return 'text-warning'
  return ''
}

const getStockStatusType = (row) => {
  if (!row.inventoryStatus) {
    // 如果没有库存状态信息，使用简单逻辑
    if (row.quantity === 0) return 'danger'
    return 'success'
  }

  const status = row.inventoryStatus
  if (status.isZeroStock || row.quantity === 0) return 'danger'
  if (status.isExpired) return 'danger'
  if (status.isNearExpiry) return 'warning'
  if (status.isHighStock) return 'warning'
  if (status.isLowStock) return 'warning'
  if (status.hasLocked) return 'info'
  return 'success'
}

const getStockStatusText = (row) => {
  if (!row.inventoryStatus) {
    // 如果没有库存状态信息，使用简单逻辑
    if (row.quantity === 0) return '零库存'
    return '正常'
  }

  const status = row.inventoryStatus
  if (status.statusText) {
    return status.statusText
  }

  // 备用逻辑 - 按优先级排序
  if (status.isZeroStock || row.quantity === 0) return '零库存'
  if (status.isExpired) return '已过期'
  if (status.isNearExpiry) return '即将过期'
  if (status.isHighStock) return '库存超量'
  if (status.isLowStock) return '库存不足'
  if (status.hasLocked) return '有锁定'
  return '正常'
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      warehouseId: searchForm.warehouseId,
      keyword: searchForm.keyword,
      categoryId: searchForm.categoryId,
      stockStatus: searchForm.stockStatus
    }
    
    const response = await request.get('/inventory', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载库存数据失败:', error)
    // 清空数据，不显示模拟数据
    tableData.value = []
    pagination.total = 0
    // 只在非连接错误时显示错误消息
    if (error.code !== 'ERR_CONNECTION_REFUSED') {
      ElMessage.error('加载库存数据失败')
    }
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await request.get('/inventory/statistics')
    if (response.success) {
      // 映射后端字段到前端字段
      const data = response.data
      Object.assign(stats, {
        totalGoods: data.goodsCount || 0,
        totalQuantity: data.totalQuantity || 0,
        lowStockCount: data.lowStockItems || 0,
        zeroStockCount: data.zeroStockItems || 0
      })
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 清空统计数据，不显示模拟数据
    Object.assign(stats, {
      totalGoods: 0,
      totalQuantity: 0,
      lowStockCount: 0,
      zeroStockCount: 0
    })
    // 只在非连接错误时显示错误消息
    if (error.code !== 'ERR_CONNECTION_REFUSED') {
      ElMessage.error('加载统计数据失败')
    }
  }
}

const loadWarehouses = async () => {
  try {
    const response = await request.get('/warehouses/enabled')
    if (response.success) {
      warehouses.value = response.data || []
    }
  } catch (error) {
    console.error('加载仓库数据失败:', error)
    warehouses.value = []
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
    categories.value = []
  }
}

const loadGoodsList = async () => {
  try {
    // 获取有库存记录的货物列表
    const response = await request.get('/inventory/goods-with-stock')
    if (response.success) {
      goodsList.value = response.data || []
      console.log('货物列表数据:', goodsList.value)
    }
  } catch (error) {
    console.error('加载货物数据失败:', error)
    goodsList.value = []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.warehouseId = null
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.stockStatus = null
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

const handleAdjust = () => {
  resetAdjustForm()
  // 右上角进入：货物和仓库都可以选择
  adjustForm.isWarehouseLocked = false
  adjustForm.isGoodsLocked = false

  // 如果用户已经选择了仓库，自动填入但不锁定
  if (searchForm.warehouseId) {
    adjustForm.warehouseId = searchForm.warehouseId
  }

  adjustDialogVisible.value = true
}

const handleAdjustItem = (row) => {
  resetAdjustForm()
  // 表格内进入：锁定仓库和货物选择
  adjustForm.warehouseId = row.warehouseId
  adjustForm.goodsId = row.goodsId
  adjustForm.isWarehouseLocked = true
  adjustForm.isGoodsLocked = true

  adjustDialogVisible.value = true
}

const handleSubmitAdjust = async () => {
  try {
    await adjustFormRef.value.validate()

    // 特殊验证：对于增加和减少类型，数量不能为0
    if ((adjustForm.adjustType === 'increase' || adjustForm.adjustType === 'decrease') && adjustForm.quantity === 0) {
      ElMessage.warning('增加或减少类型的调整数量不能为0')
      return
    }

    adjustLoading.value = true

    // 根据调整类型计算实际调整数量或设置数量
    let requestData = {
      warehouseId: adjustForm.warehouseId,
      goodsId: adjustForm.goodsId,
      reason: adjustForm.reason
    }

    if (adjustForm.adjustType === 'set') {
      // 设置类型：直接设置为指定数量（允许为0）
      requestData.setQuantity = adjustForm.quantity
      requestData.adjustQuantity = null
    } else {
      // 增加/减少类型：计算调整数量
      let adjustQuantity = adjustForm.quantity
      if (adjustForm.adjustType === 'decrease') {
        adjustQuantity = -Math.abs(adjustForm.quantity)
      } else if (adjustForm.adjustType === 'increase') {
        adjustQuantity = Math.abs(adjustForm.quantity)
      }
      requestData.adjustQuantity = adjustQuantity
      requestData.setQuantity = null
    }

    const response = await request.post('/inventory/adjust', requestData)
    if (response.success) {
      ElMessage.success('库存调整成功')
      adjustDialogVisible.value = false
      loadData()
      loadStats()
    }
  } catch (error) {
    console.error('库存调整失败:', error)
    ElMessage.error('库存调整失败: ' + (error.response?.data?.message || error.message))
  } finally {
    adjustLoading.value = false
  }
}

const handleViewHistory = async (row) => {
  try {
    historyLoading.value = true
    historyDialogVisible.value = true
    
    const response = await request.get(`/inventory/${row.id}/history`)
    if (response.success) {
      historyData.value = response.data || []
    }
  } catch (error) {
    console.error('加载库存历史失败:', error)
    // 清空数据，不使用模拟数据
    historyData.value = []
  } finally {
    historyLoading.value = false
  }
}

const handleExport = async () => {
  try {
    ElMessage.info('正在导出库存数据...')

    // 构建请求数据
    const exportData = {
      keyword: searchForm.keyword || null,
      warehouseId: searchForm.warehouseId || null,
      categoryId: searchForm.categoryId || null,
      stockStatus: searchForm.stockStatus || null
    }

    // 使用POST请求避免CORS问题
    const response = await request.post('/inventory/export', exportData, {
      responseType: 'blob'
    })

    // 处理文件下载
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `库存报表_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + (error.response?.data?.message || error.message))
  }
}

const resetAdjustForm = () => {
  Object.assign(adjustForm, {
    warehouseId: null,
    goodsId: null,
    adjustType: 'increase',
    quantity: null,
    reason: '',
    isWarehouseLocked: false,
    isGoodsLocked: false
  })
  adjustFormRef.value?.resetFields()
}

const getCurrentWarehouseName = () => {
  if (userStore.warehouses && userStore.warehouses.length > 0) {
    return userStore.warehouses[0].name
  }
  return '主仓库'
}

// 生命周期
onMounted(() => {
  loadData()
  loadStats()
  loadWarehouses()
  loadCategories()
  loadGoodsList()
})
</script>

<style lang="scss" scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.field-lock-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.field-lock-tip .el-icon {
  font-size: 12px;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #909399;
}

.empty-state p {
  margin: 12px 0 0 0;
  font-size: 14px;
}

.empty-tip {
  font-size: 12px !important;
  color: #c0c4cc !important;
}

.history-dialog .el-dialog__body {
  padding: 10px 20px 20px;
  max-height: 500px;
  overflow-y: auto;
}

.history-dialog .el-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

// 操作按钮样式
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;

  .el-button {
    margin: 0;
    padding: 6px 12px;
    font-size: 12px;
    border-radius: 4px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .inventory-container {
    padding: 15px;

    .search-section {
      .search-form {
        .el-form-item {
          margin-bottom: 15px;

          .el-form-item__label {
            width: auto !important;
            margin-bottom: 5px;
          }

          .el-form-item__content {
            margin-left: 0 !important;
          }
        }
      }

      .search-actions {
        flex-direction: column;
        gap: 10px;

        .el-button {
          width: 100%;
        }
      }
    }

    .table-section {
      .table-header {
        flex-direction: column;
        gap: 15px;

        .table-title {
          text-align: center;
        }

        .table-actions {
          justify-content: center;
        }
      }

      .el-table {
        font-size: 12px;

        .el-table__cell {
          padding: 8px 4px;
        }
      }

      .pagination-wrapper {
        .el-pagination {
          justify-content: center;

          .el-pagination__sizes,
          .el-pagination__jump {
            display: none;
          }
        }
      }
    }
  }

  // 对话框移动端优化
  .el-dialog {
    width: 95% !important;
    margin: 5vh auto !important;

    .el-dialog__body {
      padding: 15px;

      .el-form {
        .el-form-item {
          margin-bottom: 15px;

          .el-form-item__label {
            width: auto !important;
            margin-bottom: 5px;
            text-align: left !important;
          }

          .el-form-item__content {
            margin-left: 0 !important;
          }
        }
      }

      .el-table {
        font-size: 12px;

        .el-table__cell {
          padding: 6px 4px;
        }
      }
    }
  }
}

/* 小屏手机优化 */
@media (max-width: 480px) {
  .inventory-container {
    padding: 10px;

    .search-section {
      .search-form {
        .el-row {
          .el-col {
            width: 100% !important;
            max-width: 100% !important;
            flex: 0 0 100% !important;
          }
        }
      }
    }

    .table-section {
      .el-table {
        .el-table__cell {
          padding: 6px 2px;
          font-size: 11px;
        }
      }
    }
  }

  .el-dialog {
    width: 98% !important;
    margin: 2vh auto !important;

    .el-dialog__header {
      padding: 15px;

      .el-dialog__title {
        font-size: 16px;
      }
    }

    .el-dialog__body {
      padding: 10px;
    }
  }
}
</style>
