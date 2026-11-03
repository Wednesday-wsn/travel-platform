import request from './request'

export const getCommentPage = (params) => request.get('/merchant/comment/page', { params })
export const replyComment = (id, reply) => request.put(`/merchant/comment/${id}/reply`, { reply })
export const appealComment = (id, reason) => request.post(`/merchant/comment/${id}/appeal`, { reason })
