import request from './request'

// 收藏
export const listFavorites = () => request.get('/favorites')
export const addFavorite = (dishId) => request.post(`/favorites/${dishId}`)
export const cancelFavorite = (dishId) => request.delete(`/favorites/${dishId}`)

// 站内消息
export const listNotifications = (limit = 30) => request.get('/notifications', { params: { limit } })
export const unreadNotifications = () => request.get('/notifications/unread')
export const readNotification = (id) => request.put(`/notifications/${id}/read`)
export const readAllNotifications = () => request.put('/notifications/read-all')

// 过敏原
export const listAllergens = () => request.get('/allergens')
export const getDishAllergens = (dishId) => request.get(`/dishes/${dishId}/allergens`)
export const setDishAllergens = (dishId, allergenIds) =>
  request.put(`/dishes/${dishId}/allergens`, { allergenIds })

// 推荐反馈（喜欢/不喜欢，用于协同过滤闭环）
export const sendRecommendFeedback = (data) => request.post('/recommendations/feedback', data)
