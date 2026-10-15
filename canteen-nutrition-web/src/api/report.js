import request from './request'

// 数据大屏与操作日志
export const getDashboard = () => request.get('/report/dashboard')
export const pageLogs = (params) => request.get('/logs', { params })

// Excel 报表导出（商户导出本商户数据，管理员导出全量）
export const exportDishes = () => request.get('/export/dishes', { responseType: 'blob', timeout: 60000 })
export const exportOrders = () => request.get('/export/orders', { responseType: 'blob', timeout: 60000 })

export const saveBlob = (blob, fileName) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
