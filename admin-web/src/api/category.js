import request from './request'

export const getCategoryList = (params) => request.get('/admin/category/list', { params })
export const createCategory = (data) => request.post('/admin/category', data)
export const updateCategory = (data) => request.put('/admin/category', data)
export const deleteCategory = (id) => request.delete(`/admin/category/${id}`)
