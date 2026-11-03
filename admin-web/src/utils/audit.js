import { ElMessage, ElMessageBox } from 'element-plus'
import { audit as auditApi } from '@/api/audit'

/**
 * 审核通过（带确认）
 * @param {number} targetType 1商家 2酒店 3景点 4餐厅 5评价
 * @param {number} targetId 审核对象 ID
 * @returns {Promise<boolean>} 是否成功
 */
export async function auditPass(targetType, targetId) {
  try {
    await ElMessageBox.confirm('确认审核通过？', '提示', { type: 'warning' })
  } catch {
    return false
  }
  await auditApi({ targetType, targetId, status: 1 })
  ElMessage.success('已通过')
  return true
}

/**
 * 审核驳回（需填写原因）
 */
export async function auditReject(targetType, targetId) {
  let reason
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '审核驳回', {
      inputPattern: /\S+/,
      inputErrorMessage: '原因不能为空'
    })
    reason = value
  } catch {
    return false
  }
  await auditApi({ targetType, targetId, status: 2, reason })
  ElMessage.success('已驳回')
  return true
}
