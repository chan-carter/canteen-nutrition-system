import request from './request'

export const login = (data) => request.post('/login', data)
export const register = (data) => request.post('/register', data)
export const logout = () => request.post('/logout')
export const getCaptcha = () => request.get('/captcha')

export const getMyInfo = () => request.get('/users/me')
export const updateProfile = (data) => request.put('/users', data)
export const updatePassword = (data) => request.put('/users/password', data)
