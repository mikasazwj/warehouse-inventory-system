<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">入库管理</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建入库单
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="单号">
          <el-input
            v-model="searchForm.orderNumber"
            placeholder="请输入入库单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
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
        <el-form-item label="类型">
          <el-select v-model="searchForm.businessType" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="采购入库" value="PURCHASE_IN" />
            <el-option label="归还入库" value="RETURN_IN" />
            <el-option label="调拨入库" value="TRANSFER_IN" />
            <el-option label="盘盈入库" value="INVENTORY_GAIN" />
            <el-option label="其他入库" value="OTHER_IN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="已执行" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
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
        <el-table-column prop="orderNumber" label="入库单号" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column label="入库类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeColor(row.businessType)">
              {{ getBusinessTypeText(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="总数量" width="100" align="right">
          <template #default="{ row }">
            {{ calculateRowTotalQuantity(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalAmount?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedDate" label="入库时间" min-width="110" />

        <el-table-column prop="createdBy" label="制单人" min-width="100" />
        <el-table-column prop="createdTime" label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                type="warning"
                size="small"
                @click="handleEdit(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="row.status === 'PENDING' && (userStore.isAdmin || userStore.isWarehouseAdmin)"
                type="success"
                size="small"
                @click="handleApprove(row)"
              >
                <el-icon><Check /></el-icon>
                审批
              </el-button>
              <el-button
                v-if="row.status === 'APPROVED' && userStore.isWarehouseAdmin"
                type="success"
                size="small"
                @click="handleExecute(row)"
              >
                <el-icon><Position /></el-icon>
                执行
              </el-button>
              <el-button
                v-if="row.status === 'EXECUTED'"
                type="success"
                size="small"
                @click="handleConsistency(row)"
              >
                <el-icon><Check /></el-icon>
                已执行
              </el-button>
              <el-button
                v-if="['PENDING', 'APPROVED'].includes(row.status) && (userStore.isAdmin || userStore.isWarehouseAdmin || (userStore.role === 'ROLE_USER' && row.status === 'PENDING'))"
                type="danger"
                size="small"
                @click="handleCancel(row)"
              >
                <el-icon><Close /></el-icon>
                取消
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
      width="1200px"
      :close-on-click-modal="false"
      top="5vh"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入库单号" prop="orderNumber">
              <el-input v-model="form.orderNumber" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                style="width: 100%"
                :disabled="isWarehouseLocked"
              >
                <el-option
                  v-for="warehouse in availableWarehouses"
                  :key="warehouse.id"
                  :label="warehouse.name"
                  :value="warehouse.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务类型" prop="businessType">
              <el-select v-model="form.businessType" placeholder="请选择业务类型" style="width: 100%">
                <el-option label="采购入库" value="PURCHASE_IN" />
                <el-option label="调拨入库" value="TRANSFER_IN" />
                <el-option label="盘盈入库" value="INVENTORY_GAIN" />
                <el-option label="其他入库" value="OTHER_IN" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入库时间" prop="plannedDate">
              <el-date-picker
                v-model="form.plannedDate"
                type="date"
                placeholder="请选择入库时间"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="制单人" prop="createdBy">
              <el-input v-model="form.createdBy" placeholder="请输入制单人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="参考单号" prop="referenceNumber">
              <el-input v-model="form.referenceNumber" placeholder="请输入参考单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>

        <!-- 入库明细 -->
        <div class="detail-section">
          <div class="detail-header">
            <h4>入库明细</h4>
            <el-button type="primary" size="small" @click="handleAddDetail">
              <el-icon><Plus /></el-icon>
              添加明细
            </el-button>
          </div>
          <el-table :data="form.details" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="货物" min-width="200">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.goodsName"
                  placeholder="点击选择货物"
                  readonly
                  style="width: 100%"
                  @click="openGoodsSelector($index)"
                >
                  <template #suffix>
                    <el-icon class="cursor-pointer">
                      <Search />
                    </el-icon>
                  </template>
                </el-input>
              </template>
            </el-table-column>
            <el-table-column label="规格/型号" width="120">
              <template #default="{ row }">
                <span>{{ getDetailSpecificationModel(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80">
              <template #default="{ row }">
                <span>{{ row.goodsUnit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="160">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="0"
                  :precision="0"
                  size="default"
                  style="width: 100%"
                  @change="calculateDetailAmount(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="150">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.unitPrice"
                  :min="0"
                  :precision="2"
                  style="width: 100%"
                  @change="calculateDetailAmount(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">
                <span>{{ (row.quantity * row.unitPrice || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button
                  type="text"
                  size="small"
                  class="text-danger"
                  @click="handleRemoveDetail($index)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 合计信息 -->
          <div class="total-info">
            <el-row :gutter="20">
              <el-col :span="12">
                <span>总数量：{{ totalQuantity }}</span>
              </el-col>
              <el-col :span="12" style="text-align: right">
                <span>总金额：¥{{ totalAmount.toFixed(2) }}</span>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="入库单详情"
      width="1000px"
    >
      <div class="detail-view" v-if="viewData">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="入库单号">{{ viewData.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ viewData.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="入库类型">
            <el-tag :type="getBusinessTypeColor(viewData.businessType)">
              {{ getBusinessTypeText(viewData.businessType) }}
            </el-tag>
          </el-descriptions-item>

          <el-descriptions-item label="入库时间">{{ viewData.plannedDate }}</el-descriptions-item>

          <el-descriptions-item label="制单人">{{ viewData.createdBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参考单号">{{ viewData.referenceNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(viewData.status)">
              {{ getStatusText(viewData.status) }}
            </el-tag>
          </el-descriptions-item>

          <el-descriptions-item label="备注" :span="3">{{ viewData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 明细信息 -->
        <div class="detail-table" style="margin-top: 20px;">
          <h4>入库明细</h4>
          <el-table :data="viewData.details" border>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="goodsCode" label="货物编码" width="120" />
            <el-table-column prop="goodsName" label="货物名称" min-width="150" />
            <el-table-column label="规格/型号" min-width="120">
              <template #default="{ row }">
                {{ getDetailSpecificationModel(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="goodsUnit" label="单位" width="80" />
            <el-table-column prop="quantity" label="数量" width="100" align="right" />
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120" align="right">
              <template #default="{ row }">
                ¥{{ (row.quantity * row.unitPrice || 0).toFixed(2) }}
              </template>
            </el-table-column>

          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      title="审批入库单"
      width="500px"
    >
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approvalForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="approvalForm.approvalRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApproval" :loading="approvalLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 货物选择弹出框 -->
    <el-dialog
      v-model="goodsSelectorVisible"
      title="选择货物"
      width="80%"
      :close-on-click-modal="false"
    >
      <!-- 筛选条件 -->
      <div class="goods-filter">
        <el-form :model="goodsFilter" inline>
          <el-form-item label="货物编码">
            <el-input v-model="goodsFilter.code" placeholder="请输入货物编码" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="货物名称">
            <el-input v-model="goodsFilter.name" placeholder="请输入货物名称" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="规格/型号">
            <el-input v-model="goodsFilter.specification" placeholder="请输入规格/型号" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="filterGoods">筛选</el-button>
            <el-button @click="resetGoodsFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 货物列表 -->
      <el-table
        ref="goodsTableRef"
        :data="filteredGoodsList"
        border
        style="width: 100%"
        max-height="400"
        @selection-change="handleGoodsSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="code" label="货物编码" width="120" />
        <el-table-column prop="name" label="货物名称" min-width="150" />
        <el-table-column label="规格/型号" min-width="150">
          <template #default="{ row }">
            {{ getSpecificationModel(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <span class="selected-info">已选择 {{ selectedGoods.length }} 个货物</span>
          <div>
            <el-button @click="goodsSelectorVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmGoodsSelection">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { useDeviceDetection, mobileOptimizations } from '@/utils/responsive'

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 响应式数据
const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const approvalLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const currentRow = ref(null)
const warehouses = ref([])
const goodsList = ref([])
const filteredGoodsList = ref([])
const goodsLoading = ref(false)
const formRef = ref()

// 货物选择弹出框相关
const goodsSelectorVisible = ref(false)
const goodsTableRef = ref()
const selectedGoods = ref([])
const currentDetailIndex = ref(-1)
const goodsFilter = reactive({
  code: '',
  name: '',
  specification: ''
})

// 搜索表单
const searchForm = reactive({
  orderNumber: '',
  warehouseId: null,
  businessType: '',
  status: '',
  dateRange: []
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
  orderNumber: '',
  warehouseId: null,
  businessType: 'PURCHASE_IN',
  plannedDate: '',
  createdBy: '',
  referenceNumber: '',
  remark: '',
  details: []
})

// 审批表单
const approvalForm = reactive({
  status: 'APPROVED',
  approvalRemark: ''
})

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  businessType: [
    { required: true, message: '请选择业务类型', trigger: 'change' }
  ],
  plannedDate: [
    { required: true, message: '请选择入库时间', trigger: 'change' }
  ],
  createdBy: [
    { required: true, message: '请输入制单人', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑入库单' : '新建入库单'
})

const totalQuantity = computed(() => {
  return form.details.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

const totalAmount = computed(() => {
  return form.details.reduce((sum, item) => sum + (item.quantity * item.unitPrice || 0), 0)
})

// 仓库锁定相关计算属性
const isWarehouseLocked = computed(() => {
  // 系统管理员不锁定仓库
  if (userStore.isAdmin) {
    return false
  }
  // 其他角色锁定仓库
  return true
})

const availableWarehouses = computed(() => {
  // 系统管理员可以看到所有仓库
  if (userStore.isAdmin) {
    return warehouses.value
  }
  // 其他角色只能看到分配给他们的仓库
  return userStore.warehouses || []
})

// 方法
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

// 格式化数量为整数
const formatQuantity = (quantity) => {
  if (quantity === null || quantity === undefined) return 0
  return Math.round(Number(quantity))
}

// 业务类型映射
const getBusinessTypeText = (businessType) => {
  const typeMap = {
    'PURCHASE_IN': '采购入库',
    'RETURN_IN': '归还入库',
    'TRANSFER_IN': '调拨入库',
    'INVENTORY_GAIN': '盘盈入库',
    'OTHER_IN': '其他入库'
  }
  return typeMap[businessType] || businessType || '-'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

// 获取规格/型号组合显示
const getSpecificationModel = (goods) => {
  const parts = []
  if (goods.specification) parts.push(goods.specification)
  if (goods.model) parts.push(goods.model)
  return parts.length > 0 ? parts.join(' / ') : '-'
}

const getBusinessTypeColor = (businessType) => {
  const colorMap = {
    'PURCHASE_IN': 'primary',
    'RETURN_IN': 'success',
    'TRANSFER_IN': 'info',
    'INVENTORY_GAIN': 'success',
    'OTHER_IN': 'warning'
  }
  return colorMap[businessType] || 'info'
}

// 计算行的总数量
const calculateRowTotalQuantity = (row) => {
  if (!row.details || !Array.isArray(row.details)) {
    return 0
  }
  return row.details.reduce((sum, item) => sum + (item.quantity || 0), 0)
}

// 获取明细的规格/型号显示
const getDetailSpecificationModel = (row) => {
  const parts = []
  if (row.goodsModel && row.goodsModel.trim()) {
    parts.push(row.goodsModel.trim())
  }
  if (row.goodsSpecification && row.goodsSpecification.trim()) {
    parts.push(row.goodsSpecification.trim())
  }
  return parts.length > 0 ? parts.join(' / ') : '-'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'EXECUTED': '已执行',
    'COMPLETED': '已执行',
    'CONSISTENCY': '已执行',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'primary',
    'EXECUTED': 'success',
    'COMPLETED': 'success',
    'CONSISTENCY': 'success',
    'CANCELLED': 'info',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.orderNumber,
      warehouseId: searchForm.warehouseId,
      businessType: searchForm.businessType,
      status: searchForm.status
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await request.get('/inbound-orders', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载入库单数据失败:', error)
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
    if (response.success) {
      warehouses.value = response.data || []
    }
  } catch (error) {
    console.error('加载仓库数据失败:', error)
    warehouses.value = [
      { id: 1, name: '主仓库' },
      { id: 2, name: '分仓库A' }
    ]
  }
}

const loadGoodsList = async () => {
  try {
    const response = await request.get('/goods/list')
    if (response.success) {
      goodsList.value = response.data || []
      filteredGoodsList.value = response.data || []
    }
  } catch (error) {
    console.error('加载货物数据失败:', error)
    goodsList.value = [
      { id: 1, code: 'G001', name: '苹果手机', unit: '台' }
    ]
    filteredGoodsList.value = goodsList.value
  }
}

// 搜索货物
const searchGoods = (query) => {
  if (query) {
    goodsLoading.value = true
    setTimeout(() => {
      filteredGoodsList.value = goodsList.value.filter(goods => {
        return goods.code.toLowerCase().includes(query.toLowerCase()) ||
               goods.name.toLowerCase().includes(query.toLowerCase()) ||
               (goods.specification && goods.specification.toLowerCase().includes(query.toLowerCase()))
      })
      goodsLoading.value = false
    }, 200)
  } else {
    filteredGoodsList.value = goodsList.value
  }
}

// 加载所有货物（当聚焦时）
const loadAllGoods = () => {
  filteredGoodsList.value = goodsList.value
}

// 打开货物选择弹出框
const openGoodsSelector = (index) => {
  currentDetailIndex.value = index
  goodsSelectorVisible.value = true
  filteredGoodsList.value = goodsList.value
  selectedGoods.value = []
  resetGoodsFilter()
}

// 筛选货物
const filterGoods = () => {
  filteredGoodsList.value = goodsList.value.filter(goods => {
    const specModel = getSpecificationModel(goods)
    return (!goodsFilter.code || goods.code.toLowerCase().includes(goodsFilter.code.toLowerCase())) &&
           (!goodsFilter.name || goods.name.toLowerCase().includes(goodsFilter.name.toLowerCase())) &&
           (!goodsFilter.specification || specModel.toLowerCase().includes(goodsFilter.specification.toLowerCase()))
  })
}

// 重置筛选条件
const resetGoodsFilter = () => {
  Object.assign(goodsFilter, {
    code: '',
    name: '',
    specification: ''
  })
  filteredGoodsList.value = goodsList.value
}

// 处理货物选择变化
const handleGoodsSelectionChange = (selection) => {
  selectedGoods.value = selection
}

// 确认货物选择
const confirmGoodsSelection = () => {
  if (selectedGoods.value.length === 0) {
    ElMessage.warning('请选择至少一个货物')
    return
  }

  // 如果是单选模式（修改现有行）
  if (currentDetailIndex.value >= 0) {
    const goods = selectedGoods.value[0] // 取第一个选中的货物
    const detail = form.details[currentDetailIndex.value]
    detail.goodsId = goods.id
    detail.goodsCode = goods.code
    detail.goodsName = goods.name
    detail.goodsModel = goods.model
    detail.goodsSpecification = goods.specification
    detail.goodsUnit = goods.unit
    // 不自动填充单价，保持为0，让用户手动输入
    detail.unitPrice = 0
  } else {
    // 批量添加模式
    selectedGoods.value.forEach(goods => {
      form.details.push({
        goodsId: goods.id,
        goodsCode: goods.code,
        goodsName: goods.name,
        goodsModel: goods.model,
        goodsSpecification: goods.specification,
        goodsUnit: goods.unit,
        quantity: 0,
        // 不自动填充单价，保持为0，让用户手动输入
        unitPrice: 0
      })
    })
  }

  goodsSelectorVisible.value = false
  ElMessage.success(`已添加 ${selectedGoods.value.length} 个货物`)
}



const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    orderNumber: '',
    warehouseId: null,
    businessType: '',
    status: '',
    dateRange: []
  })
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

const handleAdd = async () => {
  resetForm()
  try {
    const response = await request.get('/inbound-orders/generate-order-number')
    if (response.success) {
      form.orderNumber = response.data
    }
  } catch (error) {
    form.orderNumber = 'IN' + Date.now()
  }

  // 自动填充当前用户信息
  const currentUser = userStore.user
  if (currentUser) {
    form.createdBy = currentUser.realName || currentUser.username
  }

  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, {
    ...row,
    details: row.details || []
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleApprove = (row) => {
  currentRow.value = row
  approvalForm.status = 'APPROVED'
  approvalForm.approvalRemark = ''
  approvalDialogVisible.value = true
}

const handleExecute = async (row) => {
  try {
    await ElMessageBox.confirm('确定要执行该入库单吗？执行后将更新库存。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.put(`/inbound-orders/${row.id}/execute`)
    if (response.success) {
      ElMessage.success('入库单执行成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('执行入库单失败:', error)
    }
  }
}

const handleCancel = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消入库单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入取消原因'
    })
    
    const response = await request.put(`/inbound-orders/${row.id}/cancel`, { reason })
    if (response.success) {
      ElMessage.success('入库单取消成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消入库单失败:', error)
    }
  }
}

const handleConsistency = (row) => {
  ElMessage.warning('已执行入库，无法再次入库')
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (form.details.length === 0) {
      ElMessage.error('请添加入库明细')
      return
    }
    
    // 更新总数量和总金额
    form.totalQuantity = totalQuantity.value
    form.totalAmount = totalAmount.value
    
    submitLoading.value = true
    const url = form.id ? `/inbound-orders/${form.id}` : '/inbound-orders'
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

const handleSubmitApproval = async () => {
  try {
    approvalLoading.value = true
    const response = await request.put(`/inbound-orders/${currentRow.value.id}/approve`, approvalForm)
    if (response.success) {
      ElMessage.success('审批成功')
      approvalDialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('审批失败:', error)
  } finally {
    approvalLoading.value = false
  }
}

const handleAddDetail = () => {
  // 打开货物选择弹出框，批量添加模式
  currentDetailIndex.value = -1
  goodsSelectorVisible.value = true
  filteredGoodsList.value = goodsList.value
  selectedGoods.value = []
  resetGoodsFilter()
}

const handleRemoveDetail = (index) => {
  form.details.splice(index, 1)
}

const handleGoodsChange = (row, index) => {
  const goods = goodsList.value.find(g => g.id === row.goodsId)
  if (goods) {
    row.goodsCode = goods.code
    row.goodsName = goods.name
    row.unit = goods.unit
  }
}

const calculateDetailAmount = (row) => {
  row.amount = (row.quantity || 0) * (row.unitPrice || 0)
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    orderNumber: '',
    warehouseId: null,
    businessType: 'PURCHASE_IN',
    plannedDate: '',
    createdBy: userStore.user?.realName || userStore.user?.username || '',
    referenceNumber: '',
    remark: '',
    details: []
  })
  formRef.value?.resetFields()

  // 为非管理员用户重新设置默认仓库
  initializeDefaultWarehouse()
}

const getCurrentWarehouseName = () => {
  if (userStore.warehouses && userStore.warehouses.length > 0) {
    return userStore.warehouses[0].name
  }
  return '主仓库'
}

// 初始化默认仓库
const initializeDefaultWarehouse = () => {
  if (!userStore.isAdmin && userStore.warehouses && userStore.warehouses.length > 0) {
    // 设置搜索表单的默认仓库
    searchForm.warehouseId = userStore.warehouses[0].id
    // 设置新建表单的默认仓库
    form.warehouseId = userStore.warehouses[0].id
  }
}

// 生命周期
onMounted(() => {
  loadData()
  loadWarehouses()
  loadGoodsList()

  // 为非管理员用户设置默认仓库
  initializeDefaultWarehouse()
})
</script>

<style lang="scss" scoped>
.text-success {
  color: #67c23a;
}

.text-primary {
  color: #409eff;
}

.text-danger {
  color: #f56c6c;
}

// 货物选择样式
.goods-option {
  padding: 8px 0;

  .goods-main {
    display: flex;
    align-items: center;
    margin-bottom: 4px;

    .goods-code {
      background: #f0f2f5;
      color: #606266;
      padding: 2px 6px;
      border-radius: 3px;
      font-size: 12px;
      margin-right: 8px;
      font-family: monospace;
    }

    .goods-name {
      font-weight: 500;
      color: #303133;
    }
  }

  .goods-detail {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    font-size: 12px;
    color: #909399;

    span {
      background: #f8f9fa;
      padding: 1px 4px;
      border-radius: 2px;
    }

    .goods-price {
      color: #e6a23c;
      font-weight: 500;
    }
  }
}

.detail-section {
  margin-top: 20px;
  
  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h4 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .total-info {
    margin-top: 16px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 4px;
    font-weight: 600;
  }
}

.detail-view {
  .detail-table {
    h4 {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
}

// 货物选择弹出框样式
.goods-filter {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .selected-info {
    color: #606266;
    font-size: 14px;
  }
}

.cursor-pointer {
  cursor: pointer;
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
  .inbound-container {
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
  .inbound-container {
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
