// 状态字典（与后端状态码一致）

export const merchantStatus = { 0: '待审核', 1: '已通过', 2: '已驳回', 3: '已禁用' }
export const userStatus = { 0: '已禁用', 1: '正常' }
export const resourceStatus = { 0: '待审核', 1: '已上架', 2: '已驳回', 3: '已下架' }
export const commentStatus = { 0: '待审核', 1: '已通过', 2: '已驳回' }
export const contentStatus = { 0: '已下架', 1: '已上架' }

export const merchantType = { 1: '酒店', 2: '餐厅', 3: '综合' }
export const auditTargetType = { 1: '商家入驻', 2: '酒店', 3: '景点', 4: '餐厅', 5: '评价', 6: '评价撤销申诉' }
export const categoryType = { 1: '酒店', 2: '景点', 3: '餐厅', 4: '菜品' }
export const commentTargetType = { 1: '酒店', 2: '景点', 3: '餐厅' }

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
