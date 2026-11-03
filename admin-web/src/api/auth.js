import request from './request'

// 管理员登录
export const adminLogin = (data) => request.post('/public/admin/login', data)
