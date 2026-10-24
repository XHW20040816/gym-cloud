import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'
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
      avatar: ''
    }
  }),
  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.userInfo.role || '',
    avatarUrl: (state) =>
      state.userInfo.avatar ||
      'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
  },
  actions: {
    async login(loginForm) {
      const res = await login(loginForm)
      // 后端 /auth/login 返回 TokenVO：access_token / refresh_token
      if (!res?.access_token) {
        throw new Error('登录响应缺少 access_token')
      }
      this.token = res.access_token
      this.refreshToken = res.refresh_token || ''
      this.userInfo = {
        id: null,
        username: loginForm.username,
        nickname: '',
        role: '',
        avatar: ''
      }
      return this.token
    },
    async getInfo() {
      try {
        const res = await getUserInfo()
        this.setUserInfo(res || this.userInfo)
        return this.userInfo
      } catch (e) {
        if (!this.userInfo.id) {
          throw e
        }
        return this.userInfo
      }
    },
    setUserInfo(info) {
      this.userInfo = {
        id: info.id ?? this.userInfo.id,
        username: info.username ?? this.userInfo.username,
        nickname: info.nickname ?? this.userInfo.nickname,
        role: info.role ?? this.userInfo.role,
        avatar: info.avatar ?? this.userInfo.avatar
      }
    },
    async logout() {
      try {
        await logout()
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.refreshToken = ''
      this.userInfo = {
        id: null,
        username: '',
        nickname: '',
        role: '',
        avatar: ''
      }
      localStorage.removeItem('token')
      router.push('/login')
    }
  },
  persist: {
    key: 'gym-admin-user',
    storage: localStorage,
    paths: ['token', 'refreshToken', 'userInfo']
  }
})
