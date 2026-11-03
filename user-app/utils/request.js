import { BASE_URL } from '../config/index.js'
import store from '../store/index.js'

/**
 * uni.request 统一封装
 * - 自动注入 Authorization 请求头
 * - 统一解包后端 R{ code, message, data } 结构
 * - code !== 200 时 toast 提示；401 时清登录态并跳登录页
 */
function request(options) {
  return new Promise((resolve, reject) => {
    const token = store.getToken()
    const header = { 'Content-Type': 'application/json' }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        const body = res.data
        if (body && body.code === 200) {
          resolve(body.data)
        } else if (body && body.code === 401) {
          store.logout()
          uni.showToast({ title: '请先登录', icon: 'none' })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' })
          }, 600)
          reject(body)
        } else {
          const msg = (body && body.message) || '请求失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(body || {})
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request
