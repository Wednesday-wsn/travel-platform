<template>
  <view class="page">
    <!-- 沉浸式头部：搜索框浮在轮播图上 -->
    <view class="hero" :class="{ 'hero-empty': !banners.length }">
      <swiper v-if="banners.length" class="banner" circular autoplay :interval="4000" indicator-dots indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#ffffff">
        <swiper-item v-for="b in banners" :key="b.id" @click="onBanner(b)">
          <image class="banner-img" :src="resolveUrl(b.image)" mode="aspectFill" />
        </swiper-item>
      </swiper>
      <view class="hero-mask"></view>
      <view class="ink-scenery"></view>
      <view class="search-bar" @click="goDiscover()">
        <view class="search-input">
          <text class="search-icon tn-icon-search"></text>
          <text class="search-text">搜索酒店 / 景点 / 餐厅</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-nav card">
      <view class="quick-item" @click="goDiscover('hotel')">
        <view class="quick-badge g-blue"><text class="tn-icon-home-capsule-fill"></text></view>
        <text class="quick-text">酒店</text>
      </view>
      <view class="quick-item" @click="goDiscover('spot')">
        <view class="quick-badge g-cyan"><text class="tn-icon-location-fill"></text></view>
        <text class="quick-text">景点</text>
      </view>
      <view class="quick-item" @click="goDiscover('restaurant')">
        <view class="quick-badge g-green"><text class="tn-icon-food-fill"></text></view>
        <text class="quick-text">餐厅</text>
      </view>
      <view class="quick-item" @click="goAi()">
        <view class="quick-badge g-purple"><text class="tn-icon-ai-fill"></text></view>
        <text class="quick-text">AI 助手</text>
      </view>
    </view>

    <!-- 猜你喜欢（个性化推荐） -->
    <view v-if="recommends.length" class="section">
      <view class="section-head">
        <view class="section-title">猜你喜欢</view>
        <text class="more">为你推荐</text>
      </view>
      <scroll-view scroll-x class="h-scroll">
        <view class="h-card" v-for="it in recommends" :key="it.type + '-' + it.id" @click="goRecommend(it)">
          <image class="h-img" :src="resolveUrl(it.coverImage)" mode="aspectFill" />
          <view class="h-info">
            <text class="rec-reason">{{ it.reason }}</text>
            <text class="h-name ellipsis">{{ it.name }}</text>
            <text class="h-sub ellipsis">{{ it.sub }}</text>
            <view class="h-bottom">
              <text class="score"><text class="score-star tn-icon-star-fill"></text>{{ it.rating }}</text>
              <text class="h-comment">{{ it.city }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 公告 -->
    <view v-if="notices.length" class="notice card" @click="goNotice()">
      <text class="notice-tag">公告</text>
      <text class="notice-title ellipsis">{{ notices[0].title }}</text>
      <text class="notice-arrow">›</text>
    </view>

    <!-- 精选酒店 -->
    <view class="section">
      <view class="section-head">
        <view class="section-title">精选酒店</view>
        <text class="more" @click="goDiscover('hotel')">更多 ›</text>
      </view>
      <scroll-view scroll-x class="h-scroll">
        <view class="h-card" v-for="h in hotels" :key="h.id" @click="goHotel(h.id)">
          <image class="h-img" :src="resolveUrl(h.coverImage)" mode="aspectFill" />
          <view class="h-info">
            <text class="h-name ellipsis">{{ h.name }}</text>
            <text class="h-sub ellipsis">{{ h.city }} · {{ h.starLevel }}星</text>
            <view class="h-bottom">
              <text class="score"><text class="score-star tn-icon-star-fill"></text>{{ h.rating }}</text>
              <text class="h-comment">{{ h.commentCount }}条点评</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 热门景点 -->
    <view class="section">
      <view class="section-head">
        <view class="section-title">热门景点</view>
        <text class="more" @click="goDiscover('spot')">更多 ›</text>
      </view>
      <scroll-view scroll-x class="h-scroll">
        <view class="h-card" v-for="s in spots" :key="s.id" @click="goSpot(s.id)">
          <image class="h-img" :src="resolveUrl(s.coverImage)" mode="aspectFill" />
          <view class="h-info">
            <text class="h-name ellipsis">{{ s.name }}</text>
            <text class="h-sub ellipsis">{{ s.city }}</text>
            <view class="h-bottom">
              <text class="score"><text class="score-star tn-icon-star-fill"></text>{{ s.rating }}</text>
              <text class="h-comment">{{ s.commentCount }}条点评</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 美食餐厅 -->
    <view class="section">
      <view class="section-head">
        <view class="section-title">美食餐厅</view>
        <text class="more" @click="goDiscover('restaurant')">更多 ›</text>
      </view>
      <view class="rest-list card" v-for="r in restaurants" :key="r.id" @click="goRestaurant(r.id)">
        <image class="rest-img" :src="resolveUrl(r.coverImage)" mode="aspectFill" />
        <view class="rest-info">
          <text class="h-name ellipsis">{{ r.name }}</text>
          <text class="h-sub ellipsis">{{ r.city }} · <text class="tn-icon-star-fill"></text> {{ r.rating }}</text>
          <text class="rest-price">人均 <text class="price">¥{{ formatPrice(r.avgPrice) }}</text></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getBanners, getNotices, hotelPage, spotPage, restaurantPage, getRecommend } from '../../api/index.js'
import { formatPrice } from '../../utils/format.js'
import { resolveUrl } from '../../config/index.js'
import store from '../../store/index.js'

const banners = ref([])
const notices = ref([])
const hotels = ref([])
const spots = ref([])
const restaurants = ref([])
const recommends = ref([])

onLoad(() => {
  loadData()
})

async function loadData() {
  try {
    const [b, n, h, s, r] = await Promise.all([
      getBanners(),
      getNotices(),
      hotelPage({ current: 1, size: 6 }),
      spotPage({ current: 1, size: 6 }),
      restaurantPage({ current: 1, size: 3 })
    ])
    banners.value = b || []
    notices.value = n || []
    hotels.value = (h && h.records) || []
    spots.value = (s && s.records) || []
    restaurants.value = (r && r.records) || []
  } catch (e) {
    // 错误已在 request 中提示
  }

  // 个性化推荐（仅登录用户；未登录则隐藏该区块）
  if (store.isLogin()) {
    try {
      const recs = await getRecommend()
      recommends.value = recs || []
    } catch (e) {
      // 推荐失败静默处理，不影响首页其他内容
    }
  }
}

function goDiscover(type) {
  const url = type ? `/pages/discover/discover?type=${type}` : '/pages/discover/discover'
  uni.navigateTo({ url })
}
function goHotel(id) {
  uni.navigateTo({ url: `/pages/hotel/detail?id=${id}` })
}
function goSpot(id) {
  uni.navigateTo({ url: `/pages/spot/detail?id=${id}` })
}
function goRestaurant(id) {
  uni.navigateTo({ url: `/pages/restaurant/detail?id=${id}` })
}
function goRecommend(it) {
  const urlMap = { 1: '/pages/hotel/detail', 2: '/pages/spot/detail', 3: '/pages/restaurant/detail' }
  uni.navigateTo({ url: `${urlMap[it.type] || '/pages/index/index'}?id=${it.id}` })
}
function goNotice() {
  uni.navigateTo({ url: '/pages/notice/list' })
}
function goAi() {
  uni.navigateTo({ url: '/pages/ai/index' })
}
function onBanner(b) {
  // linkType: 0无 1酒店 2景点 3餐厅
  if (b.linkType === 1 && b.linkId) goHotel(b.linkId)
  else if (b.linkType === 2 && b.linkId) goSpot(b.linkId)
  else if (b.linkType === 3 && b.linkId) goRestaurant(b.linkId)
}
</script>

<style lang="scss" scoped>
.page {
  padding-bottom: 40rpx;
}
.hero {
  position: relative;
  height: 400rpx;
  overflow: hidden;
}
.hero-empty {
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 45%, #22352F 100%);
}
.banner {
  width: 100%;
  height: 400rpx;
  margin: 0;
}
.banner-img {
  width: 100%;
  height: 400rpx;
}
.hero-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 160rpx;
  background-image: linear-gradient(180deg, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.28));
  pointer-events: none;
}
.ink-scenery {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 200rpx;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 750 200' preserveAspectRatio='none'%3E%3Cpath d='M0 140 Q 140 75 300 120 Q 460 165 750 115 L 750 200 L 0 200 Z' fill='rgba(34,53,47,0.30)'/%3E%3Cpath d='M0 155 Q 180 110 400 150 Q 620 190 750 140 L 750 200 L 0 200 Z' fill='rgba(34,53,47,0.55)'/%3E%3Cpath d='M0 172 Q 190 164 380 172 T 750 172' fill='none' stroke='rgba(255,255,255,0.40)' stroke-width='2'/%3E%3Cpath d='M0 186 Q 190 178 380 186 T 750 186' fill='none' stroke='rgba(255,255,255,0.22)' stroke-width='2'/%3E%3C/svg%3E");
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
.search-bar {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  padding: 24rpx;
  z-index: 2;
}
.search-input {
  display: flex;
  align-items: center;
  height: 68rpx;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 34rpx;
  padding: 0 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}
.search-icon {
  margin-right: 12rpx;
  font-size: 30rpx;
  color: #ABA79A;
}
.search-text {
  color: #ABA79A;
  font-size: 26rpx;
}
.quick-nav {
  display: flex;
  justify-content: space-around;
  padding: 28rpx 12rpx;
  margin-top: -32rpx;
  position: relative;
  z-index: 3;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.quick-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  height: 88rpx;
  border-radius: 28rpx;
  color: #ffffff;
  font-size: 44rpx;
  margin-bottom: 14rpx;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.12);
}
.quick-text {
  font-size: 24rpx;
  color: #5A5F56;
}
.notice {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
}
.notice-tag {
  background: #F3E9D2;
  color: #C9A05A;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}
.notice-title {
  flex: 1;
  color: #3A3F36;
  font-size: 26rpx;
}
.notice-arrow {
  color: #ABA79A;
  margin-left: 12rpx;
}
.section {
  margin-top: 10rpx;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx 12rpx;
}
.section-title {
  position: relative;
  padding-left: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #33382F;
}
.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8rpx;
  height: 30rpx;
  border-radius: 4rpx;
  background-image: linear-gradient(180deg, #2F4F45, #22352F);
}
.more {
  font-size: 24rpx;
  color: #ABA79A;
}
.h-scroll {
  white-space: nowrap;
  padding: 0 24rpx;
}
.h-card {
  display: inline-block;
  width: 280rpx;
  background: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-right: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(31, 41, 55, 0.06);
}
.h-img {
  width: 280rpx;
  height: 180rpx;
  background: #E3E0D4;
}
.h-info {
  padding: 16rpx;
}
.h-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #33382F;
}
.rec-reason {
  display: inline-block;
  font-size: 20rpx;
  color: #2F4F45;
  background: #EDF2EA;
  padding: 2rpx 12rpx;
  border-radius: 6rpx;
  margin-bottom: 8rpx;
}
.h-sub {
  display: block;
  font-size: 22rpx;
  color: #ABA79A;
  margin-top: 6rpx;
}
.h-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
}
.score {
  color: #C9A05A;
  font-size: 24rpx;
  font-weight: 600;
}
.score-star {
  margin-right: 6rpx;
}
.h-comment {
  color: #ABA79A;
  font-size: 20rpx;
}
.rest-list {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
}
.rest-img {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #E3E0D4;
  flex-shrink: 0;
}
.rest-info {
  flex: 1;
  margin-left: 20rpx;
  overflow: hidden;
}
.rest-price {
  font-size: 24rpx;
  color: #7A7A6E;
  margin-top: 6rpx;
}
</style>
