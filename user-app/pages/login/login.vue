<template>
  <view class="page">
    <view class="logo">
      <view class="logo-icon g-primary moon-gate">
        <text class="logo-ico tn-icon-lotus"></text>
      </view>
      <text class="logo-text">旅游服务平台</text>
    </view>

    <view class="form">
      <view class="field">
        <text class="label">用户名</text>
        <input class="input" v-model="form.username" placeholder="请输入用户名" />
      </view>
      <view class="field">
        <text class="label">密码</text>
        <input class="input" v-model="form.password" password placeholder="请输入密码" />
      </view>
    </view>

    <button class="btn-primary submit" :loading="loading" @click="onLogin">登 录</button>

    <view class="footer">
      <text>还没有账号？</text>
      <text class="link" @click="goRegister">立即注册</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { login } from '../../api/index.js'
import store from '../../store/index.js'
import { refreshOrderBadge } from '../../utils/badge.js'

const form = reactive({ username: '', password: '' })
const loading = ref(false)

function onLogin() {
  if (!form.username) return uni.showToast({ title: '请输入用户名', icon: 'none' })
  if (!form.password) return uni.showToast({ title: '请输入密码', icon: 'none' })

  loading.value = true
  login({ username: form.username, password: form.password })
    .then((data) => {
      store.login(data.token, data)
      refreshOrderBadge()
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          uni.navigateBack()
        } else {
          uni.switchTab({ url: '/pages/index/index' })
        }
      }, 500)
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

function goRegister() {
  uni.navigateTo({ url: '/pages/register/register' })
}
</script>

<style lang="scss" scoped>
.page {
  padding: 60rpx 48rpx;
}
.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 40rpx 0 60rpx;
}
.logo-icon {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.15);
}
.logo-ico {
  font-size: 80rpx;
  color: #ffffff;
}
.logo-text {
  font-size: 40rpx;
  font-weight: 700;
  color: #33382F;
  margin-top: 16rpx;
}
.form {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 10rpx 32rpx;
}
.field {
  padding: 28rpx 0;
  border-bottom: 1rpx solid #EFEBE0;
}
.field:last-child {
  border-bottom: none;
}
.label {
  display: block;
  font-size: 26rpx;
  color: #7A7A6E;
  margin-bottom: 12rpx;
}
.input {
  font-size: 30rpx;
  height: 48rpx;
}
.submit {
  margin-top: 60rpx;
  border: none;
}
.footer {
  text-align: center;
  margin-top: 30rpx;
  font-size: 26rpx;
  color: #7A7A6E;
}
.link {
  color: #2F4F45;
}
</style>
