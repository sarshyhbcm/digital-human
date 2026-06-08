import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动添加 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理返回数据
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      console.error('接口异常:', res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      // 不在登录页才跳转
      if (!window.location.hash.includes('/login')) {
        window.location.href = '/login'
      }
    }
    console.error('网络错误:', error)
    return Promise.reject(error)
  }
)

export default request
