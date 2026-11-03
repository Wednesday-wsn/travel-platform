import request from './request'

// ---------- 餐厅 ----------
export const getRestaurantPage = (params) => request.get('/merchant/restaurant/page', { params })
export const getRestaurantDetail = (id) => request.get(`/merchant/restaurant/${id}`)
export const createRestaurant = (data) => request.post('/merchant/restaurant', data)
export const updateRestaurant = (data) => request.put('/merchant/restaurant', data)
export const deleteRestaurant = (id) => request.delete(`/merchant/restaurant/${id}`)
export const shelfRestaurant = (id) => request.put(`/merchant/restaurant/${id}/shelf`)
export const offRestaurant = (id) => request.put(`/merchant/restaurant/${id}/off`)

// ---------- 菜品 ----------
export const getDishList = (restaurantId) => request.get('/merchant/restaurant/dish/list', { params: { restaurantId } })
export const createDish = (data) => request.post('/merchant/restaurant/dish', data)
export const updateDish = (data) => request.put('/merchant/restaurant/dish', data)
export const deleteDish = (id) => request.delete(`/merchant/restaurant/dish/${id}`)
