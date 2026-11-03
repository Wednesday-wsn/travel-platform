<template>
  <view class="page">
    <view v-if="list.length" class="comment-list">
      <view class="comment-card card" v-for="c in list" :key="c.id">
        <view class="comment-head">
          <text class="comment-target">{{ TARGET_TYPE[c.targetType] }} #{{ c.targetId }}</text>
          <text class="comment-star">{{ stars(c.rating) }}</text>
        </view>
        <text class="comment-content">{{ c.content }}</text>
        <view class="comment-foot">
          <text class="comment-time">{{ formatDateTime(c.createTime) }}</text>
          <text class="comment-status" :class="'st' + c.status">{{ statusText(c.status) }}</text>
        </view>
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
      <text class="empty-text">还没有发表过评价</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { myCommentPage } from '../../api/index.js'
import { TARGET_TYPE } from '../../config/index.js'
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
    const data = await myCommentPage({ current: current.value, size })
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

function statusText(status) {
  return { 0: '待审核', 1: '已通过', 2: '已驳回' }[status] || ''
}
</script>

<style lang="scss" scoped>
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
.comment-target {
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
.comment-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}
.comment-time {
  font-size: 22rpx;
  color: #ABA79A;
}
.comment-status {
  font-size: 22rpx;
}
.st0 { color: #C9A05A; }
.st1 { color: #5A8A66; }
.st2 { color: #A63D40; }
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
