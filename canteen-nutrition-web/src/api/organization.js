import request from './request'

export const pageUsers = (params) => request.get('/users', { params })
export const getUser = (id) => request.get(`/users/${id}`)
export const addUser = (data) => request.post('/users', data)
export const updateUser = (data) => request.put('/users', data)
export const deleteUser = (ids) => request.delete(`/users/${ids}`)
export const updateUserStatus = (id, status) => request.put(`/users/${id}/status`, { status })

export const pageMerchants = (params) => request.get('/merchants', { params })
export const getMerchant = (id) => request.get(`/merchants/${id}`)
export const addMerchant = (data) => request.post('/merchants', data)
export const updateMerchant = (data) => request.put('/merchants', data)
export const deleteMerchant = (ids) => request.delete(`/merchants/${ids}`)
export const updateMerchantStatus = (id, status) => request.put(`/merchants/${id}/status`, { status })
