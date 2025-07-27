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
            <v-chart :option="trendChartOption" style="height: 300px;" />
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
            <v-chart :option="warehouseChartOption" style="height: 300px;" />
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'
import dashboardApi from '@/api/dashboard'
import dayjs from 'dayjs'
import { useDeviceDetection } from '@/utils/responsive'

const router = useRouter()
const userStore = useUserStore()

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

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
    description: '处理采购入库、归还入库等',
    icon: 'Download',
    type: 'success',
    path: '/inbound'
  },
  {
    key: 'outbound',
    title: '出库管理',
    description: '处理领用出库、调拨出库等',
    icon: 'Upload',
    type: 'warning',
    path: '/outbound'
  },
  {
    key: 'transfer',
    title: '调拨管理',
    description: '仓库间货物调拨转移',
    icon: 'Switch',
    type: 'primary',
    path: '/transfer'
  },
  {
    key: 'stocktake',
    title: '盘点管理',
    description: '库存盘点和差异处理',
    icon: 'DocumentChecked',
    type: 'info',
    path: '/stocktake'
  },
  {
    key: 'inventory',
    title: '库存查询',
    description: '查看实时库存信息',
    icon: 'List',
    type: 'success',
    path: '/inventory'
  },
  {
    key: 'reports',
    title: '报表统计',
    description: '业务数据分析报表',
    icon: 'DataAnalysis',
    type: 'primary',
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

// 加载数据
const loadDashboardData = async () => {
  try {
    loading.value = true
    
    // 并行加载所有数据
    const [statsRes, alertRes, todoRes, trendRes, warehouseRes] = await Promise.all([
      dashboardApi.getStats(),
      dashboardApi.getAlerts(),
      dashboardApi.getTodos(),
      dashboardApi.getTrend(trendPeriod.value),
      dashboardApi.getWarehouseDistribution()
    ])
    
    // 处理统计数据
    if (statsRes.success) {
      const stats = statsRes.data
      statsData.value = [
        {
          key: 'totalGoods',
          label: '货物种类',
          value: stats.totalGoods || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
          icon: 'Box',
          type: 'primary'
        },
        {
          key: 'totalInventory',
          label: '库存总量',
          value: stats.totalInventory || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
          icon: 'List',
          type: 'success'
        },
        {
          key: 'pendingOrders',
          label: '待处理单据',
          value: stats.pendingOrders || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
          icon: 'Document',
          type: 'warning'
        },
        {
          key: 'alertCount',
          label: '库存预警',
          value: stats.alertCount || 0,
          change: '持平',
          changeType: 'neutral',
          changeIcon: 'Minus',
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

// 生命周期
onMounted(() => {
  loadDashboardData()
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
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
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

  .chart-card, .alert-card, .todo-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

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
    }
  }

  .alert-content, .todo-content {
    .no-alerts, .no-todos {
      text-align: center;
      color: #909399;
      padding: 40px 0;

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

    .alert-list, .todo-list {
      .alert-item, .todo-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background: #f8f9fa;
          margin: 0 -12px;
          padding: 12px;
          border-radius: 6px;
        }

        &:last-child {
          border-bottom: none;
        }

        .alert-info, .todo-info {
          flex: 1;

          .alert-goods, .todo-title {
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }

          .alert-warehouse, .todo-desc {
            font-size: 12px;
            color: #909399;
          }
        }

        .todo-meta {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 4px;

          .todo-time {
            font-size: 12px;
            color: #c0c4cc;
          }
        }
      }
    }
  }

  .quick-actions {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 20px 0;
    }

    .action-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;

      .action-item {
        display: flex;
        align-items: center;
        padding: 16px;
        border: 1px solid #e6e6e6;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
        }

        .action-icon {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: white;
          margin-right: 16px;

          &.primary { background: linear-gradient(135deg, #409eff, #66b1ff); }
          &.success { background: linear-gradient(135deg, #67c23a, #85ce61); }
          &.warning { background: linear-gradient(135deg, #e6a23c, #ebb563); }
          &.info { background: linear-gradient(135deg, #909399, #b1b3b8); }
        }

        .action-content {
          flex: 1;

          .action-title {
            font-size: 16px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }

          .action-desc {
            font-size: 14px;
            color: #909399;
          }
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
        flex-direction: column;
        gap: 10px;

        .el-button {
          width: 100%;
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

      .chart-card {
        padding: 15px;

        .chart-header h3 {
          font-size: 16px;
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
    }
  }
}
</style>
