import request from './request'

export const getMerchantPage = (params) => request.get('/admin/merchant/page', { params })
export const enableMerchant = (id) => request.put(`/admin/merchant/${id}/enable`)
export const disableMerchant = (id) => request.put(`/admin/merchant/${id}/disable`)
