import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = 'Bearer ' + userStore.token
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.clear()
      router.push('/login')
      ElMessage.error('登录已失效，请重新登录')
      return Promise.reject(new Error(res.msg || '登录已失效'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.clear()
      router.push('/login')
      ElMessage.error('登录已失效，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
