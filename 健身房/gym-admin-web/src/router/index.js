import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layouts/index.vue'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据大屏', icon: 'DataAnalysis', roles: ['ADMIN', 'COACH'] }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'goods',
        name: 'Goods',
        component: () => import('@/views/goods/index.vue'),
        meta: { title: '商品管理', icon: 'Goods', roles: ['ADMIN'] }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程管理', icon: 'Reading', roles: ['ADMIN', 'COACH'] }
      },
      {
        path: 'seckill',
        name: 'Seckill',
        component: () => import('@/views/seckill/index.vue'),
        meta: { title: '秒杀活动', icon: 'Promotion', roles: ['ADMIN'] }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'List', roles: ['ADMIN'] }
      },
      {
        path: 'payment',
        name: 'Payment',
        component: () => import('@/views/payment/index.vue'),
        meta: { title: '支付记录', icon: 'Money', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const whiteList = ['/login']

// 清除管理后台登录态（普通用户 user1 不允许进入后台，登出并回登录页）
function clearAdminAuth(userStore) {
  userStore.token = ''
  userStore.refreshToken = ''
  userStore.userInfo = { id: null, username: '', nickname: '', role: '', avatar: '' }
  localStorage.removeItem('gym-admin-user')
  localStorage.removeItem('token')
}

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const hasToken = userStore.token || localStorage.getItem('token')

  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '健身房管理系统'

  if (hasToken) {
    if (to.path === '/login') {
      next('/')
    } else {
      if (!userStore.userInfo || !userStore.userInfo.id) {
        userStore.getInfo().then(() => {
          const role = userStore.userInfo.role
          if (role === 'USER') {
            ElMessage.warning('普通用户没有后台权限，请使用后台账号登录')
            clearAdminAuth(userStore)
            next('/login')
            return
          }
          if (to.meta.roles && !to.meta.roles.includes(role)) {
            ElMessage.error('没有权限访问该页面')
            next('/dashboard')
          } else {
            next()
          }
        }).catch(() => {
          userStore.logout()
          next('/login')
        })
      } else {
        const role = userStore.userInfo.role
        if (role === 'USER' && to.path !== '/login') {
          ElMessage.warning('普通用户没有后台权限，请使用后台账号登录')
          clearAdminAuth(userStore)
          next('/login')
          return
        }
        if (to.meta.roles && !to.meta.roles.includes(role)) {
          ElMessage.error('没有权限访问该页面')
          next('/dashboard')
        } else {
          next()
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
