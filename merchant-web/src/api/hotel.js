import request from './request'

// ---------- 酒店 ----------
export const getHotelPage = (params) => request.get('/merchant/hotel/page', { params })
export const getHotelDetail = (id) => request.get(`/merchant/hotel/${id}`)
export const createHotel = (data) => request.post('/merchant/hotel', data)
export const updateHotel = (data) => request.put('/merchant/hotel', data)
export const deleteHotel = (id) => request.delete(`/merchant/hotel/${id}`)
export const shelfHotel = (id) => request.put(`/merchant/hotel/${id}/shelf`)
export const offHotel = (id) => request.put(`/merchant/hotel/${id}/off`)

// ---------- 房型 ----------
export const getRoomList = (hotelId) => request.get('/merchant/hotel/room/list', { params: { hotelId } })
export const createRoom = (data) => request.post('/merchant/hotel/room', data)
export const updateRoom = (data) => request.put('/merchant/hotel/room', data)
export const deleteRoom = (id) => request.delete(`/merchant/hotel/room/${id}`)
