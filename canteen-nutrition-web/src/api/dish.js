import request from './request'

export const pageDishes = (params) => request.get('/dishes', { params })
export const getDish = (id) => request.get(`/dishes/${id}`)
export const addDish = (data) => request.post('/dishes', data)
export const updateDish = (data) => request.put('/dishes', data)
export const deleteDish = (ids) => request.delete(`/dishes/${ids}`)
export const updateDishStatus = (id, status) => request.put(`/dishes/${id}/status`, { status })
export const auditDish = (id, data) => request.put(`/dishes/${id}/audit`, data)

export const analyzeNutrition = (id, data) => request.post(`/dishes/${id}/nutrition/analyze`, data || {})
export const getNutrition = (id) => request.get(`/dishes/${id}/nutrition`)

export const uploadFile = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
