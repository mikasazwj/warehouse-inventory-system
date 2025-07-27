import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 响应式断点配置
 */
export const breakpoints = {
  xs: 480,    // 超小屏幕 (手机竖屏)
  sm: 768,    // 小屏幕 (手机横屏/平板竖屏)
  md: 1024,   // 中等屏幕 (平板横屏/小笔记本)
  lg: 1280,   // 大屏幕 (桌面)
  xl: 1920    // 超大屏幕 (大桌面)
}

/**
 * 设备类型检测
 */
export const useDeviceDetection = () => {
  const isMobile = ref(false)
  const isTablet = ref(false)
  const isDesktop = ref(false)
  const screenWidth = ref(window.innerWidth)
  const screenHeight = ref(window.innerHeight)

  const updateDeviceType = () => {
    const width = window.innerWidth
    screenWidth.value = width
    screenHeight.value = window.innerHeight

    // 基于屏幕宽度判断设备类型
    isMobile.value = width < breakpoints.sm
    isTablet.value = width >= breakpoints.sm && width < breakpoints.lg
    isDesktop.value = width >= breakpoints.lg

    // 更精确的移动设备检测
    const userAgent = navigator.userAgent.toLowerCase()
    const mobileKeywords = ['mobile', 'android', 'iphone', 'ipad', 'ipod', 'blackberry', 'windows phone']
    const isMobileUA = mobileKeywords.some(keyword => userAgent.includes(keyword))
    
    // 如果用户代理表明是移动设备，且屏幕宽度不是很大，则认为是移动设备
    if (isMobileUA && width < breakpoints.lg) {
      isMobile.value = width < breakpoints.md
      isTablet.value = width >= breakpoints.md && width < breakpoints.lg
    }
  }

  const handleResize = () => {
    updateDeviceType()
  }

  onMounted(() => {
    updateDeviceType()
    window.addEventListener('resize', handleResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
  })

  return {
    isMobile,
    isTablet,
    isDesktop,
    screenWidth,
    screenHeight
  }
}

/**
 * 响应式断点检测
 */
export const useBreakpoints = () => {
  const currentBreakpoint = ref('lg')
  
  const isXs = ref(false)
  const isSm = ref(false)
  const isMd = ref(false)
  const isLg = ref(false)
  const isXl = ref(false)

  const updateBreakpoint = () => {
    const width = window.innerWidth
    
    if (width < breakpoints.xs) {
      currentBreakpoint.value = 'xs'
      isXs.value = true
      isSm.value = false
      isMd.value = false
      isLg.value = false
      isXl.value = false
    } else if (width < breakpoints.sm) {
      currentBreakpoint.value = 'sm'
      isXs.value = false
      isSm.value = true
      isMd.value = false
      isLg.value = false
      isXl.value = false
    } else if (width < breakpoints.md) {
      currentBreakpoint.value = 'md'
      isXs.value = false
      isSm.value = false
      isMd.value = true
      isLg.value = false
      isXl.value = false
    } else if (width < breakpoints.lg) {
      currentBreakpoint.value = 'lg'
      isXs.value = false
      isSm.value = false
      isMd.value = false
      isLg.value = true
      isXl.value = false
    } else {
      currentBreakpoint.value = 'xl'
      isXs.value = false
      isSm.value = false
      isMd.value = false
      isLg.value = false
      isXl.value = true
    }
  }

  onMounted(() => {
    updateBreakpoint()
    window.addEventListener('resize', updateBreakpoint)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateBreakpoint)
  })

  return {
    currentBreakpoint,
    isXs,
    isSm,
    isMd,
    isLg,
    isXl
  }
}

/**
 * 移动端优化配置
 */
export const mobileOptimizations = {
  // 表格在移动端的列配置
  getTableColumns: (allColumns, isMobile) => {
    if (!isMobile) return allColumns
    
    // 移动端只显示关键列
    return allColumns.filter(col => col.mobile !== false).slice(0, 3)
  },
  
  // 移动端分页配置
  getPaginationConfig: (isMobile) => ({
    pageSize: isMobile ? 5 : 10,
    pageSizes: isMobile ? [5, 10, 20] : [10, 20, 50, 100],
    layout: isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'
  }),
  
  // 移动端表单配置
  getFormConfig: (isMobile) => ({
    labelPosition: isMobile ? 'top' : 'right',
    labelWidth: isMobile ? 'auto' : '120px'
  })
}
