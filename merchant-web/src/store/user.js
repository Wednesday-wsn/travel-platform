import { defineStore } from 'pinia'
import { merchantLogin } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    merchantInfo: JSON.parse(localStorage.getItem('merchantInfo') || 'null'),
    expireAt: Number(localStorage.getItem('expireAt')) || 0
  }),
  actions: {
    async login(form) {
      const data = await merchantLogin(form)
      this.token = data.token
      this.merchantInfo = data
      this.expireAt = data.expireAt || 0
      localStorage.setItem('token', data.token)
      localStorage.setItem('merchantInfo', JSON.stringify(data))
      localStorage.setItem('expireAt', data.expireAt || 0)
      return data
    },
    logout() {
      this.token = ''
      this.merchantInfo = null
      this.expireAt = 0
      localStorage.removeItem('token')
      localStorage.removeItem('merchantInfo')
      localStorage.removeItem('expireAt')
    }
  }
})
