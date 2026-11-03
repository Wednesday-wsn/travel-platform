import { defineStore } from 'pinia'
import { adminLogin } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    adminInfo: JSON.parse(localStorage.getItem('adminInfo') || 'null'),
    expireAt: Number(localStorage.getItem('expireAt')) || 0
  }),
  actions: {
    async login(form) {
      const data = await adminLogin(form)
      this.token = data.token
      this.adminInfo = data
      this.expireAt = data.expireAt || 0
      localStorage.setItem('token', data.token)
      localStorage.setItem('adminInfo', JSON.stringify(data))
      localStorage.setItem('expireAt', data.expireAt || 0)
      return data
    },
    logout() {
      this.token = ''
      this.adminInfo = null
      this.expireAt = 0
      localStorage.removeItem('token')
      localStorage.removeItem('adminInfo')
      localStorage.removeItem('expireAt')
    }
  }
})
