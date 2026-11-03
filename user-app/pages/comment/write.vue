<template>
  <view class="page">
    <view class="target card">
      <text class="target-name">{{ name }}</text>
      <text class="target-sub">{{ TARGET_TYPE[targetType] }}</text>
    </view>

    <view class="card">
      <view class="section-title">评分</view>
      <view class="star-row">
        <text
          v-for="i in 5"
          :key="i"
          class="star"
          :class="[i <= rating ? 'tn-icon-star-fill on' : 'tn-icon-star']"
          @click="setRating(i)"
        ></text>
        <text class="star-text">{{ rating }} 分</text>
      </view>
    </view>

    <view class="card">
      <view class="section-title">评价内容</view>
      <textarea
        class="textarea"
        v-model="content"
        placeholder="分享你的体验感受..."
        maxlength="500"
      />
    </view>

    <button class="btn-primary submit" :loading="loading" @click="submit">发 表</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addComment } from '../../api/index.js'
import { TARGET_TYPE } from '../../config/index.js'

const name = ref('')
const targetType = ref(null)
const targetId = ref(null)
const orderId = ref(null)
const rating = ref(5)
const content = ref('')
const loading = ref(false)

onLoad((options) => {
  name.value = decodeURIComponent(options.name || '')
  targetType.value = Number(options.targetType)
  targetId.value = Number(options.targetId)
  orderId.value = options.orderId ? Number(options.orderId) : null
})

function setRating(n) {
  rating.value = n
}

function submit() {
  if (!content.value) return uni.showToast({ title: '请输入评价内容', icon: 'none' })

  const payload = {
    targetType: targetType.value,
    targetId: targetId.value,
    rating: rating.value,
    content: content.value
  }
  if (orderId.value) {
    payload.orderId = orderId.value
  }

  loading.value = true
  addComment(payload)
    .then(() => {
      uni.showToast({ title: '评价成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 600)
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}
</script>

<style lang="scss" scoped>
.target-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #33382F;
}
.target-sub {
  display: block;
  font-size: 24rpx;
  color: #ABA79A;
  margin-top: 8rpx;
}
.section-title {
  position: relative;
  padding-left: 20rpx;
  font-size: 28rpx;
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
  height: 28rpx;
  border-radius: 4rpx;
  background-image: linear-gradient(180deg, #2F4F45, #22352F);
}
.star-row {
  display: flex;
  align-items: center;
}
.star {
  font-size: 56rpx;
  color: #D3CFC2;
  margin-right: 12rpx;
}
.star.on {
  color: #C9A05A;
}
.star-text {
  margin-left: 16rpx;
  font-size: 26rpx;
  color: #C9A05A;
}
.textarea {
  width: 100%;
  height: 240rpx;
  background: #F8F5EE;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.submit {
  margin-top: 40rpx;
  border: none;
}
</style>
