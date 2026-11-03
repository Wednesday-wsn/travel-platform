<template>
  <view class="page">
    <!-- 头部渐变 -->
    <view class="user-head g-primary">
      <view class="garden-overlay"></view>
      <view class="head-circle c1"></view>
      <view class="head-circle c2"></view>
      <template v-if="isLogin">
        <view class="avatar">
          <image v-if="userInfo.avatar" class="avatar-img" :src="resolveUrl(userInfo.avatar)" mode="aspectFill" />
          <text v-else class="avatar-text">{{ initial }}</text>
        </view>
        <view class="user-info">
          <text class="user-name">{{ userInfo.name || userInfo.username }}</text>
          <text class="user-sub">账号：{{ userInfo.username }}</text>
        </view>
      </template>
      <template v-else>
        <view class="avatar">
          <text class="avatar-text">旅</text>
        </view>
        <view class="user-info" @click="goLogin">
          <text class="user-name">点击登录</text>
          <text class="user-sub">登录后享受完整服务</text>
        </view>
      </template>
    </view>

    <!-- 菜单 -->
    <view class="menu card">
      <view class="menu-item" @click="go('/pages/favorite/list')">
        <view class="icon-badge g-orange"><text class="tn-icon-star-fill"></text></view>
        <text class="menu-text">我的收藏</text>
        <text class="menu-arrow tn-icon-right"></text>
      </view>
      <view class="menu-item" @click="go('/pages/comment/mine')">
        <view class="icon-badge g-blue"><text class="tn-icon-comment-fill"></text></view>
        <text class="menu-text">我的评价</text>
        <text class="menu-arrow tn-icon-right"></text>
      </view>
      <view class="menu-item" @click="go('/pages/profile/edit')">
        <view class="icon-badge g-green"><text class="tn-icon-edit-write-fill"></text></view>
        <text class="menu-text">编辑资料</text>
        <text class="menu-arrow tn-icon-right"></text>
      </view>
      <view class="menu-item" @click="go('/pages/notice/list')">
        <view class="icon-badge g-purple"><text class="tn-icon-notice-fill"></text></view>
        <text class="menu-text">平台公告</text>
        <text class="menu-arrow tn-icon-right"></text>
      </view>
    </view>

    <!-- 退出登录 -->
    <button v-if="isLogin" class="logout-btn" @click="onLogout">退出登录</button>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import store from '../../store/index.js'
import { resolveUrl } from '../../config/index.js'
import { refreshOrderBadge } from '../../utils/badge.js'

const isLogin = ref(false)
const userInfo = ref({})

onShow(() => {
  isLogin.value = store.isLogin()
  userInfo.value = store.getUserInfo() || {}
})

const initial = computed(() => {
  const name = userInfo.value.name || userInfo.value.username || '旅'
  return name.charAt(0)
})

function go(url) {
  if (!isLogin.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 500)
    return
  }
  uni.navigateTo({ url })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}

function onLogout() {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        store.logout()
        refreshOrderBadge()
        isLogin.value = false
        userInfo.value = {}
        uni.showToast({ title: '已退出', icon: 'none' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding-bottom: 40rpx;
}
.user-head {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  padding: 80rpx 40rpx 100rpx;
  border-radius: 0 0 40rpx 40rpx;
}
.head-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}
.c1 {
  width: 300rpx;
  height: 300rpx;
  right: -80rpx;
  top: -100rpx;
}
.c2 {
  width: 200rpx;
  height: 200rpx;
  right: 60rpx;
  bottom: -60rpx;
  background: rgba(255, 255, 255, 0.08);
}
.avatar {
  position: relative;
  z-index: 1;
  width: 130rpx;
  height: 130rpx;
  border-radius: 50%;
  background: #ffffff;
  border: 6rpx solid rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 10rpx 24rpx rgba(0, 0, 0, 0.15);
}
.avatar-img {
  width: 130rpx;
  height: 130rpx;
}
.avatar-text {
  font-size: 52rpx;
  color: #2F4F45;
  font-weight: 600;
}
.user-info {
  position: relative;
  z-index: 1;
  margin-left: 28rpx;
  flex: 1;
}
.user-name {
  display: block;
  font-size: 40rpx;
  font-weight: 600;
  color: #ffffff;
}
.user-sub {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 10rpx;
}
.menu {
  position: relative;
  z-index: 2;
  margin-top: -60rpx;
  padding: 8rpx 12rpx;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 26rpx 16rpx;
  border-bottom: 1rpx solid #EFEBE0;
}
.menu-item:last-child {
  border-bottom: none;
}
.menu-text {
  flex: 1;
  margin-left: 24rpx;
  font-size: 30rpx;
  color: #33382F;
}
.menu-arrow {
  color: #D3CFC2;
  font-size: 32rpx;
}
.logout-btn {
  margin: 40rpx 24rpx;
  background: #ffffff;
  color: #A63D40;
  border-radius: 44rpx;
  font-size: 30rpx;
  box-shadow: 0 6rpx 24rpx rgba(31, 41, 55, 0.06);
}
</style>
