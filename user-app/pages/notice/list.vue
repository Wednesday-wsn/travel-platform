<template>
  <view class="page">
    <view v-if="list.length" class="notice-list">
      <view class="notice-card card" v-for="n in list" :key="n.id" @click="goDetail(n)">
        <view class="notice-head">
          <text class="notice-title ellipsis">{{ n.title }}</text>
          <text class="notice-date">{{ formatDate(n.createTime) }}</text>
        </view>
        <text class="notice-preview ellipsis">{{ n.content }}</text>
      </view>
    </view>

    <view v-else-if="!loading" class="empty">
      <text class="empty-icon tn-icon-notice-fill"></text>
      <text class="empty-text">暂无公告</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNotices } from '../../api/index.js'
import { formatDate } from '../../utils/format.js'

const list = ref([])
const loading = ref(false)

onLoad(() => {
  load()
})

async function load() {
  loading.value = true
  try {
    list.value = (await getNotices()) || []
  } catch (e) {}
  loading.value = false
}

function goDetail(n) {
  uni.navigateTo({
    url: `/pages/notice/detail?title=${encodeURIComponent(n.title)}&content=${encodeURIComponent(n.content)}&time=${encodeURIComponent(n.createTime)}`
  })
}
</script>

<style lang="scss" scoped>
.notice-list {
  padding-bottom: 20rpx;
}
.notice-card {
  padding: 24rpx;
}
.notice-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notice-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #33382F;
}
.notice-date {
  font-size: 22rpx;
  color: #ABA79A;
  margin-left: 16rpx;
  flex-shrink: 0;
}
.notice-preview {
  display: block;
  font-size: 24rpx;
  color: #7A7A6E;
  margin-top: 14rpx;
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
