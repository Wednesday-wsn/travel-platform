import request from '../utils/request.js'

// ---------- 认证 ----------
export const login = (data) => request({ url: '/api/public/user/login', method: 'POST', data })
export const register = (data) => request({ url: '/api/public/user/register', method: 'POST', data })

// ---------- 内容 ----------
export const getBanners = () => request({ url: '/api/public/banner/list' })
export const getNotices = () => request({ url: '/api/public/notice/list' })

// ---------- 酒店 ----------
export const hotelPage = (params) => request({ url: '/api/public/hotel/page', method: 'GET', data: params })
export const hotelDetail = (id) => request({ url: `/api/public/hotel/${id}` })

// ---------- 景点 ----------
export const spotPage = (params) => request({ url: '/api/public/spot/page', method: 'GET', data: params })
export const spotDetail = (id) => request({ url: `/api/public/spot/${id}` })

// ---------- 餐厅 ----------
export const restaurantPage = (params) => request({ url: '/api/public/restaurant/page', method: 'GET', data: params })
export const restaurantDetail = (id) => request({ url: `/api/public/restaurant/${id}` })

// ---------- 评价（公开） ----------
export const commentPage = (params) => request({ url: '/api/public/comment/page', method: 'GET', data: params })

// ---------- 订单 ----------
export const createOrder = (data) => request({ url: '/api/user/order', method: 'POST', data })
export const payOrder = (id) => request({ url: `/api/user/order/${id}/pay`, method: 'POST' })
export const cancelOrder = (id) => request({ url: `/api/user/order/${id}/cancel`, method: 'POST' })
export const completeOrder = (id) => request({ url: `/api/user/order/${id}/complete`, method: 'POST' })
export const orderPage = (params) => request({ url: '/api/user/order/page', method: 'GET', data: params })
export const orderDetail = (id) => request({ url: `/api/user/order/${id}` })

// ---------- 收藏 ----------
export const addFavorite = (data) => request({ url: '/api/user/favorite', method: 'POST', data })
export const removeFavorite = (id) => request({ url: `/api/user/favorite/${id}`, method: 'DELETE' })
export const favoritePage = (params) => request({ url: '/api/user/favorite/page', method: 'GET', data: params })

// ---------- 个人资料 ----------
export const getProfile = () => request({ url: '/api/user/profile' })
export const updateProfile = (data) => request({ url: '/api/user/profile', method: 'PUT', data })

// ---------- 评价（用户） ----------
export const addComment = (data) => request({ url: '/api/user/comment', method: 'POST', data })
export const myCommentPage = (params) => request({ url: '/api/user/comment/page', method: 'GET', data: params })

// ---------- AI 助手 ----------
export const aiChat = (data) => request({ url: '/api/user/ai/chat', method: 'POST', data })

// ---------- 个性化推荐 ----------
export const getRecommend = () => request({ url: '/api/user/recommend' })
