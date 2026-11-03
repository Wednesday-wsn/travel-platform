import request from './request'

export const getSpotPage = (params) => request.get('/admin/spot/page', { params })
export const getSpotDetail = (id) => request.get(`/admin/spot/${id}`)
export const createSpot = (data) => request.post('/admin/spot', data)
export const updateSpot = (data) => request.put('/admin/spot', data)
export const shelfSpot = (id) => request.put(`/admin/spot/${id}/shelf`)
export const offSpot = (id) => request.put(`/admin/spot/${id}/off`)
export const deleteSpot = (id) => request.delete(`/admin/spot/${id}`)

export const getTicketList = (spotId) => request.get('/admin/spot/ticket/list', { params: { spotId } })
export const createTicket = (data) => request.post('/admin/spot/ticket', data)
export const updateTicket = (data) => request.put('/admin/spot/ticket', data)
export const deleteTicket = (id) => request.delete(`/admin/spot/ticket/${id}`)
