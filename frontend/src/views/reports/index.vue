<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">报表统计</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" inline>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="仓库" v-if="userStore.userInfo?.role === 'ROLE_ADMIN'">
          <el-select v-model="filterForm.warehouseId" placeholder="请选择仓库" clearable style="width: 150px" @change="loadData">
            <el-option label="全部仓库" :value="null" />
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
        <el-form-item label="快捷选择">
          <el-button-group>
            <el-button @click="setQuickDate('today')">今天</el-button>
            <el-button @click="setQuickDate('week')">本周</el-button>
            <el-button @click="setQuickDate('month')">本月</el-button>
            <el-button @click="setQuickDate('quarter')">本季度</el-button>
            <el-button @click="setQuickDate('year')">本年</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-overview">
      <div class="stat-card" v-for="stat in overviewStats" :key="stat.key">
        <div class="stat-icon" :class="stat.type">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="stat.changeType">
            <el-icon><component :is="stat.changeIcon" /></el-icon>
            {{ stat.change }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 第一行图表 -->
      <div class="chart-row">
        <!-- 业务趋势图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>业务趋势分析</h3>
            <el-radio-group v-model="trendType" size="small" @change="loadTrendData">
              <el-radio-button label="quantity">数量</el-radio-button>
              <el-radio-button label="amount">金额</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-content">
            <v-chart :option="trendChartOption" style="height: 350px;" />
          </div>
        </div>

        <!-- 业务分布饼图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>业务类型分布</h3>
            <el-select v-model="distributionType" size="small" @change="loadDistributionData">
              <el-option label="入库类型" value="inbound" />
              <el-option label="出库类型" value="outbound" />
              <el-option label="仓库分布" value="warehouse" />
            </el-select>
          </div>
          <div class="chart-content">
            <v-chart :option="distributionChartOption" style="height: 350px;" />
          </div>
        </div>
      </div>

      <!-- 第二行图表 -->
      <div class="chart-row">
        <!-- 库存周转分析 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>库存周转分析</h3>
            <el-select v-model="turnoverPeriod" size="small" @change="loadTurnoverData">
              <el-option label="近30天" value="30" />
              <el-option label="近60天" value="60" />
              <el-option label="近90天" value="90" />
            </el-select>
          </div>
          <div class="chart-content">
            <v-chart :option="turnoverChartOption" style="height: 350px;" />
          </div>
        </div>

        <!-- 货物流动热力图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>货物流动热力图</h3>
            <span class="chart-subtitle">显示最活跃的货物</span>
          </div>
          <div class="chart-content">
            <v-chart :option="heatmapChartOption" style="height: 350px;" />
          </div>
        </div>
      </div>

      <!-- 第三行图表 -->
      <div class="chart-row">
        <!-- 仓库利用率 -->
        <div class="chart-card full-width">
          <div class="chart-header">
            <h3>仓库利用率分析</h3>
            <div class="chart-actions">
              <el-switch
                v-model="showUtilizationDetails"
                active-text="显示详情"
                inactive-text="简化视图"
                @change="loadUtilizationData"
              />
            </div>
          </div>
          <div class="chart-content">
            <v-chart :option="utilizationChartOption" style="height: 400px;" />
          </div>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="data-tables">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 业务汇总 -->
        <el-tab-pane label="业务汇总" name="business">
          <el-table :data="businessSummary" border style="width: 100%">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="inboundCount" label="入库单数" width="100" align="right" />
            <el-table-column prop="inboundQuantity" label="入库数量" width="100" align="right" />
            <el-table-column prop="inboundAmount" label="入库金额" width="120" align="right">
              <template #default="{ row }">
                ¥{{ row.inboundAmount?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="outboundCount" label="出库单数" width="100" align="right" />
            <el-table-column prop="outboundQuantity" label="出库数量" width="100" align="right" />
            <el-table-column prop="outboundAmount" label="出库金额" width="120" align="right">
              <template #default="{ row }">
                ¥{{ row.outboundAmount?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="transferCount" label="调拨单数" width="100" align="right" />
            <el-table-column prop="stocktakeCount" label="盘点单数" width="100" align="right" />
          </el-table>
        </el-tab-pane>

        <!-- 库存分析 -->
        <el-tab-pane label="库存分析" name="inventory">
          <el-table :data="inventoryAnalysis" border style="width: 100%">
            <el-table-column prop="warehouseName" label="仓库" width="120" />
            <el-table-column prop="goodsCount" label="货物种类" width="100" align="right" />
            <el-table-column prop="totalQuantity" label="总库存" width="100" align="right" />
            <el-table-column prop="totalValue" label="库存价值" width="120" align="right">
              <template #default="{ row }">
                ¥{{ row.totalValue?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="turnoverRate" label="周转率" width="100" align="right">
              <template #default="{ row }">
                {{ row.turnoverRate?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="utilizationRate" label="利用率" width="100" align="right">
              <template #default="{ row }">
                {{ (row.utilizationRate * 100)?.toFixed(1) || '0.0' }}%
              </template>
            </el-table-column>
            <el-table-column prop="alertCount" label="预警数量" width="100" align="right">
              <template #default="{ row }">
                <span :class="row.alertCount > 0 ? 'text-danger' : ''">
                  {{ row.alertCount || 0 }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 货物排行 -->
        <el-tab-pane label="货物排行" name="goods">
          <div class="ranking-controls">
            <el-radio-group v-model="rankingType" @change="loadGoodsRanking">
              <el-radio-button label="inbound">入库排行</el-radio-button>
              <el-radio-button label="outbound">出库排行</el-radio-button>
              <el-radio-button label="turnover">周转排行</el-radio-button>
            </el-radio-group>
          </div>
          <el-table :data="goodsRanking" border style="width: 100%">
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="goodsCode" label="货物编码" width="120" />
            <el-table-column prop="goodsName" label="货物名称" min-width="150" />
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column prop="quantity" label="数量" width="100" align="right" />
            <el-table-column prop="amount" label="金额" width="120" align="right">
              <template #default="{ row }">
                ¥{{ row.amount?.toFixed(2) || '0.00' }}
              </template>
            </el-table-column>
            <el-table-column prop="percentage" label="占比" width="100" align="right">
              <template #default="{ row }">
                {{ row.percentage?.toFixed(1) || '0.0' }}%
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 异常分析 -->
        <el-tab-pane label="异常分析" name="exception">
          <el-table :data="exceptionAnalysis" border style="width: 100%">
            <el-table-column prop="type" label="异常类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getExceptionType(row.type)">
                  {{ getExceptionText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="异常描述" min-width="200" />
            <el-table-column prop="count" label="发生次数" width="100" align="right" />
            <el-table-column prop="lastOccurrence" label="最后发生时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.lastOccurrence) }}
              </template>
            </el-table-column>
            <el-table-column prop="impact" label="影响程度" width="100">
              <template #default="{ row }">
                <el-tag :type="getImpactType(row.impact)">
                  {{ row.impact }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="handleViewException(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { request } from '@/utils/request'
import { useUserStore } from '@/stores/user'
// VChart 已在 main.js 中全局注册
import dayjs from 'dayjs'
import { useDeviceDetection } from '@/utils/responsive'

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 响应式数据
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('business')
const trendType = ref('quantity')
const distributionType = ref('inbound')
const turnoverPeriod = ref('30')
const rankingType = ref('inbound')
const showUtilizationDetails = ref(false)

const warehouses = ref([])
const overviewStats = ref([])
const businessSummary = ref([])
const inventoryAnalysis = ref([])
const goodsRanking = ref([])
const exceptionAnalysis = ref([])

// 图表数据
const trendData = ref([])
const distributionData = ref([])
const turnoverData = ref([])
const heatmapData = ref([])
const utilizationData = ref([])

// 筛选表单
const filterForm = reactive({
  dateRange: [
    dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
    dayjs().format('YYYY-MM-DD')
  ],
  warehouseId: null
})

// 图表配置
const trendChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  legend: {
    data: ['入库', '出库', '调拨']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: trendData.value.map(item => item.date)
  },
  yAxis: {
    type: 'value',
    name: trendType.value === 'quantity' ? '数量' : '金额(元)'
  },
  series: [
    {
      name: '入库',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => trendType.value === 'quantity' ? item.inboundQuantity : item.inboundAmount),
      itemStyle: { color: '#67c23a' }
    },
    {
      name: '出库',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => trendType.value === 'quantity' ? item.outboundQuantity : item.outboundAmount),
      itemStyle: { color: '#f56c6c' }
    },
    {
      name: '调拨',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => trendType.value === 'quantity' ? item.transferQuantity : item.transferAmount),
      itemStyle: { color: '#409eff' }
    }
  ]
}))

const distributionChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: getDistributionTitle(),
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '18',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: distributionData.value
    }
  ]
}))

const turnoverChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: turnoverData.value.map(item => item.goodsName)
  },
  yAxis: {
    type: 'value',
    name: '周转率'
  },
  series: [
    {
      name: '周转率',
      type: 'bar',
      data: turnoverData.value.map(item => item.turnoverRate),
      itemStyle: {
        color: '#409eff'
      }
    }
  ]
}))

const heatmapChartOption = computed(() => {
  // 确保数据存在且为数组
  const data = heatmapData.value || []

  return {
    tooltip: {
      position: 'top'
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.goodsName || ''),
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: 100,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%'
    },
    series: [
      {
        name: '活跃度',
        type: 'heatmap',
        data: data.flatMap((goods, goodsIndex) =>
          (goods.weekData || []).map((value, dayIndex) => [dayIndex, goodsIndex, value || 0])
        ),
        label: {
          show: true
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
})

const utilizationChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {
    data: ['已用容量', '剩余容量']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: utilizationData.value.map(item => item.warehouseName)
  },
  yAxis: {
    type: 'value',
    name: '容量'
  },
  series: [
    {
      name: '已用容量',
      type: 'bar',
      stack: 'total',
      data: utilizationData.value.map(item => item.usedCapacity),
      itemStyle: { color: '#409eff' }
    },
    {
      name: '剩余容量',
      type: 'bar',
      stack: 'total',
      data: utilizationData.value.map(item => item.remainingCapacity),
      itemStyle: { color: '#e6e6e6' }
    }
  ]
}))

// 方法
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const getDistributionTitle = () => {
  const titleMap = {
    'inbound': '入库类型分布',
    'outbound': '出库类型分布',
    'warehouse': '仓库分布'
  }
  return titleMap[distributionType.value] || '分布'
}

const getExceptionType = (type) => {
  const typeMap = {
    'stock_shortage': 'danger',
    'overstock': 'warning',
    'expired_batch': 'info',
    'system_error': 'danger'
  }
  return typeMap[type] || 'info'
}

const getExceptionText = (type) => {
  const textMap = {
    'stock_shortage': '库存不足',
    'overstock': '库存积压',
    'expired_batch': '批次过期',
    'system_error': '系统异常'
  }
  return textMap[type] || type
}

const getImpactType = (impact) => {
  const typeMap = {
    '高': 'danger',
    '中': 'warning',
    '低': 'success'
  }
  return typeMap[impact] || 'info'
}

const getCurrentWarehouseName = () => {
  if (userStore.warehouses && userStore.warehouses.length > 0) {
    return userStore.warehouses[0].name
  }
  return '主仓库'
}

const setQuickDate = (type) => {
  const today = dayjs()
  switch (type) {
    case 'today':
      filterForm.dateRange = [today.format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
      break
    case 'week':
      filterForm.dateRange = [today.startOf('week').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
      break
    case 'month':
      filterForm.dateRange = [today.startOf('month').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
      break
    case 'quarter':
      filterForm.dateRange = [today.startOf('quarter').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
      break
    case 'year':
      filterForm.dateRange = [today.startOf('year').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
      break
  }
  loadData()
}

const handleDateChange = () => {
  loadData()
}

const handleTabChange = (tabName) => {
  activeTab.value = tabName
  loadTabData(tabName)
}

const handleRefresh = () => {
  loadData()
}

const handleExport = async () => {
  try {
    const params = {
      ...filterForm,
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1]
    }
    
    const response = await request.download('/reports/export', params)
    // 处理文件下载
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `统计报表_${dayjs().format('YYYY-MM-DD')}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('报表导出成功')
  } catch (error) {
    ElMessage.error('导出报表失败')
  }
}

const handleViewException = () => {
  // 查看异常详情
  ElMessage.info('查看异常详情功能待实现')
}

// 数据加载方法
const loadData = async () => {
  await Promise.all([
    loadOverviewStats(),
    loadTrendData(),
    loadDistributionData(),
    loadTurnoverData(),
    loadHeatmapData(),
    loadUtilizationData(),
    loadTabData(activeTab.value)
  ])
}

const loadOverviewStats = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId
    }
    
    const response = await request.get('/reports/overview', params)
    if (response.success) {
      const data = response.data
      overviewStats.value = [
        {
          key: 'totalInbound',
          label: '入库总量',
          value: data.totalInbound || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
          icon: 'Download',
          type: 'success'
        },
        {
          key: 'totalOutbound',
          label: '出库总量',
          value: data.totalOutbound || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
          icon: 'Upload',
          type: 'warning'
        },
        {
          key: 'totalTransfer',
          label: '调拨总量',
          value: data.totalTransfer || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
          icon: 'Switch',
          type: 'primary'
        },
        {
          key: 'totalStocktake',
          label: '盘点次数',
          value: data.totalStocktake || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
          icon: 'DocumentChecked',
          type: 'info'
        }
      ]
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    overviewStats.value = []
  }
}

const loadTrendData = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId,
      type: trendType.value
    }
    
    const response = await request.get('/reports/trend', params)
    if (response.success) {
      trendData.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    trendData.value = []
  }
}

const loadDistributionData = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId,
      type: distributionType.value
    }
    
    const response = await request.get('/reports/distribution', params)
    if (response.success) {
      distributionData.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    distributionData.value = []
  }
}

const loadTurnoverData = async () => {
  try {
    const params = {
      period: turnoverPeriod.value,
      warehouseId: filterForm.warehouseId
    }
    
    const response = await request.get('/reports/turnover', params)
    if (response.success) {
      turnoverData.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    turnoverData.value = []
  }
}

const loadHeatmapData = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId
    }
    const response = await request.get('/reports/heatmap', params)
    if (response.success) {
      heatmapData.value = response.data || []
    } else {
      // 如果响应不成功，设置空数组
      heatmapData.value = []
    }
  } catch (error) {
    // 设置空数组，避免undefined错误
    heatmapData.value = []
  }
}

const loadUtilizationData = async () => {
  try {
    const response = await request.get('/reports/utilization', { warehouseId: filterForm.warehouseId })
    if (response.success) {
      utilizationData.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    utilizationData.value = []
  }
}

const loadTabData = async (tabName) => {
  switch (tabName) {
    case 'business':
      await loadBusinessSummary()
      break
    case 'inventory':
      await loadInventoryAnalysis()
      break
    case 'goods':
      await loadGoodsRanking()
      break
    case 'exception':
      await loadExceptionAnalysis()
      break
  }
}

const loadBusinessSummary = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId
    }
    
    const response = await request.get('/reports/business-summary', params)
    if (response.success) {
      businessSummary.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    businessSummary.value = []
  }
}

const loadInventoryAnalysis = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId
    }
    const response = await request.get('/reports/inventory-analysis', params)
    if (response.success) {
      inventoryAnalysis.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    inventoryAnalysis.value = []
  }
}

const loadGoodsRanking = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId,
      type: rankingType.value
    }
    
    const response = await request.get('/reports/goods-ranking', params)
    if (response.success) {
      goodsRanking.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    goodsRanking.value = []
  }
}

const loadExceptionAnalysis = async () => {
  try {
    const params = {
      startDate: filterForm.dateRange[0],
      endDate: filterForm.dateRange[1],
      warehouseId: filterForm.warehouseId
    }
    const response = await request.get('/reports/exception-analysis', params)
    if (response.success) {
      exceptionAnalysis.value = response.data || []
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    exceptionAnalysis.value = []
  }
}

const loadWarehouses = async () => {
  try {
    const response = await request.get('/warehouses/all')
    if (response.success) {
      warehouses.value = response.data || []
      // 设置默认仓库为第一个仓库
      if (warehouses.value.length > 0 && !filterForm.warehouseId) {
        filterForm.warehouseId = warehouses.value[0].id
      }
    }
  } catch (error) {
    // 清空数据，不使用模拟数据
    warehouses.value = []
  }
}

// 监听器
watch(() => filterForm.warehouseId, (newWarehouseId, oldWarehouseId) => {
  // 只有当仓库ID真正改变时才重新加载数据
  if (newWarehouseId !== oldWarehouseId && newWarehouseId !== undefined) {
    loadData()
  }
})

// 生命周期
onMounted(async () => {
  await loadWarehouses()
  loadData()
})
</script>

<style lang="scss" scoped>
.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.charts-section {
  .chart-row {
    display: grid;
    gap: 20px;
    margin-bottom: 24px;

    &:nth-child(1),
    &:nth-child(2) {
      grid-template-columns: 2fr 1fr;
    }

    &:nth-child(3) {
      grid-template-columns: 1fr;
    }
  }

  .chart-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    &.full-width {
      grid-column: 1 / -1;
    }

    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }

      .chart-subtitle {
        font-size: 12px;
        color: #909399;
      }

      .chart-actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
  }
}

.data-tables {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .ranking-controls {
    margin-bottom: 16px;
  }
}

.text-danger {
  color: #f56c6c;
}

// 响应式设计
@media (max-width: 1200px) {
  .charts-section .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: 1fr;
  }

  .filter-section {
    .el-form--inline .el-form-item {
      display: block;
      margin-bottom: 16px;
    }
  }
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .reports-container {
    padding: 15px;

    .overview-section {
      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 15px;

        .stat-card {
          padding: 15px;

          .stat-value {
            font-size: 20px;
          }

          .stat-label {
            font-size: 12px;
          }
        }
      }
    }

    .charts-section {
      .chart-container {
        padding: 15px;

        .chart-header {
          flex-direction: column;
          gap: 15px;

          .chart-title {
            text-align: center;
          }

          .chart-controls {
            justify-content: center;
            flex-wrap: wrap;
            gap: 10px;

            .el-button {
              flex: 1;
              min-width: 80px;
            }
          }
        }

        .chart-content {
          height: 300px;
        }
      }
    }

    .el-tabs {
      .el-tabs__header {
        .el-tabs__nav-wrap {
          .el-tabs__nav {
            .el-tabs__item {
              padding: 0 15px;
              font-size: 14px;
            }
          }
        }
      }
    }

    .filter-section {
      .el-form--inline {
        .el-form-item {
          width: 100%;
          margin-bottom: 15px;

          .el-form-item__label {
            width: auto !important;
            margin-bottom: 5px;
          }

          .el-form-item__content {
            width: 100%;
            margin-left: 0 !important;

            .el-select,
            .el-date-picker {
              width: 100%;
            }
          }
        }
      }
    }
  }
}

/* 小屏手机优化 */
@media (max-width: 480px) {
  .reports-container {
    padding: 10px;

    .overview-section {
      .stats-grid {
        grid-template-columns: 1fr;
        gap: 10px;

        .stat-card {
          padding: 12px;
        }
      }
    }

    .charts-section {
      .chart-container {
        padding: 10px;

        .chart-content {
          height: 250px;
        }
      }
    }

    .el-tabs {
      .el-tabs__header {
        .el-tabs__nav-wrap {
          .el-tabs__nav {
            .el-tabs__item {
              padding: 0 10px;
              font-size: 12px;
            }
          }
        }
      }
    }
  }
}
</style>
