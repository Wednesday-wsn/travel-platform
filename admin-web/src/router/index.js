import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '数据统计' } },
      { path: 'merchant', name: 'Merchant', component: () => import('@/views/merchant/List.vue'), meta: { title: '商家管理' } },
      { path: 'user', name: 'User', component: () => import('@/views/user/List.vue'), meta: { title: '用户管理' } },
      { path: 'hotel', name: 'Hotel', component: () => import('@/views/hotel/List.vue'), meta: { title: '酒店管理' } },
      { path: 'restaurant', name: 'Restaurant', component: () => import('@/views/restaurant/List.vue'), meta: { title: '餐厅管理' } },
      { path: 'spot', name: 'Spot', component: () => import('@/views/spot/List.vue'), meta: { title: '景点管理' } },
      { path: 'comment', name: 'Comment', component: () => import('@/views/comment/List.vue'), meta: { title: '评价管理' } },
      { path: 'category', name: 'Category', component: () => import('@/views/category/List.vue'), meta: { title: '分类管理' } },
      { path: 'content', name: 'Content', component: () => import('@/views/content/List.vue'), meta: { title: '内容管理' } },
      { path: 'audit', name: 'Audit', component: () => import('@/views/audit/List.vue'), meta: { title: '审核日志' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const expireAt = Number(localStorage.getItem('expireAt')) || 0
  const expired = expireAt > 0 && Date.now() >= expireAt
  if (to.path === '/login') {
    next()
  } else if (!token || expired) {
    // 本地 token 已过期，清除残留并跳登录页
    if (expired) {
      localStorage.removeItem('token')
      localStorage.removeItem('adminInfo')
      localStorage.removeItem('expireAt')
    }
    next('/login')
  } else {
    next()
  }
})

export default router
