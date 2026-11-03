const TOKEN_KEY = 'user_token'
const USER_KEY = 'user_info'

function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token)
}

function getUserInfo() {
  return uni.getStorageSync(USER_KEY) || null
}

function setUserInfo(info) {
  uni.setStorageSync(USER_KEY, info)
}

function login(token, info) {
  setToken(token)
  if (info) {
    setUserInfo(info)
  }
}

function logout() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(USER_KEY)
}

function isLogin() {
  return !!getToken()
}

export default {
  getToken,
  setToken,
  getUserInfo,
  setUserInfo,
  login,
  logout,
  isLogin
}
