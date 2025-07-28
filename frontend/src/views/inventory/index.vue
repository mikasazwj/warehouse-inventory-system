<template>
  <div class="inventory-container">
    <!-- 现代化头部 -->
    <div class="modern-header">
      <div class="header-content">
        <div class="header-left">
          <div class="header-icon">
            <el-icon><Box /></el-icon>
          </div>
          <div class="header-text">
            <h1 class="header-title">库存管理</h1>
            <p class="header-subtitle">实时监控库存状态，精准管理货物流转</p>
          </div>
        </div>
        <div class="header-actions">
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
    </div>

    <!-- 现代化搜索区域 -->
    <div class="search-section">
      <div class="search-card">
        <div class="search-header">
          <div class="search-title">
            <el-icon><Search /></el-icon>
            <span>筛选条件</span>
          </div>
        </div>
        <div class="search-content">
          <el-form :model="searchForm" class="search-form">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="4">
                <el-form-item label="仓库" class="search-item" v-if="userStore.userInfo?.role === 'ROLE_ADMIN'">
                  <el-select
                    v-model="searchForm.warehouseId"
                    placeholder="请选择仓库"
                    clearable
                    class="search-select"
                  >
                    <el-option
                      v-for="warehouse in warehouses"
                      :key="warehouse.id"
                      :label="warehouse.name"
                      :value="warehouse.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="仓库" class="search-item" v-else>
                  <el-input
                    :value="getCurrentWarehouseName()"
                    disabled
                    class="search-input"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="5">
                <el-form-item label="货物" class="search-item">
                  <el-input
                    v-model="searchForm.keyword"
                    placeholder="请输入货物名称或编码"
                    clearable
                    class="search-input"
                  >
                    <template #prefix>
                      <el-icon><Box /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="4">
                <el-form-item label="分类" class="search-item">
                  <el-select
                    v-model="searchForm.categoryId"
                    placeholder="请选择分类"
                    clearable
                    class="search-select"
                  >
                    <el-option
                      v-for="category in categories"
                      :key="category.id"
                      :label="category.name"
                      :value="category.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="4">
                <el-form-item label="库存状态" class="search-item">
                  <el-select
                    v-model="searchForm.stockStatus"
                    placeholder="请选择状态"
                    clearable
                    class="search-select"
                  >
                    <el-option label="正常" value="normal">
                      <el-icon><CircleCheck /></el-icon>
                      正常
                    </el-option>
                    <el-option label="不足" value="low">
                      <el-icon><Warning /></el-icon>
                      不足
                    </el-option>
                    <el-option label="超量" value="high">
                      <el-icon><InfoFilled /></el-icon>
                      超量
                    </el-option>
                    <el-option label="零库存" value="zero">
                      <el-icon><CircleClose /></el-icon>
                      零库存
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="7">
                <el-form-item class="search-actions">
                  <el-button type="primary" @click="handleSearch" class="search-btn">
                    <el-icon><Search /></el-icon>
                    搜索
                  </el-button>
                  <el-button @click="handleReset" class="reset-btn">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 现代化统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card primary">
          <div class="stat-icon">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalGoods }}</div>
            <div class="stat-label">货物种类</div>
          </div>
          <div class="stat-trend">
            <el-icon><TrendCharts /></el-icon>
          </div>
        </div>
        <div class="stat-card success">
          <div class="stat-icon">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalQuantity }}</div>
            <div class="stat-label">库存总量</div>
          </div>
          <div class="stat-trend">
            <el-icon><TrendCharts /></el-icon>
          </div>
        </div>
        <div class="stat-card warning">
          <div class="stat-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.lowStockCount }}</div>
            <div class="stat-label">库存不足</div>
          </div>
          <div class="stat-trend">
            <el-icon><WarningFilled /></el-icon>
          </div>
        </div>
        <div class="stat-card danger">
          <div class="stat-icon">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.zeroStockCount }}</div>
            <div class="stat-label">零库存</div>
          </div>
          <div class="stat-trend">
            <el-icon><CircleCloseFilled /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 现代化数据表格 -->
    <div class="table-section">
      <div class="table-card">
        <div class="table-header">
          <div class="table-title">
            <el-icon><List /></el-icon>
            <span>库存列表</span>
            <el-tag v-if="pagination.total > 0" type="info" class="total-tag">
              共 {{ pagination.total }} 条记录
            </el-tag>
          </div>
        </div>
        <div class="table-content">
          <el-table
            v-loading="loading"
            :data="tableData"
            class="modern-table"
            :header-cell-style="{
              background: '#f8fafc',
              color: '#475569',
              fontWeight: '600',
              borderBottom: '2px solid #e2e8f0'
            }"
            :row-style="{ height: '60px' }"
            stripe
          >
            <template #empty>
              <div class="modern-empty">
                <div class="empty-icon">
                  <el-icon><Box /></el-icon>
                </div>
                <div class="empty-text">
                  <h3>暂无库存数据</h3>
                  <p>当前筛选条件下没有找到库存记录</p>
                </div>
                <div class="empty-actions">
                  <el-button type="primary" @click="handleReset">
                    <el-icon><Refresh /></el-icon>
                    重置筛选
                  </el-button>
                </div>
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
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">
            <span class="price-text">{{ formatPrice(row.costPrice) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存价值" width="120" align="right">
          <template #default="{ row }">
            <span class="value-text">{{ formatPrice(row.totalValue) }}</span>
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
            <el-table-column label="操作" :width="isMobile ? 80 : 280" fixed="right">
              <template #default="{ row }">
                <!-- 桌面端操作按钮 -->
                <div v-if="!isMobile" class="action-buttons desktop-actions">
                  <el-button type="primary" size="small" @click="handleViewDetail(row)" class="action-btn">
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-button>
                  <el-button type="info" size="small" @click="handleViewHistory(row)" class="action-btn">
                    <el-icon><Clock /></el-icon>
                    历史
                  </el-button>
                  <el-button type="warning" size="small" @click="handleAdjustItem(row)" class="action-btn">
                    <el-icon><EditPen /></el-icon>
                    调整
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
                        <el-dropdown-item @click="handleViewDetail(row)">
                          <el-icon><View /></el-icon>
                          查看详情
                        </el-dropdown-item>
                        <el-dropdown-item @click="handleViewHistory(row)">
                          <el-icon><Clock /></el-icon>
                          查看历史
                        </el-dropdown-item>
                        <el-dropdown-item @click="handleAdjustItem(row)">
                          <el-icon><EditPen /></el-icon>
                          库存调整
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 现代化分页 -->
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
            class="inventory-pagination"
          />
        </div>
      </div>
    </div>

    <!-- 现代化库存调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      :width="isMobile ? '95%' : '900px'"
      :fullscreen="isMobile"
      class="adjust-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="2000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header adjust-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><EditPen /></el-icon>
              </div>
              <div class="title-content">
                <h2>库存调整</h2>
                <p>调整货物库存数量，系统将自动记录变动历史</p>
              </div>
            </div>
            <el-button @click="adjustDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <div class="adjust-content">
        <div class="adjust-sections">
          <!-- 基本信息卡片 -->
          <div class="adjust-card">
            <div class="card-header">
              <div class="card-icon basic">
                <el-icon><Box /></el-icon>
              </div>
              <h3 class="card-title">基本信息</h3>
            </div>
            <div class="card-content">
              <el-form
                ref="adjustFormRef"
                :model="adjustForm"
                :rules="adjustRules"
                class="adjust-form"
              >
                <div class="form-grid">
                  <div class="form-item">
                    <label class="form-label">选择仓库</label>
                    <el-select
                      v-model="adjustForm.warehouseId"
                      placeholder="请选择仓库"
                      :disabled="adjustForm.isWarehouseLocked"
                      size="large"
                      class="form-select"
                      popper-class="adjust-dialog-select"
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
                  </div>
                  <div class="form-item">
                    <label class="form-label">选择货物</label>
                    <el-select
                      v-model="adjustForm.goodsId"
                      placeholder="请选择货物"
                      :disabled="adjustForm.isGoodsLocked"
                      size="large"
                      class="form-select"
                      filterable
                      popper-class="adjust-dialog-select"
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
                  </div>
                </div>
              </el-form>
            </div>
          </div>

          <!-- 调整设置卡片 -->
          <div class="adjust-card">
            <div class="card-header">
              <div class="card-icon adjust">
                <el-icon><EditPen /></el-icon>
              </div>
              <h3 class="card-title">调整设置</h3>
            </div>
            <div class="card-content">
              <div class="form-grid">
                <div class="form-item">
                  <label class="form-label">调整类型</label>
                  <el-radio-group v-model="adjustForm.adjustType" class="radio-group">
                    <el-radio label="increase" class="radio-item">
                      <div class="radio-content">
                        <el-icon><Plus /></el-icon>
                        <span>增加库存</span>
                      </div>
                    </el-radio>
                    <el-radio label="decrease" class="radio-item">
                      <div class="radio-content">
                        <el-icon><Minus /></el-icon>
                        <span>减少库存</span>
                      </div>
                    </el-radio>
                    <el-radio label="set" class="radio-item">
                      <div class="radio-content">
                        <el-icon><Setting /></el-icon>
                        <span>设置数量</span>
                      </div>
                    </el-radio>
                  </el-radio-group>
                </div>
                <div class="form-item">
                  <label class="form-label">调整数量</label>
                  <el-input-number
                    v-model="adjustForm.quantity"
                    :min="0"
                    :precision="0"
                    :step="1"
                    size="large"
                    placeholder="请输入调整数量"
                    class="quantity-input"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 调整原因卡片 -->
          <div class="adjust-card">
            <div class="card-header">
              <div class="card-icon reason">
                <el-icon><Document /></el-icon>
              </div>
              <h3 class="card-title">调整原因</h3>
            </div>
            <div class="card-content">
              <el-input
                v-model="adjustForm.reason"
                type="textarea"
                :rows="4"
                placeholder="请详细说明库存调整的原因..."
                size="large"
                class="reason-textarea"
              />
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer adjust-footer">
          <div class="footer-actions">
            <el-button @click="adjustDialogVisible = false" size="large" class="cancel-btn">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
            <el-button type="primary" @click="handleSubmitAdjust" :loading="adjustLoading" size="large" class="submit-btn">
              <el-icon><Check /></el-icon>
              {{ adjustLoading ? '调整中...' : '确定调整' }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 库存详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :width="isMobile ? '95%' : '900px'"
      :fullscreen="isMobile"
      class="detail-dialog modern-dialog"
      :show-close="false"
      append-to-body
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header detail-header">
          <div class="dialog-title">
            <div class="title-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="title-content">
              <h2>库存详情</h2>
              <p>{{ detailData.goodsName }} - {{ detailData.warehouseName }}</p>
            </div>
          </div>
          <el-button @click="detailDialogVisible = false" class="dialog-close" text>
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>

      <div class="detail-content">
        <div class="detail-sections">
          <!-- 基本信息卡片 -->
          <div class="detail-card">
            <div class="card-header">
              <div class="card-icon basic-info">
                <el-icon><Box /></el-icon>
              </div>
              <h3 class="card-title">基本信息</h3>
            </div>
            <div class="card-content">
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-label">仓库名称</div>
                  <div class="info-value">{{ detailData.warehouseName }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">货物编码</div>
                  <div class="info-value code-text">{{ detailData.goodsCode }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">货物名称</div>
                  <div class="info-value name-text">{{ detailData.goodsName }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">分类</div>
                  <div class="info-value">{{ detailData.categoryName }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">规格/型号</div>
                  <div class="info-value">{{ detailData.specification || '-' }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">单位</div>
                  <div class="info-value">{{ detailData.goodsUnit }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 库存信息卡片 -->
          <div class="detail-card">
            <div class="card-header">
              <div class="card-icon stock-info">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <h3 class="card-title">库存信息</h3>
            </div>
            <div class="card-content">
              <div class="stock-overview">
                <div class="stock-main">
                  <div class="stock-number">{{ formatQuantity(detailData.quantity) }}</div>
                  <div class="stock-label">当前库存</div>
                </div>
                <div class="stock-breakdown">
                  <div class="stock-item available">
                    <div class="stock-value">{{ formatQuantity(detailData.availableQuantity) }}</div>
                    <div class="stock-name">可用库存</div>
                  </div>
                  <div class="stock-item locked">
                    <div class="stock-value">{{ formatQuantity(detailData.lockedQuantity) }}</div>
                    <div class="stock-name">锁定库存</div>
                  </div>
                  <div class="stock-item status">
                    <el-tag :type="getStockStatusType(detailData)" size="large">
                      {{ getStockStatusText(detailData) }}
                    </el-tag>
                    <div class="stock-name">库存状态</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 价格信息卡片 -->
          <div class="detail-card">
            <div class="card-header">
              <div class="card-icon price-info">
                <el-icon>¥</el-icon>
              </div>
              <h3 class="card-title">价格信息</h3>
            </div>
            <div class="card-content">
              <div class="price-overview">
                <div class="price-item unit-price">
                  <div class="price-value">{{ formatPrice(detailData.costPrice) }}</div>
                  <div class="price-label">单价（加权平均）</div>
                </div>
                <div class="price-item total-value">
                  <div class="price-value total">{{ formatPrice(detailData.totalValue) }}</div>
                  <div class="price-label">库存价值</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 时间信息卡片 -->
          <div class="detail-card">
            <div class="card-header">
              <div class="card-icon time-info">
                <el-icon><Clock /></el-icon>
              </div>
              <h3 class="card-title">更新信息</h3>
            </div>
            <div class="card-content">
              <div class="info-grid">
                <div class="info-item">
                  <div class="info-label">最后更新时间</div>
                  <div class="info-value">{{ formatDateTime(detailData.updatedTime) }}</div>
                </div>
                <div class="info-item">
                  <div class="info-label">创建时间</div>
                  <div class="info-value">{{ formatDateTime(detailData.createdTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer detail-footer">
          <div class="footer-actions">
            <div class="primary-actions">
              <el-button type="primary" @click="handleViewHistoryFromDetail(detailData)" size="large">
                <el-icon><Clock /></el-icon>
                查看历史
              </el-button>
              <el-button type="warning" @click="handleAdjustItemFromDetail(detailData)" size="large">
                <el-icon><EditPen /></el-icon>
                库存调整
              </el-button>
              <el-button @click="detailDialogVisible = false" size="large">
                <el-icon><Close /></el-icon>
                关闭
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 现代化库存历史对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      :width="isMobile ? '95%' : '1200px'"
      :fullscreen="isMobile"
      class="history-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="2000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header history-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="title-content">
                <h2>库存变动历史</h2>
                <p>查看货物库存的所有变动记录和操作历史</p>
              </div>
            </div>
            <el-button @click="historyDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <div class="history-content">
        <el-table
          :data="historyData"
          v-loading="historyLoading"
          class="modern-table"
          :header-cell-style="{
            background: '#f8fafc',
            color: '#475569',
            fontWeight: '600',
            borderBottom: '2px solid #e2e8f0'
          }"
          :row-style="{ height: '50px' }"
          stripe
        >
          <template #empty>
            <div class="modern-empty">
              <div class="empty-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="empty-text">
                <h3>暂无历史记录</h3>
                <p>该货物暂无库存变动记录</p>
              </div>
            </div>
          </template>
          <el-table-column prop="operationType" label="操作类型" width="140">
            <template #default="{ row }">
              <el-tag :type="getOperationTypeTag(formatOperationType(row.operationType, row.quantity, row.reason, row.businessType, row.relatedOrderNumber))">
                {{ formatOperationType(row.operationType, row.quantity, row.reason, row.businessType, row.relatedOrderNumber) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="变动数量" width="120" align="right">
            <template #default="{ row }">
              <span :class="row.quantity > 0 ? 'quantity-increase' : 'quantity-decrease'">
                {{ row.quantity > 0 ? '+' : '' }}{{ Math.round(row.quantity) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="变动前" width="100" align="right">
            <template #default="{ row }">
              <span class="quantity-text">{{ Math.round(row.beforeQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变动后" width="100" align="right">
            <template #default="{ row }">
              <span class="quantity-text">{{ Math.round(row.afterQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="原因" min-width="200">
            <template #default="{ row }">
              <span class="reason-text">{{ formatReason(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="operatedBy" label="操作人" width="120" />
          <el-table-column prop="operatedTime" label="操作时间" width="180">
            <template #default="{ row }">
              <span class="time-text">{{ formatDateTime(row.operatedTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 导出功能组件 -->
    <InventoryExport
      ref="inventoryExportRef"
      :visible="exportDialogVisible"
      @close="exportDialogVisible = false"
      @export="handleExportData"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { useDeviceDetection, mobileOptimizations } from '@/utils/responsive'
import {
  Box,
  Search,
  Refresh,
  EditPen,
  Download,
  Clock,
  List,
  TrendCharts,
  Warning,
  WarningFilled,
  CircleCheck,
  CircleClose,
  CircleCloseFilled,
  InfoFilled,
  Close,
  Check,
  MoreFilled,
  Lock,
  View,
  Plus,
  Minus,
  Setting,
  Document
} from '@element-plus/icons-vue'
import InventoryExport from '@/components/InventoryExport.vue'

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 响应式数据
const userStore = useUserStore()
const loading = ref(false)
const adjustLoading = ref(false)
const historyLoading = ref(false)
const adjustDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const tableData = ref([])
const historyData = ref([])
const detailData = ref({})
const warehouses = ref([])
const categories = ref([])
const goodsList = ref([])
const adjustFormRef = ref()
const inventoryExportRef = ref()

// 弹窗状态
const exportDialogVisible = ref(false)

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

// 格式化价格显示
const formatPrice = (price) => {
  if (price === null || price === undefined || price === 0) {
    return '¥0.00'
  }
  const numPrice = Number(price)
  if (isNaN(numPrice)) {
    return '¥0.00'
  }
  return '¥' + numPrice.toFixed(2)
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

// 获取操作类型标签样式
const getOperationTypeTag = (operationType) => {
  const typeMap = {
    '入库': 'success',           // 绿色
    '出库': 'warning',           // 橙色
    '库存增加': 'info',          // 蓝色
    '库存减少': 'info',          // 蓝色
    '库存调整': 'info',          // 蓝色
    '调拨入库': 'primary',       // 主色（紫色）
    '调拨出库': 'primary',       // 主色（紫色）
    '盘盈入库': 'success',       // 绿色
    '盘亏出库': 'danger',        // 红色
    '手动调整': 'info',          // 蓝色
    '手动调整(+)': 'success',    // 绿色
    '手动调整(-)': 'warning',    // 橙色
    '调整': 'info',              // 蓝色
    '盘点': 'primary',           // 主色
    '转移': 'info',              // 蓝色
    '退货': 'danger',            // 红色
    '系统初始化': 'primary'      // 主色
  }
  return typeMap[operationType] || 'info'
}

// 格式化操作类型显示
const formatOperationType = (operationType, quantity, reason, businessType, businessNumber) => {
  // 根据业务类型判断操作类型
  if (businessType) {
    switch (businessType) {
      case 'INBOUND':
        return '入库'
      case 'OUTBOUND':
        return '出库'
      case 'TRANSFER':
        // 调拨业务：根据数量判断是调入还是调出
        return quantity > 0 ? '调拨入库' : '调拨出库'
      case 'STOCKTAKING':
        return quantity > 0 ? '盘盈入库' : '盘亏出库'
      case 'INVENTORY':
        // 库存调整业务
        if (quantity > 0) {
          return '手动调整(+)'
        } else if (quantity < 0) {
          return '手动调整(-)'
        } else {
          return '手动调整'
        }
    }
  }

  // 根据业务单据号判断
  if (businessNumber) {
    if (businessNumber.startsWith('IN')) {
      return '入库'
    } else if (businessNumber.startsWith('OUT')) {
      return '出库'
    } else if (businessNumber.startsWith('TR')) {
      return quantity > 0 ? '调拨入库' : '调拨出库'
    } else if (businessNumber.startsWith('ST')) {
      return quantity > 0 ? '盘盈入库' : '盘亏出库'
    }
  }

  // 根据原因字段判断
  if (reason) {
    // 检查是否包含入库单号
    if (reason.includes('入库单') || reason.match(/IN\d{11}/)) {
      return '入库'
    }
    // 检查是否包含出库单号
    if (reason.includes('出库单') || reason.match(/OUT\d{11}/)) {
      return '出库'
    }
    // 检查是否包含调拨单号
    if (reason.includes('调拨单') || reason.match(/TR\d{11}/)) {
      return quantity > 0 ? '调拨入库' : '调拨出库'
    }
    // 检查是否包含盘点单号
    if (reason.includes('盘点单') || reason.match(/ST\d{11}/)) {
      return quantity > 0 ? '盘盈入库' : '盘亏出库'
    }
  }

  // 处理真实的操作类型数据
  if (operationType) {
    switch (operationType) {
      case '库存增加':
        // 默认判断为手动调整，除非有其他业务标识
        return '手动调整(+)'
      case '库存减少':
        // 默认判断为手动调整，除非有其他业务标识
        return '手动调整(-)'
      case '库存调整':
        if (quantity > 0) {
          return '手动调整(+)'
        } else if (quantity < 0) {
          return '手动调整(-)'
        } else {
          return '手动调整'
        }
      case '入库':
        return '入库'
      case '出库':
        return '出库'
      case '调拨入库':
        return '调拨入库'
      case '调拨出库':
        return '调拨出库'
      case '盘盈入库':
        return '盘盈入库'
      case '盘亏出库':
        return '盘亏出库'
      case '系统初始化':
        return '系统初始化'
      default:
        // 根据数量变化自动判断
        if (quantity > 0) {
          return '手动调整(+)'
        } else if (quantity < 0) {
          return '手动调整(-)'
        } else {
          return operationType
        }
    }
  }

  // 兜底逻辑
  if (quantity > 0) {
    return '手动调整(+)'
  } else if (quantity < 0) {
    return '手动调整(-)'
  } else {
    return '手动调整'
  }
}

// 格式化原因显示
const formatReason = (row) => {
  const { reason, operationType, quantity, relatedOrderNumber, businessType } = row

  // 优先根据业务类型和关联单据号生成原因
  if (relatedOrderNumber) {
    // 根据单据号前缀判断业务类型
    if (relatedOrderNumber.startsWith('IN')) {
      return `入库单：${relatedOrderNumber}`;
    } else if (relatedOrderNumber.startsWith('OUT')) {
      return `出库单：${relatedOrderNumber}`;
    } else if (relatedOrderNumber.startsWith('TR')) {
      // 调拨单：根据操作类型区分调入调出
      if (operationType === '调拨入库' || (operationType === '入库' && businessType === 'TRANSFER')) {
        return `调拨单：${relatedOrderNumber} (调入)`;
      } else if (operationType === '调拨出库' || (operationType === '出库' && businessType === 'TRANSFER')) {
        return `调拨单：${relatedOrderNumber} (调出)`;
      } else {
        return `调拨单：${relatedOrderNumber}`;
      }
    } else if (relatedOrderNumber.startsWith('ST')) {
      // 盘点单：根据数量判断盘盈盘亏
      if (quantity > 0) {
        return `盘点单：${relatedOrderNumber} (盘盈)`;
      } else {
        return `盘点单：${relatedOrderNumber} (盘亏)`;
      }
    } else {
      return `相关单据：${relatedOrderNumber}`;
    }
  }

  // 根据业务类型生成原因
  if (businessType) {
    switch (businessType) {
      case 'INBOUND':
        return `货物入库，数量增加 ${Math.abs(quantity)}`;
      case 'OUTBOUND':
        return `货物出库，数量减少 ${Math.abs(quantity)}`;
      case 'TRANSFER':
        if (quantity > 0) {
          return `调拨入库，数量增加 ${Math.abs(quantity)}`;
        } else {
          return `调拨出库，数量减少 ${Math.abs(quantity)}`;
        }
      case 'STOCKTAKING':
        if (quantity > 0) {
          return `盘点盘盈，数量增加 ${Math.abs(quantity)}`;
        } else {
          return `盘点盘亏，数量减少 ${Math.abs(quantity)}`;
        }
      case 'INVENTORY':
        // 手动调整
        if (quantity > 0) {
          return `手动调整，数量增加 ${Math.abs(quantity)}`;
        } else if (quantity < 0) {
          return `手动调整，数量减少 ${Math.abs(quantity)}`;
        } else {
          return '手动调整，数量无变化';
        }
    }
  }

  // 处理真实数据中的冗长原因，提取关键信息
  if (reason) {
    // 如果原因包含单据号，提取单据号
    const orderNumberMatch = reason.match(/([A-Z]{2}\d{11})/);
    if (orderNumberMatch) {
      const orderNumber = orderNumberMatch[1];
      if (reason.includes('入库')) {
        return `入库单：${orderNumber}`;
      } else if (reason.includes('出库')) {
        return `出库单：${orderNumber}`;
      } else if (reason.includes('调拨')) {
        return `调拨单：${orderNumber}`;
      } else if (reason.includes('盘点')) {
        return `盘点单：${orderNumber}`;
      } else {
        return `相关单据：${orderNumber}`;
      }
    }

    // 如果是简短的原因，直接显示
    if (reason.length <= 20 && !reason.includes('变动数量')) {
      return reason;
    }

    // 如果是系统生成的冗长描述，且是手动调整，简化显示
    if (reason.includes('变动数量') && reason.includes('调整前') && reason.includes('调整后') &&
        (operationType === '库存增加' || operationType === '库存减少' || businessType === 'INVENTORY')) {
      // 提取货物名称
      const goodsMatch = reason.match(/库存[增减]\s+([^\s-]+)/);
      const goodsName = goodsMatch ? goodsMatch[1] : '';

      if (operationType === '库存增加') {
        return `手动调整：${goodsName} 数量增加 ${Math.abs(quantity)}`;
      } else if (operationType === '库存减少') {
        return `手动调整：${goodsName} 数量减少 ${Math.abs(quantity)}`;
      } else {
        return `手动调整：${goodsName} 数量变动 ${quantity > 0 ? '+' : ''}${quantity}`;
      }
    }
  }



  // 根据操作类型生成默认原因
  switch (operationType) {
    case '入库':
      return `货物入库，数量增加 ${Math.abs(quantity)}`;
    case '出库':
      return `货物出库，数量减少 ${Math.abs(quantity)}`;
    case '库存增加':
      return `手动调整，数量增加 ${Math.abs(quantity)}`;
    case '库存减少':
      return `手动调整，数量减少 ${Math.abs(quantity)}`;
    case '库存调整':
      if (quantity > 0) {
        return `手动调整，数量增加 ${Math.abs(quantity)}`;
      } else if (quantity < 0) {
        return `手动调整，数量减少 ${Math.abs(quantity)}`;
      } else {
        return '手动调整，数量无变化';
      }
    case '调拨入库':
      return `调拨入库，数量增加 ${Math.abs(quantity)}`;
    case '调拨出库':
      return `调拨出库，数量减少 ${Math.abs(quantity)}`;
    case '盘盈入库':
      return `盘点盘盈，数量增加 ${Math.abs(quantity)}`;
    case '盘亏出库':
      return `盘点盘亏，数量减少 ${Math.abs(quantity)}`;
    case '系统初始化':
      return '系统初始化库存';
    default:
      return `库存变动，数量${quantity > 0 ? '增加' : '减少'} ${Math.abs(quantity)}`;
  }
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
    console.log('开始库存调整，表单数据:', adjustForm)
    console.log('reason字段类型:', typeof adjustForm.reason, '值:', adjustForm.reason)

    // 确保reason字段是字符串
    if (Array.isArray(adjustForm.reason)) {
      adjustForm.reason = adjustForm.reason.join('')
    }
    adjustForm.reason = String(adjustForm.reason || '')

    await adjustFormRef.value.validate()
    console.log('表单验证通过')

    // 特殊验证：对于增加和减少类型，数量不能为0
    if ((adjustForm.adjustType === 'increase' || adjustForm.adjustType === 'decrease') && adjustForm.quantity === 0) {
      ElMessage.warning('增加或减少类型的调整数量不能为0')
      return
    }

    adjustLoading.value = true

    // 根据调整类型计算实际调整数量或设置数量
    let requestData = {
      warehouseId: Number(adjustForm.warehouseId),
      goodsId: Number(adjustForm.goodsId),
      reason: String(adjustForm.reason || '库存调整').trim()
    }

    if (adjustForm.adjustType === 'set') {
      // 设置类型：直接设置为指定数量（允许为0）
      requestData.setQuantity = Number(adjustForm.quantity)
      requestData.adjustQuantity = null
    } else {
      // 增加/减少类型：计算调整数量
      let adjustQuantity = Number(adjustForm.quantity)
      if (adjustForm.adjustType === 'decrease') {
        adjustQuantity = -Math.abs(adjustQuantity)
      } else if (adjustForm.adjustType === 'increase') {
        adjustQuantity = Math.abs(adjustQuantity)
      }
      requestData.adjustQuantity = adjustQuantity
      requestData.setQuantity = null
    }

    console.log('发送库存调整请求:', requestData)
    console.log('请求数据类型检查:', {
      warehouseId: typeof requestData.warehouseId,
      goodsId: typeof requestData.goodsId,
      reason: typeof requestData.reason,
      reasonValue: requestData.reason,
      adjustQuantity: typeof requestData.adjustQuantity,
      setQuantity: typeof requestData.setQuantity
    })

    const response = await request.post('/inventory/adjust', requestData)
    console.log('库存调整响应:', response)

    if (response.success) {
      ElMessage.success('库存调整成功')
      adjustDialogVisible.value = false
      loadData()
      loadStats()
    } else {
      throw new Error(response.message || '库存调整失败')
    }
  } catch (error) {
    console.error('库存调整失败:', error)

    let errorMessage = '库存调整失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    } else if (typeof error === 'string') {
      errorMessage = error
    } else if (error.data?.message) {
      errorMessage = error.data.message
    }

    ElMessage.error(errorMessage)
  } finally {
    adjustLoading.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  detailData.value = { ...row }
  detailDialogVisible.value = true
}

// 从详情页查看历史（关闭详情页）
const handleViewHistoryFromDetail = (row) => {
  detailDialogVisible.value = false  // 先关闭详情页
  handleViewHistory(row)  // 再打开历史页
}

// 从详情页调整库存（关闭详情页）
const handleAdjustItemFromDetail = (row) => {
  detailDialogVisible.value = false  // 先关闭详情页
  handleAdjustItem(row)  // 再打开调整页
}

const handleViewHistory = async (row) => {
  try {
    historyLoading.value = true
    historyDialogVisible.value = true

    const response = await request.get(`/inventory/${row.id}/history`)
    if (response.success) {
      historyData.value = response.data || []
      console.log('库存历史数据:', historyData.value)

      // 调试：检查格式化方法是否正确工作
      if (historyData.value.length > 0) {
        const firstRecord = historyData.value[0]
        console.log('第一条记录原始数据:', firstRecord)
        console.log('格式化参数:', {
          operationType: firstRecord.operationType,
          quantity: firstRecord.quantity,
          reason: firstRecord.reason,
          businessType: firstRecord.businessType,
          relatedOrderNumber: firstRecord.relatedOrderNumber
        })
        console.log('格式化后的操作类型:', formatOperationType(firstRecord.operationType, firstRecord.quantity, firstRecord.reason, firstRecord.businessType, firstRecord.relatedOrderNumber))
        console.log('格式化后的原因:', formatReason(firstRecord))
      }
    }
  } catch (error) {
    console.error('加载库存历史失败:', error)
    // 清空数据，不使用模拟数据
    historyData.value = []
  } finally {
    historyLoading.value = false
  }
}

const handleExport = () => {
  exportDialogVisible.value = true
}

const handleExportData = async (exportConfig) => {
  try {
    ElMessage.info('正在导出库存数据...')

    // 构建请求数据
    const exportData = {
      ...exportConfig,
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
  // 先重置表单字段
  adjustFormRef.value?.resetFields()

  // 然后设置默认值，确保数据类型正确
  Object.assign(adjustForm, {
    warehouseId: null,
    goodsId: null,
    adjustType: 'increase',
    quantity: null,
    reason: '',  // 确保是字符串
    isWarehouseLocked: false,
    isGoodsLocked: false
  })

  // 强制确保reason是字符串
  adjustForm.reason = String(adjustForm.reason || '')
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
/* 现代化库存管理页面样式 */
.inventory-container {
  min-height: 100vh;
  background: #f8fafc;
  padding: 20px;
}

/* 现代化头部样式 */
.modern-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.header-text {
  .header-title {
    font-size: 32px;
    font-weight: 700;
    color: #1a202c;
    margin: 0 0 8px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .header-subtitle {
    font-size: 16px;
    color: #64748b;
    margin: 0;
    font-weight: 500;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;

  .el-button {
    height: 44px;
    padding: 12px 24px;
    border-radius: 12px;
    font-weight: 600;
    transition: all 0.3s ease;
    border: none;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
    }

    .el-icon {
      margin-right: 8px;
      font-size: 16px;
    }
  }
}

/* 搜索区域样式 */
.search-section {
  margin-bottom: 24px;
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.search-header {
  margin-bottom: 16px;
}

.search-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;

  .el-icon {
    color: #667eea;
    font-size: 20px;
  }
}

.search-content {
  .search-form {
    .search-item {
      margin-bottom: 0;

      .el-form-item__label {
        font-weight: 600;
        color: #374151;
        margin-bottom: 6px;
        font-size: 14px;
      }
    }

    .search-input,
    .search-select {
      width: 100%;

      .el-input__wrapper {
        border-radius: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        border: 1px solid #e2e8f0;
        transition: all 0.3s ease;

        &:hover {
          border-color: #667eea;
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
        }

        &.is-focus {
          border-color: #667eea;
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }
      }
    }

    .search-actions {
      display: flex;
      gap: 10px;
      justify-content: flex-end;
      align-items: flex-end;
      margin-bottom: 0;

      .search-btn,
      .reset-btn {
        height: 36px;
        padding: 8px 20px;
        border-radius: 10px;
        font-weight: 600;
        font-size: 14px;
        transition: all 0.3s ease;

        .el-icon {
          margin-right: 6px;
          font-size: 14px;
        }

        &:hover {
          transform: translateY(-1px);
        }
      }

      .search-btn {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);

        &:hover {
          box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
        }
      }

      .reset-btn {
        background: #f8fafc;
        color: #64748b;
        border: 1px solid #e2e8f0;

        &:hover {
          background: #f1f5f9;
          border-color: #cbd5e1;
        }
      }
    }
  }
}

/* 统计卡片样式 */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  min-height: 80px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.12);
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, transparent, currentColor, transparent);
    opacity: 0.6;
  }

  &.primary {
    color: #667eea;

    .stat-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
  }

  &.success {
    color: #10b981;

    .stat-icon {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    }
  }

  &.warning {
    color: #f59e0b;

    .stat-icon {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    }
  }

  &.danger {
    color: #ef4444;

    .stat-icon {
      background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    }
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 22px;
    box-shadow: 0 3px 15px rgba(0, 0, 0, 0.15);
    flex-shrink: 0;
  }

  .stat-content {
    flex: 1;
    min-width: 0;

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #1a202c;
      line-height: 1;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 14px;
      color: #64748b;
      font-weight: 500;
    }
  }

  .stat-trend {
    color: currentColor;
    opacity: 0.5;
    font-size: 16px;
    flex-shrink: 0;
  }
}

/* 表格区域样式 */
.table-section {
  margin-bottom: 30px;
}

.table-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.table-header {
  padding: 30px 30px 0 30px;
  margin-bottom: 20px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;

  .el-icon {
    color: #667eea;
    font-size: 24px;
  }

  .total-tag {
    margin-left: 12px;
    background: #f1f5f9;
    color: #64748b;
    border: none;
  }
}

.table-content {
  padding: 0 30px 30px 30px;
}

.modern-table {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  .el-table__body tr:hover > td {
    background-color: #f8fafc !important;
  }

  .el-table__row {
    transition: all 0.3s ease;
  }
}

.pagination-container {
  padding: 20px 30px;
  border-top: 1px solid #f1f5f9;
  background: #fafbfc;

  .inventory-pagination {
    :deep(.el-pagination) {
      justify-content: center;
    }

    .el-pagination__total {
      margin-right: auto;
    }
  }
}

/* 现代化空状态 */
.modern-empty {
  padding: 60px 20px;
  text-align: center;

  .empty-icon {
    margin-bottom: 20px;

    .el-icon {
      font-size: 64px;
      color: #cbd5e1;
    }
  }

  .empty-text {
    margin-bottom: 24px;

    h3 {
      font-size: 18px;
      color: #374151;
      margin: 0 0 8px 0;
      font-weight: 600;
    }

    p {
      font-size: 14px;
      color: #9ca3af;
      margin: 0;
    }
  }

  .empty-actions {
    .el-button {
      border-radius: 12px;
      padding: 12px 24px;
      font-weight: 600;
    }
  }
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: nowrap;
  justify-content: flex-start;
  align-items: center;

  &.desktop-actions {
    width: 100%;

    .action-btn {
      margin: 0;
      padding: 6px 10px;
      font-size: 12px;
      border-radius: 4px;
      min-width: 65px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 3px;
      white-space: nowrap;
      flex-shrink: 0;

      .el-icon {
        font-size: 12px;
      }
    }
  }
}

.mobile-actions {
  display: flex;
  justify-content: center;

  .mobile-action-trigger {
    padding: 6px 12px;
    font-size: 12px;
    border-radius: 4px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

/* 现代化弹窗样式 */
.modern-dialog {
  .el-dialog__header {
    padding: 0;
    border-bottom: none;
  }

  .el-dialog__body {
    padding: 30px;
    background: #fafbfc;
  }

  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30px 30px 0 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;

    .dialog-title {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 20px;
      font-weight: 600;

      .el-icon {
        font-size: 24px;
      }
    }

    .dialog-close {
      color: white;
      font-size: 20px;
      padding: 8px;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 20px 0 0 0;

    .cancel-btn,
    .submit-btn {
      border-radius: 12px;
      padding: 12px 24px;
      font-weight: 600;
      transition: all 0.3s ease;

      .el-icon {
        margin-right: 8px;
      }

      &:hover {
        transform: translateY(-2px);
      }
    }

    .cancel-btn {
      background: #f8fafc;
      color: #64748b;
      border: 1px solid #e2e8f0;

      &:hover {
        background: #f1f5f9;
        border-color: #cbd5e1;
      }
    }

    .submit-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);

      &:hover {
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
      }
    }
  }
}

/* 历史记录弹窗特殊样式 */
.history-content {
  .modern-table {
    .quantity-increase {
      color: #10b981;
      font-weight: 600;
    }

    .quantity-decrease {
      color: #ef4444;
      font-weight: 600;
    }

    .quantity-text {
      color: #374151;
      font-weight: 500;
    }

    .time-text {
      color: #64748b;
      font-size: 13px;
    }

    .reason-text {
      color: #374151;
      font-size: 13px;
      line-height: 1.4;
    }

    .price-text {
      color: #059669 !important;
      font-weight: 600 !important;
      font-family: 'Consolas', 'Monaco', monospace !important;
    }

    .value-text {
      color: #dc2626 !important;
      font-weight: 600 !important;
      font-family: 'Consolas', 'Monaco', monospace !important;
    }

    /* 库存调整对话框样式 */
    .adjust-header {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      color: white;
      padding: 24px 32px;
      margin: -20px -24px 0 -24px;
      border-radius: 12px 12px 0 0;

      .header-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .dialog-title {
        display: flex;
        align-items: center;
        gap: 16px;

        .title-icon {
          width: 48px;
          height: 48px;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          backdrop-filter: blur(10px);

          .el-icon {
            font-size: 24px;
            color: white;
          }
        }

        .title-content {
          h2 {
            margin: 0;
            font-size: 24px;
            font-weight: 600;
            color: white;
          }

          p {
            margin: 4px 0 0 0;
            font-size: 14px;
            color: rgba(255, 255, 255, 0.8);
          }
        }
      }

      .dialog-close {
        color: white;
        background: rgba(255, 255, 255, 0.1);
        border: 1px solid rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
        }
      }
    }

    .adjust-content {
      padding: 32px 0;
      max-height: 65vh;
      overflow-y: auto;
    }

    .adjust-sections {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .adjust-card {
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      border: 1px solid #f1f5f9;
      overflow: hidden;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
      }

      .card-header {
        padding: 20px 24px 16px 24px;
        background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
        border-bottom: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        gap: 12px;

        .card-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;

          &.basic {
            background: linear-gradient(135deg, #3b82f6, #1d4ed8);
            color: white;
          }

          &.adjust {
            background: linear-gradient(135deg, #f59e0b, #d97706);
            color: white;
          }

          &.reason {
            background: linear-gradient(135deg, #8b5cf6, #7c3aed);
            color: white;
          }

          .el-icon {
            font-size: 20px;
          }
        }

        .card-title {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #1e293b;
        }
      }

      .card-content {
        padding: 24px;
      }
    }

    /* 调整表单样式 */
    .adjust-form {
      .form-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 24px;
      }

      .form-item {
        .form-label {
          font-size: 14px;
          font-weight: 600;
          color: #374151;
          margin-bottom: 12px;
          display: block;
        }

        .form-select {
          width: 100%;
        }

        .field-lock-tip {
          margin-top: 8px;
          padding: 8px 12px;
          background: #fef3c7;
          border: 1px solid #f59e0b;
          border-radius: 6px;
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: #92400e;

          .el-icon {
            font-size: 14px;
          }
        }

        .radio-group {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .radio-item {
            margin: 0;
            padding: 16px;
            border: 2px solid #e5e7eb;
            border-radius: 12px;
            transition: all 0.3s ease;
            cursor: pointer;

            &:hover {
              border-color: #f59e0b;
              background: #fef3c7;
            }

            &.is-checked {
              border-color: #f59e0b;
              background: linear-gradient(135deg, #fef3c7, #fde68a);
            }

            .radio-content {
              display: flex;
              align-items: center;
              gap: 12px;

              .el-icon {
                font-size: 20px;
                color: #6b7280;
              }

              span {
                font-weight: 500;
                color: #374151;
              }
            }
          }
        }

        .quantity-input {
          width: 100%;

          .el-input__inner {
            text-align: center;
            font-size: 18px;
            font-weight: 600;
            font-family: 'Consolas', 'Monaco', monospace;
          }
        }

        .reason-textarea {
          .el-textarea__inner {
            font-size: 14px;
            line-height: 1.6;
            resize: vertical;
          }
        }
      }
    }

    /* 调整对话框底部样式 */
    .adjust-footer {
      padding: 24px 32px;
      background: #f8fafc;
      border-top: 1px solid #e2e8f0;
      margin: 0 -24px -20px -24px;

      .footer-actions {
        display: flex;
        justify-content: center;
        gap: 16px;

        .cancel-btn {
          min-width: 120px;
        }

        .submit-btn {
          min-width: 140px;
        }
      }
    }

    /* 调整对话框移动端响应式 */
    @media (max-width: 768px) {
      .adjust-header {
        padding: 20px 16px;
        margin: -20px -16px 0 -16px;

        .title-icon {
          width: 40px;
          height: 40px;

          .el-icon {
            font-size: 20px;
          }
        }

        .title-content h2 {
          font-size: 20px;
        }
      }

      .adjust-content {
        padding: 24px 0;
      }

      .adjust-sections {
        gap: 16px;
      }

      .adjust-card .card-content {
        padding: 16px;
      }

      .adjust-form .form-grid {
        grid-template-columns: 1fr;
        gap: 20px;
      }

      .adjust-footer {
        padding: 16px;
        margin: 0 -16px -20px -16px;

        .footer-actions {
          flex-direction: column;
          gap: 12px;

          .cancel-btn,
          .submit-btn {
            width: 100%;
          }
        }
      }
    }

    /* 详情对话框样式 */
    .detail-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 24px 32px;
      margin: -20px -24px 0 -24px;
      border-radius: 12px 12px 0 0;

      .dialog-title {
        display: flex;
        align-items: center;
        gap: 16px;

        .title-icon {
          width: 48px;
          height: 48px;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          backdrop-filter: blur(10px);

          .el-icon {
            font-size: 24px;
            color: white;
          }
        }

        .title-content {
          h2 {
            margin: 0;
            font-size: 24px;
            font-weight: 600;
            color: white;
          }

          p {
            margin: 4px 0 0 0;
            font-size: 14px;
            color: rgba(255, 255, 255, 0.8);
          }
        }
      }

      .dialog-close {
        color: white;
        background: rgba(255, 255, 255, 0.1);
        border: 1px solid rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
        }
      }
    }

    .detail-content {
      padding: 32px 0;
      max-height: 65vh;
      overflow-y: auto;
    }

    .detail-sections {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 24px;
    }

    .detail-card {
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      border: 1px solid #f1f5f9;
      overflow: hidden;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
      }
    }

    .card-header {
      padding: 20px 24px 16px 24px;
      background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
      border-bottom: 1px solid #e2e8f0;
      display: flex;
      align-items: center;
      gap: 12px;

      .card-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        &.basic-info {
          background: linear-gradient(135deg, #3b82f6, #1d4ed8);
          color: white;
        }

        &.stock-info {
          background: linear-gradient(135deg, #10b981, #059669);
          color: white;
        }

        &.price-info {
          background: linear-gradient(135deg, #f59e0b, #d97706);
          color: white;
        }

        &.time-info {
          background: linear-gradient(135deg, #8b5cf6, #7c3aed);
          color: white;
        }

        .el-icon {
          font-size: 20px;
        }
      }

      .card-title {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #1e293b;
      }
    }

    .card-content {
      padding: 24px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
    }

    .info-item {
      .info-label {
        font-size: 12px;
        color: #64748b;
        font-weight: 500;
        margin-bottom: 6px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .info-value {
        font-size: 16px;
        color: #1e293b;
        font-weight: 600;

        &.code-text {
          font-family: 'Consolas', 'Monaco', monospace;
          background: #f1f5f9;
          padding: 4px 8px;
          border-radius: 6px;
          display: inline-block;
        }

        &.name-text {
          color: #3b82f6;
          font-weight: 700;
        }
      }
    }

    /* 库存信息特殊样式 */
    .stock-overview {
      display: flex;
      gap: 24px;
      align-items: center;

      .stock-main {
        text-align: center;
        padding: 20px;
        background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
        border-radius: 12px;
        border: 2px solid #0ea5e9;

        .stock-number {
          font-size: 36px;
          font-weight: 700;
          color: #0369a1;
          font-family: 'Consolas', 'Monaco', monospace;
          margin-bottom: 8px;
        }

        .stock-label {
          font-size: 14px;
          color: #0369a1;
          font-weight: 600;
        }
      }

      .stock-breakdown {
        flex: 1;
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
        gap: 16px;

        .stock-item {
          text-align: center;
          padding: 16px 12px;
          border-radius: 10px;
          transition: all 0.3s ease;

          &.available {
            background: linear-gradient(135deg, #f0fdf4, #dcfce7);
            border: 1px solid #22c55e;

            .stock-value {
              color: #15803d;
            }
          }

          &.locked {
            background: linear-gradient(135deg, #fef3c7, #fde68a);
            border: 1px solid #f59e0b;

            .stock-value {
              color: #d97706;
            }
          }

          &.status {
            background: linear-gradient(135deg, #f3f4f6, #e5e7eb);
            border: 1px solid #9ca3af;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 8px;
          }

          .stock-value {
            font-size: 24px;
            font-weight: 700;
            font-family: 'Consolas', 'Monaco', monospace;
            margin-bottom: 4px;
          }

          .stock-name {
            font-size: 12px;
            color: #64748b;
            font-weight: 500;
          }
        }
      }
    }

    /* 价格信息特殊样式 */
    .price-overview {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24px;

      .price-item {
        text-align: center;
        padding: 24px 20px;
        border-radius: 12px;
        transition: all 0.3s ease;

        &.unit-price {
          background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
          border: 2px solid #0ea5e9;

          .price-value {
            color: #0369a1;
          }
        }

        &.total-value {
          background: linear-gradient(135deg, #fef2f2, #fee2e2);
          border: 2px solid #ef4444;

          .price-value {
            color: #dc2626;
          }
        }

        .price-value {
          font-size: 28px;
          font-weight: 700;
          font-family: 'Consolas', 'Monaco', monospace;
          margin-bottom: 8px;

          &.total {
            font-size: 32px;
          }
        }

        .price-label {
          font-size: 14px;
          color: #64748b;
          font-weight: 600;
        }
      }
    }

    /* 对话框底部样式 */
    .detail-footer {
      padding: 24px 32px;
      background: #f8fafc;
      border-top: 1px solid #e2e8f0;
      margin: 0 -24px -20px -24px;

      .footer-actions {
        display: flex;
        justify-content: center;
        align-items: center;

        .primary-actions {
          display: flex;
          gap: 12px;
          align-items: center;
        }
      }
    }

    /* 移动端响应式 */
    @media (max-width: 768px) {
      .detail-sections {
        grid-template-columns: 1fr;
        gap: 16px;
      }

      .detail-header {
        padding: 20px 16px;
        margin: -20px -16px 0 -16px;

        .title-icon {
          width: 40px;
          height: 40px;

          .el-icon {
            font-size: 20px;
          }
        }

        .title-content h2 {
          font-size: 20px;
        }
      }

      .card-content {
        padding: 16px;
      }

      .info-grid {
        grid-template-columns: 1fr;
        gap: 16px;
      }

      .stock-overview {
        flex-direction: column;
        gap: 16px;

        .stock-breakdown {
          grid-template-columns: repeat(2, 1fr);
        }
      }

      .price-overview {
        grid-template-columns: 1fr;
        gap: 16px;
      }

      .detail-footer {
        padding: 16px;
        margin: 0 -16px -20px -16px;

        .footer-actions {
          justify-content: center;

          .primary-actions {
            flex-direction: column;
            width: 100%;
            gap: 8px;
          }
        }
      }
    }
  }
}

/* 字段锁定提示 */
.field-lock-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef3c7;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  font-size: 12px;
  color: #92400e;

  .el-icon {
    font-size: 14px;
  }
}

/* 修复弹窗内下拉框z-index问题 */
:deep(.adjust-dialog-select) {
  z-index: 5000 !important;
}

/* 全局修复弹窗内下拉框问题 */
.modern-dialog {
  :deep(.el-select-dropdown) {
    z-index: 5000 !important;
  }

  :deep(.el-popper) {
    z-index: 5000 !important;
  }
}

/* 更强的全局下拉框修复 */
:deep(.el-select-dropdown.adjust-dialog-select) {
  z-index: 5000 !important;
}

:deep(.el-popper.adjust-dialog-select) {
  z-index: 5000 !important;
}

/* Element Plus 下拉框全局修复 */
:deep(.el-select-dropdown) {
  z-index: 5000 !important;
}

:deep(.el-popper[data-popper-placement]) {
  z-index: 5000 !important;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .inventory-container {
    padding: 15px;
  }

  .modern-header {
    padding: 20px;
    margin-bottom: 20px;

    .header-content {
      flex-direction: column;
      text-align: center;
      gap: 16px;
    }

    .header-left {
      flex-direction: column;
      gap: 12px;
    }

    .header-icon {
      width: 50px;
      height: 50px;
      font-size: 24px;
    }

    .header-text {
      .header-title {
        font-size: 24px;
      }

      .header-subtitle {
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      flex-direction: row;
      gap: 8px;
      width: 100%;
      justify-content: center;

      .el-button {
        flex: 1;
        height: 40px;
        padding: 10px 16px;
        font-size: 14px;
      }
    }
  }

  .search-card {
    padding: 16px;
  }

  .search-title {
    font-size: 16px;
    margin-bottom: 12px;
  }

  .search-content {
    .search-form {
      .el-row {
        .el-col {
          margin-bottom: 16px;
        }
      }

      .search-item {
        .el-form-item__label {
          margin-bottom: 6px;
          font-size: 13px;
        }
      }

      .search-actions {
        display: flex;
        flex-direction: row;
        gap: 10px;
        margin-top: 16px;
        justify-content: center;

        .search-btn,
        .reset-btn {
          flex: 1;
          height: 38px;
          font-size: 14px;
        }
      }

      /* 移动端搜索按钮单独成行 */
      .el-row:last-child {
        margin-top: 16px;

        .el-col {
          margin-bottom: 0;
        }
      }
    }
  }

  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
    min-height: 70px;

    .stat-icon {
      width: 40px;
      height: 40px;
      font-size: 18px;
    }

    .stat-content {
      .stat-value {
        font-size: 24px;
      }

      .stat-label {
        font-size: 13px;
      }
    }

    .stat-trend {
      font-size: 14px;
    }
  }

  .table-card {
    .table-header {
      padding: 20px 20px 0 20px;
    }

    .table-content {
      padding: 0 20px 20px 20px;
    }

    .pagination-wrapper {
      padding: 15px 20px;
    }
  }

  .modern-table {
    font-size: 12px;

    .el-table__cell {
      padding: 8px 4px;
    }
  }

  .action-buttons {
    &.desktop-actions {
      flex-direction: row;
      flex-wrap: nowrap;
      gap: 3px;
      justify-content: flex-start;

      .action-btn {
        min-width: 50px;
        font-size: 11px;
        padding: 4px 6px;
        height: 26px;

        .el-icon {
          font-size: 11px;
        }
      }
    }
  }

  .mobile-actions {
    display: flex;
    justify-content: center;
    width: 100%;

    .mobile-action-trigger {
      width: 100%;
      font-size: 12px;
      padding: 6px 12px;
    }
  }

  .pagination-container {
    padding: 16px;

    .inventory-pagination {
      :deep(.el-pagination) {
        justify-content: center;
      }

      .el-pagination__sizes,
      .el-pagination__total,
      .el-pagination__jump {
        display: none;
      }
    }
  }

  /* 对话框移动端优化 */
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

<!-- 全局样式修复下拉框z-index问题和详情对话框样式 -->
<style>
/* 强制修复Element Plus下拉框z-index */
.el-select-dropdown {
  z-index: 5000 !important;
}

/* 详情对话框样式 - 全局应用 */
.detail-dialog .detail-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.detail-dialog .detail-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.detail-dialog .detail-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.detail-dialog .detail-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.detail-dialog .detail-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.detail-dialog .detail-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.detail-dialog .detail-header {
  position: relative !important;
}

.detail-dialog .detail-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.detail-dialog .detail-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.detail-dialog .detail-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.detail-dialog .detail-content {
  padding: 32px 0 !important;
  max-height: 65vh !important;
  overflow-y: auto !important;
}

.detail-dialog .detail-sections {
  display: grid !important;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)) !important;
  gap: 24px !important;
}

.detail-dialog .detail-card {
  background: white !important;
  border-radius: 16px !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid #f1f5f9 !important;
  overflow: hidden !important;
  transition: all 0.3s ease !important;
}

.detail-dialog .detail-card:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12) !important;
}

.detail-dialog .card-header {
  padding: 20px 24px 16px 24px !important;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%) !important;
  border-bottom: 1px solid #e2e8f0 !important;
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
}

.detail-dialog .card-icon {
  width: 40px !important;
  height: 40px !important;
  border-radius: 10px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.detail-dialog .card-icon.basic-info {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8) !important;
  color: white !important;
}

.detail-dialog .card-icon.stock-info {
  background: linear-gradient(135deg, #10b981, #059669) !important;
  color: white !important;
}

.detail-dialog .card-icon.price-info {
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
  color: white !important;
}

.detail-dialog .card-icon.time-info {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed) !important;
  color: white !important;
}

.detail-dialog .card-icon .el-icon {
  font-size: 20px !important;
}

.detail-dialog .card-title {
  margin: 0 !important;
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #1e293b !important;
}

.detail-dialog .card-content {
  padding: 24px !important;
}

.detail-dialog .info-grid {
  display: grid !important;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)) !important;
  gap: 20px !important;
}

.detail-dialog .info-item .info-label {
  font-size: 12px !important;
  color: #64748b !important;
  font-weight: 500 !important;
  margin-bottom: 6px !important;
  text-transform: uppercase !important;
  letter-spacing: 0.5px !important;
}

.detail-dialog .info-item .info-value {
  font-size: 16px !important;
  color: #1e293b !important;
  font-weight: 600 !important;
}

.detail-dialog .info-item .info-value.code-text {
  font-family: 'Consolas', 'Monaco', monospace !important;
  background: #f1f5f9 !important;
  padding: 4px 8px !important;
  border-radius: 6px !important;
  display: inline-block !important;
}

.detail-dialog .info-item .info-value.name-text {
  color: #3b82f6 !important;
  font-weight: 700 !important;
}

/* 库存信息特殊样式 */
.detail-dialog .stock-overview {
  display: flex !important;
  gap: 24px !important;
  align-items: center !important;
}

.detail-dialog .stock-main {
  text-align: center !important;
  padding: 20px !important;
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe) !important;
  border-radius: 12px !important;
  border: 2px solid #0ea5e9 !important;
}

.detail-dialog .stock-main .stock-number {
  font-size: 36px !important;
  font-weight: 700 !important;
  color: #0369a1 !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  margin-bottom: 8px !important;
}

.detail-dialog .stock-main .stock-label {
  font-size: 14px !important;
  color: #0369a1 !important;
  font-weight: 600 !important;
}

.detail-dialog .stock-breakdown {
  flex: 1 !important;
  display: grid !important;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)) !important;
  gap: 16px !important;
}

.detail-dialog .stock-item {
  text-align: center !important;
  padding: 16px 12px !important;
  border-radius: 10px !important;
  transition: all 0.3s ease !important;
}

.detail-dialog .stock-item.available {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7) !important;
  border: 1px solid #22c55e !important;
}

.detail-dialog .stock-item.available .stock-value {
  color: #15803d !important;
}

.detail-dialog .stock-item.locked {
  background: linear-gradient(135deg, #fef3c7, #fde68a) !important;
  border: 1px solid #f59e0b !important;
}

.detail-dialog .stock-item.locked .stock-value {
  color: #d97706 !important;
}

.detail-dialog .stock-item.status {
  background: linear-gradient(135deg, #f3f4f6, #e5e7eb) !important;
  border: 1px solid #9ca3af !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  gap: 8px !important;
}

.detail-dialog .stock-value {
  font-size: 24px !important;
  font-weight: 700 !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  margin-bottom: 4px !important;
}

.detail-dialog .stock-name {
  font-size: 12px !important;
  color: #64748b !important;
  font-weight: 500 !important;
}

/* 价格信息特殊样式 */
.detail-dialog .price-overview {
  display: grid !important;
  grid-template-columns: 1fr 1fr !important;
  gap: 24px !important;
}

.detail-dialog .price-item {
  text-align: center !important;
  padding: 24px 20px !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
}

.detail-dialog .price-item.unit-price {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe) !important;
  border: 2px solid #0ea5e9 !important;
}

.detail-dialog .price-item.unit-price .price-value {
  color: #0369a1 !important;
}

.detail-dialog .price-item.total-value {
  background: linear-gradient(135deg, #fef2f2, #fee2e2) !important;
  border: 2px solid #ef4444 !important;
}

.detail-dialog .price-item.total-value .price-value {
  color: #dc2626 !important;
}

.detail-dialog .price-value {
  font-size: 28px !important;
  font-weight: 700 !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
  margin-bottom: 8px !important;
}

.detail-dialog .price-value.total {
  font-size: 32px !important;
}

.detail-dialog .price-label {
  font-size: 14px !important;
  color: #64748b !important;
  font-weight: 600 !important;
}

/* 对话框底部样式 */
.detail-dialog .detail-footer {
  padding: 24px 32px !important;
  background: #f8fafc !important;
  border-top: 1px solid #e2e8f0 !important;
  margin: 0 -24px -20px -24px !important;
}

.detail-dialog .footer-actions {
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
}

.detail-dialog .primary-actions {
  display: flex !important;
  gap: 12px !important;
  align-items: center !important;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .detail-dialog .detail-sections {
    grid-template-columns: 1fr !important;
    gap: 16px !important;
  }

  .detail-dialog .detail-header {
    padding: 20px 16px !important;
    margin: -20px -16px 0 -16px !important;
  }

  .detail-dialog .title-icon {
    width: 40px !important;
    height: 40px !important;
  }

  .detail-dialog .title-icon .el-icon {
    font-size: 20px !important;
  }

  .detail-dialog .title-content h2 {
    font-size: 20px !important;
  }

  .detail-dialog .card-content {
    padding: 16px !important;
  }

  .detail-dialog .info-grid {
    grid-template-columns: 1fr !important;
    gap: 16px !important;
  }

  .detail-dialog .stock-overview {
    flex-direction: column !important;
    gap: 16px !important;
  }

  .detail-dialog .stock-breakdown {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  .detail-dialog .price-overview {
    grid-template-columns: 1fr !important;
    gap: 16px !important;
  }

  .detail-dialog .detail-footer {
    padding: 16px !important;
    margin: 0 -16px -20px -16px !important;
  }

  .detail-dialog .footer-actions {
    justify-content: center !important;
  }

  .detail-dialog .primary-actions {
    flex-direction: column !important;
    width: 100% !important;
    gap: 8px !important;
  }
}

.el-popper {
  z-index: 5000 !important;
}

.el-popper[data-popper-placement] {
  z-index: 5000 !important;
}

/* 特别针对弹窗内的下拉框 */
.el-dialog .el-select-dropdown {
  z-index: 5000 !important;
}

.el-dialog .el-popper {
  z-index: 5000 !important;
}

/* 针对adjust-dialog-select类的下拉框 */
.adjust-dialog-select {
  z-index: 5000 !important;
}

/* Element Plus组件的下拉菜单 */
.el-select-dropdown.el-popper {
  z-index: 5000 !important;
}

.el-select-dropdown.el-popper[data-popper-placement] {
  z-index: 5000 !important;
}

/* 最强修复：直接覆盖所有可能的选择器 */
div.el-select-dropdown {
  z-index: 5000 !important;
}

/* 价格样式 - 全局生效 */
.price-text {
  color: #059669 !important;
  font-weight: 600 !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
}

.value-text {
  color: #dc2626 !important;
  font-weight: 600 !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
}

div.el-popper.el-select-dropdown {
  z-index: 5000 !important;
}

/* 针对弹窗内的选择器 */
.el-dialog__wrapper .el-select-dropdown {
  z-index: 5000 !important;
}

.modern-dialog .el-select-dropdown {
  z-index: 5000 !important;
}

/* 库存调整对话框样式 - 全局应用 */
.adjust-dialog .adjust-header {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.adjust-dialog .adjust-header {
  position: relative !important;
}

.adjust-dialog .adjust-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.adjust-dialog .adjust-header .dialog-close {
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.adjust-dialog .adjust-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.adjust-dialog .adjust-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.adjust-dialog .adjust-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.adjust-dialog .adjust-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.adjust-dialog .adjust-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.adjust-dialog .adjust-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
}

.adjust-dialog .adjust-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.adjust-dialog .adjust-content {
  padding: 32px 0 !important;
  max-height: 65vh !important;
  overflow-y: auto !important;
}

.adjust-dialog .adjust-sections {
  display: flex !important;
  flex-direction: column !important;
  gap: 24px !important;
}

.adjust-dialog .adjust-card {
  background: white !important;
  border-radius: 16px !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid #f1f5f9 !important;
  overflow: hidden !important;
  transition: all 0.3s ease !important;
}

.adjust-dialog .adjust-card:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12) !important;
}

.adjust-dialog .adjust-card .card-header {
  padding: 20px 24px 16px 24px !important;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%) !important;
  border-bottom: 1px solid #e2e8f0 !important;
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
}

.adjust-dialog .adjust-card .card-icon {
  width: 40px !important;
  height: 40px !important;
  border-radius: 10px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.adjust-dialog .adjust-card .card-icon.basic {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8) !important;
  color: white !important;
}

.adjust-dialog .adjust-card .card-icon.adjust {
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
  color: white !important;
}

.adjust-dialog .adjust-card .card-icon.reason {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed) !important;
  color: white !important;
}

.adjust-dialog .adjust-card .card-icon .el-icon {
  font-size: 20px !important;
}

.adjust-dialog .adjust-card .card-title {
  margin: 0 !important;
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #1e293b !important;
}

.adjust-dialog .adjust-card .card-content {
  padding: 24px !important;
}

.adjust-dialog .adjust-form .form-grid {
  display: grid !important;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)) !important;
  gap: 24px !important;
}

.adjust-dialog .adjust-form .form-item .form-label {
  font-size: 14px !important;
  font-weight: 600 !important;
  color: #374151 !important;
  margin-bottom: 12px !important;
  display: block !important;
}

.adjust-dialog .adjust-form .form-item .form-select {
  width: 100% !important;
}

.adjust-dialog .adjust-form .form-item .field-lock-tip {
  margin-top: 8px !important;
  padding: 8px 12px !important;
  background: #fef3c7 !important;
  border: 1px solid #f59e0b !important;
  border-radius: 6px !important;
  display: flex !important;
  align-items: center !important;
  gap: 6px !important;
  font-size: 12px !important;
  color: #92400e !important;
}

.adjust-dialog .adjust-form .form-item .field-lock-tip .el-icon {
  font-size: 14px !important;
}

.adjust-dialog .adjust-form .form-item .radio-group {
  display: flex !important;
  flex-direction: column !important;
  gap: 12px !important;
}

.adjust-dialog .adjust-form .form-item .radio-group .radio-item {
  margin: 0 !important;
  padding: 16px !important;
  border: 2px solid #e5e7eb !important;
  border-radius: 12px !important;
  transition: all 0.3s ease !important;
  cursor: pointer !important;
}

.adjust-dialog .adjust-form .form-item .radio-group .radio-item:hover {
  border-color: #f59e0b !important;
  background: #fef3c7 !important;
}

.adjust-dialog .adjust-form .form-item .radio-group .radio-item.is-checked {
  border-color: #f59e0b !important;
  background: linear-gradient(135deg, #fef3c7, #fde68a) !important;
}

.adjust-dialog .adjust-form .form-item .radio-group .radio-item .radio-content {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
}

.adjust-dialog .adjust-form .form-item .radio-group .radio-item .radio-content .el-icon {
  font-size: 20px !important;
  color: #6b7280 !important;
}

.adjust-dialog .adjust-form .form-item .radio-group .radio-item .radio-content span {
  font-weight: 500 !important;
  color: #374151 !important;
}

.adjust-dialog .adjust-form .form-item .quantity-input {
  width: 100% !important;
}

.adjust-dialog .adjust-form .form-item .quantity-input .el-input__inner {
  text-align: center !important;
  font-size: 18px !important;
  font-weight: 600 !important;
  font-family: 'Consolas', 'Monaco', monospace !important;
}

.adjust-dialog .adjust-form .form-item .reason-textarea .el-textarea__inner {
  font-size: 14px !important;
  line-height: 1.6 !important;
  resize: vertical !important;
}

.adjust-dialog .adjust-footer {
  padding: 24px 32px !important;
  background: #f8fafc !important;
  border-top: 1px solid #e2e8f0 !important;
  margin: 0 -24px -20px -24px !important;
}

.adjust-dialog .adjust-footer .footer-actions {
  display: flex !important;
  justify-content: center !important;
  gap: 16px !important;
}

.adjust-dialog .adjust-footer .footer-actions .cancel-btn {
  min-width: 120px !important;
}

.adjust-dialog .adjust-footer .footer-actions .submit-btn {
  min-width: 140px !important;
}

/* 调整对话框移动端响应式 */
@media (max-width: 768px) {
  .adjust-dialog .adjust-header {
    padding: 20px 16px !important;
    margin: -20px -16px 0 -16px !important;
  }

  .adjust-dialog .adjust-header .title-icon {
    width: 40px !important;
    height: 40px !important;
  }

  .adjust-dialog .adjust-header .title-icon .el-icon {
    font-size: 20px !important;
  }

  .adjust-dialog .adjust-header .title-content h2 {
    font-size: 20px !important;
  }

  .adjust-dialog .adjust-content {
    padding: 24px 0 !important;
  }

  .adjust-dialog .adjust-sections {
    gap: 16px !important;
  }

  .adjust-dialog .adjust-card .card-content {
    padding: 16px !important;
  }

  .adjust-dialog .adjust-form .form-grid {
    grid-template-columns: 1fr !important;
    gap: 20px !important;
  }

  .adjust-dialog .adjust-footer {
    padding: 16px !important;
    margin: 0 -16px -20px -16px !important;
  }

  .adjust-dialog .adjust-footer .footer-actions {
    flex-direction: column !important;
    gap: 12px !important;
  }

  .adjust-dialog .adjust-footer .footer-actions .cancel-btn,
  .adjust-dialog .adjust-footer .footer-actions .submit-btn {
    width: 100% !important;
  }
}

/* 库存历史对话框样式 - 全局应用 */
.history-dialog .history-header {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.history-dialog .history-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.history-dialog .history-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.history-dialog .history-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.history-dialog .history-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.history-dialog .history-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.history-dialog .history-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.history-dialog .history-header {
  position: relative !important;
}

.history-dialog .history-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.history-dialog .history-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.history-dialog .history-content {
  padding: 32px 0 !important;
  max-height: 70vh !important;
  overflow-y: auto !important;
}
</style>
