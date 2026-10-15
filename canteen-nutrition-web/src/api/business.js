import request from './request'

export const getRecommendations = (params) => request.get('/recommendations', { params })
export const refreshRecommendations = () => request.post('/recommendations/refresh')

export const pageOrders = (params) => request.get('/orders', { params })
export const getOrder = (id) => request.get(`/orders/${id}`)
export const createOrder = (data) => request.post('/orders', data)
export const updateOrderStatus = (id, status) => request.put(`/orders/${id}/status`, { status })
export const deleteOrder = (ids) => request.delete(`/orders/${ids}`)

export const pageReviews = (params) => request.get('/reviews', { params })
export const addReview = (data) => request.post('/reviews', data)
export const updateReviewStatus = (id, status) => request.put(`/reviews/${id}/status`, { status })
export const deleteReview = (ids) => request.delete(`/reviews/${ids}`)

export const pageAnnouncements = (params) => request.get('/announcements', { params })
export const listPublished = () => request.get('/announcements/published')
export const listUnreadAnnouncements = () => request.get('/announcements/unread')
export const readAllAnnouncements = () => request.put('/announcements/read')
export const addAnnouncement = (data) => request.post('/announcements', data)
export const updateAnnouncement = (data) => request.put('/announcements', data)
export const updateAnnouncementStatus = (id, status) => request.put(`/announcements/${id}/status`, { status })
export const deleteAnnouncement = (ids) => request.delete(`/announcements/${ids}`)

export const getOverview = () => request.get('/report/overviewData')
export const getDishCategoryData = () => request.get('/report/dishCategoryData')
export const getDishSalesData = () => request.get('/report/dishSalesData')
export const getHealthGoalData = () => request.get('/report/healthGoalData')
export const getOrderData = () => request.get('/report/orderData')
export const getReviewData = () => request.get('/report/reviewData')
export const getRecommendHotData = () => request.get('/report/recommendHotData')
export const getAiCallData = () => request.get('/report/aiCallData')
