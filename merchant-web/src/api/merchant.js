import request from './request'

// 当前商家档案
export const getMerchantProfile = () => request.get('/merchant/profile')
