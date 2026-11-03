import request from './request'

export const getRestaurantPage = (params) => request.get('/admin/restaurant/page', { params })
export const shelfRestaurant = (id) => request.put(`/admin/restaurant/${id}/shelf`)
export const offRestaurant = (id) => request.put(`/admin/restaurant/${id}/off`)
export const deleteRestaurant = (id) => request.delete(`/admin/restaurant/${id}`)
