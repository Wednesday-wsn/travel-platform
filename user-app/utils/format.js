/**
 * 金额格式化：688.00 -> 688，1288.50 -> 1288.50
 */
export function formatPrice(val) {
  if (val === null || val === undefined || val === '') return '0'
  const n = Number(val)
  if (isNaN(n)) return val
  return n.toFixed(2).replace(/\.00$/, '').replace(/(\.\d)0$/, '$1')
}

/**
 * 取日期：'2026-10-06 12:00:00' -> '2026-10-06'
 */
export function formatDate(val) {
  if (!val) return ''
  return String(val).slice(0, 10)
}

/**
 * 取日期时间：'2026-10-06 12:00:00' -> '2026-10-06 12:00'
 */
export function formatDateTime(val) {
  if (!val) return ''
  return String(val).slice(0, 16)
}
