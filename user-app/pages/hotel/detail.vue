<template>
  <view class="page">
    <view v-if="info">
      <image class="cover" :src="resolveUrl(info.coverImage)" mode="aspectFill" />

      <view class="head card">
        <text class="name">{{ info.name }}</text>
        <view class="meta">
          <text class="star"><text class="tn-icon-star-fill"></text> {{ info.starLevel }} 星</text>
          <text class="rating">{{ info.rating }} 分</text>
          <text class="comment">{{ info.commentCount }}条点评</text>
        </view>
        <view class="addr">
          <text class="addr-ico tn-icon-location-fill"></text>
          <text>{{ info.province }}{{ info.city }}{{ info.district }} {{ info.address }}</text>
        </view>
        <view v-if="info.phone" class="addr"><text class="addr-ico tn-icon-phone-fill"></text><text>{{ info.phone }}</text></view>
      </view>

      <view class="desc card">
        <view class="section-title">酒店介绍</view>
        <text class="desc-text">{{ info.description }}</text>
      </view>

      <view class="room card">
        <view class="section-title">房型选择</view>
        <view v-if="rooms.length">
          <view class="room-item" v-for="r in rooms" :key="r.id">
            <image v-if="r.image" class="room-img" :src="resolveUrl(r.image)" mode="aspectFill" />
            <view class="room-info">
              <text class="room-name">{{ r.name }}</text>
              <text class="room-sub">{{ r.bedType }} · {{ r.area }} · 可住{{ r.maxGuests }}人{{ r.breakfast === 1 ? ' · 含早' : '' }}</text>
              <text class="room-remain">剩余 {{ r.remainingCount }} 间</text>
            </view>
            <view class="room-right">
              <text class="room-price"><text class="price">¥{{ formatPrice(r.price) }}</text>/晚</text>
              <text v-if="r.originalPrice" class="room-origin">¥{{ formatPrice(r.originalPrice) }}</text>
            </view>
          </view>
        </view>
        <text v-else class="empty-inline">暂无房型</text>
      </view>

      <view class="comment-entry card" @click="goComment">
        <view class="section-title">用户评价</view>
        <text class="more">查看全部 ›</text>
      </view>

      <view class="bottom-space"></view>

      <view class="bottom-bar">
        <view class="fav-btn" @click="onFavorite">
          <text :class="favorited ? 'tn-icon-like-fill fav-heart on' : 'tn-icon-like fav-heart'"></text>
          <text class="fav-text">{{ favorited ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="book-btn" @click="onBook">立即预订</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { hotelDetail, addFavorite, removeFavorite, favoritePage } from '../../api/index.js'
import { formatPrice } from '../../utils/format.js'
import { resolveUrl } from '../../config/index.js'
import store from '../../store/index.js'

const TARGET_TYPE = 1
const id = ref(null)
const info = ref(null)
const rooms = ref([])
const favorited = ref(false)
const favoriteId = ref(null)

onLoad((options) => {
  id.value = Number(options.id)
  loadDetail()
  loadFavorite()
})

async function loadDetail() {
  try {
    const data = await hotelDetail(id.value)
    info.value = data.info
    rooms.value = data.rooms || []
  } catch (e) {}
}

function loadFavorite() {
  if (!store.isLogin()) return
  favoritePage({ current: 1, size: 100, targetType: TARGET_TYPE })
    .then((data) => {
      const records = (data && data.records) || []
      const f = records.find((x) => Number(x.targetId) === id.value)
      if (f) {
        favorited.value = true
        favoriteId.value = f.id
      }
    })
    .catch(() => {})
}

function onFavorite() {
  if (!store.isLogin()) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 500)
    return
  }
  if (favorited.value) {
    removeFavorite(favoriteId.value).then(() => {
      favorited.value = false
      favoriteId.value = null
      uni.showToast({ title: '已取消收藏', icon: 'none' })
    }).catch(() => {})
  } else {
    addFavorite({ targetType: TARGET_TYPE, targetId: id.value }).then(() => {
      favorited.value = true
      uni.showToast({ title: '收藏成功', icon: 'success' })
    }).catch(() => {})
  }
}

function onBook() {
  if (!store.isLogin()) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 500)
    return
  }
  uni.navigateTo({ url: `/pages/order/confirm?type=1&id=${id.value}` })
}

function goComment() {
  uni.navigateTo({ url: `/pages/comment/list?targetType=${TARGET_TYPE}&targetId=${id.value}&name=${encodeURIComponent(info.value.name)}` })
}
</script>

<style lang="scss" scoped>
.cover {
  width: 100%;
  height: 360rpx;
  background: #E3E0D4;
}
.head {
  margin-top: -20rpx;
  border-radius: 24rpx 24rpx 0 0;
}
.name {
  font-size: 36rpx;
  font-weight: 700;
  color: #33382F;
}
.meta {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}
.star {
  color: #C9A05A;
  font-size: 26rpx;
}
.rating {
  color: #2F4F45;
  font-size: 26rpx;
  font-weight: 600;
  margin: 0 16rpx;
}
.comment {
  color: #ABA79A;
  font-size: 24rpx;
}
.addr {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #7A7A6E;
}
.addr-ico {
  margin-right: 8rpx;
  color: #ABA79A;
}
.desc {
  border-radius: 0;
}
.section-title {
  position: relative;
  padding-left: 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #33382F;
  margin-bottom: 16rpx;
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
.desc-text {
  font-size: 26rpx;
  color: #5A5F56;
  line-height: 1.6;
}
.room {
  border-radius: 0;
}
.room-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #EFEBE0;
}
.room-item:last-child {
  border-bottom: none;
}
.room-img {
  width: 140rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: #E3E0D4;
  flex-shrink: 0;
}
.room-info {
  flex: 1;
  margin-left: 20rpx;
  overflow: hidden;
}
.room-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #33382F;
}
.room-sub {
  display: block;
  font-size: 22rpx;
  color: #ABA79A;
  margin-top: 8rpx;
}
.room-remain {
  display: block;
  font-size: 22rpx;
  color: #5A8A66;
  margin-top: 8rpx;
}
.room-right {
  text-align: right;
  flex-shrink: 0;
}
.room-price {
  font-size: 24rpx;
  color: #7A7A6E;
}
.room-price .price {
  font-size: 32rpx;
}
.room-origin {
  display: block;
  font-size: 22rpx;
  color: #D3CFC2;
  text-decoration: line-through;
}
.comment-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 0;
  border-bottom: 1rpx solid #EFEBE0;
}
.more {
  color: #ABA79A;
  font-size: 24rpx;
}
.empty-inline {
  color: #ABA79A;
  font-size: 26rpx;
}
.bottom-space {
  height: 120rpx;
}
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #ffffff;
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.fav-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 120rpx;
  font-size: 36rpx;
}
.fav-text {
  font-size: 20rpx;
  color: #7A7A6E;
}
.fav-heart {
  font-size: 44rpx;
  color: #D3CFC2;
}
.fav-heart.on {
  color: #A63D40;
}
.book-btn {
  flex: 1;
  margin-left: 20rpx;
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 45%, #22352F 100%);
  color: #ffffff;
  text-align: center;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
}
</style>
