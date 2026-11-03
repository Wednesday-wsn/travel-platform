import request from './request'

export const getStatsOverview = () => request.get('/admin/stats/overview')
export const getOrderTrend = (days = 7) => request.get('/admin/stats/order-trend', { params: { days } })
