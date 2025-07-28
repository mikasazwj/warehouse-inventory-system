<template>
  <div class="inbound-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><Download /></el-icon>
            入库管理
          </h1>
          <p class="page-subtitle">管理系统中的所有入库业务和流程</p>
        </div>
        <div class="header-actions" :class="{ 'mobile-actions': isMobile }">
          <el-button type="primary" @click="handleAdd" :size="isMobile ? 'small' : 'default'">
            <el-icon><Plus /></el-icon>
            新建入库单
          </el-button>
          <InboundLedgerPrint
            :selected-orders="selectedRows"
            :all-orders="tableData"
            @refresh="loadData"
          />
          <InboundExport
            :selected-orders="selectedRows"
            :current-page-data="tableData"
            :search-params="searchForm"
            :size="isMobile ? 'small' : 'default'"
            @refresh="loadData"
          />
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
              <el-icon><Document /></el-icon>
              总计：{{ pagination.total }} 个入库单
            </span>
            <span class="stat-item">
              <el-icon><Clock /></el-icon>
              待审批：{{ pendingCount }} 个
            </span>
            <span class="stat-item">
              <el-icon><CircleCheck /></el-icon>
              已完成：{{ completedCount }} 个
            </span>
          </div>
        </div>
        <el-form :model="searchForm" class="search-form" :class="{ 'mobile-form': isMobile }">
          <div class="form-content">
            <div class="search-inputs">
              <el-form-item label="单号" class="search-item">
                <el-input
                  v-model="searchForm.orderNumber"
                  placeholder="请输入入库单号"
                  clearable
                  prefix-icon="Search"
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="仓库" class="search-item" v-if="userStore.userInfo?.role === 'ROLE_ADMIN'">
                <el-select v-model="searchForm.warehouseId" placeholder="请选择仓库" clearable class="search-select">
                  <el-option
                    v-for="warehouse in warehouses"
                    :key="warehouse.id"
                    :label="warehouse.name"
                    :value="warehouse.id"
                  >
                    <el-icon><House /></el-icon>
                    {{ warehouse.name }}
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="仓库" class="search-item" v-else>
                <el-input
                  :value="getCurrentWarehouseName()"
                  disabled
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="类型" class="search-item">
                <el-select v-model="searchForm.businessType" placeholder="请选择类型" clearable class="search-select">
                  <el-option label="采购入库" value="PURCHASE_IN">
                    <el-icon><ShoppingCart /></el-icon>
                    采购入库
                  </el-option>
                  <el-option label="归还入库" value="RETURN_IN">
                    <el-icon><RefreshLeft /></el-icon>
                    归还入库
                  </el-option>
                  <el-option label="调拨入库" value="TRANSFER_IN">
                    <el-icon><Switch /></el-icon>
                    调拨入库
                  </el-option>
                  <el-option label="盘盈入库" value="INVENTORY_GAIN">
                    <el-icon><Plus /></el-icon>
                    盘盈入库
                  </el-option>
                  <el-option label="其他入库" value="OTHER_IN">
                    <el-icon><More /></el-icon>
                    其他入库
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="search-select">
                  <el-option label="待审批" value="PENDING">
                    <el-icon><Clock /></el-icon>
                    待审批
                  </el-option>
                  <el-option label="已审批" value="APPROVED">
                    <el-icon><Select /></el-icon>
                    已审批
                  </el-option>
                  <el-option label="已执行" value="EXECUTED">
                    <el-icon><CircleCheck /></el-icon>
                    已执行
                  </el-option>
                  <el-option label="已取消" value="CANCELLED">
                    <el-icon><CircleClose /></el-icon>
                    已取消
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="日期" class="search-item">
                <el-date-picker
                  v-model="searchForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  class="search-date"
                  @change="handleDateChange"
                />
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
          <h3>入库单列表</h3>
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
          class="inbound-table"
          empty-text="暂无入库单数据"
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="orderNumber" label="入库单号" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="order-number-cell">
                <el-icon class="order-icon"><Document /></el-icon>
                <span class="order-text">{{ row.orderNumber }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="warehouseName" label="仓库" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="warehouse-cell">
                <el-icon class="warehouse-icon"><House /></el-icon>
                <span>{{ row.warehouseName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="入库类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getBusinessTypeColor(row.businessType)" size="small">
                {{ getBusinessTypeText(row.businessType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="总数量" width="100" align="right">
            <template #default="{ row }">
              <div class="quantity-cell">
                <span class="quantity-number">{{ calculateRowTotalQuantity(row) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
            <template #default="{ row }">
              <div class="amount-cell">
                <span class="amount-symbol">¥</span>
                <span class="amount-number">{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="plannedDate" label="入库时间" min-width="110">
            <template #default="{ row }">
              <div class="date-cell">
                <el-icon class="date-icon"><Calendar /></el-icon>
                <span>{{ formatDate(row.plannedDate) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdBy" label="制单人" min-width="100">
            <template #default="{ row }">
              <div class="creator-cell">
                <el-icon class="creator-icon"><User /></el-icon>
                <span>{{ row.createdBy || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" min-width="160">
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon class="time-icon"><Clock /></el-icon>
                <span>{{ formatDateTime(row.createdTime) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 80 : 320" fixed="right" align="center">
            <template #default="{ row }">
              <!-- 桌面端按钮组 -->
              <div class="action-buttons desktop-actions">
                <el-button type="primary" size="small" @click="handleView(row)" class="action-btn">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button type="info" size="small" @click="handlePrint(row)" class="action-btn">
                  <el-icon><Printer /></el-icon>
                  打印
                </el-button>
                <el-button
                  v-if="row.status === 'PENDING'"
                  type="warning"
                  size="small"
                  @click="handleEdit(row)"
                  class="action-btn"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  v-if="row.status === 'PENDING' && (userStore.isAdmin || userStore.isWarehouseAdmin)"
                  type="success"
                  size="small"
                  @click="handleApprove(row)"
                  class="action-btn"
                >
                  <el-icon><Check /></el-icon>
                  审批
                </el-button>
                <el-button
                  v-if="row.status === 'APPROVED' && userStore.isWarehouseAdmin"
                  type="success"
                  size="small"
                  @click="handleExecute(row)"
                  class="action-btn"
                >
                  <el-icon><Position /></el-icon>
                  执行
                </el-button>
                <el-button
                  v-if="row.status === 'EXECUTED'"
                  type="success"
                  size="small"
                  @click="handleConsistency(row)"
                  class="action-btn"
                >
                  <el-icon><Check /></el-icon>
                  已执行
                </el-button>
                <el-button
                  v-if="['PENDING', 'APPROVED'].includes(row.status) && (userStore.isAdmin || userStore.isWarehouseAdmin || (userStore.role === 'ROLE_USER' && row.status === 'PENDING'))"
                  type="danger"
                  size="small"
                  @click="handleCancel(row)"
                  class="action-btn"
                >
                  <el-icon><Close /></el-icon>
                  取消
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
                      <el-dropdown-item command="print">
                        <el-icon><Printer /></el-icon>
                        打印入库单
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="row.status === 'PENDING'"
                        command="edit"
                        divided
                      >
                        <el-icon><Edit /></el-icon>
                        编辑入库单
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="row.status === 'PENDING' && (userStore.isAdmin || userStore.isWarehouseAdmin)"
                        command="approve"
                      >
                        <el-icon><Check /></el-icon>
                        审批入库单
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="row.status === 'APPROVED' && userStore.isWarehouseAdmin"
                        command="execute"
                      >
                        <el-icon><Position /></el-icon>
                        执行入库
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="['PENDING', 'APPROVED'].includes(row.status) && (userStore.isAdmin || userStore.isWarehouseAdmin || (userStore.role === 'ROLE_USER' && row.status === 'PENDING'))"
                        command="cancel"
                        divided
                      >
                        <el-icon><Close /></el-icon>
                        取消入库单
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
            class="inbound-pagination"
          />
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :width="isMobile ? '95%' : '1400px'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="inbound-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <el-icon><Plus v-if="!form.id" /><Edit v-else /></el-icon>
            </div>
            <div class="header-text">
              <h3>{{ dialogTitle }}</h3>
              <p>{{ form.id ? '修改入库单信息和明细' : '创建新的入库单' }}</p>
            </div>
          </div>
          <el-button type="text" @click="dialogVisible = false" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 基本信息卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>
          <el-form
            ref="formRef"
            :model="form"
            :rules="formRules"
            label-width="100px"
          >
        <el-row :gutter="isMobile ? 0 : 20">
          <el-col :span="isMobile ? 24 : 8">
            <el-form-item label="入库单号" prop="orderNumber">
              <el-input v-model="form.orderNumber" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select
                v-model="form.warehouseId"
                placeholder="请选择仓库"
                style="width: 100%"
                :disabled="isWarehouseLocked"
                popper-class="inbound-warehouse-select"
                :teleported="false"
                @focus="console.log('仓库选择器获得焦点，可用仓库:', availableWarehouses)"
                @visible-change="(visible) => console.log('仓库下拉框显示状态:', visible, '可用仓库数量:', availableWarehouses.length)"
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
          <el-col :span="isMobile ? 24 : 8">
            <el-form-item label="业务类型" prop="businessType">
              <el-select
                v-model="form.businessType"
                placeholder="请选择业务类型"
                style="width: 100%"
                popper-class="inbound-business-type-select"
                :teleported="false"
                @focus="console.log('业务类型选择器获得焦点')"
                @visible-change="(visible) => console.log('业务类型下拉框显示状态:', visible)"
              >
                <el-option label="采购入库" value="PURCHASE_IN" />
                <el-option label="归还入库" value="RETURN_IN" />
                <el-option label="调拨入库" value="TRANSFER_IN" />
                <el-option label="盘盈入库" value="INVENTORY_GAIN" />
                <el-option label="其他入库" value="OTHER_IN" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="isMobile ? 0 : 20">
          <el-col :span="isMobile ? 24 : 8">
            <el-form-item label="入库时间" prop="plannedDate">
              <el-date-picker
                v-model="form.plannedDate"
                type="date"
                placeholder="请选择入库时间"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :teleported="false"
                popper-class="inbound-date-picker"
              />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 8">
            <el-form-item label="制单人" prop="createdBy">
              <el-input v-model="form.createdBy" placeholder="请输入制单人" />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 8">
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
          </el-form>
        </div>

        <!-- 入库明细卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>入库明细</span>
          </div>
          <div class="detail-actions">
              <el-button type="primary" size="small" @click="handleAddDetail">
                <el-icon><Plus /></el-icon>
                添加明细
              </el-button>
              <el-button type="success" size="small" @click="handleBatchImport" v-if="form.details.length === 0">
                <el-icon><Upload /></el-icon>
                批量导入
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
                <span class="amount-text">¥{{ (row.quantity * row.unitPrice || 0).toFixed(2) }}</span>
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
      </div>
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
      :width="isMobile ? '95%' : '1200px'"
      :fullscreen="isMobile"
      class="inbound-detail-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon view-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="header-text">
              <h3>入库单详情</h3>
              <p>查看入库单的详细信息</p>
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
                    入库单号
                  </div>
                  <div class="info-value">{{ viewData.orderNumber }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><House /></el-icon>
                    仓库
                  </div>
                  <div class="info-value">{{ viewData.warehouseName }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><Calendar /></el-icon>
                    入库时间
                  </div>
                  <div class="info-value">{{ viewData.plannedDate }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><ShoppingCart /></el-icon>
                    入库类型
                  </div>
                  <div class="info-value">
                    <el-tag :type="getBusinessTypeColor(viewData.businessType)">
                      {{ getBusinessTypeText(viewData.businessType) }}
                    </el-tag>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><User /></el-icon>
                    制单人
                  </div>
                  <div class="info-value">{{ viewData.createdBy || '-' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">
                    <el-icon><Link /></el-icon>
                    参考单号
                  </div>
                  <div class="info-value">{{ viewData.referenceNumber || '-' }}</div>
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

          <!-- 状态信息卡片 -->
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
                    <el-tag :type="getStatusType(viewData.status)">
                      {{ getStatusText(viewData.status) }}
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
              </div>
            </div>
          </div>

          <!-- 明细信息卡片 -->
          <div class="info-card">
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>入库明细</span>
            </div>
            <div class="card-content">
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
        </div>
      </div>
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handlePrint(viewData)">
          <el-icon><Printer /></el-icon>
          打印入库单
        </el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :width="isMobile ? '95%' : '700px'"
      :fullscreen="isMobile"
      class="approval-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <el-icon><Check /></el-icon>
            </div>
            <div class="header-text">
              <h3>审批入库单</h3>
              <p>审核入库单信息并做出决定</p>
            </div>
          </div>
          <el-button type="text" @click="approvalDialogVisible = false" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="dialog-body">
        <div class="form-section">
          <div class="section-title">
            <el-icon><DocumentChecked /></el-icon>
            <span>审批信息</span>
          </div>
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
        </div>
      </div>
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
      :width="isMobile ? '95%' : '90%'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="goods-selector-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
    >
      <template #header>
        <div class="dialog-header">
          <div class="header-content">
            <div class="header-icon">
              <el-icon><Search /></el-icon>
            </div>
            <div class="header-text">
              <h3>选择货物</h3>
              <p>从货物列表中选择要入库的商品</p>
            </div>
          </div>
          <el-button type="text" @click="goodsSelectorVisible = false" class="close-btn">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 筛选条件卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Filter /></el-icon>
            <span>筛选条件</span>
          </div>
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
        </div>

        <!-- 货物列表卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>货物列表</span>
          </div>
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
        </div>
      </div>

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

    <!-- 打印预览 -->
    <PrintPreview
      v-model="printPreviewVisible"
      :content="printContent"
      :title="printTitle"
      @print="handlePrintComplete"
      @close="handlePrintPreviewClose"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowDown, View, Edit, Check, Position, Close, Delete, Refresh, Download, Upload, Calendar, Clock, User, Document, CircleCheck, House, ShoppingCart, RefreshLeft, Switch, More, CircleClose, Printer } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { useDeviceDetection, mobileOptimizations } from '@/utils/responsive'
import { printHtml, generateInboundPrintContent } from '@/utils/print'
import PrintPreview from '@/components/PrintPreview.vue'
import InboundLedgerPrint from '@/components/InboundLedgerPrint.vue'
import InboundExport from '@/components/InboundExport.vue'

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
const selectedRows = ref([])
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

// 打印预览相关
const printPreviewVisible = ref(false)
const printContent = ref('')
const printTitle = ref('')
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

// 统计数据计算属性
const pendingCount = computed(() => {
  return tableData.value.filter(item => item.status === 'PENDING').length
})

const completedCount = computed(() => {
  return tableData.value.filter(item => item.status === 'EXECUTED').length
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
    console.log('开始加载仓库数据...')
    const response = await request.get('/warehouses/enabled')
    console.log('仓库数据响应:', response)
    if (response.success) {
      warehouses.value = response.data || []
      console.log('仓库数据加载成功:', warehouses.value)
    } else {
      console.error('仓库数据加载失败:', response.message)
      warehouses.value = [
        { id: 1, name: '主仓库' },
        { id: 2, name: '分仓库A' }
      ]
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

// 处理表格行选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
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

const handleDateChange = () => {
  // 日期变化时自动搜索
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

  // 强制重新加载仓库数据
  console.log('新增入库单 - 开始加载仓库数据')
  await loadWarehouses()
  console.log('新增入库单 - 仓库数据加载完成，当前仓库列表:', warehouses.value)
  console.log('新增入库单 - 可用仓库列表:', availableWarehouses.value)

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

  // 为非管理员用户设置默认仓库
  if (!userStore.isAdmin && userStore.warehouses && userStore.warehouses.length > 0) {
    form.warehouseId = userStore.warehouses[0].id
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

  // 确保清除表格选中状态
  nextTick(() => {
    if (goodsTableRef.value) {
      goodsTableRef.value.clearSelection()
    }
  })
}

// 批量导入功能
const handleBatchImport = () => {
  ElMessage.info('批量导入功能开发中，敬请期待！')
  // TODO: 实现Excel批量导入功能
}



// 刷新功能
const handleRefresh = () => {
  loadData()
}

// 打印功能
const handlePrint = async (row) => {
  try {
    // 如果传入的是行数据，需要获取完整的详情数据
    let printData = row
    if (!row.details || row.details.length === 0) {
      // 获取完整的入库单详情
      const response = await request.get(`/inbound-orders/${row.id}`)
      if (response.success) {
        printData = response.data
      } else {
        ElMessage.error('获取入库单详情失败')
        return
      }
    }

    // 生成打印内容
    printContent.value = generateInboundPrintContent(printData)
    printTitle.value = `入库单-${printData.orderNumber}`

    // 显示打印预览
    printPreviewVisible.value = true
  } catch (error) {
    console.error('打印失败:', error)
    ElMessage.error('获取打印数据失败，请重试')
  }
}

// 打印完成回调
const handlePrintComplete = () => {
  printPreviewVisible.value = false
}

// 打印预览关闭回调
const handlePrintPreviewClose = () => {
  printContent.value = ''
  printTitle.value = ''
}

// 移动端操作处理
const handleMobileAction = (command, row) => {
  switch (command) {
    case 'view':
      handleView(row)
      break
    case 'print':
      handlePrint(row)
      break
    case 'edit':
      handleEdit(row)
      break
    case 'approve':
      handleApprove(row)
      break
    case 'execute':
      handleExecute(row)
      break
    case 'cancel':
      handleCancel(row)
      break
    default:
      break
  }
}

const handleRemoveDetail = (index) => {
  form.details.splice(index, 1)
  // 清除货物选择表格的选中状态，避免下次打开时显示错误的选中状态
  if (goodsTableRef.value) {
    goodsTableRef.value.clearSelection()
  }
  selectedGoods.value = []
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
.inbound-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  // 页面头部样式
  .page-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 40px; // 增加左右两边的间距

      .header-left {
        flex: 1; // 让左侧内容占据剩余空间
        min-width: 0; // 防止内容溢出

        .page-title {
          display: flex;
          align-items: center;
          margin: 0 0 8px 0;
          font-size: 28px;
          font-weight: 600;
          color: white;

          .title-icon {
            margin-right: 12px;
            font-size: 32px;
          }
        }

        .page-subtitle {
          margin: 0;
          color: rgba(255, 255, 255, 0.8);
          font-size: 14px;
          line-height: 1.4;
        }
      }

      .header-actions {
        display: flex;
        gap: 12px;
        flex-shrink: 0; // 防止按钮被压缩

        &.mobile-actions {
          // 移动端样式将在媒体查询中覆盖
          flex-direction: row;
          gap: 12px;
        }
      }
    }
  }

  // 搜索区域样式
  .search-section {
    margin-bottom: 20px;

    .search-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      .search-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 1px solid #f0f2f5;

        h3 {
          margin: 0;
          color: #303133;
          font-size: 16px;
          font-weight: 600;
        }

        .search-stats {
          display: flex;
          gap: 24px;

          .stat-item {
            display: flex;
            align-items: center;
            gap: 6px;
            color: #606266;
            font-size: 14px;

            .el-icon {
              color: #409eff;
            }
          }
        }
      }

      .search-form {
        .form-content {
          display: flex;
          justify-content: space-between;
          align-items: flex-end;

          .search-inputs {
            display: flex;
            gap: 16px;
            flex-wrap: wrap;

            .search-item {
              margin-bottom: 0;

              .search-input,
              .search-select {
                width: 200px;
              }

              .search-date {
                width: 240px;
              }
            }
          }

          .search-actions {
            display: flex;
            gap: 8px;
            margin-left: 16px;
            flex-shrink: 0;
          }
        }

        &.mobile-form {
          .form-content {
            flex-direction: column;
            gap: 16px;

            .search-inputs {
              width: 100%;
              justify-content: space-between;

              .search-item {
                flex: 1;
                min-width: 200px;

                .search-input,
                .search-select,
                .search-date {
                  width: 100%;
                }
              }
            }

            .search-actions {
              width: 100%;
              margin-left: 0;
              justify-content: center;

              .el-button {
                flex: 1;
                max-width: 120px;
              }
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
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

      .table-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f2f5;

        h3 {
          margin: 0;
          color: #303133;
          font-size: 16px;
          font-weight: 600;
        }

        .table-actions {
          display: flex;
          gap: 8px;
        }
      }

      .inbound-table {
        .order-number-cell {
          display: flex;
          align-items: center;
          gap: 8px;

          .order-icon {
            color: #409eff;
            font-size: 16px;
          }

          .order-text {
            font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
            font-weight: 500;
          }
        }

        .warehouse-cell {
          display: flex;
          align-items: center;
          gap: 8px;

          .warehouse-icon {
            color: #67c23a;
            font-size: 16px;
          }
        }

        .date-cell {
          display: flex;
          align-items: center;
          gap: 6px;

          .date-icon {
            color: #e6a23c;
            font-size: 14px;
          }
        }

        .quantity-cell {
          .quantity-number {
            font-weight: 600;
            color: #303133;
          }
        }

        .amount-cell {
          .amount-symbol {
            color: #909399;
            margin-right: 2px;
          }

          .amount-number {
            font-weight: 600;
            color: #f56c6c;
          }
        }

        .creator-cell {
          display: flex;
          align-items: center;
          gap: 6px;

          .creator-icon {
            color: #909399;
            font-size: 14px;
          }
        }

        .time-cell {
          display: flex;
          align-items: center;
          gap: 6px;

          .time-icon {
            color: #909399;
            font-size: 14px;
          }
        }

        .action-buttons {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
          justify-content: center;

          &.desktop-actions {
            .action-btn {
              margin: 0;
              padding: 6px 12px;
              font-size: 12px;
              border-radius: 4px;

              .el-icon {
                margin-right: 4px;
              }
            }
          }
        }

        .mobile-actions {
          display: none;
        }

        .desktop-actions {
          display: flex;
        }
      }

      .pagination-container {
        display: flex;
        justify-content: center;
        margin-top: 24px;
        padding: 20px 24px;
        background: #fafbfc;
        border-radius: 8px;

        .inbound-pagination {
          :deep(.el-pagination) {
            justify-content: center;
          }

          .el-pagination__total {
            margin-right: auto;
          }
        }
      }
    }
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

  // 明细区域样式
  .detail-section {
    margin-top: 20px;

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h4 {
        margin: 0;
        color: #303133;
        font-size: 16px;
        font-weight: 600;
      }

      .detail-actions {
        display: flex;
        gap: 8px;
      }
    }

    .total-info {
      margin-top: 16px;
      padding: 15px;
      background: #f8f9fa;
      border-radius: 6px;
      border: 1px solid #e4e7ed;

      span {
        font-weight: 600;
        color: #303133;
      }
    }

    .amount-text {
      color: #f56c6c;
      font-weight: 600;
    }
  }

  // 详情查看样式
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

  // 工具样式
  .cursor-pointer {
    cursor: pointer;
  }

  .text-success {
    color: #67c23a;
  }

  .text-primary {
    color: #409eff;
  }

  .text-danger {
    color: #f56c6c;
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
}

// 现代化弹窗样式
.modern-dialog {
  :deep(.el-dialog) {
    z-index: 3000 !important;
  }

  :deep(.el-overlay) {
    z-index: 2999 !important;
  }

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

        .el-input, .el-select, .el-textarea {
          .el-input__wrapper,
          .el-select__wrapper,
          .el-textarea__inner {
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;

            &:hover {
              box-shadow: 0 2px 6px rgba(102, 126, 234, 0.15);
            }

            &.is-focus {
              box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }
          }
        }
      }

      .detail-actions {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;

        .el-button {
          border-radius: 8px;
          font-weight: 500;
        }
      }

      .total-info {
        margin-top: 16px;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 8px;
        font-weight: 600;
        color: #303133;
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

        .el-table {
          border-radius: 8px;
          overflow: hidden;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

          .el-table__header {
            background: #f8f9fa;

            th {
              background: #f8f9fa !important;
              color: #2c3e50;
              font-weight: 600;
              border-bottom: 2px solid #e9ecef;
            }
          }

          .el-table__body {
            tr:hover {
              background: #f0f7ff !important;
            }
          }
        }
      }
    }
  }
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .inbound-management {
    padding: 15px;

    // 移动端页面头部优化
    .page-header {
      .header-content {
        gap: 20px; // 移动端减少间距
        flex-direction: column;
        text-align: center;

        .header-left {
          .page-title {
            font-size: 24px;
            justify-content: center;
          }

          .page-subtitle {
            text-align: center;
          }
        }

        .header-actions {
          width: 100%;
          justify-content: center;
          flex-direction: row !important; // 强制保持水平排列
          gap: 12px;

          .el-button {
            flex: 1; // 让按钮等宽
            max-width: 120px; // 限制最大宽度
            min-width: 100px; // 设置最小宽度
          }
        }
      }
    }

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
    }

    // 移动端操作栏优化
    .table-section {
      .inbound-table {
        .desktop-actions {
          display: none !important;
        }

        .mobile-actions {
          display: block !important;
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

      .pagination-container {
        padding: 16px;

        .inbound-pagination {
          .el-pagination__sizes,
          .el-pagination__total,
          .el-pagination__jump {
            display: none;
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

        .info-card {
          padding: 16px;
          margin-bottom: 16px;

          .card-header {
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
      }
    }

    .inbound-dialog,
    .inbound-detail-dialog,
    .approval-dialog,
    .goods-selector-dialog {
      .el-dialog__body {
        padding: 16px;

        .el-form {
          .el-form-item {
            margin-bottom: 16px;

            .el-form-item__label {
              width: auto !important;
              margin-bottom: 6px;
              text-align: left !important;
              font-size: 14px;
            }

            .el-form-item__content {
              margin-left: 0 !important;
            }

            .el-input,
            .el-select,
            .el-date-picker,
            .el-textarea {
              width: 100%;
            }
          }
        }

        .el-table {
          font-size: 12px;

          .el-table__cell {
            padding: 8px 4px;
          }

          .el-table__header {
            th {
              font-size: 12px;
              padding: 8px 4px;
            }
          }
        }

        .detail-actions {
          .el-button {
            padding: 6px 12px;
            font-size: 12px;
            margin: 0 4px;
          }
        }
      }

      .el-dialog__footer {
        padding: 16px;
        text-align: center;

        .el-button {
          margin: 0 6px;
          min-width: 80px;
          padding: 8px 16px;
          font-size: 14px;
        }
      }
    }

    // 货物选择器特殊适配
    .goods-selector-dialog {
      .el-dialog__body {
        .goods-search {
          .el-form-item {
            margin-bottom: 12px;
          }
        }

        .pagination-container {
          padding: 12px;

          .el-pagination {
            .el-pagination__sizes,
            .el-pagination__total,
            .el-pagination__jump {
              display: none;
            }
          }
        }
      }
    }
  }



}

/* 平板端优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .inbound-management {
    .page-header {
      .header-content {
        gap: 30px; // 平板端适中间距

        .header-left {
          .page-title {
            font-size: 26px;
          }
        }
      }
    }

    // 平板端弹窗优化
    .inbound-dialog,
    .inbound-detail-dialog,
    .approval-dialog,
    .goods-selector-dialog {
      .el-dialog__body {
        .el-form {
          .el-form-item {
            .el-form-item__label {
              width: 120px !important;
              text-align: right !important;
            }

            .el-form-item__content {
              margin-left: 130px !important;
            }
          }
        }

        .el-table {
          font-size: 13px;

          .el-table__cell {
            padding: 10px 6px;
          }
        }
      }
    }
  }
}

/* 小屏手机优化 */
@media (max-width: 480px) {
  .inbound-management {
    // 超小屏弹窗优化
    .inbound-dialog,
    .inbound-detail-dialog,
    .approval-dialog,
    .goods-selector-dialog {
      .el-dialog__body {
        padding: 12px;

        .el-form {
          .el-form-item {
            margin-bottom: 12px;

            .el-form-item__label {
              font-size: 12px;
              margin-bottom: 4px;
            }

            .el-input,
            .el-select,
            .el-date-picker,
            .el-textarea {
              font-size: 14px;
            }
          }
        }

        .el-table {
          font-size: 11px;

          .el-table__cell {
            padding: 6px 2px;
          }

          .el-table__header {
            th {
              font-size: 11px;
              padding: 6px 2px;
            }
          }
        }

        .detail-actions {
          .el-button {
            padding: 4px 8px;
            font-size: 11px;
            min-width: 60px;
            margin: 0 2px;
          }
        }
      }

      .el-dialog__footer {
        padding: 12px;

        .el-button {
          margin: 0 4px;
          min-width: 70px;
          padding: 6px 12px;
          font-size: 12px;
        }
      }
    }

    // 货物选择器超小屏优化
    .goods-selector-dialog {
      .el-dialog__body {
        .goods-search {
          .el-form-item {
            margin-bottom: 8px;
          }
        }

        .pagination-container {
          padding: 8px;

          .el-pagination {
            .el-pager li {
              min-width: 24px;
              height: 24px;
              line-height: 24px;
              font-size: 10px;
            }

            .btn-prev,
            .btn-next {
              min-width: 24px;
              height: 24px;
              line-height: 24px;
              font-size: 10px;
            }
          }
        }
      }
    }
  }
}

/* 小屏手机优化 */
@media (max-width: 480px) {
  .inbound-management {
    padding: 10px;

    // 超小屏幕按钮优化
    .page-header {
      .header-content {
        .header-actions {
          .el-button {
            min-width: 90px; // 减小最小宽度
            max-width: 110px; // 减小最大宽度
            font-size: 13px; // 稍微减小字体
            padding: 8px 12px; // 调整内边距
          }
        }
      }
    }

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

  // 分页小屏适配
  .pagination-container {
    padding: 12px;

    .inbound-pagination {
      .el-pager li {
        min-width: 28px;
        height: 28px;
        line-height: 28px;
        font-size: 12px;
      }

      .btn-prev,
      .btn-next {
        min-width: 28px;
        height: 28px;
        line-height: 28px;
        font-size: 12px;
      }
    }
  }
}

/* 日期选择器样式修复 */
.inbound-date-picker {
  z-index: 4000 !important;
}

/* 确保弹窗内的日期选择器正常显示 */
.modern-dialog .el-date-editor {
  width: 100%;
}

.modern-dialog .el-date-editor .el-input__inner {
  cursor: pointer;
}

/* 仓库和业务类型选择器样式修复 */
.inbound-warehouse-select,
.inbound-business-type-select {
  z-index: 4000 !important;
  max-height: 300px !important;
  overflow-y: auto !important;
}

/* 确保弹窗内的选择器正常显示 */
.modern-dialog .el-select {
  width: 100%;
}

.modern-dialog .el-select .el-input__inner {
  cursor: pointer;
}

/* 强制确保下拉选项可见 */
.el-select-dropdown {
  z-index: 4000 !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.el-select-dropdown .el-select-dropdown__item {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
  height: auto !important;
  line-height: normal !important;
  padding: 8px 12px !important;
  color: #606266 !important;
  background-color: #fff !important;
}

.el-select-dropdown .el-select-dropdown__item:hover {
  background-color: #f5f7fa !important;
}

.el-popper {
  z-index: 4000 !important;
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

/* 特别针对入库弹窗内的选择器 */
.inbound-dialog .el-select-dropdown {
  z-index: 4000 !important;
  position: fixed !important;
}

.inbound-dialog .el-select-dropdown .el-select-dropdown__list {
  padding: 4px 0 !important;
}

.inbound-dialog .el-select-dropdown .el-select-dropdown__item {
  padding: 8px 12px !important;
  font-size: 14px !important;
  color: #606266 !important;
  background-color: #fff !important;
  border: none !important;
  outline: none !important;
}

.inbound-dialog .el-select-dropdown .el-select-dropdown__item.is-selected {
  color: #409eff !important;
  background-color: #f0f9ff !important;
  font-weight: 500 !important;
}

.inbound-dialog .el-select-dropdown .el-select-dropdown__item:hover {
  background-color: #f5f7fa !important;
}
</style>
