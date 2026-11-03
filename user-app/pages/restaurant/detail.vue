<template>
  <view class="page">
    <view v-if="info">
      <image class="cover" :src="resolveUrl(info.coverImage)" mode="aspectFill" />

      <view class="head card">
        <text class="name">{{ info.name }}</text>
        <view class="meta">
          <text class="rating">{{ info.rating }} 分</text>
          <text class="comment">{{ info.commentCount }}条点评</text>
          <text class="avg">人均 ¥{{ formatPrice(info.avgPrice) }}</text>
        </view>
        <view class="addr"><text class="addr-ico tn-icon-location-fill"></text><text>{{ info.province }}{{ info.city }} {{ info.address }}</text></view>
        <view v-if="info.businessHours" class="addr"><text class="addr-ico tn-icon-clock-fill"></text><text>营业时间：{{ info.businessHours }}</text></view>
      </view>

      <view class="desc card">
        <view class="section-title">餐厅介绍</view>
        <text class="desc-text">{{ info.description }}</text>
      </view>

      <view class="dish card">
        <view class="section-title">推荐菜品</view>
        <view v-if="dishes.length">
          <view class="dish-item" v-for="d in dishes" :key="d.id">
            <image v-if="d.image" class="dish-img" :src="resolveUrl(d.image)" mode="aspectFill" />
            <view class="dish-info">
              <text class="dish-name">{{ d.name }}</text>
              <text class="dish-sub">{{ d.category }} · 已售{{ d.sales }}</text>
              <text v-if="d.description" class="dish-desc ellipsis">{{ d.description }}</text>
            </view>
            <text class="dish-price"><text class="price">¥{{ formatPrice(d.price) }}</text></text>
          </view>
        </view>
        <text v-else class="empty-inline">暂无菜品</text>
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
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { restaurantDetail, addFavorite, removeFavorite, favoritePage } from '../../api/index.js'
import { formatPrice } from '../../utils/format.js'
import { resolveUrl } from '../../config/index.js'
import store from '../../store/index.js'

const TARGET_TYPE = 3
const id = ref(null)
const info = ref(null)
const dishes = ref([])
const favorited = ref(false)
const favoriteId = ref(null)

onLoad((options) => {
  id.value = Number(options.id)
  loadDetail()
  loadFavorite()
})

async function loadDetail() {
  try {
    const data = await restaurantDetail(id.value)
    info.value = data.info
    dishes.value = data.dishes || []
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
.rating {
  color: #2F4F45;
  font-size: 26rpx;
  font-weight: 600;
}
.comment {
  color: #ABA79A;
  font-size: 24rpx;
  margin: 0 16rpx;
}
.avg {
  color: #A63D40;
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
.dish {
  border-radius: 0;
}
.dish-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #EFEBE0;
}
.dish-item:last-child {
  border-bottom: none;
}
.dish-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  background: #E3E0D4;
  flex-shrink: 0;
}
.dish-info {
  flex: 1;
  margin: 0 20rpx;
  overflow: hidden;
}
.dish-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #33382F;
}
.dish-sub {
  display: block;
  font-size: 22rpx;
  color: #ABA79A;
  margin-top: 6rpx;
}
.dish-desc {
  display: block;
  font-size: 22rpx;
  color: #7A7A6E;
  margin-top: 6rpx;
}
.dish-price {
  flex-shrink: 0;
}
.dish-price .price {
  font-size: 30rpx;
}
.comment-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 0;
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
</style>
