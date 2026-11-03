<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-wrap g-primary">
      <view class="search-box">
        <text class="search-ico tn-icon-search"></text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索名称 / 城市"
          confirm-type="search"
          @confirm="onSearch"
        />
      </view>
      <text class="search-btn" @click="onSearch">搜索</text>
    </view>

    <!-- 类型分段 -->
    <view class="seg-bar">
      <view
        v-for="t in types"
        :key="t.key"
        class="seg-item"
        :class="{ active: currentType === t.key }"
        @click="switchType(t.key)"
      >
        {{ t.label }}
      </view>
    </view>

    <!-- 分类筛选 -->
    <scroll-view scroll-x class="cat-scroll">
      <view class="cat-item" :class="{ active: categoryId === null }" @click="selectCategory(null)">全部</view>
      <view
        v-for="c in categories"
        :key="c.id"
        class="cat-item"
        :class="{ active: categoryId === c.id }"
        @click="selectCategory(c.id)"
      >
        {{ c.name }}
      </view>
    </scroll-view>

    <!-- 列表 -->
    <view class="list">
      <view
        v-for="item in list"
        :key="item.id"
        class="list-card card"
        @click="goDetail(item.id)"
      >
        <image class="card-img" :src="resolveUrl(item.coverImage)" mode="aspectFill" />
        <view class="card-info">
          <text class="card-name ellipsis">{{ item.name }}</text>
          <text class="card-sub ellipsis">
            {{ item.city }}
            <template v-if="currentType === 'hotel'"> · {{ item.starLevel }}星</template>
            <template v-else-if="currentType === 'restaurant'"> · 人均 ¥{{ formatPrice(item.avgPrice) }}</template>
          </text>
          <view class="card-bottom">
            <text class="score"><text class="score-star tn-icon-star-fill"></text>{{ item.rating }}</text>
            <text class="comment-count">{{ item.commentCount }}条点评</text>
          </view>
        </view>
      </view>

      <view v-if="loadingMore" class="load-tip">加载中...</view>
      <view v-else-if="noMore && list.length" class="load-tip">没有更多了</view>
      <view v-if="!list.length && !loadingMore" class="empty">
        <text class="empty-icon tn-icon-empty-search"></text>
        <text class="empty-text">暂无相关数据</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { hotelPage, spotPage, restaurantPage } from '../../api/index.js'
import { CATEGORIES, resolveUrl } from '../../config/index.js'
import { formatPrice } from '../../utils/format.js'

const types = [
  { key: 'hotel', label: '酒店' },
  { key: 'spot', label: '景点' },
  { key: 'restaurant', label: '餐厅' }
]

const currentType = ref('hotel')
const keyword = ref('')
const categoryId = ref(null)
const list = ref([])
const current = ref(1)
const size = 10
const noMore = ref(false)
const loadingMore = ref(false)

const categories = computed(() => CATEGORIES[currentType.value] || [])

onLoad((options) => {
  if (options && options.type) {
    currentType.value = options.type
  }
  if (options && options.categoryId) {
    categoryId.value = Number(options.categoryId)
  }
  resetAndLoad()
})

function switchType(key) {
  if (currentType.value === key) return
  currentType.value = key
  categoryId.value = null
  resetAndLoad()
}

function selectCategory(id) {
  categoryId.value = id
  resetAndLoad()
}

function onSearch() {
  resetAndLoad()
}

function resetAndLoad() {
  current.value = 1
  list.value = []
  noMore.value = false
  loadList()
}

function buildParams() {
  const params = { current: current.value, size }
  if (keyword.value) params.name = keyword.value
  if (categoryId.value !== null) params.categoryId = categoryId.value
  return params
}

async function loadList() {
  const params = buildParams()
  const api = { hotel: hotelPage, spot: spotPage, restaurant: restaurantPage }[currentType.value]
  try {
    const data = await api(params)
    const records = (data && data.records) || []
    list.value = list.value.concat(records)
    noMore.value = list.value.length >= (data.total || 0)
  } catch (e) {}
}

onReachBottom(() => {
  if (noMore.value || loadingMore.value) return
  loadingMore.value = true
  current.value += 1
  loadList().finally(() => {
    loadingMore.value = false
  })
})

function goDetail(id) {
  const urlMap = {
    hotel: `/pages/hotel/detail?id=${id}`,
    spot: `/pages/spot/detail?id=${id}`,
    restaurant: `/pages/restaurant/detail?id=${id}`
  }
  uni.navigateTo({ url: urlMap[currentType.value] })
}
</script>

<style lang="scss" scoped>
.search-wrap {
  display: flex;
  align-items: center;
  padding: 24rpx 24rpx 56rpx;
  border-radius: 0 0 32rpx 32rpx;
}
.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  height: 72rpx;
  background: #ffffff;
  border-radius: 36rpx;
  padding: 0 24rpx;
}
.search-ico {
  font-size: 30rpx;
  color: #ABA79A;
  margin-right: 12rpx;
}
.search-input {
  flex: 1;
  height: 72rpx;
  font-size: 26rpx;
}
.search-btn {
  margin-left: 20rpx;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
}
.seg-bar {
  display: flex;
  background: #ffffff;
  margin: -32rpx 24rpx 0;
  padding: 8rpx 12rpx;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(31, 41, 55, 0.06);
}
.seg-item {
  padding: 20rpx 40rpx;
  font-size: 30rpx;
  color: #7A7A6E;
  position: relative;
}
.seg-item.active {
  color: #2F4F45;
  font-weight: 600;
}
.seg-item.active::after {
  content: '';
  position: absolute;
  bottom: 6rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background-image: linear-gradient(90deg, #2F4F45, #22352F);
}
.cat-scroll {
  white-space: nowrap;
  padding: 20rpx 24rpx;
  background: #ffffff;
  margin: 16rpx 24rpx 0;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(31, 41, 55, 0.06);
}
.cat-item {
  display: inline-block;
  padding: 8rpx 28rpx;
  background: #EFEBE0;
  color: #5A5F56;
  font-size: 24rpx;
  border-radius: 28rpx;
  margin-right: 16rpx;
}
.cat-item.active {
  background: linear-gradient(135deg, #2F4F45, #22352F);
  color: #ffffff;
}
.list {
  padding-bottom: 20rpx;
}
.list-card {
  display: flex;
  padding: 20rpx 24rpx;
}
.card-img {
  width: 200rpx;
  height: 150rpx;
  border-radius: 16rpx;
  background: #E3E0D4;
  flex-shrink: 0;
}
.card-info {
  flex: 1;
  margin-left: 20rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.card-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #33382F;
}
.card-sub {
  font-size: 24rpx;
  color: #ABA79A;
  margin-top: 8rpx;
}
.card-bottom {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.score {
  color: #C9A05A;
  font-size: 24rpx;
  font-weight: 600;
}
.score-star {
  margin-right: 6rpx;
}
.comment-count {
  color: #ABA79A;
  font-size: 22rpx;
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
  padding-top: 160rpx;
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
