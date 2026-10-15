import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AppLayout from '@/layout/AppLayout.vue'
import { ROLE_STUDENT, ROLE_MERCHANT, ROLE_ADMIN } from '@/utils/dict'

export const homePathOf = (role) => {
  if (role === ROLE_STUDENT) return '/student/dishes'
  if (role === ROLE_MERCHANT) return '/merchant/dishes'
  if (role === ROLE_ADMIN) return '/admin/dashboard'
  return '/login'
}

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true, title: '注册' }
  },
  {
    path: '/',
    component: AppLayout,
    children: [
      {
        path: 'student/dishes',
        name: 'studentDishes',
        component: () => import('@/views/student/DishList.vue'),
        meta: { title: '菜品浏览', icon: 'Bowl', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/dish/:id',
        name: 'studentDishDetail',
        component: () => import('@/views/student/DishDetail.vue'),
        meta: { title: '菜品详情', roles: [ROLE_STUDENT], hidden: true }
      },
      {
        path: 'student/recommendations',
        name: 'studentRecommend',
        component: () => import('@/views/student/Recommendations.vue'),
        meta: { title: '智能推荐', icon: 'MagicStick', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/cart',
        name: 'studentCart',
        component: () => import('@/views/student/Cart.vue'),
        meta: { title: '购物车', icon: 'ShoppingCart', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/orders',
        name: 'studentOrders',
        component: () => import('@/views/student/OrderList.vue'),
        meta: { title: '我的订单', icon: 'List', roles: [ROLE_STUDENT] }
      },
      {
        path: 'order/:id',
        name: 'orderDetail',
        component: () => import('@/views/student/OrderDetail.vue'),
        meta: { title: '订单详情', roles: [ROLE_STUDENT, ROLE_MERCHANT, ROLE_ADMIN], hidden: true }
      },
      {
        path: 'student/health',
        name: 'studentHealth',
        component: () => import('@/views/student/HealthProfile.vue'),
        meta: { title: '健康档案', icon: 'Odometer', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/diet',
        name: 'studentDiet',
        component: () => import('@/views/student/DietTrack.vue'),
        meta: { title: '营养追踪', icon: 'PieChart', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/meal-plan',
        name: 'studentMealPlan',
        component: () => import('@/views/student/MealPlan.vue'),
        meta: { title: '周期餐计划', icon: 'Calendar', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/assistant',
        name: 'studentAssistant',
        component: () => import('@/views/student/AiAssistant.vue'),
        meta: { title: 'AI 营养问答', icon: 'ChatLineSquare', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/favorites',
        name: 'studentFavorites',
        component: () => import('@/views/student/Favorites.vue'),
        meta: { title: '我的收藏', icon: 'Star', roles: [ROLE_STUDENT] }
      },
      {
        path: 'student/profile',
        name: 'studentProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User', roles: [ROLE_STUDENT] }
      },

      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '消息中心', icon: 'BellFilled', roles: [ROLE_STUDENT, ROLE_MERCHANT, ROLE_ADMIN] }
      },

      {
        path: 'merchant/stalls',
        name: 'merchantStalls',
        component: () => import('@/views/merchant/StallList.vue'),
        meta: { title: '档口管理', icon: 'HomeFilled', roles: [ROLE_MERCHANT] }
      },
      {
        path: 'merchant/dishes',
        name: 'merchantDishes',
        component: () => import('@/views/merchant/DishManage.vue'),
        meta: { title: '菜品管理', icon: 'Dish', roles: [ROLE_MERCHANT] }
      },
      {
        path: 'merchant/orders',
        name: 'merchantOrders',
        component: () => import('@/views/merchant/OrderManage.vue'),
        meta: { title: '订单处理', icon: 'Tickets', roles: [ROLE_MERCHANT] }
      },
      {
        path: 'merchant/reviews',
        name: 'merchantReviews',
        component: () => import('@/views/merchant/ReviewList.vue'),
        meta: { title: '学生评价', icon: 'ChatDotSquare', roles: [ROLE_MERCHANT] }
      },
      {
        path: 'merchant/info',
        name: 'merchantInfo',
        component: () => import('@/views/merchant/MerchantInfo.vue'),
        meta: { title: '商户资料', icon: 'OfficeBuilding', roles: [ROLE_MERCHANT] }
      },
      {
        path: 'merchant/profile',
        name: 'merchantProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '账号设置', icon: 'User', roles: [ROLE_MERCHANT] }
      },

      {
        path: 'admin/dashboard',
        name: 'adminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/big-screen',
        name: 'adminBigScreen',
        component: () => import('@/views/admin/BigScreen.vue'),
        meta: { title: '数据大屏', icon: 'Monitor', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/logs',
        name: 'adminLogs',
        component: () => import('@/views/admin/OperationLog.vue'),
        meta: { title: '操作日志', icon: 'Document', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/users',
        name: 'adminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/merchants',
        name: 'adminMerchants',
        component: () => import('@/views/admin/MerchantManage.vue'),
        meta: { title: '商户管理', icon: 'OfficeBuilding', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/stalls',
        name: 'adminStalls',
        component: () => import('@/views/admin/StallManage.vue'),
        meta: { title: '档口管理', icon: 'HomeFilled', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/categories',
        name: 'adminCategories',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理', icon: 'Menu', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/dishes',
        name: 'adminDishes',
        component: () => import('@/views/admin/DishAudit.vue'),
        meta: { title: '菜品审核', icon: 'Search', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/reviews',
        name: 'adminReviews',
        component: () => import('@/views/admin/ReviewManage.vue'),
        meta: { title: '评价管理', icon: 'ChatDotSquare', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/announcements',
        name: 'adminAnnouncements',
        component: () => import('@/views/admin/AnnouncementManage.vue'),
        meta: { title: '公告管理', icon: 'Bell', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/orders',
        name: 'adminOrders',
        component: () => import('@/views/admin/OrderManage.vue'),
        meta: { title: '订单查询', icon: 'Tickets', roles: [ROLE_ADMIN] }
      },
      {
        path: 'admin/profile',
        name: 'adminProfile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '账号设置', icon: 'User', roles: [ROLE_ADMIN] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (to.meta.public) {
    return store.logged && store.info.role ? homePathOf(store.info.role) : true
  }
  if (!store.logged) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!store.info.role) {
    try {
      await store.loadInfo()
    } catch (e) {
      store.clear()
      return { path: '/login' }
    }
  }
  const roles = to.meta.roles
  if (roles && !roles.includes(store.info.role)) {
    return homePathOf(store.info.role)
  }
  if (to.path === '/') {
    return homePathOf(store.info.role)
  }
  return true
})

export default router
