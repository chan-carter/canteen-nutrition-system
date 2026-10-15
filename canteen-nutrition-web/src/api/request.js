import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 15000
})

// 请求头携带JWT令牌，后端拦截器读取的 header 名称为 token
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.token = token
  }
  return config
})

service.interceptors.response.use(
  async (response) => {
    // 文件下载返回二进制，后端出错时才会退化成 JSON，需要区分处理
    if (response.config.responseType === 'blob') {
      const blob = response.data
      if (blob && blob.type && blob.type.indexOf('json') >= 0) {
        const text = await blob.text()
        let msg = '下载失败'
        try {
          msg = JSON.parse(text).msg || msg
        } catch (e) {
          // 非标准错误体，沿用默认提示
        }
        ElMessage.error(msg)
        return Promise.reject(new Error(msg))
      }
      return blob
    }
    const res = response.data
    if (res && res.code === 1) {
      return res
    }
    ElMessage.error((res && res.msg) || '请求失败')
    return Promise.reject(new Error((res && res.msg) || '请求失败'))
  },
  (error) => {
    const status = error.response && error.response.status
    if (status === 401) {
      localStorage.removeItem('token')
      ElMessage.warning('登录状态已失效，请重新登录')
      window.location.href = '/login'
    } else if (status === 403) {
      ElMessage.error((error.response.data && error.response.data.msg) || '当前角色无权访问该功能')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('无法连接后端服务，请确认 8080 服务已启动')
    }
    return Promise.reject(error)
  }
)

export default service
