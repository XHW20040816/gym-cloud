import { defineStore } from 'pinia'
import { useUserStore } from './user'

const allMenus = [
  {
    path: '/dashboard',
    title: '数据大屏',
    icon: 'DataAnalysis',
    roles: ['ADMIN', 'COACH']
  },
  {
    path: '/user',
    title: '用户管理',
    icon: 'User',
    roles: ['ADMIN']
  },
  {
    path: '/goods',
    title: '商品管理',
    icon: 'Goods',
    roles: ['ADMIN']
  },
  {
    path: '/course',
    title: '课程管理',
    icon: 'Reading',
    roles: ['ADMIN', 'COACH']
  },
  {
    path: '/seckill',
    title: '秒杀活动',
    icon: 'Promotion',
    roles: ['ADMIN']
  },
  {
    path: '/order',
    title: '订单管理',
    icon: 'List',
    roles: ['ADMIN']
  },
  {
    path: '/payment',
    title: '支付记录',
    icon: 'Money',
    roles: ['ADMIN']
  }
]

export const useMenuStore = defineStore('menu', {
  state: () => ({
    menus: []
  }),
  getters: {
    menuList: (state) => state.menus
  },
  actions: {
    generateMenus() {
      const userStore = useUserStore()
      const role = userStore.userInfo.role
      this.menus = allMenus.filter((m) => m.roles.includes(role))
      return this.menus
    }
  }
})
