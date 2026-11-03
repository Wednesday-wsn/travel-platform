import request from './request'

export const getCommentPage = (params) => request.get('/admin/comment/page', { params })
export const deleteComment = (id) => request.delete(`/admin/comment/${id}`)
