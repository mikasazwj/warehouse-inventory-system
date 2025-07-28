<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">盘点管理</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建盘点单
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="单号">
          <el-input
            v-model="searchForm.orderNumber"
            placeholder="请输入盘点单号"
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
          <el-select v-model="searchForm.stocktakeType" placeholder="请选择类型" clearable style="width: 120px">
            <el-option label="全盘" value="FULL" />
            <el-option label="抽盘" value="PARTIAL" />
            <el-option label="循环盘点" value="CYCLE" />
            <el-option label="动态盘点" value="DYNAMIC" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待执行" value="PENDING" />
            <el-option label="盘点中" value="IN_PROGRESS" />
            <el-option label="待审批" value="COMPLETED" />
            <el-option label="已审批" value="APPROVED" />
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
        <el-table-column prop="orderNumber" label="盘点单号" width="140" />
        <el-table-column prop="warehouseName" label="仓库" width="120" />
        <el-table-column label="盘点类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getStocktakeTypeColor(row.stocktakeType)">
              {{ getStocktakeTypeText(row.stocktakeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalItems" label="盘点项目" width="100" align="right" />
        <el-table-column prop="completedItems" label="已盘点" width="100" align="right" />
        <el-table-column prop="differenceItems" label="差异项目" width="100" align="right">
          <template #default="{ row }">
            <span :class="row.differenceItems > 0 ? 'text-warning' : ''">
              {{ row.differenceItems }}
            </span>
          </template>
        </el-table-column>
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
              <el-button 
                v-if="row.status === 'PENDING'" 
                type="text" 
                size="small" 
                @click="handleEdit(row)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                v-if="row.status === 'APPROVED'"
                type="text"
                size="small"
                class="text-primary"
                @click="handleStart(row)"
              >
                <el-icon><VideoPlay /></el-icon>
                开始盘点
              </el-button>
              <el-button 
                v-if="row.status === 'IN_PROGRESS'" 
                type="text" 
                size="small" 
                class="text-success"
                @click="handleComplete(row)"
              >
                <el-icon><Check /></el-icon>
                完成盘点
              </el-button>
              <el-button
                v-if="row.status === 'PENDING'"
                type="text"
                size="small"
                class="text-warning"
                @click="handleApprove(row)"
              >
                <el-icon><CircleCheck /></el-icon>
                审批
              </el-button>
              <el-button 
                v-if="['PENDING', 'IN_PROGRESS'].includes(row.status)" 
                type="text" 
                size="small" 
                class="text-danger"
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
            <el-form-item label="盘点单号" prop="orderNumber">
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
            <el-form-item label="盘点类型" prop="stocktakeType">
              <el-select 
                v-model="form.stocktakeType" 
                placeholder="请选择类型" 
                style="width: 100%"
                @change="handleTypeChange"
              >
                <el-option label="全盘" value="FULL" />
                <el-option label="抽盘" value="PARTIAL" />
                <el-option label="循环盘点" value="CYCLE" />
                <el-option label="动态盘点" value="DYNAMIC" />
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
            <el-form-item label="盘点人员" prop="stocktakeUserNames">
              <el-input
                v-model="form.stocktakeUserNames"
                placeholder="请输入盘点人员姓名，多人用逗号分隔"
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
        <el-form-item label="盘点说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入盘点说明"
          />
        </el-form-item>

        <!-- 盘点范围 -->
        <div class="detail-section">
          <div class="detail-header">
            <h4>{{ form.stocktakeType === 'FULL' ? '盘点货物' : '盘点范围' }}</h4>
            <div class="range-actions">
              <el-button type="primary" size="small" @click="handleAddRange">
                <el-icon><Plus /></el-icon>
                添加货物
              </el-button>
              <el-button type="success" size="small" @click="handleBatchAdd">
                <el-icon><DocumentAdd /></el-icon>
                批量添加
              </el-button>
            </div>
          </div>
          <el-table :data="form.ranges" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="货物选择" min-width="200">
              <template #default="{ row, $index }">
                <div v-if="row.goodsId">
                  <div>{{ row.goodsCode }} - {{ row.goodsName }}</div>
                  <el-button type="text" size="small" @click="handleSelectGoodsForRow($index)">
                    重新选择
                  </el-button>
                </div>
                <el-button v-else type="primary" size="small" @click="handleSelectGoodsForRow($index)">
                  选择货物
                </el-button>
              </template>
            </el-table-column>
            <el-table-column label="规格/型号" width="120">
              <template #default="{ row }">
                <span>{{ row.specification || row.model || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="账面库存" width="100" align="right">
              <template #default="{ row }">
                <span>{{ row.bookQuantity || 0 }}</span>
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
                  @click="handleRemoveRange($index)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>

      <!-- 已生成的盘点明细（编辑时显示） -->
      <div v-if="form.id && stocktakeDetails.length > 0" class="mt-4">
        <el-divider content-position="left">已生成的盘点明细（可删除不需要的项目）</el-divider>
        <el-table :data="stocktakeDetails" border max-height="300">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="goodsCode" label="货物编码" width="120" />
          <el-table-column prop="goodsName" label="货物名称" min-width="150" />
          <el-table-column prop="goodsSpecification" label="规格型号" width="120" />
          <el-table-column prop="goodsUnit" label="单位" width="80" />
          <el-table-column label="账面库存" width="100" align="right">
            <template #default="{ row }">
              {{ formatQuantity(row.bookQuantity) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row, $index }">
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
      title="盘点单详情"
      width="1200px"
    >
      <div class="detail-view" v-if="viewData">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="3" border>
          <el-descriptions-item label="盘点单号">{{ viewData.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ viewData.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="盘点类型">
            <el-tag :type="getStocktakeTypeColor(viewData.stocktakeType)">
              {{ getStocktakeTypeText(viewData.stocktakeType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="计划日期">{{ viewData.plannedDate }}</el-descriptions-item>
          <el-descriptions-item label="实际日期">{{ viewData.actualDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(viewData.status)">
              {{ getStatusText(viewData.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="盘点人员">{{ viewData.stocktakeUserNames || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参考单号">{{ viewData.referenceNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ viewData.createdBy }}</el-descriptions-item>
          <el-descriptions-item label="盘点说明" :span="3">{{ viewData.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 盘点明细 -->
        <div class="detail-table" style="margin-top: 20px;">
          <div class="detail-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h4>{{ viewData.status === 'IN_PROGRESS' ? '盘点录入' : '盘点结果' }}</h4>
            <div v-if="viewData.status === 'IN_PROGRESS'">
              <el-button type="primary" size="small" @click="handleBatchSave">
                <el-icon><DocumentAdd /></el-icon>
                批量保存
              </el-button>
              <el-button type="success" size="small" @click="handleRefreshDetails">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
            </div>
          </div>

          <!-- 盘点中状态：可编辑表格 -->
          <el-table v-if="viewData.status === 'IN_PROGRESS'" :data="stocktakeDetails" border>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="goodsCode" label="货物编码" width="120" />
            <el-table-column prop="goodsName" label="货物名称" min-width="150" />
            <el-table-column prop="goodsSpecification" label="规格型号" width="120" />
            <el-table-column prop="goodsUnit" label="单位" width="80" />
            <el-table-column label="账面库存" width="100" align="right">
              <template #default="{ row }">
                {{ formatQuantity(row.bookQuantity) }}
              </template>
            </el-table-column>
            <el-table-column label="实盘数量" width="140" align="right">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.actualQuantity"
                  :min="0"
                  :precision="0"
                  size="default"
                  style="width: 100%"
                  @change="handleActualQuantityChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="差异数量" width="100" align="right">
              <template #default="{ row }">
                <span :class="getDifferenceClass(row)">
                  {{ calculateDifference(row) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template #default="{ row }">
                <el-input v-model="row.remark" size="small" placeholder="备注" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button
                  type="text"
                  size="small"
                  class="text-primary"
                  @click="handleSaveDetail(row)"
                  :loading="row.saving"
                >
                  保存
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 其他状态：只读表格 -->
          <el-table v-else :data="stocktakeDetails" border>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="goodsCode" label="货物编码" width="120" />
            <el-table-column prop="goodsName" label="货物名称" min-width="150" />
            <el-table-column prop="goodsSpecification" label="规格型号" width="120" />
            <el-table-column prop="goodsUnit" label="单位" width="80" />
            <el-table-column label="账面库存" width="100" align="right">
              <template #default="{ row }">
                {{ formatQuantity(row.bookQuantity) }}
              </template>
            </el-table-column>
            <el-table-column label="实盘数量" width="100" align="right">
              <template #default="{ row }">
                {{ formatQuantity(row.actualQuantity) }}
              </template>
            </el-table-column>
            <el-table-column label="差异数量" width="100" align="right">
              <template #default="{ row }">
                <span :class="getDifferenceClass(row)">
                  {{ formatQuantity((row.actualQuantity || 0) - (row.bookQuantity || 0)) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" />
          </el-table>
        </div>

        <!-- 差异分析 -->
        <div class="difference-analysis" style="margin-top: 20px;" v-if="viewData.status === 'EXECUTED'">
          <h4>差异分析</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="analysis-card gain">
                <div class="card-header">
                  <el-icon><TrendCharts /></el-icon>
                  <span>盘盈</span>
                </div>
                <div class="card-value">{{ viewData.gainItems || 0 }}</div>
                <div class="card-desc">项目数量</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="analysis-card loss">
                <div class="card-header">
                  <el-icon><TrendCharts /></el-icon>
                  <span>盘亏</span>
                </div>
                <div class="card-value">{{ viewData.lossItems || 0 }}</div>
                <div class="card-desc">项目数量</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="analysis-card normal">
                <div class="card-header">
                  <el-icon><CircleCheck /></el-icon>
                  <span>正常</span>
                </div>
                <div class="card-value">{{ viewData.normalItems || 0 }}</div>
                <div class="card-desc">项目数量</div>
              </div>
            </el-col>
          </el-row>

          <!-- 查看详细报告按钮 -->
          <div style="text-align: center; margin-top: 20px;">
            <el-button type="primary" @click="handleViewReport(viewData.id)">
              <el-icon><Document /></el-icon>
              查看详细盘点报告
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 盘点报告对话框 -->
    <el-dialog
      v-model="reportDialogVisible"
      title="盘点分析报告"
      width="1200px"
      :close-on-click-modal="false"
    >
      <div v-if="reportData" class="report-container">
        <!-- 报告头部信息 -->
        <div class="report-header">
          <h3>{{ reportData.orderNumber }} - 盘点分析报告</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="仓库">{{ reportData.warehouseName }}</el-descriptions-item>
            <el-descriptions-item label="盘点类型">
              <el-tag :type="getStocktakeTypeColor(reportData.stocktakeType)">
                {{ getStocktakeTypeText(reportData.stocktakeType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(reportData.status)">
                {{ getStatusText(reportData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="计划日期">{{ reportData.plannedDate }}</el-descriptions-item>
            <el-descriptions-item label="实际日期">{{ reportData.actualDate }}</el-descriptions-item>
            <el-descriptions-item label="完成人">{{ reportData.completedBy }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 统计概览 -->
        <div class="report-statistics" style="margin: 20px 0;">
          <h4>统计概览</h4>
          <el-row :gutter="20">
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ reportData.totalItems || 0 }}</div>
                <div class="stat-label">总项目数</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ reportData.completedItems || 0 }}</div>
                <div class="stat-label">已盘点</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card gain">
                <div class="stat-value">{{ reportData.gainItems || 0 }}</div>
                <div class="stat-label">盘盈项目</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card loss">
                <div class="stat-value">{{ reportData.lossItems || 0 }}</div>
                <div class="stat-label">盘亏项目</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card normal">
                <div class="stat-value">{{ reportData.normalItems || 0 }}</div>
                <div class="stat-label">正常项目</div>
              </div>
            </el-col>
            <el-col :span="4">
              <div class="stat-card">
                <div class="stat-value">{{ reportData.differenceItems || 0 }}</div>
                <div class="stat-label">差异项目</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 明细分析 -->
        <div class="report-details">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="盘盈明细" name="gain">
              <el-table :data="reportData.gainDetails" border>
                <el-table-column prop="goodsCode" label="货物编码" width="120" />
                <el-table-column prop="goodsName" label="货物名称" min-width="150" />
                <el-table-column prop="goodsSpecification" label="规格型号" width="120" />
                <el-table-column prop="goodsUnit" label="单位" width="80" />
                <el-table-column prop="bookQuantity" label="账面数量" width="100" align="right" />
                <el-table-column prop="actualQuantity" label="实盘数量" width="100" align="right" />
                <el-table-column prop="differenceQuantity" label="盘盈数量" width="100" align="right">
                  <template #default="{ row }">
                    <span class="text-success">+{{ row.differenceQuantity }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="120" />
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="盘亏明细" name="loss">
              <el-table :data="reportData.lossDetails" border>
                <el-table-column prop="goodsCode" label="货物编码" width="120" />
                <el-table-column prop="goodsName" label="货物名称" min-width="150" />
                <el-table-column prop="goodsSpecification" label="规格型号" width="120" />
                <el-table-column prop="goodsUnit" label="单位" width="80" />
                <el-table-column prop="bookQuantity" label="账面数量" width="100" align="right" />
                <el-table-column prop="actualQuantity" label="实盘数量" width="100" align="right" />
                <el-table-column prop="differenceQuantity" label="盘亏数量" width="100" align="right">
                  <template #default="{ row }">
                    <span class="text-danger">{{ row.differenceQuantity }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="120" />
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="正常明细" name="normal">
              <el-table :data="reportData.normalDetails" border>
                <el-table-column prop="goodsCode" label="货物编码" width="120" />
                <el-table-column prop="goodsName" label="货物名称" min-width="150" />
                <el-table-column prop="goodsSpecification" label="规格型号" width="120" />
                <el-table-column prop="goodsUnit" label="单位" width="80" />
                <el-table-column prop="bookQuantity" label="账面数量" width="100" align="right" />
                <el-table-column prop="actualQuantity" label="实盘数量" width="100" align="right" />
                <el-table-column prop="differenceQuantity" label="差异数量" width="100" align="right">
                  <template #default="{ row }">
                    <span>{{ row.differenceQuantity || 0 }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="120" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <template #footer>
        <el-button @click="reportDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleExportReport">导出报告</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      title="审批盘点单"
      width="500px"
    >
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approvalForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否调整库存" v-if="approvalForm.status === 'APPROVED'">
          <el-switch
            v-model="approvalForm.adjustInventory"
            active-text="是"
            inactive-text="否"
          />
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

    <!-- 批量添加对话框 -->
    <el-dialog
      v-model="batchAddVisible"
      title="选择盘点货物"
      width="1000px"
    >
      <div class="batch-add-content">
        <div class="filter-section">
          <el-form inline>
            <el-form-item label="分类">
              <el-select v-model="batchFilter.categoryId" placeholder="请选择分类" clearable>
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="库存状态">
              <el-select v-model="batchFilter.onlyWithStock" placeholder="请选择">
                <el-option label="全部货物" :value="false" />
                <el-option label="仅有库存" :value="true" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadBatchGoods">查询</el-button>
              <el-button type="success" @click="handleSelectAll">全选</el-button>
              <el-button @click="handleClearSelection">清空</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table
          ref="batchTableRef"
          :data="batchGoodsList"
          @selection-change="handleBatchSelectionChange"
          max-height="400"
          border
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="code" label="货物编码" width="120" />
          <el-table-column prop="name" label="货物名称" min-width="150" />
          <el-table-column label="规格/型号" width="120">
            <template #default="{ row }">
              <span>{{ row.specification || row.model || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="currentStock" label="当前库存" width="100" align="right" />
          <el-table-column prop="availableStock" label="可用库存" width="100" align="right" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="batchAddVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmBatchAdd">
          确定添加 ({{ batchSelectedGoods.length }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 货物选择对话框 -->
    <el-dialog
      v-model="goodsSelectVisible"
      title="选择货物"
      width="800px"
    >
      <div class="goods-select-content">
        <div class="filter-section">
          <el-form inline>
            <el-form-item label="分类">
              <el-select v-model="goodsFilter.categoryId" placeholder="请选择分类" clearable>
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="goodsFilter.keyword" placeholder="请输入货物编码或名称" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadGoodsForSelect">查询</el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-table
          :data="goodsSelectList"
          @row-click="handleSelectGoods"
          max-height="400"
          border
          highlight-current-row
        >
          <el-table-column prop="code" label="货物编码" width="120" />
          <el-table-column prop="name" label="货物名称" min-width="150" />
          <el-table-column label="规格/型号" width="120">
            <template #default="{ row }">
              <span>{{ row.specification || row.model || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column prop="categoryName" label="分类" width="100" />
          <el-table-column prop="currentStock" label="当前库存" width="100" align="right" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="goodsSelectVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

// 响应式数据
const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const approvalLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const batchAddVisible = ref(false)
const goodsSelectVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const currentRow = ref(null)
const currentRowIndex = ref(-1)
const warehouses = ref([])
const users = ref([])
const goodsList = ref([])
const goodsSelectList = ref([])
const categories = ref([])
const batchGoodsList = ref([])
const batchSelectedGoods = ref([])
const batchTableRef = ref()
const formRef = ref()
const stocktakeDetails = ref([])
const reportDialogVisible = ref(false)
const reportData = ref(null)
const activeTab = ref('gain')

// 搜索表单
const searchForm = reactive({
  orderNumber: '',
  warehouseId: null,
  stocktakeType: '',
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
  stocktakeType: 'FULL',
  plannedDate: '',
  stocktakeUserNames: '',
  referenceNumber: '',
  description: '',
  ranges: []
})

// 审批表单
const approvalForm = reactive({
  status: 'APPROVED',
  adjustInventory: true,
  approvalRemark: ''
})

// 批量添加筛选
const batchFilter = reactive({
  categoryId: null,
  onlyWithStock: false
})

// 货物选择筛选
const goodsFilter = reactive({
  categoryId: null,
  keyword: ''
})

// 表单验证规则
const formRules = {
  warehouseId: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  stocktakeType: [
    { required: true, message: '请选择盘点类型', trigger: 'change' }
  ],
  plannedDate: [
    { required: true, message: '请选择计划日期', trigger: 'change' }
  ],
  stocktakeUserNames: [
    { required: true, message: '请输入盘点人员', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑盘点单' : '新建盘点单'
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
  const num = Number(quantity)
  return Number.isInteger(num) ? num : num.toFixed(1)
}

const getStocktakeTypeText = (type) => {
  const typeMap = {
    'FULL': '全盘',
    'PARTIAL': '抽盘',
    'CYCLE': '循环盘点',
    'DYNAMIC': '动态盘点'
  }
  return typeMap[type] || type
}

const getStocktakeTypeColor = (type) => {
  const colorMap = {
    'FULL': 'primary',
    'PARTIAL': 'success',
    'CYCLE': 'warning',
    'DYNAMIC': 'info'
  }
  return colorMap[type] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已审批',
    'IN_PROGRESS': '盘点中',
    'EXECUTED': '已完成',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'IN_PROGRESS': 'primary',
    'EXECUTED': 'info',
    'COMPLETED': 'info',
    'CANCELLED': 'info',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getDifferenceClass = (row) => {
  const difference = (row.actualQuantity || 0) - (row.bookQuantity || 0)
  if (difference > 0) return 'text-success'
  if (difference < 0) return 'text-danger'
  return ''
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      orderNumber: searchForm.orderNumber,
      warehouseId: searchForm.warehouseId,
      stocktakeType: searchForm.stocktakeType,
      status: searchForm.status
    }
    
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await request.get('/stocktake-orders', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载盘点单数据失败:', error)
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const loadWarehouses = async () => {
  try {
    const response = await request.get('/warehouses/all')
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

const loadUsers = async () => {
  try {
    const response = await request.get('/users/all')
    if (response.success) {
      users.value = response.data || []
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    users.value = [
      { id: 1, realName: '张三' },
      { id: 2, realName: '李四' }
    ]
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
    goodsList.value = [
      { id: 1, code: 'G001', name: '苹果手机', unit: '台' }
    ]
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
      { id: 1, name: '电子产品' },
      { id: 2, name: '办公用品' }
    ]
  }
}

const loadBatchGoods = async () => {
  try {
    if (!form.warehouseId) {
      ElMessage.error('请先选择仓库')
      return
    }

    const params = {
      categoryId: batchFilter.categoryId,
      onlyWithStock: batchFilter.onlyWithStock
    }

    const response = await request.get(`/inventory/warehouse/${form.warehouseId}/goods-list`, params)
    if (response.success) {
      batchGoodsList.value = response.data || []
    }
  } catch (error) {
    console.error('加载批量货物数据失败:', error)
    batchGoodsList.value = []
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    orderNumber: '',
    warehouseId: null,
    stocktakeType: '',
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
    const response = await request.get('/stocktake-orders/generate-order-number')
    if (response.success) {
      form.orderNumber = response.data
    }
  } catch (error) {
    form.orderNumber = 'ST' + Date.now()
  }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  Object.assign(form, {
    ...row,
    ranges: row.ranges || []
  })

  // 加载盘点明细数据
  await loadStocktakeDetails(row.id)

  dialogVisible.value = true
}

const handleView = async (row) => {
  viewData.value = row

  // 加载盘点明细数据（无论什么状态都需要显示明细）
  await loadStocktakeDetails(row.id)

  viewDialogVisible.value = true
}

const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm('确定要开始盘点吗？开始后将锁定相关库存。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.post(`/stocktake-orders/${row.id}/start`)
    if (response.success) {
      ElMessage.success('盘点已开始')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('开始盘点失败:', error)
    }
  }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成盘点吗？完成后将生成盘点结果。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.post(`/stocktake-orders/${row.id}/complete`)
    if (response.success) {
      ElMessage.success('盘点已完成')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成盘点失败:', error)
    }
  }
}

const handleApprove = (row) => {
  currentRow.value = row
  approvalForm.status = 'APPROVED'
  approvalForm.adjustInventory = true
  approvalForm.approvalRemark = ''
  approvalDialogVisible.value = true
}

const handleCancel = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消盘点单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入取消原因'
    })
    
    const response = await request.post(`/stocktake-orders/${row.id}/cancel?reason=${encodeURIComponent(reason)}`)
    if (response.success) {
      ElMessage.success('盘点单取消成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消盘点单失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    // 检查盘点范围
    if (form.ranges.length === 0) {
      if (form.stocktakeType === 'FULL') {
        ElMessage.error('全盘盘点需要先批量添加所有货物')
      } else {
        ElMessage.error('请添加盘点范围')
      }
      return
    }

    submitLoading.value = true

    // 准备提交数据，将ranges转换为details
    const submitData = {
      ...form,
      details: form.ranges.map(range => ({
        goodsId: range.goodsId,
        bookQuantity: range.bookQuantity || 0,
        actualQuantity: range.actualQuantity || 0,
        remark: range.remark || ''
      }))
    }

    const url = form.id ? `/stocktake-orders/${form.id}` : '/stocktake-orders'
    const method = form.id ? 'put' : 'post'

    const response = await request[method](url, submitData)
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
    const response = await request.post(`/stocktake-orders/${currentRow.value.id}/approve`, approvalForm)
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

const handleWarehouseChange = () => {
  // 清空盘点范围
  form.ranges = []
}

const handleTypeChange = () => {
  // 如果是全盘，提示用户批量添加所有货物
  if (form.stocktakeType === 'FULL') {
    if (form.ranges.length === 0) {
      ElMessage.info('全盘盘点需要批量添加所有货物，请点击"批量添加"按钮')
    }
  }
}

const handleAddRange = () => {
  form.ranges.push({
    goodsId: null,
    goodsCode: '',
    goodsName: '',
    specification: '',
    model: '',
    brand: '',
    unit: '',
    categoryName: '',
    bookQuantity: 0,
    availableStock: 0,
    remark: ''
  })
}

const handleRemoveRange = (index) => {
  form.ranges.splice(index, 1)
}

// 删除盘点明细
const handleRemoveDetail = (index) => {
  ElMessageBox.confirm('确定要删除这条明细吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    stocktakeDetails.value.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 用户取消删除
  })
}

const handleBatchAdd = () => {
  if (!form.warehouseId) {
    ElMessage.error('请先选择仓库')
    return
  }

  // 如果是全盘盘点，默认设置为显示所有货物
  if (form.stocktakeType === 'FULL') {
    batchFilter.onlyWithStock = false
    ElMessage.info('全盘盘点将显示所有货物，包括零库存货物')
  }

  batchAddVisible.value = true
  loadBatchGoods()
}

const handleBatchSelectionChange = (selection) => {
  batchSelectedGoods.value = selection
}

const handleSelectAll = () => {
  if (batchTableRef.value) {
    batchGoodsList.value.forEach(row => {
      batchTableRef.value.toggleRowSelection(row, true)
    })
  }
}

const handleClearSelection = () => {
  if (batchTableRef.value) {
    batchTableRef.value.clearSelection()
  }
}

const handleSelectGoodsForRow = (index) => {
  currentRowIndex.value = index
  goodsSelectVisible.value = true
  loadGoodsForSelect()
}

const loadGoodsForSelect = async () => {
  try {
    if (!form.warehouseId) {
      ElMessage.error('请先选择仓库')
      return
    }

    const params = {
      categoryId: goodsFilter.categoryId,
      keyword: goodsFilter.keyword,
      onlyWithStock: true
    }

    const response = await request.get(`/inventory/warehouse/${form.warehouseId}/goods-list`, params)
    if (response.success) {
      goodsSelectList.value = response.data || []
    }
  } catch (error) {
    console.error('加载货物数据失败:', error)
    goodsSelectList.value = []
  }
}

const handleSelectGoods = (goods) => {
  if (currentRowIndex.value >= 0) {
    const row = form.ranges[currentRowIndex.value]
    row.goodsId = goods.id
    row.goodsCode = goods.code
    row.goodsName = goods.name
    row.specification = goods.specification
    row.model = goods.model
    row.brand = goods.brand
    row.unit = goods.unit
    row.categoryName = goods.categoryName
    row.bookQuantity = goods.currentStock || 0
    row.availableStock = goods.availableStock || 0
  }
  goodsSelectVisible.value = false
}

const handleConfirmBatchAdd = () => {
  const addedCount = batchSelectedGoods.value.length
  batchSelectedGoods.value.forEach(goods => {
    const exists = form.ranges.find(r => r.goodsId === goods.id)
    if (!exists) {
      form.ranges.push({
        goodsId: goods.id,
        goodsCode: goods.code,
        goodsName: goods.name,
        specification: goods.specification,
        model: goods.model,
        brand: goods.brand,
        unit: goods.unit,
        categoryName: goods.categoryName,
        bookQuantity: goods.currentStock || 0,
        availableStock: goods.availableStock || 0,
        remark: ''
      })
    }
  })

  batchAddVisible.value = false
  batchSelectedGoods.value = []
  ElMessage.success(`已添加 ${addedCount} 个货物`)
}

const handleGoodsChange = async (row, index) => {
  const goods = goodsList.value.find(g => g.id === row.goodsId)
  if (goods) {
    row.goodsCode = goods.code
    row.goodsName = goods.name
    row.unit = goods.unit
    
    // 获取库存信息
    if (form.warehouseId) {
      try {
        const response = await request.get(`/inventory/goods/${row.goodsId}/warehouse/${form.warehouseId}`)
        if (response.success) {
          row.bookQuantity = response.data.quantity || 0
        }
      } catch (error) {
        console.error('获取库存信息失败:', error)
        row.bookQuantity = 0
      }
    }
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    orderNumber: '',
    warehouseId: null,
    stocktakeType: 'FULL',
    plannedDate: '',
    stocktakeUserNames: '',
    referenceNumber: '',
    description: '',
    ranges: []
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

// 盘点明细相关方法
const loadStocktakeDetails = async (stocktakeOrderId) => {
  try {
    const response = await request.get(`/stocktake-orders/${stocktakeOrderId}/details`)
    if (response.success) {
      stocktakeDetails.value = response.data.map(detail => ({
        ...detail,
        saving: false,
        actualQuantity: detail.actualQuantity || detail.bookQuantity || 0
      }))
      console.log('处理后的明细数据:', stocktakeDetails.value)
    } else {
      console.error('API返回失败:', response)
      ElMessage.error('加载盘点明细失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('加载盘点明细失败:', error)
    ElMessage.error('加载盘点明细失败')
  }
}

const handleActualQuantityChange = (row) => {
  // 实时计算差异数量
  row.differenceQuantity = calculateDifference(row)
}

const calculateDifference = (row) => {
  const actual = row.actualQuantity || 0
  const book = row.bookQuantity || 0
  return (actual - book).toFixed(3)
}

const handleSaveDetail = async (row) => {
  try {
    row.saving = true
    const response = await request.put(`/stocktake-orders/${viewData.value.id}/details/${row.id}`, {
      actualQuantity: row.actualQuantity,
      remark: row.remark,
      isCompleted: true
    })

    if (response.success) {
      ElMessage.success('保存成功')
      // 更新本地数据
      Object.assign(row, response.data)
    }
  } catch (error) {
    console.error('保存盘点明细失败:', error)
    ElMessage.error('保存失败')
  } finally {
    row.saving = false
  }
}

const handleBatchSave = async () => {
  try {
    const updates = stocktakeDetails.value.map(detail => ({
      id: detail.id,
      actualQuantity: detail.actualQuantity,
      remark: detail.remark,
      isCompleted: true
    }))

    const response = await request.put(`/stocktake-orders/${viewData.value.id}/details/batch`, updates)
    if (response.success) {
      ElMessage.success('批量保存成功')
      // 更新本地数据
      stocktakeDetails.value = response.data.map(detail => ({
        ...detail,
        saving: false
      }))
    }
  } catch (error) {
    console.error('批量保存失败:', error)
    ElMessage.error('批量保存失败')
  }
}

const handleRefreshDetails = () => {
  loadStocktakeDetails(viewData.value.id)
}

// 查看盘点报告
const handleViewReport = async (stocktakeOrderId) => {
  try {
    const response = await request.get(`/stocktake-orders/${stocktakeOrderId}/report`)
    if (response.success) {
      reportData.value = response.data
      activeTab.value = 'gain'
      reportDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取盘点报告失败:', error)
    ElMessage.error('获取盘点报告失败')
  }
}

// 导出报告
const handleExportReport = () => {
  ElMessage.info('导出功能开发中...')
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
  loadUsers()
  loadGoodsList()
  loadCategories()

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
    
    .range-actions {
      display: flex;
      gap: 8px;
    }
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
  
  .difference-analysis {
    h4 {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 600;
    }
    
    .analysis-card {
      background: white;
      border-radius: 8px;
      padding: 20px;
      text-align: center;
      border: 1px solid #e6e6e6;
      
      .card-header {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
        font-size: 14px;
        
        .el-icon {
          margin-right: 8px;
          font-size: 18px;
        }
      }
      
      .card-value {
        font-size: 28px;
        font-weight: bold;
        margin-bottom: 8px;
      }
      
      .card-desc {
        font-size: 12px;
        color: #909399;
      }
      
      &.gain {
        .card-header {
          color: #67c23a;
        }
        .card-value {
          color: #67c23a;
        }
      }
      
      &.loss {
        .card-header {
          color: #f56c6c;
        }
        .card-value {
          color: #f56c6c;
        }
      }
      
      &.normal {
        .card-header {
          color: #409eff;
        }
        .card-value {
          color: #409eff;
        }
      }
    }
  }
}

.batch-add-content {
  .filter-section {
    margin-bottom: 16px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 6px;
  }
}

.report-container {
  .report-header {
    margin-bottom: 20px;

    h3 {
      margin-bottom: 16px;
      color: #303133;
    }
  }

  .report-statistics {
    .stat-card {
      text-align: center;
      padding: 20px;
      background: #f8f9fa;
      border-radius: 8px;
      border: 1px solid #e4e7ed;

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }

      &.gain {
        background: #f0f9ff;
        border-color: #67c23a;

        .stat-value {
          color: #67c23a;
        }
      }

      &.loss {
        background: #fef0f0;
        border-color: #f56c6c;

        .stat-value {
          color: #f56c6c;
        }
      }

      &.normal {
        background: #f4f4f5;
        border-color: #909399;

        .stat-value {
          color: #909399;
        }
      }
    }
  }

  .report-details {
    margin-top: 20px;
  }
}
</style>
