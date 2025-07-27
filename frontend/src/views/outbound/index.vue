<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">出库管理</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建出库单
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="单号">
          <el-input
            v-model="searchForm.orderNumber"
            placeholder="请输入出库单号"
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
            <el-option label="领用出库" value="SALE_OUT" />
            <el-option label="调拨出库" value="TRANSFER_OUT" />
            <el-option label="借用出库" value="DAMAGE_OUT" />
            <el-option label="其他出库" value="OTHER_OUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已审批" value="APPROVED" />
            <el-option label="已执行" value="EXECUTED" />
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
        <el-table-column prop="orderNumber" label="出库单号" min-width="140" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column label="出库类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOutboundTypeColor(row.businessType)">
              {{ getOutboundTypeText(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recipientName" label="领取人" width="120" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" align="right" />
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
        <el-table-column prop="plannedDate" label="出库日期" min-width="110" />
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
                v-if="canApprove(row)"
                type="success"
                size="small"
                @click="handleApprove(row)"
              >
                <el-icon><Check /></el-icon>
                审批
              </el-button>
              <el-button
                v-if="row.status === 'APPROVED' && (userStore.isWarehouseAdmin || userStore.isTeamLeader || userStore.isSquadLeader)"
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
                @click="handleView(row)"
              >
                <el-icon><Check /></el-icon>
                已执行
              </el-button>
              <el-button
                v-if="canCancel(row)"
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
            <el-form-item label="出库单号" prop="orderNumber">
              <el-input v-model="form.orderNumber" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                style="width: 100%"
                @change="handleWarehouseChange"
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
            <el-form-item label="出库类型" prop="outboundType">
              <el-select v-model="form.outboundType" placeholder="请选择类型" style="width: 100%">
                <el-option label="领用出库" value="SALE_OUT" />
                <el-option label="调拨出库" value="TRANSFER_OUT" />
                <el-option label="报废出库" value="DAMAGE_OUT" />
                <el-option label="其他出库" value="OTHER_OUT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="领取人" prop="recipientName">
              <el-input
                v-model="form.recipientName"
                placeholder="请输入领取人姓名"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出库日期" prop="plannedDate">
              <el-date-picker
                v-model="form.plannedDate"
                type="date"
                placeholder="请选择出库日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="参考单号" prop="referenceNumber">
              <el-input v-model="form.referenceNumber" placeholder="请输入参考单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="制单人" prop="createdBy">
              <el-input v-model="form.createdBy" placeholder="请输入制单人姓名" />
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

        <!-- 出库明细 -->
        <div class="detail-section">
          <div class="detail-header">
            <h4>出库明细</h4>
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
            <el-table-column label="规格/型号" width="150">
              <template #default="{ row }">
                <span>{{ row.specification || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="库存" width="100">
              <template #default="{ row }">
                <span :class="getStockClass(row)">{{ formatQuantity(row.availableStock) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="140">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="0"
                  :max="row.availableStock || 0"
                  :precision="0"
                  size="default"
                  style="width: 100%"
                  @change="calculateDetailAmount(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <span>¥{{ (row.unitPrice || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">
                <span>¥{{ (row.quantity * row.unitPrice || 0).toFixed(2) }}</span>
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
      title="出库单详情"
      width="1000px"
    >
      <div class="detail-view" v-if="viewData">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="出库单号">{{ viewData.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ viewData.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="出库类型">
            <el-tag :type="getOutboundTypeColor(viewData.businessType)">
              {{ getOutboundTypeText(viewData.businessType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="领取人">{{ viewData.recipientName }}</el-descriptions-item>
          <el-descriptions-item label="计划日期">{{ viewData.plannedDate }}</el-descriptions-item>
          <el-descriptions-item label="实际日期">{{ viewData.actualDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参考单号">{{ viewData.referenceNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(viewData.status)">
              {{ getStatusText(viewData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ viewData.createdBy }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ viewData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 明细信息 -->
        <div class="detail-table" style="margin-top: 20px;">
          <h4>出库明细</h4>
          <el-table :data="viewData.details" border>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="goodsCode" label="货物编码" width="120" />
            <el-table-column prop="goodsName" label="货物名称" min-width="150" />
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
      title="审批出库单"
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

    <!-- 库存不足提示对话框 -->
    <el-dialog
      v-model="stockWarningVisible"
      title="库存不足提示"
      width="600px"
    >
      <div class="stock-warning">
        <el-alert
          title="以下货物库存不足，请检查后重新提交"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-table :data="stockWarnings" style="margin-top: 16px;">
          <el-table-column prop="goodsName" label="货物名称" />
          <el-table-column prop="requestQuantity" label="申请数量" align="right" />
          <el-table-column prop="availableStock" label="可用库存" align="right" />
          <el-table-column prop="shortage" label="缺少数量" align="right">
            <template #default="{ row }">
              <span class="text-danger">{{ row.shortage }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
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
        :data="filteredInventoryGoodsList"
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
        <el-table-column label="库存数量" width="100">
          <template #default="{ row }">
            <span :class="getStockClass(row)">{{ row.availableStock || 0 }}</span>
          </template>
        </el-table-column>
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
import { Plus, Delete, View, Edit, Check, Close, Search } from '@element-plus/icons-vue'
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
const stockWarningVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const currentRow = ref(null)
const warehouses = ref([])
const goodsList = ref([])
const inventoryGoodsList = ref([])
const filteredInventoryGoodsList = ref([])
const stockWarnings = ref([])
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
  businessType: null,
  status: null,
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
  outboundType: 'SALE_OUT',
  recipientName: '',
  plannedDate: '',
  referenceNumber: '',
  remark: '',
  createdBy: '',
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
  outboundType: [
    { required: true, message: '请选择出库类型', trigger: 'change' }
  ],
  plannedDate: [
    { required: true, message: '请选择出库日期', trigger: 'change' }
  ],
  recipientName: [
    { required: true, message: '请输入领取人', trigger: 'blur' },
    { min: 2, max: 50, message: '领取人长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  createdBy: [
    { required: true, message: '请输入制单人', trigger: 'blur' },
    { min: 2, max: 50, message: '制单人长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑出库单' : '新建出库单'
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

// 权限判断方法
const canApprove = (row) => {
  const status = row.status
  const userRole = userStore.role

  // 库房管理员和系统管理员可以直接审批待审批状态的出库单
  if ((userRole === 'WAREHOUSE_ADMIN' || userRole === 'ROLE_ADMIN') && status === 'PENDING') {
    return true
  }

  // 其他角色按照严格审批流程
  switch (status) {
    case 'PENDING':
      // 待审批状态：班长可以审批
      return userRole === 'SQUAD_LEADER'
    case 'SQUAD_APPROVED':
      // 班长已审批：队长可以审批
      return userRole === 'TEAM_LEADER'
    case 'TEAM_APPROVED':
      // 队长已审批：库房管理员可以审批
      return userRole === 'WAREHOUSE_ADMIN' || userRole === 'ROLE_ADMIN'
    default:
      return false
  }
}

const canCancel = (row) => {
  const status = row.status
  const userRole = userStore.role

  // 可以取消的状态
  const cancelableStatuses = ['PENDING', 'SQUAD_APPROVED', 'TEAM_APPROVED', 'APPROVED']
  if (!cancelableStatuses.includes(status)) {
    return false
  }

  // 权限判断：管理员可以取消任何状态，其他角色只能取消自己权限范围内的
  if (userRole === 'ROLE_ADMIN') {
    return true
  }

  // 普通用户只能取消待审批状态
  if (userRole === 'ROLE_USER') {
    return status === 'PENDING'
  }

  // 班长可以取消待审批和班长已审批状态
  if (userRole === 'SQUAD_LEADER') {
    return ['PENDING', 'SQUAD_APPROVED'].includes(status)
  }

  // 队长可以取消待审批、班长已审批、队长已审批状态
  if (userRole === 'TEAM_LEADER') {
    return ['PENDING', 'SQUAD_APPROVED', 'TEAM_APPROVED'].includes(status)
  }

  // 库房管理员可以取消所有状态
  if (userRole === 'WAREHOUSE_ADMIN') {
    return true
  }

  return false
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

const getOutboundTypeText = (type) => {
  const typeMap = {
    'SALE_OUT': '领用出库',
    'TRANSFER_OUT': '调拨出库',
    'DAMAGE_OUT': '借用出库',
    'OTHER_OUT': '其他出库'
  }
  return typeMap[type] || type
}

const getOutboundTypeColor = (type) => {
  const colorMap = {
    'SALE_OUT': 'primary',
    'TRANSFER_OUT': 'info',
    'DAMAGE_OUT': 'danger',
    'OTHER_OUT': 'warning'
  }
  return colorMap[type] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'SQUAD_APPROVED': '班长已审批',
    'TEAM_APPROVED': '队长已审批',
    'APPROVED': '已审批',
    'EXECUTED': '已执行',
    'COMPLETED': '已执行',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'SQUAD_APPROVED': 'primary',
    'TEAM_APPROVED': 'primary',
    'APPROVED': 'success',
    'EXECUTED': 'success',
    'COMPLETED': 'success',
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
      size: pagination.size
    }

    // 只添加非空的参数
    if (searchForm.orderNumber && searchForm.orderNumber.trim()) {
      params.orderNumber = searchForm.orderNumber.trim()
    }
    if (searchForm.warehouseId) {
      params.warehouseId = searchForm.warehouseId
    }
    if (searchForm.businessType && searchForm.businessType !== null) {
      params.businessType = searchForm.businessType
    }
    if (searchForm.status && searchForm.status !== null) {
      params.status = searchForm.status
    }

    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const response = await request.get('/outbound-orders', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载出库单数据失败:', error)
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

// 移除客户加载函数，改为直接输入领取人姓名

const loadGoodsList = async () => {
  try {
    const response = await request.get('/goods/all')
    if (response.success) {
      goodsList.value = response.data || []
    }
  } catch (error) {
    console.error('加载货物数据失败:', error)
    goodsList.value = []
  }
}

// 加载有库存的货物列表
const loadInventoryGoodsList = async () => {
  try {
    const response = await request.get('/inventory/goods-with-stock')
    if (response.success) {
      inventoryGoodsList.value = response.data || []
      filteredInventoryGoodsList.value = response.data || []
    }
  } catch (error) {
    console.error('加载库存货物数据失败:', error)
    inventoryGoodsList.value = []
    filteredInventoryGoodsList.value = []
  }
}

// 加载指定仓库的库存货物列表
const loadWarehouseInventoryGoods = async () => {
  if (!form.warehouseId) {
    filteredInventoryGoodsList.value = []
    return
  }

  try {
    console.log('正在加载仓库库存货物，仓库ID:', form.warehouseId)
    const response = await request.get(`/inventory/goods-with-stock?warehouseId=${form.warehouseId}`)
    console.log('货物列表响应:', response)

    if (response.success) {
      console.log('开始为每个货物加载库存信息...')
      // 为每个货物添加库存信息和加权平均价
      const goodsWithStock = []

      for (const goods of response.data || []) {
        try {
          console.log(`正在获取货物 ${goods.name}(ID:${goods.id}) 的库存信息`)
          const stockResponse = await request.get(`/inventory/goods/${goods.id}/stock-info?warehouseId=${form.warehouseId}`)
          console.log(`货物 ${goods.name} 库存响应:`, stockResponse)

          if (stockResponse.success) {
            const goodsWithStockInfo = {
              ...goods,
              availableStock: stockResponse.data.availableStock || 0,
              weightedAveragePrice: stockResponse.data.weightedAveragePrice || 0
            }
            console.log(`货物 ${goods.name} 库存信息:`, goodsWithStockInfo)

            // 添加所有货物（包括库存为0的）
            goodsWithStock.push(goodsWithStockInfo)
          } else {
            console.warn(`获取货物 ${goods.name} 库存信息失败:`, stockResponse.message)
          }
        } catch (error) {
          console.error(`获取货物${goods.id}库存信息失败:`, error)
        }
      }

      filteredInventoryGoodsList.value = goodsWithStock
      console.log('最终加载的有库存货物数量:', filteredInventoryGoodsList.value.length)
    } else {
      console.error('获取货物列表失败:', response.message)
      filteredInventoryGoodsList.value = []
    }
  } catch (error) {
    console.error('加载仓库库存货物数据失败:', error)
    // 如果指定仓库的接口失败，回退到加载所有货物
    try {
      console.log('回退到加载所有库存货物')
      const fallbackResponse = await request.get('/inventory/goods-with-stock')
      if (fallbackResponse.success) {
        filteredInventoryGoodsList.value = fallbackResponse.data || []
        console.log('回退加载成功，货物数量:', filteredInventoryGoodsList.value.length)
      }
    } catch (fallbackError) {
      console.error('回退加载也失败:', fallbackError)
      filteredInventoryGoodsList.value = []
    }
  }
}

// 获取规格/型号显示文本
const getSpecificationModel = (goods) => {
  if (goods.specification && goods.model) {
    return `${goods.specification} / ${goods.model}`
  }
  return goods.specification || goods.model || '-'
}

// 获取库存样式类
const getStockClass = (row) => {
  const stock = row.availableStock || 0
  if (stock <= 0) {
    return 'text-danger'
  } else if (stock <= 10) {
    return 'text-warning'
  } else {
    return 'text-success'
  }
}

// 打开货物选择弹出框
const openGoodsSelector = (index) => {
  // 检查是否已选择仓库
  if (!form.warehouseId) {
    ElMessage.warning('请先选择仓库')
    return
  }

  currentDetailIndex.value = index
  goodsSelectorVisible.value = true
  loadWarehouseInventoryGoods() // 加载指定仓库的库存货物
  selectedGoods.value = []
  resetGoodsFilter()
}

// 筛选货物
const filterGoods = () => {
  // 获取当前仓库的货物列表作为筛选基础
  const baseGoodsList = form.warehouseId ?
    filteredInventoryGoodsList.value :
    inventoryGoodsList.value

  let filtered = [...baseGoodsList]

  if (goodsFilter.code) {
    filtered = filtered.filter(goods =>
      goods.code.toLowerCase().includes(goodsFilter.code.toLowerCase())
    )
  }

  if (goodsFilter.name) {
    filtered = filtered.filter(goods =>
      goods.name.toLowerCase().includes(goodsFilter.name.toLowerCase())
    )
  }

  if (goodsFilter.specification) {
    filtered = filtered.filter(goods =>
      (goods.specification && goods.specification.toLowerCase().includes(goodsFilter.specification.toLowerCase())) ||
      (goods.model && goods.model.toLowerCase().includes(goodsFilter.specification.toLowerCase()))
    )
  }

  filteredInventoryGoodsList.value = filtered
}

// 重置货物筛选
const resetGoodsFilter = () => {
  Object.assign(goodsFilter, {
    code: '',
    name: '',
    specification: ''
  })
  // 如果已选择仓库，重新加载该仓库的货物；否则显示所有货物
  if (form.warehouseId) {
    loadWarehouseInventoryGoods()
  } else {
    filteredInventoryGoodsList.value = inventoryGoodsList.value
  }
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
    console.log('选中的货物数据:', goods)
    console.log('货物的加权平均价:', goods.weightedAveragePrice)

    const detail = form.details[currentDetailIndex.value]

    detail.goodsId = goods.id
    detail.goodsCode = goods.code
    detail.goodsName = goods.name
    detail.unit = goods.unit
    detail.specification = getSpecificationModel(goods)
    detail.availableStock = goods.availableStock || 0
    detail.unitPrice = goods.weightedAveragePrice || 0

    console.log('设置后的明细单价:', detail.unitPrice)
  } else {
    // 批量添加模式
    selectedGoods.value.forEach(goods => {
      console.log('批量添加货物数据:', goods)
      console.log('货物的加权平均价:', goods.weightedAveragePrice)

      const detail = {
        goodsId: goods.id,
        goodsCode: goods.code,
        goodsName: goods.name,
        unit: goods.unit,
        specification: getSpecificationModel(goods),
        quantity: 0,
        unitPrice: goods.weightedAveragePrice || 0,
        availableStock: goods.availableStock || 0
      }

      console.log('创建的明细单价:', detail.unitPrice)
      form.details.push(detail)
    })
  }

  goodsSelectorVisible.value = false
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    orderNumber: '',
    warehouseId: null,
    businessType: null,
    status: null,
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
    const response = await request.get('/outbound-orders/generate-order-number')
    if (response.success) {
      form.orderNumber = response.data
    }
  } catch (error) {
    form.orderNumber = 'OUT' + Date.now()
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    resetForm()

    // 通过API获取完整的出库单详情
    const response = await request.get(`/outbound-orders/${row.id}`)
    if (response.success) {
      Object.assign(form, {
        ...response.data,
        details: response.data.details || []
      })

      // 加载仓库的库存信息
      await loadWarehouseInventoryGoods()

      // 为明细项补充库存信息和规格型号
      await updateDetailsStockInfo()

      dialogVisible.value = true
    } else {
      ElMessage.error('获取出库单详情失败')
    }
  } catch (error) {
    console.error('获取出库单详情失败:', error)
    ElMessage.error('获取出库单详情失败')
  }
}

// 更新明细项的库存信息
const updateDetailsStockInfo = async () => {
  for (const detail of form.details) {
    if (detail.goodsId) {
      try {
        // 获取货物的库存信息
        const stockResponse = await request.get(`/inventory/goods/${detail.goodsId}/stock-info?warehouseId=${form.warehouseId}`)
        if (stockResponse.success) {
          detail.availableStock = stockResponse.data.availableStock || 0
          if (!detail.unitPrice || detail.unitPrice === 0) {
            detail.unitPrice = stockResponse.data.weightedAveragePrice || 0
          }
        }

        // 如果后端已经返回了规格型号信息，直接使用
        if (detail.goodsSpecification || detail.goodsModel) {
          const goods = {
            specification: detail.goodsSpecification,
            model: detail.goodsModel
          }
          detail.specification = getSpecificationModel(goods)
        } else {
          // 否则获取货物的详细信息（包括规格型号）
          const goodsResponse = await request.get(`/goods/${detail.goodsId}`)
          if (goodsResponse.success) {
            const goods = goodsResponse.data
            detail.specification = getSpecificationModel(goods)
          }
        }
      } catch (error) {
        console.error(`获取货物${detail.goodsId}信息失败:`, error)
      }
    }
  }
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
    await ElMessageBox.confirm('确定要执行该出库单吗？执行后将扣减库存。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.put(`/outbound-orders/${row.id}/execute`)
    if (response.success) {
      ElMessage.success('出库单执行成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('执行出库单失败:', error)
    }
  }
}

const handleCancel = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消出库单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入取消原因'
    })
    
    const response = await request.put(`/outbound-orders/${row.id}/cancel`, { reason })
    if (response.success) {
      ElMessage.success('出库单取消成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消出库单失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    console.log('开始提交，原始表单数据:', form)

    await formRef.value.validate()

    if (form.details.length === 0) {
      ElMessage.error('请添加出库明细')
      return
    }

    // 检查库存
    const stockCheck = await checkStock()
    if (!stockCheck) {
      return
    }
    
    // 更新总数量和总金额
    form.totalQuantity = totalQuantity.value
    form.totalAmount = totalAmount.value

    // 格式化表单数据
    const submitData = {
      ...form,
      // 确保日期字段是字符串格式
      plannedDate: Array.isArray(form.plannedDate) ? form.plannedDate[0] : form.plannedDate,
      actualDate: Array.isArray(form.actualDate) ? form.actualDate[0] : form.actualDate,
      // 字段名映射：前端outboundType -> 后端businessType
      businessType: form.outboundType
    }

    // 删除原来的outboundType字段，避免混淆
    delete submitData.outboundType

    console.log('提交的表单数据:', submitData)

    submitLoading.value = true
    const url = form.id ? `/outbound-orders/${form.id}` : '/outbound-orders'
    const method = form.id ? 'put' : 'post'

    const response = await request[method](url, submitData)
    if (response.success) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    } else {
      console.error('服务器返回错误:', response)
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    if (error.response && error.response.data) {
      console.error('错误详情:', error.response.data)
      ElMessage.error(error.response.data.message || '提交失败')
    } else {
      ElMessage.error('提交失败，请检查网络连接')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleSubmitApproval = async () => {
  try {
    approvalLoading.value = true
    const response = await request.put(`/outbound-orders/${currentRow.value.id}/approve`, approvalForm)
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
  // 检查是否已选择仓库
  if (!form.warehouseId) {
    ElMessage.warning('请先选择仓库')
    return
  }

  // 打开货物选择弹出框，批量添加模式
  currentDetailIndex.value = -1
  goodsSelectorVisible.value = true
  loadWarehouseInventoryGoods() // 加载指定仓库的库存货物
  selectedGoods.value = []
  resetGoodsFilter()
}

const handleRemoveDetail = (index) => {
  form.details.splice(index, 1)
}

// 仓库变更处理
const handleWarehouseChange = () => {
  // 清空现有明细
  if (form.details.length > 0) {
    ElMessageBox.confirm(
      '更换仓库将清空已添加的出库明细，是否继续？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(() => {
      form.details = []
    }).catch(() => {
      // 用户取消，恢复原仓库选择
      // 这里可以根据需要实现恢复逻辑
    })
  }
}

const handleGoodsChange = async (row, index) => {
  const goods = inventoryGoodsList.value.find(g => g.id === row.goodsId)
  if (goods) {
    row.goodsCode = goods.code
    row.goodsName = goods.name
    row.unit = goods.unit
    row.specification = goods.specification || goods.model || '-'

    // 获取库存信息和加权平均价
    try {
      const response = await request.get(`/inventory/goods/${row.goodsId}/stock-info?warehouseId=${form.warehouseId}`)
      if (response.success) {
        row.availableStock = response.data.availableStock || 0
        row.unitPrice = response.data.weightedAveragePrice || 0
      } else {
        row.availableStock = 0
        row.unitPrice = 0
      }
    } catch (error) {
      console.error('获取库存信息失败:', error)
      row.availableStock = 0
      row.unitPrice = 0
    }
  }
}

const calculateDetailAmount = (row) => {
  row.amount = (row.quantity || 0) * (row.unitPrice || 0)
}

const checkStock = async () => {
  const warnings = []
  
  for (const detail of form.details) {
    if (detail.quantity > detail.availableStock) {
      warnings.push({
        goodsName: detail.goodsName,
        requestQuantity: detail.quantity,
        availableStock: detail.availableStock,
        shortage: detail.quantity - detail.availableStock
      })
    }
  }
  
  if (warnings.length > 0) {
    stockWarnings.value = warnings
    stockWarningVisible.value = true
    return false
  }
  
  return true
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    orderNumber: '',
    warehouseId: null,
    outboundType: 'SALE_OUT',
    recipientName: '',
    plannedDate: '',
    referenceNumber: '',
    remark: '',
    createdBy: '',
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
  loadInventoryGoodsList()

  // 为非管理员用户设置默认仓库
  initializeDefaultWarehouse()
})
</script>

<style lang="scss" scoped>
.text-success {
  color: #67c23a;
  font-weight: 600;
}

.text-primary {
  color: #409eff;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

.text-warning {
  color: #e6a23c;
  font-weight: 600;
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

.stock-warning {
  .text-danger {
    color: #f56c6c;
    font-weight: 600;
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
  .outbound-container {
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
  .outbound-container {
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
