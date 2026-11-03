import request from './request'

// 轮播图
export const getBannerList = () => request.get('/admin/content/banner/list')
export const createBanner = (data) => request.post('/admin/content/banner', data)
export const updateBanner = (data) => request.put('/admin/content/banner', data)
export const deleteBanner = (id) => request.delete(`/admin/content/banner/${id}`)

// 公告
export const getNoticeList = () => request.get('/admin/content/notice/list')
export const createNotice = (data) => request.post('/admin/content/notice', data)
export const updateNotice = (data) => request.put('/admin/content/notice', data)
export const deleteNotice = (id) => request.delete(`/admin/content/notice/${id}`)
