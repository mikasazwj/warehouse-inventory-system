import request from '@/utils/request'

/**
 * 仪表盘API
 */
export default {
  /**
   * 获取仪表盘统计数据
   */
  getStats() {
    return request.get('/dashboard/stats')
  },

  /**
   * 获取库存预警列表
   */
  getAlerts() {
    return request.get('/dashboard/alerts')
  },

  /**
   * 获取待办事项
   */
  getTodos() {
    return request.get('/dashboard/todos')
  },

  /**
   * 获取业务趋势数据
   * @param {number} period - 时间周期（天数）
   */
  getTrend(period = 30) {
    return request.get('/dashboard/trend', {
      params: { period }
    })
  },

  /**
   * 获取仓库分布数据
   */
  getWarehouseDistribution() {
    return request.get('/dashboard/warehouse-distribution')
  },

  /**
   * 获取最近活动
   */
  getRecentActivities() {
    return request.get('/dashboard/recent-activities')
  }
}
