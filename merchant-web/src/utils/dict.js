// 状态字典（与后端状态码一致）

export const resourceStatus = { 0: '待审核', 1: '已上架', 2: '已驳回', 3: '已下架' }
export const roomStatus = { 0: '已下架', 1: '已上架' }
export const orderStatus = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '已完成', 4: '已退款' }
export const merchantType = { 1: '酒店', 2: '餐厅', 3: '综合' }
export const merchantStatus = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已禁用' }
export const commentStatus = { 0: '待审核', 1: '已通过', 2: '已驳回' }
export const commentTargetType = { 1: '酒店', 3: '餐厅' }
export const appealStatus = { 0: '申诉中', 1: '已撤销', 2: '申诉驳回' }

// 通用状态标签颜色
export function tagType(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'danger'
  return 'info'
}

// 过滤空值（注意 status=0 是有效值）
export function val(v) {
  return (v === null || v === undefined || v === '') ? undefined : v
}
