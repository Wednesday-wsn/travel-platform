import request from './request'

export const getHotelPage = (params) => request.get('/admin/hotel/page', { params })
export const shelfHotel = (id) => request.put(`/admin/hotel/${id}/shelf`)
export const offHotel = (id) => request.put(`/admin/hotel/${id}/off`)
export const deleteHotel = (id) => request.delete(`/admin/hotel/${id}`)
