import { defineStore } from 'pinia'
import { login, logout, getMyInfo } from '@/api'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    refreshToken: '',
    userInfo: {
      id: null,
      username: '',
      nickname: '',
      role: '',
      avatar: '',
      balance: 0,
      point: 0
    }
  }),
  getters: {
    isLogin: (state) => !!state.token,
    displayName: (state) => state.userInfo.nickname || state.userInfo.username || '会员'
  },
  actions: {
    async login(form) {
      const res = await login(form)
      if (!res?.access_token) {
        throw new Error('登录响应缺少 access_token')
      }
      this.token = res.access_token
      this.refreshToken = res.refresh_token || ''
      return this.token
    },
    async fetchUserInfo() {
      const user = await getMyInfo()
      if (user) {
        this.userInfo = {
          id: user.id,
          username: user.username,
          nickname: user.nickname,
          role: user.role,
          avatar: user.avatar,
          balance: user.balance ?? 0,
          point: user.point ?? 0
        }
      }
      return this.userInfo
    },
    clear() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = {
        id: null,
        username: '',
        nickname: '',
        role: '',
        avatar: '',
        balance: 0,
        point: 0
      }
    },
    async logout() {
      try {
        await logout()
      } catch (e) {
        // 退出接口失败不影响本地清理
      }
      this.clear()
      router.push('/login')
    }
  },
  persist: {
    key: 'gym-user-web',
    storage: localStorage,
    paths: ['token', 'refreshToken', 'userInfo']
  }
})
