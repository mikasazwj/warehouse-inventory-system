/**
 * 性能监控工具
 */

// 性能指标收集
export class PerformanceMonitor {
  constructor() {
    this.metrics = new Map()
    this.observers = []
    this.init()
  }

  init() {
    // 监听页面加载性能
    this.observePageLoad()
    
    // 监听长任务
    this.observeLongTasks()
    
    // 监听内存使用
    this.observeMemory()
    
    // 监听网络请求
    this.observeNetwork()
  }

  // 监听页面加载性能
  observePageLoad() {
    if ('performance' in window) {
      window.addEventListener('load', () => {
        setTimeout(() => {
          const navigation = performance.getEntriesByType('navigation')[0]
          if (navigation) {
            this.recordMetric('page_load', {
              dns: navigation.domainLookupEnd - navigation.domainLookupStart,
              tcp: navigation.connectEnd - navigation.connectStart,
              request: navigation.responseStart - navigation.requestStart,
              response: navigation.responseEnd - navigation.responseStart,
              dom: navigation.domContentLoadedEventEnd - navigation.responseEnd,
              load: navigation.loadEventEnd - navigation.loadEventStart,
              total: navigation.loadEventEnd - navigation.navigationStart
            })
          }
        }, 0)
      })
    }
  }

  // 监听长任务
  observeLongTasks() {
    if ('PerformanceObserver' in window) {
      try {
        const observer = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            this.recordMetric('long_task', {
              duration: entry.duration,
              startTime: entry.startTime,
              name: entry.name
            })
          }
        })
        observer.observe({ entryTypes: ['longtask'] })
        this.observers.push(observer)
      } catch (e) {
        // Long task observer not supported
      }
    }
  }

  // 监听内存使用
  observeMemory() {
    if ('memory' in performance) {
      setInterval(() => {
        const memory = performance.memory
        this.recordMetric('memory', {
          used: memory.usedJSHeapSize,
          total: memory.totalJSHeapSize,
          limit: memory.jsHeapSizeLimit,
          usage: (memory.usedJSHeapSize / memory.jsHeapSizeLimit * 100).toFixed(2)
        })
      }, 30000) // 每30秒记录一次
    }
  }

  // 监听网络请求
  observeNetwork() {
    if ('PerformanceObserver' in window) {
      try {
        const observer = new PerformanceObserver((list) => {
          for (const entry of list.getEntries()) {
            if (entry.name.includes('/api/')) {
              this.recordMetric('api_request', {
                url: entry.name,
                duration: entry.duration,
                size: entry.transferSize,
                status: entry.responseStatus
              })
            }
          }
        })
        observer.observe({ entryTypes: ['resource'] })
        this.observers.push(observer)
      } catch (e) {
        // Resource observer not supported
      }
    }
  }

  // 记录性能指标
  recordMetric(type, data) {
    const timestamp = Date.now()
    const metric = {
      type,
      data,
      timestamp,
      url: window.location.href,
      userAgent: navigator.userAgent
    }

    // 存储到本地
    if (!this.metrics.has(type)) {
      this.metrics.set(type, [])
    }
    this.metrics.get(type).push(metric)

    // 限制存储数量
    const maxEntries = 100
    const entries = this.metrics.get(type)
    if (entries.length > maxEntries) {
      entries.splice(0, entries.length - maxEntries)
    }

    // 发送到监控服务（可选）
    this.sendToMonitoring(metric)
  }

  // 发送到监控服务
  sendToMonitoring(metric) {
    // 这里可以发送到实际的监控服务
    // 生产环境下可以发送到监控平台
  }

  // 获取性能报告
  getReport() {
    const report = {}
    for (const [type, entries] of this.metrics) {
      report[type] = {
        count: entries.length,
        latest: entries[entries.length - 1],
        entries: entries.slice(-10) // 最近10条
      }
    }
    return report
  }

  // 清理资源
  destroy() {
    this.observers.forEach(observer => observer.disconnect())
    this.observers = []
    this.metrics.clear()
  }
}

// 页面性能测量
export const measurePagePerformance = () => {
  if (!('performance' in window)) return null

  const navigation = performance.getEntriesByType('navigation')[0]
  if (!navigation) return null

  return {
    // 页面加载时间
    pageLoad: navigation.loadEventEnd - navigation.navigationStart,
    // DNS查询时间
    dns: navigation.domainLookupEnd - navigation.domainLookupStart,
    // TCP连接时间
    tcp: navigation.connectEnd - navigation.connectStart,
    // 请求响应时间
    request: navigation.responseEnd - navigation.requestStart,
    // DOM解析时间
    domParse: navigation.domContentLoadedEventEnd - navigation.responseEnd,
    // 资源加载时间
    resourceLoad: navigation.loadEventEnd - navigation.domContentLoadedEventEnd,
    // 首次内容绘制
    fcp: getFCP(),
    // 最大内容绘制
    lcp: getLCP()
  }
}

// 获取首次内容绘制时间
const getFCP = () => {
  const entries = performance.getEntriesByType('paint')
  const fcpEntry = entries.find(entry => entry.name === 'first-contentful-paint')
  return fcpEntry ? fcpEntry.startTime : null
}

// 获取最大内容绘制时间
const getLCP = () => {
  return new Promise((resolve) => {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        const lastEntry = entries[entries.length - 1]
        resolve(lastEntry.startTime)
        observer.disconnect()
      })
      observer.observe({ entryTypes: ['largest-contentful-paint'] })
      
      // 超时处理
      setTimeout(() => {
        observer.disconnect()
        resolve(null)
      }, 5000)
    } else {
      resolve(null)
    }
  })
}

// 创建全局实例
export const performanceMonitor = new PerformanceMonitor()

// 在开发环境下暴露到全局
if (import.meta.env.DEV) {
  window.__PERFORMANCE_MONITOR__ = performanceMonitor
}
