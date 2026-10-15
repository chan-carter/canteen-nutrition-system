import request from './request'

export const pageStalls = (params) => request.get('/stalls', { params })
export const getStall = (id) => request.get(`/stalls/${id}`)
export const addStall = (data) => request.post('/stalls', data)
export const updateStall = (data) => request.put('/stalls', data)
export const deleteStall = (ids) => request.delete(`/stalls/${ids}`)
export const updateStallStatus = (id, status) => request.put(`/stalls/${id}/status`, { status })

export const pageCategories = (params) => request.get('/categories', { params })
export const listEnabledCategories = () => request.get('/categories/enabled')
export const addCategory = (data) => request.post('/categories', data)
export const updateCategory = (data) => request.put('/categories', data)
export const deleteCategory = (ids) => request.delete(`/categories/${ids}`)
