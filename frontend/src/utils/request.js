import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import NProgress from 'nprogress'

// 请求去重管理
const pendingRequests = new Map()

// 生成请求唯一标识
const generateRequestKey = (config) => {
  const { method, url, params, data } = config
  return `${method}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
}

// 创建axios实例 - 本地开发环境
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    NProgress.start()

    // 添加认证token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }

    // 过滤空字符串参数
    if (config.params) {
      const filteredParams = {}
      Object.keys(config.params).forEach(key => {
        const value = config.params[key]
        // 只保留非空字符串、非null、非undefined的参数
        if (value !== '' && value !== null && value !== undefined) {
          filteredParams[key] = value
        }
      })
      config.params = filteredParams
    }

    return config
  },
  error => {
    NProgress.done()
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    NProgress.done()

    const { data } = response

    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 检查业务状态码
    if (data.success === false) {
      // 处理业务错误
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message || '操作失败'))
    }

    return data
  },
  error => {
    NProgress.done()

    const { response } = error
    let message = '网络错误，请稍后重试'

    if (response) {
      const { status, data } = response

      switch (status) {
        case 400:
          message = data.message || '请求参数错误'
          break
        case 401:
          message = '登录已过期，请重新登录'
          // 直接清除用户信息，避免调用logout()造成无限循环
          const userStore = useUserStore()
          userStore.clearUserData()
          router.push('/login')
          break
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务暂不可用'
          break
        default:
          message = data.message || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.code === 'ERR_NETWORK' || error.code === 'ERR_CONNECTION_REFUSED') {
      message = '无法连接到服务器'
    }

    // 只在非连接错误时显示错误消息
    if (error.code !== 'ERR_CONNECTION_REFUSED') {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

// 移除了重试逻辑，直接执行请求

// 封装常用请求方法
export const request = {
  get(url, params = {}, options = {}) {
    return service.get(url, { params, ...options })
  },

  post(url, data = {}, options = {}) {
    return service.post(url, data, options)
  },

  put(url, data = {}, options = {}) {
    return service.put(url, data, options)
  },

  delete(url, params = {}, options = {}) {
    return service.delete(url, { params, ...options })
  },

  upload(url, formData, options = {}) {
    return service.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      ...options
    })
  },

  download(url, params = {}, options = {}) {
    return service.get(url, {
      params,
      responseType: 'blob',
      ...options
    })
  }
}

export default service
