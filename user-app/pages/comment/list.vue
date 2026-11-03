<template>
  <view class="page">
    <view class="title-bar card">
      <text class="title">{{ name }}</text>
      <text class="sub">用户评价</text>
    </view>

    <view v-if="list.length" class="comment-list">
      <view class="comment-card card" v-for="c in list" :key="c.id">
        <view class="comment-head">
          <text class="comment-user">游客{{ c.userId }}</text>
          <text class="comment-star">{{ stars(c.rating) }}</text>
        </view>
        <text class="comment-content">{{ c.content }}</text>
        <text class="comment-time">{{ formatDateTime(c.createTime) }}</text>
        <view v-if="c.reply" class="reply">
          <text class="reply-label">商家回复：</text>
          <text class="reply-text">{{ c.reply }}</text>
        </view>
      </view>
      <view v-if="loadingMore" class="load-tip">加载中...</view>
      <view v-else-if="noMore" class="load-tip">没有更多了</view>
    </view>

    <view v-else-if="!loading" class="empty">
      <text class="empty-icon tn-icon-comment-fill"></text>
      <text class="empty-text">暂无评价</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { commentPage } from '../../api/index.js'
import { formatDateTime } from '../../utils/format.js'

const name = ref('')
const targetType = ref(null)
const targetId = ref(null)
const list = ref([])
const current = ref(1)
const size = 10
const noMore = ref(false)
const loadingMore = ref(false)
const loading = ref(false)

onLoad((options) => {
  name.value = decodeURIComponent(options.name || '')
  targetType.value = Number(options.targetType)
  targetId.value = Number(options.targetId)
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
    const data = await commentPage({ current: current.value, size, targetType: targetType.value, targetId: targetId.value })
    const records = (data && data.records) || []
    list.value = list.value.concat(records)
    noMore.value = list.value.length >= (data.total || 0)
  } catch (e) {}
  loading.value = false
}

onReachBottom(() => {
  if (noMore.value || loadingMore.value) return
  loadingMore.value = true
  current.value += 1
  loadList().finally(() => {
    loadingMore.value = false
  })
})

function stars(rating) {
  let s = ''
  for (let i = 0; i < 5; i++) {
    s += i < rating ? '★' : '☆'
  }
  return s
}
</script>

<style lang="scss" scoped>
.title-bar {
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 34rpx;
  font-weight: 700;
  color: #33382F;
}
.sub {
  font-size: 24rpx;
  color: #ABA79A;
  margin-top: 8rpx;
}
.comment-list {
  padding-bottom: 20rpx;
}
.comment-card {
  padding: 24rpx;
}
.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.comment-user {
  font-size: 26rpx;
  color: #5A5F56;
}
.comment-star {
  color: #C9A05A;
  font-size: 24rpx;
}
.comment-content {
  display: block;
  font-size: 28rpx;
  color: #33382F;
  margin-top: 16rpx;
  line-height: 1.6;
}
.comment-time {
  display: block;
  font-size: 22rpx;
  color: #ABA79A;
  margin-top: 12rpx;
}
.reply {
  background: #F8F5EE;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-top: 16rpx;
}
.reply-label {
  font-size: 24rpx;
  color: #2F4F45;
}
.reply-text {
  font-size: 24rpx;
  color: #5A5F56;
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
