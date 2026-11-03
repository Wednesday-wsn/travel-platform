<template>
  <view class="page">
    <view v-if="list.length" class="fav-list">
      <view class="fav-card card" v-for="f in list" :key="f.id" @click="goDetail(f)">
        <image v-if="f.cover" class="fav-img" :src="resolveUrl(f.cover)" mode="aspectFill" />
        <view v-else class="fav-img placeholder"></view>
        <view class="fav-info">
          <text class="fav-name ellipsis">{{ f.name }}</text>
          <text class="fav-sub">{{ TARGET_TYPE[f.targetType] }} · {{ formatDateTime(f.createTime) }}</text>
        </view>
        <view class="fav-del" @click.stop="onRemove(f)">取消</view>
      </view>
      <view v-if="loadingMore" class="load-tip">加载中...</view>
      <view v-else-if="noMore" class="load-tip">没有更多了</view>
    </view>

    <view v-else-if="!loading" class="empty">
      <text class="empty-icon tn-icon-star-fill"></text>
      <text class="empty-text">还没有收藏内容</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { favoritePage, removeFavorite, hotelDetail, spotDetail, restaurantDetail } from '../../api/index.js'
import { TARGET_TYPE, resolveUrl } from '../../config/index.js'
import { formatDateTime } from '../../utils/format.js'
import store from '../../store/index.js'

const list = ref([])
const current = ref(1)
const size = 10
const noMore = ref(false)
const loadingMore = ref(false)
const loading = ref(false)

onShow(() => {
  if (!store.isLogin()) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 500)
    return
  }
  resetAndLoad()
})

function resetAndLoad() {
  current.value = 1
  list.value = []
  noMore.value = false
  loadList()
}

async function loadList() {
  loading.value = true
  try {
    const data = await favoritePage({ current: current.value, size })
    const records = (data && data.records) || []
    const enriched = await Promise.all(records.map((f) => enrich(f)))
    list.value = list.value.concat(enriched)
    noMore.value = list.value.length >= (data.total || 0)
  } catch (e) {}
  loading.value = false
}

async function enrich(f) {
  try {
    let detail
    if (f.targetType === 1) detail = await hotelDetail(f.targetId)
    else if (f.targetType === 2) detail = await spotDetail(f.targetId)
    else detail = await restaurantDetail(f.targetId)
    return { ...f, name: detail.info.name, cover: detail.info.coverImage }
  } catch (e) {
    return { ...f, name: '已下架', cover: '' }
  }
}

onReachBottom(() => {
  if (noMore.value || loadingMore.value) return
  loadingMore.value = true
  current.value += 1
  loadList().finally(() => {
    loadingMore.value = false
  })
})

function onRemove(f) {
  uni.showModal({
    title: '提示',
    content: '确定取消收藏吗？',
    success: (res) => {
      if (res.confirm) {
        removeFavorite(f.id).then(() => {
          list.value = list.value.filter((x) => x.id !== f.id)
          uni.showToast({ title: '已取消', icon: 'none' })
        }).catch(() => {})
      }
    }
  })
}

function goDetail(f) {
  const urlMap = {
    1: `/pages/hotel/detail?id=${f.targetId}`,
    2: `/pages/spot/detail?id=${f.targetId}`,
    3: `/pages/restaurant/detail?id=${f.targetId}`
  }
  uni.navigateTo({ url: urlMap[f.targetType] })
}
</script>

<style lang="scss" scoped>
.fav-list {
  padding-bottom: 20rpx;
}
.fav-card {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
}
.fav-img {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #E3E0D4;
  flex-shrink: 0;
}
.fav-img.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}
.fav-info {
  flex: 1;
  margin: 0 20rpx;
  overflow: hidden;
}
.fav-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #33382F;
}
.fav-sub {
  display: block;
  font-size: 22rpx;
  color: #ABA79A;
  margin-top: 10rpx;
}
.fav-del {
  padding: 10rpx 24rpx;
  border: 1rpx solid #D3CFC2;
  color: #7A7A6E;
  border-radius: 28rpx;
  font-size: 24rpx;
  flex-shrink: 0;
}
.load-tip {
  text-align: center;
  color: #ABA79A;
  font-size: 24rpx;
  padding: 20rpx 0;
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}
.empty-icon {
  font-size: 90rpx;
  color: #D3CFC2;
}
.empty-text {
  color: #ABA79A;
  font-size: 28rpx;
  margin-top: 20rpx;
}
</style>
