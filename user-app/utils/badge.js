import { orderPage } from '../api/index.js'
import store from '../store/index.js'

const ORDER_TAB_INDEX = 1 // 底部 tabBar 里「订单」的下标

/**
 * 刷新「订单」tab 右上角的未支付数量红点。
 * 未登录或没有未支付订单时清除红点。
 */
export function refreshOrderBadge() {
  if (!store.isLogin()) {
    uni.removeTabBarBadge({ index: ORDER_TAB_INDEX, fail: () => {} })
    return
  }
  orderPage({ current: 1, size: 1, status: 0 })
    .then((data) => {
      const count = (data && data.total) || 0
      if (count > 0) {
        uni.setTabBarBadge({ index: ORDER_TAB_INDEX, text: count > 99 ? '99+' : String(count) })
      } else {
        uni.removeTabBarBadge({ index: ORDER_TAB_INDEX, fail: () => {} })
      }
    })
    .catch(() => {})
}
