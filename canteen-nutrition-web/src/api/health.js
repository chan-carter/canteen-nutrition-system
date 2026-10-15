import request from './request'

// 健康档案与营养追踪
export const getHealthProfile = () => request.get('/health/profile')
export const saveHealthProfile = (data) => request.post('/health/profile', data)
export const getDailyIntake = (date) => request.get('/health/daily', { params: { date } })
export const listDietRecords = (params) => request.get('/health/diet', { params })
export const addDietRecord = (data) => request.post('/health/diet', data)
export const deleteDietRecord = (id) => request.delete(`/health/diet/${id}`)
export const getWeightTrend = (days) => request.get('/health/weight/trend', { params: { days } })
export const addWeightLog = (data) => request.post('/health/weight', data)

// 周期餐计划
export const generateMealPlan = (data) => request.post('/meal-plans/generate', data)
export const listMealPlans = (params) => request.get('/meal-plans', { params })
export const clearMealPlans = (params) => request.delete('/meal-plans', { params })
export const adoptMealPlan = (id) => request.put(`/meal-plans/${id}/adopt`)

// AI 营养问答（大模型响应慢，单独放宽超时）
export const aiChat = (data) => request.post('/ai/chat', data, { timeout: 60000 })
export const aiHistory = (sessionId) => request.get('/ai/chat/history', { params: { sessionId } })
