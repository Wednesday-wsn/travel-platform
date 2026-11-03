import request from './request'

// 统一审核：targetType 1商家 2酒店 3景点 4餐厅 5评价
export const audit = (data) => request.post('/admin/audit', data)
// 审核日志
export const getAuditPage = (params) => request.get('/admin/audit/page', { params })
