import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '我的店铺' } },
      { path: 'order', name: 'Order', component: () => import('@/views/order/List.vue'), meta: { title: '订单管理' } },
      { path: 'comment', name: 'Comment', component: () => import('@/views/comment/List.vue'), meta: { title: '评价管理' } }
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
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else if (!token || expired) {
    // 本地 token 已过期，清除残留并跳登录页
    if (expired) {
      localStorage.removeItem('token')
      localStorage.removeItem('merchantInfo')
      localStorage.removeItem('expireAt')
    }
    next('/login')
  } else {
    next()
  }
})

export default router
