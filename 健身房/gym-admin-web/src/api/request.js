import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.token || localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录失效，请重新登录')
      return Promise.reject(new Error(res.msg || '登录失效'))
    } else if (res.code === 403) {
      ElMessage.error('没有权限访问')
      return Promise.reject(new Error(res.msg || '没有权限访问'))
    } else if (res.code >= 500) {
      ElMessage.error('服务器错误')
      return Promise.reject(new Error(res.msg || '服务器错误'))
    } else {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
        ElMessage.error('登录失效，请重新登录')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status >= 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(error.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default service
