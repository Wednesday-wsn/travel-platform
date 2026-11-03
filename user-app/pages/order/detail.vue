<template>
  <view class="page" v-if="order">
    <!-- 状态卡 -->
    <view class="status-card g-primary">
      <view class="garden-overlay"></view>
      <view class="status-circle c1"></view>
      <view class="status-circle c2"></view>
      <text class="status-text">{{ ORDER_STATUS[order.status] }}</text>
      <text class="status-amount">¥{{ formatPrice(order.totalAmount) }}</text>
    </view>

    <!-- 订单信息 -->
    <view class="card">
      <view class="section-title">订单信息</view>
      <view class="row"><text class="row-label">目标</text><text class="row-value">{{ order.targetName }}</text></view>
      <view class="row"><text class="row-label">订单号</text><text class="row-value">{{ order.orderNo }}</text></view>
      <view class="row"><text class="row-label">类型</text><text class="row-value">{{ order.orderType === 1 ? '酒店' : '景点' }}</text></view>
      <view class="row"><text class="row-label">联系人</text><text class="row-value">{{ order.contactName }} {{ order.contactPhone }}</text></view>
      <view class="row"><text class="row-label">下单时间</text><text class="row-value">{{ formatDateTime(order.createTime) }}</text></view>
      <view v-if="order.remark" class="row"><text class="row-label">备注</text><text class="row-value">{{ order.remark }}</text></view>
    </view>

    <!-- 明细 -->
    <view class="card">
      <view class="section-title">订单明细</view>
      <view class="item" v-for="it in items" :key="it.id">
        <view class="item-info">
          <text class="item-name">{{ it.itemName }}</text>
          <text class="item-sub">
            {{ it.itemType === 1 ? '房型' : '票型' }} × {{ it.quantity }}
            <template v-if="it.checkInDate"> · {{ it.checkInDate }} 入住</template>
            <template v-if="it.checkOutDate"> 至 {{ it.checkOutDate }}</template>
            <template v-if="it.visitDate"> · {{ it.visitDate }} 游玩</template>
          </text>
        </view>
        <text class="item-price">¥{{ formatPrice(it.subtotal) }}</text>
      </view>
    </view>

    <view class="bottom-space"></view>

    <!-- 操作 -->
    <view class="bottom-bar" v-if="order.status === 0 || order.status === 1 || order.status === 3">
      <view v-if="order.status === 0" class="action ghost" @click="onCancel">取消订单</view>
      <view v-if="order.status === 0" class="action primary" @click="onPay">立即支付</view>
      <view v-if="order.status === 1" class="action primary" @click="onComplete">确认完成</view>
      <view v-if="order.status === 3" class="action primary" @click="onComment">去评价</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderDetail, payOrder, cancelOrder, completeOrder } from '../../api/index.js'
import { ORDER_STATUS } from '../../config/index.js'
import { formatPrice, formatDateTime } from '../../utils/format.js'
import { refreshOrderBadge } from '../../utils/badge.js'

const order = ref(null)
const items = ref([])
const id = ref(null)

onLoad((options) => {
  id.value = Number(options.id)
  load()
})

async function load() {
  try {
    const data = await orderDetail(id.value)
    order.value = data.order
    items.value = data.items || []
  } catch (e) {}
}

function onPay() {
  uni.showModal({
    title: '确认支付',
    content: `支付 ¥${formatPrice(order.value.totalAmount)}？`,
    success: (res) => {
      if (res.confirm) {
        payOrder(id.value).then(() => {
          uni.showToast({ title: '支付成功', icon: 'success' })
          load()
          refreshOrderBadge()
        }).catch(() => {})
      }
    }
  })
}

function onCancel() {
  uni.showModal({
    title: '取消订单',
    content: '确定取消该订单吗？',
    success: (res) => {
      if (res.confirm) {
        cancelOrder(id.value).then(() => {
          uni.showToast({ title: '已取消', icon: 'success' })
          load()
          refreshOrderBadge()
        }).catch(() => {})
      }
    }
  })
}

function onComplete() {
  uni.showModal({
    title: '确认完成',
    content: '确认订单已完成？完成后即可评价。',
    success: (res) => {
      if (res.confirm) {
        completeOrder(id.value).then(() => {
          uni.showToast({ title: '已完成', icon: 'success' })
          load()
        }).catch(() => {})
      }
    }
  })
}

function onComment() {
  uni.navigateTo({
    url: `/pages/comment/write?orderId=${id.value}&targetType=${order.value.orderType}&targetId=${order.value.targetId}&name=${encodeURIComponent(order.value.targetName)}`
  })
}
</script>

<style lang="scss" scoped>
.status-card {
  position: relative;
  overflow: hidden;
  padding: 60rpx 40rpx 80rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 0 0 40rpx 40rpx;
}
.status-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}
.c1 {
  width: 260rpx;
  height: 260rpx;
  right: -60rpx;
  top: -80rpx;
}
.c2 {
  width: 160rpx;
  height: 160rpx;
  left: 40rpx;
  bottom: -50rpx;
  background: rgba(255, 255, 255, 0.08);
}
.status-text {
  position: relative;
  z-index: 1;
  color: #ffffff;
  font-size: 38rpx;
  font-weight: 700;
}
.status-amount {
  position: relative;
  z-index: 1;
  color: #ffffff;
  font-size: 36rpx;
  font-weight: 600;
}
.section-title {
  position: relative;
  padding-left: 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #33382F;
  margin-bottom: 8rpx;
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
.row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  font-size: 26rpx;
  border-bottom: 1rpx solid #EFEBE0;
}
.row:last-child {
  border-bottom: none;
}
.row-label {
  color: #ABA79A;
  flex-shrink: 0;
}
.row-value {
  color: #33382F;
  text-align: right;
}
.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #EFEBE0;
}
.item:last-child {
  border-bottom: none;
}
.item-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #33382F;
}
.item-sub {
  display: block;
  font-size: 22rpx;
  color: #ABA79A;
  margin-top: 6rpx;
}
.item-price {
  font-size: 28rpx;
  color: #33382F;
}
.bottom-space {
  height: 130rpx;
}
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #ffffff;
  display: flex;
  justify-content: flex-end;
  padding: 20rpx 24rpx;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.action {
  padding: 0 48rpx;
  line-height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin-left: 20rpx;
}
.action.primary {
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 45%, #22352F 100%);
  color: #ffffff;
}
.action.ghost {
  border: 1rpx solid #D3CFC2;
  color: #7A7A6E;
}
</style>
