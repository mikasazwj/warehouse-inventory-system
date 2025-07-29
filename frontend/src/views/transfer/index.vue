<template>
  <div class="page-container modern-container">
    <div class="page-header modern-header">
      <div class="header-content">
        <div class="header-left">
          <div class="page-title">
            <div class="title-icon">
              <el-icon><Position /></el-icon>
            </div>
            <div class="title-content">
              <h1>调拨管理</h1>
              <p>管理货物调拨单据和流程</p>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="handleAdd" class="modern-button">
            <el-icon><Plus /></el-icon>
            新建调拨单
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form modern-search">
      <div class="search-header">
        <div class="search-title">
          <el-icon><Search /></el-icon>
          <span>筛选条件</span>
        </div>
        <el-button
          text
          @click="handleReset"
          class="reset-button"
          :icon="Refresh"
        >
          重置
        </el-button>
      </div>
      <el-form :model="searchForm" :inline="!isMobile" class="search-form-content" :class="{ 'mobile-form': isMobile }">
        <el-form-item label="单号">
          <el-input
            v-model="searchForm.orderNumber"
            placeholder="请输入调拨单号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="源仓库">
          <el-select v-model="searchForm.sourceWarehouseId" placeholder="请选择源仓库" clearable style="width: 150px">
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标仓库">
          <el-select v-model="searchForm.targetWarehouseId" placeholder="请选择目标仓库" clearable style="width: 150px">
            <el-option
              v-for="warehouse in warehouses"
              :key="warehouse.id"
              :label="warehouse.name"
              :value="warehouse.id"
            />
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
    <div class="data-table modern-table">
      <div class="table-header">
        <div class="table-title">
          <el-icon><Document /></el-icon>
          <span>调拨单列表</span>
        </div>
        <div class="table-actions">
          <el-button
            text
            @click="handleRefresh"
            :icon="Refresh"
            class="refresh-button"
          >
            刷新
          </el-button>
        </div>
      </div>
      <div class="table-content">
        <el-table
          v-loading="loading"
          :data="tableData"
          class="modern-data-table"
          :class="{ 'mobile-table': isMobile }"
          empty-text="暂无调拨单数据"
        >
        <el-table-column prop="orderNumber" label="调拨单号" width="140" />
        <el-table-column prop="sourceWarehouseName" label="源仓库" width="120" />
        <el-table-column prop="targetWarehouseName" label="目标仓库" width="120" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" align="right" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="plannedDate" label="计划日期" width="110" />
        <el-table-column prop="actualDate" label="实际日期" width="110" />
        <el-table-column prop="createdBy" label="创建人" width="100" />
        <el-table-column label="入库时间" width="160">
          <template #default="{ row }">
            <span v-if="row.status === 'EXECUTED'">
              {{ formatDateTime(row.operationTime) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 80 : 320" fixed="right">
          <template #default="{ row }">
            <!-- 桌面端操作按钮 -->
            <div v-if="!isMobile" class="action-buttons desktop-actions">
              <el-button type="primary" size="small" @click="handleView(row)" class="action-btn">
                <el-icon><View /></el-icon>
                查看
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
                v-if="row.status === 'PENDING'"
                type="success"
                size="small"
                @click="handleApprove(row)"
                class="action-btn"
              >
                <el-icon><Check /></el-icon>
                审批
              </el-button>
              <el-button
                v-if="row.status === 'APPROVED'"
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
                @click="handleView(row)"
                class="action-btn"
              >
                <el-icon><Check /></el-icon>
                已执行
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                type="danger"
                size="small"
                @click="handleCancel(row)"
                class="action-btn"
              >
                <el-icon><Close /></el-icon>
                取消
              </el-button>
            </div>

            <!-- 移动端操作按钮 -->
            <div v-else class="mobile-actions">
              <el-dropdown trigger="click" placement="bottom-end">
                <el-button type="primary" size="small" class="mobile-action-trigger">
                  <el-icon><MoreFilled /></el-icon>
                  操作
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleView(row)">
                      <el-icon><View /></el-icon>
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="row.status === 'PENDING'"
                      @click="handleEdit(row)"
                    >
                      <el-icon><Edit /></el-icon>
                      编辑调拨单
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="row.status === 'PENDING'"
                      @click="handleApprove(row)"
                    >
                      <el-icon><Check /></el-icon>
                      审批调拨单
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="row.status === 'APPROVED'"
                      @click="handleExecute(row)"
                    >
                      <el-icon><Position /></el-icon>
                      执行调拨单
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="row.status === 'PENDING'"
                      @click="handleCancel(row)"
                      class="danger-item"
                    >
                      <el-icon><Close /></el-icon>
                      取消调拨单
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
            background
            class="modern-pagination"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :width="isMobile ? '95%' : '1200px'"
      :fullscreen="isMobile"
      class="transfer-dialog modern-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header transfer-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Plus v-if="!form.id" /><Edit v-else /></el-icon>
              </div>
              <div class="title-content">
                <h2>{{ dialogTitle }}</h2>
                <p>{{ form.id ? '修改调拨单信息' : '创建新的调拨单' }}</p>
              </div>
            </div>
            <el-button @click="dialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="调拨单号" prop="orderNumber">
              <el-input v-model="form.orderNumber" placeholder="系统自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="源仓库" prop="sourceWarehouseId">
              <el-select
                v-model="form.sourceWarehouseId"
                placeholder="请选择源仓库"
                style="width: 100%"
                @change="handleSourceWarehouseChange"
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
            <el-form-item label="目标仓库" prop="targetWarehouseId">
              <el-select 
                v-model="form.targetWarehouseId" 
                placeholder="请选择目标仓库" 
                style="width: 100%"
                @change="handleTargetWarehouseChange"
              >
                <el-option
                  v-for="warehouse in warehouses"
                  :key="warehouse.id"
                  :label="warehouse.name"
                  :value="warehouse.id"
                  :disabled="warehouse.id === form.sourceWarehouseId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="计划日期" prop="plannedDate">
              <el-date-picker
                v-model="form.plannedDate"
                type="date"
                placeholder="请选择计划日期"
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
          <el-col :span="8">
            <el-form-item label="紧急程度" prop="priority">
              <el-select v-model="form.priority" placeholder="请选择紧急程度" style="width: 100%">
                <el-option label="普通" value="NORMAL" />
                <el-option label="紧急" value="URGENT" />
                <el-option label="特急" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="调拨原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="2"
            placeholder="请输入调拨原因"
          />
        </el-form-item>

        <!-- 调拨明细 -->
        <div class="detail-section">
          <div class="detail-header">
            <h4>调拨明细</h4>
            <el-button type="primary" size="small" @click="handleAddDetail">
              <el-icon><Plus /></el-icon>
              添加明细
            </el-button>
          </div>
          <el-table :data="form.details" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="货物编码" width="120">
              <template #default="{ row, $index }">
                <el-input
                  v-model="row.goodsCode"
                  :placeholder="form.id ? '货物编码' : '点击选择货物'"
                  readonly
                  style="width: 100%"
                  :disabled="!!form.id"
                  @click="!form.id && openGoodsSelector($index)"
                >
                  <template #suffix v-if="!form.id">
                    <el-icon class="cursor-pointer">
                      <Search />
                    </el-icon>
                  </template>
                </el-input>
              </template>
            </el-table-column>
            <el-table-column label="货物名称" min-width="150">
              <template #default="{ row }">
                <span>{{ row.goodsName || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="规格/型号" width="120">
              <template #default="{ row }">
                <span>{{ row.specification || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="80">
              <template #default="{ row }">
                <span>{{ row.unit || row.goodsUnit || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100" align="right">
              <template #default="{ row }">
                <span>{{ row.unitPrice ? row.unitPrice.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="源库存" width="100" align="right">
              <template #default="{ row }">
                <span :class="getStockClass(row)">{{ formatQuantity(row.sourceStock) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="调拨数量" width="160">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.quantity"
                  :min="0"
                  :max="row.sourceStock || 0"
                  :precision="0"
                  size="default"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="备注" width="120">
              <template #default="{ row }">
                <el-input v-model="row.remark" placeholder="备注" />
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
            <span>总数量：{{ totalQuantity }}</span>
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
      :width="isMobile ? '95%' : '1000px'"
      :fullscreen="isMobile"
      class="transfer-detail-dialog modern-dialog"
      :show-close="false"
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header transfer-detail-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><View /></el-icon>
              </div>
              <div class="title-content">
                <h2>调拨单详情</h2>
                <p>查看调拨单的详细信息和货物清单</p>
              </div>
            </div>
            <el-button @click="viewDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <div class="detail-view" v-if="viewData">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="调拨单号">{{ viewData.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="源仓库">{{ viewData.sourceWarehouseName }}</el-descriptions-item>
          <el-descriptions-item label="目标仓库">{{ viewData.targetWarehouseName }}</el-descriptions-item>
          <el-descriptions-item label="计划日期">{{ viewData.plannedDate }}</el-descriptions-item>
          <el-descriptions-item label="实际日期">{{ viewData.actualDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="getPriorityType(viewData.priority)">
              {{ getPriorityText(viewData.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参考单号">{{ viewData.referenceNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(viewData.status)">
              {{ getStatusText(viewData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ viewData.createdBy }}</el-descriptions-item>
          <el-descriptions-item label="调拨原因" :span="3">{{ viewData.reason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 明细信息 -->
        <div class="detail-table" style="margin-top: 20px;">
          <h4>调拨明细</h4>
          <el-table :data="viewData.details" border>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="goodsCode" label="货物编码" width="120" />
            <el-table-column prop="goodsName" label="货物名称" min-width="150" />
            <el-table-column prop="specification" label="规格/型号" width="120" />
            <el-table-column label="单位" width="80">
              <template #default="scope">
                {{ scope.row.unit || scope.row.goodsUnit || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="scope">
                {{ scope.row.unitPrice ? scope.row.unitPrice.toFixed(2) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="调拨数量" width="100" align="right" />
            <el-table-column prop="remark" label="备注" min-width="120" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :width="isMobile ? '95%' : '600px'"
      :fullscreen="isMobile"
      class="transfer-approval-dialog modern-dialog"
      :show-close="false"
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header transfer-approval-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="title-content">
                <h2>审批调拨单</h2>
                <p>审核调拨单申请并给出审批意见</p>
              </div>
            </div>
            <el-button @click="approvalDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
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
      :width="isMobile ? '95%' : '700px'"
      :fullscreen="isMobile"
      class="transfer-warning-dialog modern-dialog"
      :show-close="false"
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header transfer-warning-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="title-content">
                <h2>库存不足提示</h2>
                <p>以下货物在源仓库库存不足，请检查后重新提交</p>
              </div>
            </div>
            <el-button @click="stockWarningVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <div class="stock-warning">
        <el-alert
          title="以下货物在源仓库库存不足，请检查后重新提交"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-table :data="stockWarnings" style="margin-top: 16px;">
          <el-table-column prop="goodsName" label="货物名称" />
          <el-table-column prop="requestQuantity" label="申请数量" align="right" />
          <el-table-column prop="sourceStock" label="源仓库库存" align="right" />
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
      :width="isMobile ? '95%' : '1000px'"
      :fullscreen="isMobile"
      class="transfer-goods-dialog modern-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header transfer-goods-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="title-content">
                <h2>选择货物</h2>
                <p>选择需要调拨的货物并设置数量</p>
              </div>
            </div>
            <el-button @click="goodsSelectorVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <!-- 筛选条件 -->
      <div class="goods-filter">
        <el-form :model="goodsFilter" inline>
          <el-form-item label="货物编码">
            <el-input v-model="goodsFilter.code" placeholder="请输入货物编码" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item label="货物名称">
            <el-input v-model="goodsFilter.name" placeholder="请输入货物名称" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="filterGoods">搜索</el-button>
            <el-button @click="resetGoodsFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 货物列表 -->
      <el-table
        ref="goodsTableRef"
        :data="filteredInventoryGoodsList"
        @selection-change="handleGoodsSelectionChange"
        max-height="400"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="code" label="货物编码" width="120" />
        <el-table-column prop="name" label="货物名称" min-width="200" />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="availableStock" label="可用库存" width="100" align="right">
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
import { Plus, Delete, View, Edit, Check, Close, Search, Warning, Box, Position, Refresh, MoreFilled } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

// 响应式数据
const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)

// 移动端适配
const isMobile = ref(false)

// 检查是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 监听窗口大小变化
const handleResize = () => {
  checkMobile()
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
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
const stockWarnings = ref([])
const formRef = ref()

// 货物选择弹出框相关
const goodsSelectorVisible = ref(false)
const goodsTableRef = ref()
const selectedGoods = ref([])
const currentDetailIndex = ref(-1)
const inventoryGoodsList = ref([])
const filteredInventoryGoodsList = ref([])
const goodsFilter = reactive({
  code: '',
  name: '',
  specification: ''
})

// 搜索表单
const searchForm = reactive({
  orderNumber: '',
  sourceWarehouseId: null,
  targetWarehouseId: null,
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
  sourceWarehouseId: null,
  targetWarehouseId: null,
  plannedDate: '',
  referenceNumber: '',
  priority: 'NORMAL',
  reason: '',
  totalQuantity: 0,
  details: []
})

// 审批表单
const approvalForm = reactive({
  status: 'APPROVED',
  approvalRemark: ''
})

// 表单验证规则
const formRules = {
  sourceWarehouseId: [
    { required: true, message: '请选择源仓库', trigger: 'change' }
  ],
  targetWarehouseId: [
    { required: true, message: '请选择目标仓库', trigger: 'change' }
  ],
  plannedDate: [
    { required: true, message: '请选择计划日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入调拨原因', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑调拨单' : '新建调拨单'
})

const totalQuantity = computed(() => {
  return form.details.reduce((sum, item) => {
    const quantity = parseFloat(item.quantity) || 0
    return sum + quantity
  }, 0)
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

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'EXECUTED': '已执行',
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
    'CANCELLED': 'info',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getPriorityText = (priority) => {
  const priorityMap = {
    'NORMAL': '普通',
    'URGENT': '紧急',
    'CRITICAL': '特急'
  }
  return priorityMap[priority] || priority
}

const getPriorityType = (priority) => {
  const typeMap = {
    'NORMAL': 'info',
    'URGENT': 'warning',
    'CRITICAL': 'danger'
  }
  return typeMap[priority] || 'info'
}

const getStockClass = (row) => {
  // 对于货物选择弹出框中的行，使用availableStock
  const stock = row.availableStock !== undefined ? row.availableStock : row.sourceStock
  if (!stock || stock === 0) return 'text-danger'
  if (row.quantity && row.quantity > stock) return 'text-warning'
  return 'text-success'
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      orderNumber: searchForm.orderNumber,
      sourceWarehouseId: searchForm.sourceWarehouseId,
      targetWarehouseId: searchForm.targetWarehouseId,
      status: searchForm.status
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await request.get('/transfer-orders', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载调拨单数据失败:', error)
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
    warehouses.value = []
  }
}

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

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    orderNumber: '',
    sourceWarehouseId: null,
    targetWarehouseId: null,
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
    const response = await request.get('/transfer-orders/generate-order-number')
    if (response.success) {
      form.orderNumber = response.data
    }
  } catch (error) {
    form.orderNumber = 'TR' + Date.now()
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    resetForm()

    // 通过API获取完整的调拨单详情
    const response = await request.get(`/transfer-orders/${row.id}`)
    if (response.success) {
      Object.assign(form, {
        ...response.data,
        details: response.data.details || []
      })

      // 加载源仓库的库存信息
      await loadWarehouseInventoryGoods()

      // 为明细项补充库存信息
      await updateDetailsStockInfo()

      dialogVisible.value = true
    } else {
      ElMessage.error('获取调拨单详情失败')
    }
  } catch (error) {
    console.error('获取调拨单详情失败:', error)
    ElMessage.error('获取调拨单详情失败')
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
    await ElMessageBox.confirm('确定要执行该调拨单吗？执行后将进行库存转移。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.put(`/transfer-orders/${row.id}/execute`)
    if (response.success) {
      ElMessage.success('调拨单执行成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('执行调拨单失败:', error)
    }
  }
}

const handleCancel = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消调拨单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入取消原因'
    })
    
    const response = await request.put(`/transfer-orders/${row.id}/cancel`, { reason })
    if (response.success) {
      ElMessage.success('调拨单取消成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消调拨单失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (form.details.length === 0) {
      ElMessage.error('请添加调拨明细')
      return
    }
    
    if (form.sourceWarehouseId === form.targetWarehouseId) {
      ElMessage.error('源仓库和目标仓库不能相同')
      return
    }
    
    // 检查库存
    const stockCheck = await checkStock()
    if (!stockCheck) {
      return
    }
    
    // 更新总数量
    form.totalQuantity = totalQuantity.value
    
    submitLoading.value = true
    const url = form.id ? `/transfer-orders/${form.id}` : '/transfer-orders'
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
    const response = await request.put(`/transfer-orders/${currentRow.value.id}/approve`, approvalForm)
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
  // 检查是否已选择源仓库
  if (!form.sourceWarehouseId) {
    ElMessage.warning('请先选择源仓库')
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

const handleSourceWarehouseChange = () => {
  // 清空现有明细
  if (form.details.length > 0) {
    ElMessageBox.confirm(
      '更换源仓库将清空已添加的调拨明细，是否继续？',
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

const handleTargetWarehouseChange = () => {
  // 可以在这里处理目标仓库变化的逻辑
}



const checkStock = async () => {
  const warnings = []
  
  for (const detail of form.details) {
    if (detail.quantity > detail.sourceStock) {
      warnings.push({
        goodsName: detail.goodsName,
        requestQuantity: detail.quantity,
        sourceStock: detail.sourceStock,
        shortage: detail.quantity - detail.sourceStock
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
    sourceWarehouseId: null,
    targetWarehouseId: null,
    plannedDate: '',
    referenceNumber: '',
    priority: 'NORMAL',
    reason: '',
    totalQuantity: 0,
    details: []
  })
  formRef.value?.resetFields()

  // 为非管理员用户重新设置默认仓库
  initializeDefaultWarehouse()
}

// 为明细项补充库存信息（编辑模式使用）
const updateDetailsStockInfo = async () => {
  if (!form.sourceWarehouseId || !form.details.length) {
    return
  }

  try {
    console.log('正在为明细项补充库存信息...')
    for (const detail of form.details) {
      if (detail.goodsId) {
        try {
          const stockResponse = await request.get(`/inventory/goods/${detail.goodsId}/stock-info?warehouseId=${form.sourceWarehouseId}`)
          if (stockResponse.success) {
            detail.sourceStock = stockResponse.data.availableStock || 0
            // 如果没有单价信息，使用加权平均价
            if (!detail.unitPrice) {
              detail.unitPrice = stockResponse.data.weightedAveragePrice || 0
            }
            console.log(`货物 ${detail.goodsName} 库存信息已更新:`, detail.sourceStock)
          }
        } catch (error) {
          console.error(`获取货物${detail.goodsId}库存信息失败:`, error)
          detail.sourceStock = 0
        }
      }
    }
  } catch (error) {
    console.error('更新明细库存信息失败:', error)
  }
}

// 加载指定仓库的库存货物列表
const loadWarehouseInventoryGoods = async () => {
  if (!form.sourceWarehouseId) {
    filteredInventoryGoodsList.value = []
    return
  }

  try {
    console.log('正在加载仓库库存货物，仓库ID:', form.sourceWarehouseId)
    const response = await request.get(`/inventory/goods-with-stock?warehouseId=${form.sourceWarehouseId}`)
    console.log('货物列表响应:', response)

    if (response.success) {
      console.log('开始为每个货物加载库存信息...')
      // 为每个货物添加库存信息
      const goodsWithStock = []

      for (const goods of response.data || []) {
        try {
          console.log(`正在获取货物 ${goods.name}(ID:${goods.id}) 的库存信息`)
          const stockResponse = await request.get(`/inventory/goods/${goods.id}/stock-info?warehouseId=${form.sourceWarehouseId}`)
          console.log(`货物 ${goods.name} 库存响应:`, stockResponse)

          if (stockResponse.success) {
            const goodsWithStockInfo = {
              ...goods,
              availableStock: stockResponse.data.availableStock || 0,
              weightedAveragePrice: stockResponse.data.weightedAveragePrice || 0
            }
            console.log(`货物 ${goods.name} 库存信息:`, goodsWithStockInfo)

            // 只添加有库存的货物
            if (goodsWithStockInfo.availableStock > 0) {
              goodsWithStock.push(goodsWithStockInfo)
            }
          } else {
            console.warn(`获取货物 ${goods.name} 库存信息失败:`, stockResponse.message)
          }
        } catch (error) {
          console.error(`获取货物${goods.id}库存信息失败:`, error)
        }
      }

      console.log('最终货物列表:', goodsWithStock)
      inventoryGoodsList.value = goodsWithStock
      filteredInventoryGoodsList.value = goodsWithStock
    } else {
      inventoryGoodsList.value = []
      filteredInventoryGoodsList.value = []
    }
  } catch (error) {
    console.error('加载仓库库存货物失败:', error)
    inventoryGoodsList.value = []
    filteredInventoryGoodsList.value = []
  }
}

// 打开货物选择弹出框
const openGoodsSelector = (index) => {
  // 检查是否已选择源仓库
  if (!form.sourceWarehouseId) {
    ElMessage.warning('请先选择源仓库')
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
  filteredInventoryGoodsList.value = inventoryGoodsList.value.filter(goods => {
    return (!goodsFilter.code || goods.code.toLowerCase().includes(goodsFilter.code.toLowerCase())) &&
           (!goodsFilter.name || goods.name.toLowerCase().includes(goodsFilter.name.toLowerCase())) &&
           (!goodsFilter.specification || (goods.specification && goods.specification.toLowerCase().includes(goodsFilter.specification.toLowerCase())))
  })
}

// 重置货物筛选
const resetGoodsFilter = () => {
  Object.assign(goodsFilter, {
    code: '',
    name: '',
    specification: ''
  })
  // 如果已选择源仓库，重新加载该仓库的货物；否则显示所有货物
  if (form.sourceWarehouseId) {
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

  if (currentDetailIndex.value >= 0) {
    // 单个明细修改模式
    const detail = form.details[currentDetailIndex.value]
    const goods = selectedGoods.value[0]
    detail.goodsId = goods.id
    detail.goodsCode = goods.code
    detail.goodsName = goods.name
    detail.unit = goods.unit
    detail.specification = goods.specification || goods.model || '-'
    detail.unitPrice = goods.weightedAveragePrice || 0
    detail.sourceStock = goods.availableStock || 0
    detail.quantity = 0
  } else {
    // 批量添加模式
    selectedGoods.value.forEach(goods => {
      const detail = {
        goodsId: goods.id,
        goodsCode: goods.code,
        goodsName: goods.name,
        unit: goods.unit,
        specification: goods.specification || goods.model || '-',
        unitPrice: goods.weightedAveragePrice || 0,
        quantity: 0,
        sourceStock: goods.availableStock || 0,
        remark: ''
      }
      form.details.push(detail)
    })
  }

  goodsSelectorVisible.value = false
}

// 初始化默认仓库
const initializeDefaultWarehouse = () => {
  if (!userStore.isAdmin && userStore.warehouses && userStore.warehouses.length > 0) {
    // 设置搜索表单的默认源仓库
    searchForm.sourceWarehouseId = userStore.warehouses[0].id
    // 设置新建表单的默认源仓库
    form.sourceWarehouseId = userStore.warehouses[0].id
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
/* 现代化容器样式 */
.modern-container {
  background: #f8fafc;
  min-height: 100vh;
  padding: 24px;
}

/* 现代化头部样式 */
.modern-header {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;
  }

  .page-title {
    display: flex;
    align-items: center;
    gap: 16px;

    .title-icon {
      width: 48px;
      height: 48px;
      background: linear-gradient(135deg, #06b6d4, #0891b2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 24px;
    }

    .title-content {
      h1 {
        margin: 0;
        font-size: 28px;
        font-weight: 700;
        color: #1e293b;
        line-height: 1.2;
      }

      p {
        margin: 4px 0 0 0;
        font-size: 16px;
        color: #64748b;
        font-weight: 500;
      }
    }
  }

  .header-right {
    .modern-button {
      background: linear-gradient(135deg, #06b6d4, #0891b2);
      border: none;
      border-radius: 12px;
      padding: 12px 24px;
      font-weight: 600;
      font-size: 16px;
      box-shadow: 0 4px 12px rgba(6, 182, 212, 0.3);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(6, 182, 212, 0.4);
      }
    }
  }
}

/* 现代化搜索表单样式 */
.modern-search {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;

  .search-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e2e8f0;

    .search-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;

      .el-icon {
        color: #06b6d4;
        font-size: 20px;
      }
    }
  }

  .search-form-content {
    .el-form-item {
      margin-bottom: 16px;
      margin-right: 16px;

      .el-form-item__label {
        font-weight: 600;
        color: #374151;
      }
    }

    /* PC端横向布局 */
    &:not(.mobile-form) {
      .el-form-item {
        display: inline-block;
        vertical-align: top;
      }
    }
  }

  /* 移动端竖向布局 */
  &.mobile-form {
    .search-form-content {
      .el-form-item {
        display: block;
        margin-bottom: 20px;
        margin-right: 0;

        .el-form-item__label {
          width: 100% !important;
          margin-bottom: 8px;
          text-align: left !important;
        }

        .el-form-item__content {
          margin-left: 0 !important;
          width: 100%;
        }
      }
    }
  }
}

/* 现代化表格样式 */
.modern-table {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e2e8f0;

    .table-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;

      .el-icon {
        color: #06b6d4;
        font-size: 20px;
      }
    }
  }

  .table-content {
    .modern-data-table {
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid #e2e8f0;

      .el-table__header {
        background: #f8fafc;

        th {
          background: #f8fafc !important;
          color: #374151;
          font-weight: 600;
          border-bottom: 1px solid #e2e8f0;
        }
      }
    }
  }
}

/* 移动端操作栏样式 */
.mobile-actions {
  display: flex;
  justify-content: center;
  width: 100%;

  .mobile-action-trigger {
    background: linear-gradient(135deg, #3b82f6, #1d4ed8);
    border: none;
    border-radius: 8px;
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 600;
    color: white;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
    }

    .el-icon {
      margin-right: 4px;
      font-size: 14px;
    }
  }
}

.desktop-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;

  .action-btn {
    border-radius: 6px;
    font-size: 12px;
    padding: 4px 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }
}

/* 下拉菜单样式 */
.el-dropdown-menu {
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border: 1px solid #e2e8f0;
  padding: 8px 0;

  .el-dropdown-menu__item {
    padding: 12px 16px;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    transition: all 0.3s ease;
    border-radius: 8px;
    margin: 2px 8px;

    &:hover {
      background: #f8fafc;
      color: #1e293b;
    }

    .el-icon {
      margin-right: 8px;
      font-size: 16px;
      color: #6b7280;
    }

    &.danger-item {
      color: #dc2626;

      &:hover {
        background: #fef2f2;
        color: #dc2626;
      }

      .el-icon {
        color: #dc2626;
      }
    }
  }
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

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  .el-button {
    margin: 0;
  }
}

.text-warning {
  color: #e6a23c;
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

.goods-filter {
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
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
</style>

<!-- 全局样式 - 调拨单对话框 -->
<style>
/* 调拨单新增/编辑对话框样式 */
.transfer-dialog .transfer-header {
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
  position: relative !important;
}

.transfer-dialog .transfer-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.transfer-dialog .transfer-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.transfer-dialog .transfer-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.transfer-dialog .transfer-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.transfer-dialog .transfer-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.transfer-dialog .transfer-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.transfer-dialog .transfer-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.transfer-dialog .transfer-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 调拨单详情对话框样式 */
.transfer-detail-dialog .transfer-detail-header {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
  position: relative !important;
}

.transfer-detail-dialog .transfer-detail-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.transfer-detail-dialog .transfer-detail-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.transfer-detail-dialog .transfer-detail-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.transfer-detail-dialog .transfer-detail-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.transfer-detail-dialog .transfer-detail-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.transfer-detail-dialog .transfer-detail-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.transfer-detail-dialog .transfer-detail-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.transfer-detail-dialog .transfer-detail-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 调拨单审批对话框样式 */
.transfer-approval-dialog .transfer-approval-header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
  position: relative !important;
}

.transfer-approval-dialog .transfer-approval-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.transfer-approval-dialog .transfer-approval-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.transfer-approval-dialog .transfer-approval-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.transfer-approval-dialog .transfer-approval-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.transfer-approval-dialog .transfer-approval-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.transfer-approval-dialog .transfer-approval-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.transfer-approval-dialog .transfer-approval-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.transfer-approval-dialog .transfer-approval-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 调拨单库存警告对话框样式 */
.transfer-warning-dialog .transfer-warning-header {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
  position: relative !important;
}

.transfer-warning-dialog .transfer-warning-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.transfer-warning-dialog .transfer-warning-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.transfer-warning-dialog .transfer-warning-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.transfer-warning-dialog .transfer-warning-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.transfer-warning-dialog .transfer-warning-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.transfer-warning-dialog .transfer-warning-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.transfer-warning-dialog .transfer-warning-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.transfer-warning-dialog .transfer-warning-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 调拨单货物选择对话框样式 */
.transfer-goods-dialog .transfer-goods-header {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
  position: relative !important;
}

.transfer-goods-dialog .transfer-goods-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.transfer-goods-dialog .transfer-goods-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.transfer-goods-dialog .transfer-goods-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.transfer-goods-dialog .transfer-goods-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.transfer-goods-dialog .transfer-goods-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.transfer-goods-dialog .transfer-goods-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.transfer-goods-dialog .transfer-goods-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.transfer-goods-dialog .transfer-goods-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .transfer-dialog .transfer-header,
  .transfer-detail-dialog .transfer-detail-header,
  .transfer-approval-dialog .transfer-approval-header,
  .transfer-warning-dialog .transfer-warning-header,
  .transfer-goods-dialog .transfer-goods-header {
    padding: 20px 16px !important;
    margin: -20px -16px 0 -16px !important;
  }

  .transfer-dialog .transfer-header .title-icon,
  .transfer-detail-dialog .transfer-detail-header .title-icon,
  .transfer-approval-dialog .transfer-approval-header .title-icon,
  .transfer-warning-dialog .transfer-warning-header .title-icon,
  .transfer-goods-dialog .transfer-goods-header .title-icon {
    width: 40px !important;
    height: 40px !important;
  }

  .transfer-dialog .transfer-header .title-icon .el-icon,
  .transfer-detail-dialog .transfer-detail-header .title-icon .el-icon,
  .transfer-approval-dialog .transfer-approval-header .title-icon .el-icon,
  .transfer-warning-dialog .transfer-warning-header .title-icon .el-icon,
  .transfer-goods-dialog .transfer-goods-header .title-icon .el-icon {
    font-size: 20px !important;
  }

  .transfer-dialog .transfer-header .title-content h2,
  .transfer-detail-dialog .transfer-detail-header .title-content h2,
  .transfer-approval-dialog .transfer-approval-header .title-content h2,
  .transfer-warning-dialog .transfer-warning-header .title-content h2,
  .transfer-goods-dialog .transfer-goods-header .title-content h2 {
    font-size: 20px !important;
  }
}
</style>
