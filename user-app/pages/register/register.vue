<template>
  <view class="page">
    <view class="head">
      <view class="head-icon g-primary moon-gate">
        <text class="head-ico tn-icon-flower-fill"></text>
      </view>
      <text class="head-title">注册账号</text>
      <text class="head-sub">创建你的旅行账号</text>
    </view>

    <view class="form">
      <view class="field">
        <text class="label">用户名 *</text>
        <input class="input" v-model="form.username" placeholder="请输入用户名" />
      </view>
      <view class="field">
        <text class="label">密码 *</text>
        <input class="input" v-model="form.password" password placeholder="请输入密码" />
      </view>
      <view class="field">
        <text class="label">昵称</text>
        <input class="input" v-model="form.nickname" placeholder="请输入昵称" />
      </view>
      <view class="field">
        <text class="label">手机号</text>
        <input class="input" v-model="form.phone" type="number" placeholder="请输入手机号" />
      </view>
      <view class="field">
        <text class="label">邮箱</text>
        <input class="input" v-model="form.email" placeholder="请输入邮箱" />
      </view>
    </view>

    <button class="btn-primary submit" :loading="loading" @click="onRegister">注 册</button>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { register } from '../../api/index.js'

const form = reactive({ username: '', password: '', nickname: '', phone: '', email: '' })
const loading = ref(false)

function onRegister() {
  if (!form.username) return uni.showToast({ title: '请输入用户名', icon: 'none' })
  if (!form.password) return uni.showToast({ title: '请输入密码', icon: 'none' })

  loading.value = true
  register({ ...form })
    .then(() => {
      uni.showToast({ title: '注册成功，请登录', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: '/pages/login/login' })
      }, 600)
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}
</script>

<style lang="scss" scoped>
.page {
  padding: 40rpx 48rpx;
}
.head {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 20rpx 0 50rpx;
}
.head-icon {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.15);
}
.head-ico {
  font-size: 70rpx;
  color: #ffffff;
}
.head-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #33382F;
  margin-top: 20rpx;
}
.head-sub {
  font-size: 24rpx;
  color: #ABA79A;
  margin-top: 8rpx;
}
.form {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 10rpx 32rpx;
  box-shadow: 0 6rpx 24rpx rgba(31, 41, 55, 0.06);
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
</style>
