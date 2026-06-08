import { createRouter, createWebHistory } from 'vue-router'
import { openAuthDialog } from '../stores/authDialog'

const routes = [
  // ---- 游客端 ----
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/home/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/attractions',
    name: 'AttractionList',
    component: () => import('../views/attraction/AttractionList.vue')
  },
  {
    path: '/attractions/:id',
    name: 'AttractionDetail',
    component: () => import('../views/attraction/AttractionDetail.vue')
  },
  {
    path: '/routes',
    name: 'TourRouteList',
    component: () => import('../views/route/TourRouteList.vue')
  },
  {
    path: '/routes/:id',
    name: 'TourRouteDetail',
    component: () => import('../views/route/TourRouteDetail.vue')
  },
  {
    path: '/routes/custom',
    name: 'RouteBuilder',
    component: () => import('../views/route/RouteBuilder.vue')
  },
  {
    path: '/my-routes',
    name: 'MyRoutes',
    component: () => import('../views/route/MyRoutes.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/checkin',
    name: 'CheckInMap',
    component: () => import('../views/checkin/CheckInMap.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/achievements',
    name: 'AchievementHall',
    component: () => import('../views/achievement/AchievementHall.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/chat/Chat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/profile/Profile.vue'),
    meta: { requiresAuth: true }
  },
  // ---- 管理后台 ----
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLogin.vue')
  },
  {
    path: '/admin',
    redirect: '/admin/dashboard',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue')
      },
      {
        path: 'conversations',
        name: 'AdminConversations',
        component: () => import('../views/admin/Conversations.vue')
      },
      {
        path: 'achievements',
        name: 'AdminAchievements',
        component: () => import('../views/admin/Achievements.vue')
      },
      {
        path: 'knowledge',
        name: 'AdminKnowledge',
        component: () => import('../views/admin/Knowledge.vue')
      },
      {
        path: 'qrcodes',
        name: 'AdminQrCodes',
        component: () => import('../views/admin/QrCodes.vue')
      },
      {
        path: 'digital-human',
        name: 'AdminDigitalHuman',
        component: () => import('../views/admin/DigitalHuman.vue')
      },
      {
        path: 'attractions',
        name: 'AdminAttractions',
        component: () => import('../views/admin/Attractions.vue')
      },
      {
        path: 'banners',
        name: 'AdminBanners',
        component: () => import('../views/admin/Banners.vue')
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('../views/admin/Reports.vue')
      },
      {
        path: 'hot-search',
        name: 'AdminHotSearch',
        component: () => import('../views/admin/HotSearch.vue')
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/admin/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫
router.beforeEach((to, from) => {
  // 保存滚动位置（游客端）
  if (from.name && !to.path.startsWith('/admin')) {
    const key = from.params.id ? `${from.name}_${from.params.id}` : from.name
    sessionStorage.setItem(`scroll_${key}`, window.scrollY)
  }

  // 登录拦截 — 弹 dialog 代替强制跳转
  if (to.meta.requiresAuth && !localStorage.getItem('token')) {
    openAuthDialog(to)
    return false
  }

  // 管理后台鉴权
  if (to.matched.some(r => r.meta.requiresAdmin)) {
    const role = localStorage.getItem('role')
    const token = localStorage.getItem('token')
    if (!token || role !== 'admin') {
      return '/admin/login'
    }
  }
})

export default router
