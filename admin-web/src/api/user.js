import request from './request'

export const getUserPage = (params) => request.get('/admin/user/page', { params })
export const enableUser = (id) => request.put(`/admin/user/${id}/enable`)
export const disableUser = (id) => request.put(`/admin/user/${id}/disable`)
