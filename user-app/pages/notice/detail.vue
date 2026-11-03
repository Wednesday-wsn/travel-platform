<template>
  <view class="page">
    <view class="card">
      <text class="title">{{ title }}</text>
      <text class="time">{{ time }}</text>
      <view class="divider"></view>
      <text class="content">{{ content }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const title = ref('')
const content = ref('')
const time = ref('')

onLoad((options) => {
  title.value = decodeURIComponent(options.title || '')
  content.value = decodeURIComponent(options.content || '')
  time.value = decodeURIComponent(options.time || '')
})
</script>

<style lang="scss" scoped>
.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #33382F;
}
.time {
  display: block;
  font-size: 24rpx;
  color: #ABA79A;
  margin-top: 12rpx;
}
.divider {
  height: 1rpx;
  background: #EFEBE0;
  margin: 24rpx 0;
}
.content {
  font-size: 28rpx;
  color: #3A3F36;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
