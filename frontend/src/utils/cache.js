/**
 * 前端缓存工具
 * 提供内存缓存和本地存储缓存功能
 */

class CacheManager {
  constructor() {
    this.memoryCache = new Map()
    this.cacheConfig = {
      // 缓存配置：key -> { ttl: 过期时间(秒), storage: 存储类型 }
      'dashboard-stats': { ttl: 300, storage: 'memory' },      // 5分钟
      'dashboard-alerts': { ttl: 120, storage: 'memory' },     // 2分钟
      'dashboard-todos': { ttl: 300, storage: 'memory' },      // 5分钟
      'user-permissions': { ttl: 1800, storage: 'local' },     // 30分钟
      'user-warehouses': { ttl: 1800, storage: 'local' },      // 30分钟
      'goods-categories': { ttl: 3600, storage: 'local' },     // 1小时
      'warehouses': { ttl: 3600, storage: 'local' },           // 1小时
      'inventory-stats': { ttl: 600, storage: 'memory' },      // 10分钟
      'business-trend': { ttl: 900, storage: 'memory' }        // 15分钟
    }
  }

  /**
   * 设置缓存
   * @param {string} key 缓存键
   * @param {any} value 缓存值
   * @param {number} ttl 过期时间(秒)，可选
   */
  set(key, value, ttl = null) {
    const config = this.cacheConfig[key] || { ttl: 300, storage: 'memory' }
    const expireTime = Date.now() + (ttl || config.ttl) * 1000
    
    const cacheItem = {
      value,
      expireTime,
      timestamp: Date.now()
    }

    if (config.storage === 'local') {
      try {
        localStorage.setItem(`cache_${key}`, JSON.stringify(cacheItem))
      } catch (e) {
        console.warn('localStorage写入失败，使用内存缓存:', e)
        this.memoryCache.set(key, cacheItem)
      }
    } else {
      this.memoryCache.set(key, cacheItem)
    }
  }

  /**
   * 获取缓存
   * @param {string} key 缓存键
   * @returns {any|null} 缓存值，过期或不存在返回null
   */
  get(key) {
    const config = this.cacheConfig[key] || { storage: 'memory' }
    let cacheItem = null

    if (config.storage === 'local') {
      try {
        const stored = localStorage.getItem(`cache_${key}`)
        if (stored) {
          cacheItem = JSON.parse(stored)
        }
      } catch (e) {
        console.warn('localStorage读取失败:', e)
      }
    } else {
      cacheItem = this.memoryCache.get(key)
    }

    if (!cacheItem) {
      return null
    }

    // 检查是否过期
    if (Date.now() > cacheItem.expireTime) {
      this.delete(key)
      return null
    }

    return cacheItem.value
  }

  /**
   * 删除缓存
   * @param {string} key 缓存键
   */
  delete(key) {
    const config = this.cacheConfig[key] || { storage: 'memory' }
    
    if (config.storage === 'local') {
      localStorage.removeItem(`cache_${key}`)
    } else {
      this.memoryCache.delete(key)
    }
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.memoryCache.clear()
    
    // 清空localStorage中的缓存
    const keys = Object.keys(localStorage)
    keys.forEach(key => {
      if (key.startsWith('cache_')) {
        localStorage.removeItem(key)
      }
    })
  }

  /**
   * 获取或设置缓存（如果不存在则执行获取函数）
   * @param {string} key 缓存键
   * @param {Function} fetchFn 获取数据的函数
   * @param {number} ttl 过期时间(秒)，可选
   * @returns {Promise<any>} 缓存值
   */
  async getOrSet(key, fetchFn, ttl = null) {
    let cached = this.get(key)
    
    if (cached !== null) {
      return cached
    }

    try {
      const value = await fetchFn()
      this.set(key, value, ttl)
      return value
    } catch (error) {
      console.error(`缓存获取失败 [${key}]:`, error)
      throw error
    }
  }

  /**
   * 检查缓存是否存在且未过期
   * @param {string} key 缓存键
   * @returns {boolean}
   */
  has(key) {
    return this.get(key) !== null
  }

  /**
   * 获取缓存统计信息
   * @returns {Object} 统计信息
   */
  getStats() {
    const memorySize = this.memoryCache.size
    const localStorageKeys = Object.keys(localStorage).filter(key => key.startsWith('cache_'))
    
    return {
      memoryCache: memorySize,
      localStorage: localStorageKeys.length,
      total: memorySize + localStorageKeys.length
    }
  }

  /**
   * 清理过期缓存
   */
  cleanup() {
    const now = Date.now()
    
    // 清理内存缓存
    for (const [key, item] of this.memoryCache.entries()) {
      if (now > item.expireTime) {
        this.memoryCache.delete(key)
      }
    }

    // 清理localStorage缓存
    const keys = Object.keys(localStorage)
    keys.forEach(key => {
      if (key.startsWith('cache_')) {
        try {
          const item = JSON.parse(localStorage.getItem(key))
          if (now > item.expireTime) {
            localStorage.removeItem(key)
          }
        } catch (e) {
          // 无效的缓存项，直接删除
          localStorage.removeItem(key)
        }
      }
    })
  }
}

// 创建全局缓存实例
const cache = new CacheManager()

// 定期清理过期缓存（每5分钟）
setInterval(() => {
  cache.cleanup()
}, 5 * 60 * 1000)

export default cache
