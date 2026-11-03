// 后端接口基础地址
// H5 与微信开发者工具（运行在电脑上）均可访问 localhost；
// 手机真机调试请把这里改成电脑的局域网 IP，例如 http://192.168.1.100:8080
// #ifdef H5
export const BASE_URL = 'http://localhost:8080'
// #endif
// #ifdef MP-WEIXIN
export const BASE_URL = 'http://localhost:8080'
// #endif

/**
 * 把后端返回的相对路径（如 /upload/xxx.png）补全成完整地址；
 * 已是 http(s) 开头的绝对地址则原样返回。
 */
export function resolveUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  // 加版本参数，避免图片更新后浏览器/小程序仍使用缓存的旧图
  return BASE_URL + path + '?v=20261028'
}

// 分类常量（与后端 biz_category 表一致，后端暂无公开分类接口，前端硬编码）
export const CATEGORIES = {
  hotel: [
    { id: 1, name: '豪华型' },
    { id: 2, name: '舒适型' },
    { id: 3, name: '经济型' },
    { id: 4, name: '度假酒店' },
    { id: 5, name: '民宿客栈' }
  ],
  spot: [
    { id: 6, name: '自然风光' },
    { id: 7, name: '人文历史' },
    { id: 8, name: '主题乐园' },
    { id: 9, name: '古镇园林' },
    { id: 10, name: '博物馆' }
  ],
  restaurant: [
    { id: 11, name: '中餐' },
    { id: 12, name: '火锅' },
    { id: 13, name: '西餐' },
    { id: 14, name: '日料' },
    { id: 15, name: '地方小吃' }
  ]
}

// 订单状态：0待支付 1已支付 2已取消 3已完成 4已退款
export const ORDER_STATUS = {
  0: '待支付',
  1: '已支付',
  2: '已取消',
  3: '已完成',
  4: '已退款'
}

// 目标类型：1酒店 2景点 3餐厅
export const TARGET_TYPE = {
  1: '酒店',
  2: '景点',
  3: '餐厅'
}
