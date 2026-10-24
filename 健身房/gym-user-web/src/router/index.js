import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layouts/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'goods',
        name: 'Goods',
        component: () => import('@/views/goods/index.vue'),
        meta: { title: '商品' }
      },
      {
        path: 'goods/:id',
        name: 'GoodsDetail',
        component: () => import('@/views/goods/detail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'seckill',
        name: 'Seckill',
        component: () => import('@/views/seckill/index.vue'),
        meta: { title: '限时秒杀' }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程' }
      },
      {
        path: 'food',
        name: 'Food',
        component: () => import('@/views/food/index.vue'),
        meta: { title: '健身餐' }
      },
      {
        path: 'member',
        name: 'Member',
        component: () => import('@/views/member/index.vue'),
        meta: { title: '会员卡' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 健身房会员中心` : '健身房会员中心'
  const { useUserStore } = await import('@/store/user')
  const userStore = useUserStore()

  if (!userStore.token) {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }

  if (to.path === '/login') {
    next('/home')
    return
  }

  if (!userStore.userInfo.id) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      userStore.clear()
      next('/login')
      return
    }
  }
  next()
})

export default router
