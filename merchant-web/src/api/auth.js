import request from './request'

// 商家登录
export const merchantLogin = (data) => request.post('/public/merchant/login', data)

// 商家入驻申请
export const merchantRegister = (data) => request.post('/public/merchant/register', data)
