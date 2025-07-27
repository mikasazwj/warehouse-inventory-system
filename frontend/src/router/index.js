import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      requiresAuth: false,
      title: '登录'
    }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404.vue'),
    meta: {
      requiresAuth: false,
      title: '页面不存在'
    }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: {
          title: '仪表盘',
          icon: 'DataBoard',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/warehouses',
        name: 'Warehouses',
        component: () => import('@/views/warehouse/index.vue'),
        meta: {
          title: '仓库管理',
          icon: 'House',
          roles: ['ROLE_ADMIN']
        }
      },
      {
        path: '/goods',
        name: 'Goods',
        component: () => import('@/views/goods/index.vue'),
        meta: {
          title: '货物管理',
          icon: 'Box',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/inventory',
        name: 'Inventory',
        component: () => import('@/views/inventory/index.vue'),
        meta: {
          title: '库存管理',
          icon: 'List',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/inbound',
        name: 'Inbound',
        component: () => import('@/views/inbound/index.vue'),
        meta: {
          title: '入库管理',
          icon: 'Download',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/outbound',
        name: 'Outbound',
        component: () => import('@/views/outbound/index.vue'),
        meta: {
          title: '出库管理',
          icon: 'Upload',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN', 'TEAM_LEADER', 'SQUAD_LEADER', 'ROLE_USER']
        }
      },
      {
        path: '/transfer',
        name: 'Transfer',
        component: () => import('@/views/transfer/index.vue'),
        meta: {
          title: '调拨管理',
          icon: 'Switch',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/stocktake',
        name: 'Stocktake',
        component: () => import('@/views/stocktake/index.vue'),
        meta: {
          title: '盘点管理',
          icon: 'DocumentChecked',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: {
          title: '用户管理',
          icon: 'User',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/reports',
        name: 'Reports',
        component: () => import('@/views/reports/index.vue'),
        meta: {
          title: '报表统计',
          icon: 'TrendCharts',
          roles: ['ROLE_ADMIN', 'WAREHOUSE_ADMIN']
        }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: {
          title: '个人中心',
          icon: 'User',
          hidden: true
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 根据用户角色获取默认路由
export function getDefaultRoute(role) {
  switch (role) {
    case 'ROLE_USER':
    case 'SQUAD_LEADER':
    case 'TEAM_LEADER':
      return '/outbound'
    case 'WAREHOUSE_ADMIN':
    case 'ROLE_ADMIN':
    default:
      return '/dashboard'
  }
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 库房管理系统` : '库房管理系统'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth !== false) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
      // 没有权限，重定向到默认页面
      next(getDefaultRoute(userStore.role))
      return
    }
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
