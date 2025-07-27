<template>
  <div class="dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">
          欢迎回来，{{ userStore.realName || userStore.username }}！
        </h1>
        <p class="welcome-subtitle">
          今天是 {{ currentDate }}，{{ getGreeting() }}
        </p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="$router.push('/inbound')">
          <el-icon><Download /></el-icon>
          新建入库单
        </el-button>
        <el-button type="success" @click="$router.push('/outbound')">
          <el-icon><Upload /></el-icon>
          新建出库单
        </el-button>
      </div>
    </div>

    <!-- 库房看板 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statsData" :key="stat.key">
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

    <!-- 图表和数据 -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- 业务趋势图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>业务趋势</h3>
            <el-radio-group v-model="trendPeriod" size="small">
              <el-radio-button label="7">近7天</el-radio-button>
              <el-radio-button label="30">近30天</el-radio-button>
              <el-radio-button label="90">近90天</el-radio-button>
            </el-radio-group>
          </div>
          <div class="chart-content">
            <v-chart
              ref="trendChart"
              :option="trendChartOption"
              style="height: 300px; width: 100%;"
              :autoresize="true"
            />
          </div>
        </div>

        <!-- 库存预警 -->
        <div class="alert-card">
          <div class="alert-header">
            <h3>库存预警</h3>
            <el-badge :value="alertCount" class="alert-badge">
              <el-button type="text" @click="$router.push('/inventory')">
                查看全部
              </el-button>
            </el-badge>
          </div>
          <div class="alert-content">
            <div v-if="alertList.length === 0" class="no-alerts">
              <el-icon><SuccessFilled /></el-icon>
              <p>暂无库存预警</p>
            </div>
            <div v-else class="alert-list">
              <div
                v-for="alert in alertList.slice(0, 5)"
                :key="alert.id"
                class="alert-item"
              >
                <div class="alert-info">
                  <div class="alert-goods">{{ alert.goodsName }}</div>
                  <div class="alert-warehouse">{{ alert.warehouseName }}</div>
                </div>
                <div class="alert-status">
                  <el-tag :type="alert.level === 'danger' ? 'danger' : 'warning'" size="small">
                    {{ alert.message }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <!-- 仓库分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>仓库库存分布</h3>
          </div>
          <div class="chart-content">
            <v-chart
              ref="warehouseChart"
              :option="warehouseChartOption"
              style="height: 300px; width: 100%;"
              :autoresize="true"
            />
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="todo-card">
          <div class="todo-header">
            <h3>待办事项</h3>
            <el-badge :value="todoCount" class="todo-badge" />
          </div>
          <div class="todo-content">
            <div v-if="todoList.length === 0" class="no-todos">
              <el-icon><CircleCheckFilled /></el-icon>
              <p>暂无待办事项</p>
            </div>
            <div v-else class="todo-list">
              <div
                v-for="todo in todoList.slice(0, 6)"
                :key="todo.id"
                class="todo-item"
                @click="handleTodoClick(todo)"
              >
                <div class="todo-info">
                  <div class="todo-title">{{ todo.title }}</div>
                  <div class="todo-desc">{{ todo.description }}</div>
                </div>
                <div class="todo-meta">
                  <el-tag :type="todo.priority" size="small">
                    {{ todo.priorityText }}
                  </el-tag>
                  <span class="todo-time">{{ formatTime(todo.createdTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-grid">
        <div
          v-for="action in quickActions"
          :key="action.key"
          class="action-item"
          @click="$router.push(action.path)"
        >
          <div class="action-icon" :class="action.type">
            <el-icon><component :is="action.icon" /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">{{ action.title }}</div>
            <div class="action-desc">{{ action.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'
import dashboardApi from '@/api/dashboard'
import cache from '@/utils/cache'
import dayjs from 'dayjs'
import { useDeviceDetection } from '@/utils/responsive'

const router = useRouter()
const userStore = useUserStore()

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 图表引用
const trendChart = ref(null)
const warehouseChart = ref(null)

// 响应式数据
const loading = ref(false)
const trendPeriod = ref('30')
const statsData = ref([])
const alertList = ref([])
const todoList = ref([])
const trendData = ref([])
const warehouseData = ref([])

// 计算属性
const currentDate = computed(() => {
  return dayjs().format('YYYY年MM月DD日 dddd')
})

const alertCount = computed(() => alertList.value.length)
const todoCount = computed(() => todoList.value.length)

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
    type: 'value'
  },
  series: [
    {
      name: '入库',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => item.inbound),
      itemStyle: { color: '#67c23a' }
    },
    {
      name: '出库',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => item.outbound),
      itemStyle: { color: '#f56c6c' }
    },
    {
      name: '调拨',
      type: 'line',
      smooth: true,
      data: trendData.value.map(item => item.transfer),
      itemStyle: { color: '#409eff' }
    }
  ]
}))

const warehouseChartOption = computed(() => ({
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
      name: '库存分布',
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
      data: warehouseData.value
    }
  ]
}))

// 快捷操作配置
const quickActions = [
  {
    key: 'inbound',
    title: '入库管理',
    description: '快速处理采购入库、归还入库等业务',
    icon: 'Download',
    type: 'success',
    path: '/inbound'
  },
  {
    key: 'outbound',
    title: '出库管理',
    description: '高效处理领用出库、销售出库等',
    icon: 'Upload',
    type: 'warning',
    path: '/outbound'
  },
  {
    key: 'transfer',
    title: '调拨管理',
    description: '灵活管理仓库间货物调拨转移',
    icon: 'Switch',
    type: 'primary',
    path: '/transfer'
  },
  {
    key: 'stocktake',
    title: '盘点管理',
    description: '精准进行库存盘点和差异处理',
    icon: 'DocumentChecked',
    type: 'info',
    path: '/stocktake'
  },
  {
    key: 'inventory',
    title: '库存查询',
    description: '实时查看库存信息和货物状态',
    icon: 'List',
    type: 'success',
    path: '/inventory'
  },
  {
    key: 'reports',
    title: '报表统计',
    description: '深度分析业务数据和趋势报表',
    icon: 'DataAnalysis',
    type: 'danger',
    path: '/reports'
  }
]

// 方法
const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了，注意休息'
}

const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm')
}

const handleTodoClick = (todo) => {
  // 根据待办类型跳转到相应页面
  switch (todo.type) {
    case 'inbound_approval':
      router.push(`/inbound?status=pending`)
      break
    case 'outbound_approval':
      router.push(`/outbound?status=pending`)
      break
    case 'transfer_approval':
      router.push(`/transfer?status=pending`)
      break
    case 'stocktake_approval':
      router.push(`/stocktake?status=pending`)
      break
    default:
      break
  }
}

// 加载数据 - 优化版本，使用缓存
const loadDashboardData = async () => {
  try {
    loading.value = true

    // 使用缓存并行加载所有数据
    const [statsRes, alertRes, todoRes, trendRes, warehouseRes] = await Promise.all([
      cache.getOrSet('dashboard-stats', () => dashboardApi.getStats()),
      cache.getOrSet('dashboard-alerts', () => dashboardApi.getAlerts()),
      cache.getOrSet('dashboard-todos', () => dashboardApi.getTodos()),
      cache.getOrSet(`business-trend-${trendPeriod.value}`, () => dashboardApi.getTrend(trendPeriod.value)),
      cache.getOrSet('warehouse-distribution', () => dashboardApi.getWarehouseDistribution())
    ])
    
    // 处理统计数据
    if (statsRes.success) {
      const stats = statsRes.data

      // 格式化增长率显示
      const formatGrowth = (growth) => {
        if (growth === 0) return { text: '持平', type: 'neutral', icon: 'Minus' }
        if (growth > 0) return { text: `+${growth}%`, type: 'positive', icon: 'ArrowUp' }
        return { text: `${growth}%`, type: 'negative', icon: 'ArrowDown' }
      }

      const goodsGrowth = formatGrowth(stats.goodsGrowth || 0)
      const inventoryGrowth = formatGrowth(stats.inventoryGrowth || 0)
      const ordersGrowth = formatGrowth(stats.ordersGrowth || 0)
      const alertGrowth = formatGrowth(stats.alertGrowth || 0)

      statsData.value = [
        {
          key: 'totalGoods',
          label: '货物种类',
          value: stats.totalGoods || 0,
          change: goodsGrowth.text,
          changeType: goodsGrowth.type,
          changeIcon: goodsGrowth.icon,
          icon: 'Box',
          type: 'primary'
        },
        {
          key: 'totalInventory',
          label: '库存总量',
          value: stats.totalInventory || 0,
          change: inventoryGrowth.text,
          changeType: inventoryGrowth.type,
          changeIcon: inventoryGrowth.icon,
          icon: 'List',
          type: 'success'
        },
        {
          key: 'pendingOrders',
          label: '待处理单据',
          value: stats.pendingOrders || 0,
          change: ordersGrowth.text,
          changeType: ordersGrowth.type,
          changeIcon: ordersGrowth.icon,
          icon: 'Document',
          type: 'warning'
        },
        {
          key: 'alertCount',
          label: '库存预警',
          value: stats.alertCount || 0,
          change: alertGrowth.text,
          changeType: alertGrowth.type,
          changeIcon: alertGrowth.icon,
          icon: 'Warning',
          type: 'danger'
        }
      ]
    }
    
    // 处理预警数据
    if (alertRes.success) {
      alertList.value = alertRes.data.alerts || []
    }

    // 处理待办数据
    if (todoRes.success) {
      todoList.value = (todoRes.data.todos || []).map(item => ({
        ...item,
        priorityText: item.priority === 'high' ? '紧急' : item.priority === 'medium' ? '重要' : '普通'
      }))
    }

    // 处理趋势数据
    if (trendRes.success) {
      trendData.value = trendRes.data.data || []
    }

    // 处理仓库分布数据
    if (warehouseRes.success) {
      warehouseData.value = warehouseRes.data.data || []
    }

    // 数据加载完成后resize图表
    nextTick(() => {
      resizeCharts()
    })

  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
    // 清空数据，不使用模拟数据
    statsData.value = []
    alertList.value = []
    todoList.value = []
    trendData.value = []
    warehouseData.value = []
  } finally {
    loading.value = false
  }
}



// 监听趋势周期变化
watch(trendPeriod, async () => {
  try {
    const trendRes = await dashboardApi.getTrend(trendPeriod.value)
    if (trendRes.success) {
      trendData.value = trendRes.data.data || []
    }
  } catch (error) {
    console.error('重新加载趋势数据失败:', error)
  }
})

// 图表resize函数
const resizeCharts = () => {
  nextTick(() => {
    if (trendChart.value) {
      trendChart.value.resize()
    }
    if (warehouseChart.value) {
      warehouseChart.value.resize()
    }
  })
}

// 窗口resize监听
const handleResize = () => {
  resizeCharts()
}

// 生命周期
onMounted(() => {
  loadDashboardData()

  // 添加窗口resize监听
  window.addEventListener('resize', handleResize)

  // 延迟执行一次resize，确保图表正确渲染
  setTimeout(() => {
    resizeCharts()
  }, 100)
})

onUnmounted(() => {
  // 移除窗口resize监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 30px;
    border-radius: 12px;
    margin-bottom: 24px;

    .welcome-content {
      .welcome-title {
        font-size: 28px;
        font-weight: 600;
        margin: 0 0 8px 0;
      }

      .welcome-subtitle {
        font-size: 16px;
        opacity: 0.9;
        margin: 0;
      }
    }

    .welcome-actions {
      display: flex;
      gap: 12px;
      align-items: center;
      justify-content: flex-end;

      .el-button {
        margin: 0;
        flex-shrink: 0;
      }
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
  }

  .stat-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: white;
      margin-right: 20px;

      &.primary { background: linear-gradient(135deg, #409eff, #66b1ff); }
      &.success { background: linear-gradient(135deg, #67c23a, #85ce61); }
      &.warning { background: linear-gradient(135deg, #e6a23c, #ebb563); }
      &.danger { background: linear-gradient(135deg, #f56c6c, #f78989); }
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        color: #606266;
        margin-bottom: 8px;
      }

      .stat-change {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        font-weight: 500;

        &.positive {
          color: #67c23a;
        }

        &.negative {
          color: #f56c6c;
        }

        &.neutral {
          color: #909399;
        }

        .el-icon {
          font-size: 12px;
        }
      }
    }
  }

  .charts-section {
    .chart-row {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 20px;
      margin-bottom: 24px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  // 图表容器优化
  .chart-content {
    width: 100%;
    overflow: hidden;

    .echarts {
      width: 100% !important;
      min-width: 0 !important;
    }
  }

  .chart-card, .alert-card, .todo-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.3s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    }

    .chart-header, .alert-header, .todo-header {
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

      .alert-badge, .todo-badge {
        .el-button {
          color: #409eff;
          font-size: 12px;
          padding: 0;

          &:hover {
            color: #66b1ff;
          }
        }
      }
    }
  }

  // 库存预警样式
  .alert-card {
    .alert-content {
      .no-alerts {
        text-align: center;
        padding: 40px 20px;
        color: #909399;

        .el-icon {
          font-size: 48px;
          color: #67c23a;
          margin-bottom: 12px;
        }

        p {
          margin: 0;
          font-size: 14px;
        }
      }

      .alert-list {
        .alert-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 16px 0;
          border-bottom: 1px solid #f0f0f0;
          transition: all 0.3s;

          &:hover {
            background: linear-gradient(90deg, #fff2f0 0%, transparent 100%);
            margin: 0 -24px;
            padding: 16px 24px;
            border-radius: 8px;
            border-bottom: 1px solid transparent;
          }

          &:last-child {
            border-bottom: none;
          }

          .alert-info {
            flex: 1;

            .alert-goods {
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
              font-size: 14px;
            }

            .alert-warehouse {
              font-size: 12px;
              color: #909399;
              display: flex;
              align-items: center;
              gap: 4px;

              &::before {
                content: "📍";
                font-size: 10px;
              }
            }
          }

          .alert-status {
            .el-tag {
              font-weight: 500;
              border: none;

              &.el-tag--danger {
                background: linear-gradient(135deg, #f56c6c, #f78989);
                color: white;
              }

              &.el-tag--warning {
                background: linear-gradient(135deg, #e6a23c, #ebb563);
                color: white;
              }
            }
          }
        }
      }
    }
  }

  // 待办事项样式
  .todo-card {
    .todo-content {
      .no-todos {
        text-align: center;
        padding: 40px 20px;
        color: #909399;

        .el-icon {
          font-size: 48px;
          color: #67c23a;
          margin-bottom: 12px;
        }

        p {
          margin: 0;
          font-size: 14px;
        }
      }

      .todo-list {
        .todo-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding: 16px 0;
          border-bottom: 1px solid #f0f0f0;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            background: linear-gradient(90deg, #f0f9ff 0%, transparent 100%);
            margin: 0 -24px;
            padding: 16px 24px;
            border-radius: 8px;
            border-bottom: 1px solid transparent;
          }

          &:last-child {
            border-bottom: none;
          }

          .todo-info {
            flex: 1;
            margin-right: 12px;

            .todo-title {
              font-weight: 600;
              color: #303133;
              margin-bottom: 6px;
              font-size: 14px;
              line-height: 1.4;
            }

            .todo-desc {
              font-size: 12px;
              color: #909399;
              line-height: 1.4;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }
          }

          .todo-meta {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 8px;
            min-width: 60px;

            .el-tag {
              font-weight: 500;
              border: none;
              font-size: 10px;

              &.el-tag--danger {
                background: linear-gradient(135deg, #f56c6c, #f78989);
                color: white;
              }

              &.el-tag--warning {
                background: linear-gradient(135deg, #e6a23c, #ebb563);
                color: white;
              }

              &.el-tag--success {
                background: linear-gradient(135deg, #67c23a, #85ce61);
                color: white;
              }
            }

            .todo-time {
              font-size: 10px;
              color: #c0c4cc;
              white-space: nowrap;
            }
          }
        }
      }
    }
  }



  .quick-actions {
    background: white;
    border-radius: 16px;
    padding: 32px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    margin-top: 32px; // 添加顶部间距
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    // 添加装饰性背景
    &::before {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      width: 200px;
      height: 200px;
      background: linear-gradient(135deg, rgba(64, 158, 255, 0.05), rgba(102, 177, 255, 0.02));
      border-radius: 50%;
      transform: translate(50%, -50%);
      pointer-events: none;
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    }

    h3 {
      font-size: 22px;
      font-weight: 700;
      color: #303133;
      margin: 0 0 24px 0;
      position: relative;
      z-index: 1;

      &::after {
        content: '';
        position: absolute;
        bottom: -8px;
        left: 0;
        width: 40px;
        height: 3px;
        background: linear-gradient(135deg, #409eff, #66b1ff);
        border-radius: 2px;
      }
    }

    .action-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
      gap: 20px;
      position: relative;
      z-index: 1;

      .action-item {
        display: flex;
        align-items: center;
        padding: 20px;
        background: linear-gradient(135deg, #fafbfc 0%, #ffffff 100%);
        border: 2px solid transparent;
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;

        // 添加微妙的内阴影
        box-shadow:
          0 2px 8px rgba(0, 0, 0, 0.04),
          inset 0 1px 0 rgba(255, 255, 255, 0.8);

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, rgba(64, 158, 255, 0.02), transparent);
          opacity: 0;
          transition: opacity 0.3s;
        }

        &:hover {
          transform: translateY(-3px) scale(1.02);
          border-color: #409eff;
          box-shadow:
            0 8px 25px rgba(64, 158, 255, 0.15),
            0 4px 12px rgba(0, 0, 0, 0.08);

          &::before {
            opacity: 1;
          }

          .action-icon {
            transform: scale(1.1) rotate(5deg);
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
          }

          .action-title {
            color: #409eff;
          }
        }

        .action-icon {
          width: 56px;
          height: 56px;
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 26px;
          color: white;
          margin-right: 20px;
          transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          position: relative;

          &::after {
            content: '';
            position: absolute;
            top: 2px;
            left: 2px;
            right: 2px;
            bottom: 2px;
            border-radius: 14px;
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), transparent);
            pointer-events: none;
          }

          &.primary {
            background: linear-gradient(135deg, #409eff, #66b1ff);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
          }
          &.success {
            background: linear-gradient(135deg, #67c23a, #85ce61);
            box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
          }
          &.warning {
            background: linear-gradient(135deg, #e6a23c, #ebb563);
            box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
          }
          &.info {
            background: linear-gradient(135deg, #909399, #b1b3b8);
            box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
          }
          &.danger {
            background: linear-gradient(135deg, #f56c6c, #f78989);
            box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
          }
        }

        .action-content {
          flex: 1;

          .action-title {
            font-size: 17px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 6px;
            transition: color 0.3s;
            line-height: 1.3;
          }

          .action-desc {
            font-size: 13px;
            color: #909399;
            line-height: 1.4;
            opacity: 0.8;
          }
        }

        // 添加右侧箭头指示器
        &::after {
          content: '→';
          position: absolute;
          right: 20px;
          top: 50%;
          transform: translateY(-50%);
          font-size: 16px;
          color: #c0c4cc;
          opacity: 0;
          transition: all 0.3s;
        }

        &:hover::after {
          opacity: 1;
          transform: translateY(-50%) translateX(4px);
          color: #409eff;
        }
      }
    }
  }
}

// 移动端响应式设计
@media (max-width: 768px) {
  .dashboard {
    padding: 15px;

    .welcome-section {
      flex-direction: column;
      text-align: center;
      gap: 15px;
      padding: 20px 15px;

      .welcome-title {
        font-size: 20px;
      }

      .welcome-subtitle {
        font-size: 14px;
      }

      .welcome-actions {
        flex-direction: row;
        justify-content: center;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
        width: 100%;

        .el-button {
          flex: 1;
          min-width: 120px;
          max-width: 160px;
          margin: 0;
        }
      }
    }

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

    .charts-section {
      .chart-row {
        grid-template-columns: 1fr;
        gap: 15px;
      }

      .chart-card, .alert-card, .todo-card {
        padding: 15px;

        .chart-header, .alert-header, .todo-header {
          h3 {
            font-size: 16px;
          }
        }

        .chart-content {
          .echarts {
            height: 250px !important;
          }
        }
      }
    }

    .quick-actions {
      .action-grid {
        grid-template-columns: 1fr;
        gap: 15px;
      }

      .action-card {
        padding: 15px;

        h3 {
          font-size: 16px;
        }

        .description {
          font-size: 12px;
        }
      }
    }
  }
}

// 中等屏幕优化 (481px - 640px)
@media (max-width: 640px) and (min-width: 481px) {
  .dashboard {
    .welcome-section {
      .welcome-actions {
        flex-direction: row;
        justify-content: center;
        gap: 10px;

        .el-button {
          flex: 0 1 auto;
          min-width: 140px;
          max-width: 180px;
        }
      }
    }
  }
}

// 小屏手机优化
@media (max-width: 480px) {
  .dashboard {
    padding: 10px;

    .stats-grid {
      grid-template-columns: 1fr;

      .stat-card {
        padding: 12px;
      }
    }

    .welcome-section {
      padding: 15px 10px;

      .welcome-title {
        font-size: 18px;
      }

      .welcome-actions {
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8px;
        width: 100%;

        .el-button {
          width: 100%;
          max-width: 280px;
          min-width: auto;
          margin: 0;
        }
      }
    }

    .charts-section {
      .chart-card, .alert-card, .todo-card {
        padding: 12px;

        .chart-content {
          .echarts {
            height: 200px !important;
          }
        }

        .chart-header, .alert-header, .todo-header {
          margin-bottom: 12px;

          h3 {
            font-size: 14px;
          }

          .el-radio-group {
            .el-radio-button {
              font-size: 10px;
            }
          }
        }
      }
    }
  }
}
</style>
