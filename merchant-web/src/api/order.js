import request from './request'

export const getOrderPage = (params) => request.get('/merchant/order/page', { params })
export const finishOrder = (id) => request.put(`/merchant/order/${id}/finish`)
