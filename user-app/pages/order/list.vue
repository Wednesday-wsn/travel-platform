<template>
  <view class="page">
    <!-- 状态筛选 -->
    <view class="status-bar">
      <view
        v-for="s in statuses"
        :key="s.value === null ? 'all' : s.value"
        class="status-item"
        :class="{ active: currentStatus === s.value }"
        @click="switchStatus(s.value)"
      >
        {{ s.label }}
      </view>
    </view>

    <!-- 未登录提示 -->
    <view v-if="!isLogin" class="empty">
      <text class="empty-icon tn-icon-my-fill"></text>
      <text class="empty-text">登录后查看订单</text>
      <button class="btn-primary empty-btn" @click="goLogin">去登录</button>
    </view>

    <!-- 订单列表 -->
    <template v-else>
      <view v-if="list.length" class="order-list">
        <view class="order-card card" v-for="o in list" :key="o.id" @click="goDetail(o.id)">
          <view class="order-head">
            <text class="order-name ellipsis">{{ o.targetName }}</text>
            <text class="order-status" :class="'st' + o.status">{{ ORDER_STATUS[o.status] }}</text>
          </view>
          <view class="order-row">
            <text class="order-label">订单号</text>
            <text class="order-value">{{ o.orderNo }}</text>
          </view>
          <view class="order-row">
            <text class="order-label">类型</text>
            <text class="order-value">{{ o.orderType === 1 ? '酒店' : '景点' }}</text>
          </view>
          <view class="order-row">
            <text class="order-label">下单时间</text>
            <text class="order-value">{{ formatDateTime(o.createTime) }}</text>
          </view>
          <view class="order-foot">
            <text class="order-amount">合计 <text class="price">¥{{ formatPrice(o.totalAmount) }}</text></text>
            <view class="order-actions" @click.stop>
              <view v-if="o.status === 0" class="action-btn" @click="onPay(o)">支付</view>
              <view v-if="o.status === 0" class="action-btn ghost" @click="onCancel(o)">取消</view>
              <view v-if="o.status === 1" class="action-btn" @click="onComplete(o)">确认完成</view>
              <view v-if="o.status === 3" class="action-btn" @click="goComment(o)">评价</view>
            </view>
          </view>
        </view>
        <view v-if="loadingMore" class="load-tip">加载中...</view>
        <view v-else-if="noMore" class="load-tip">没有更多了</view>
      </view>

      <view v-else class="empty">
        <text class="empty-icon tn-icon-empty-order"></text>
        <text class="empty-text">暂无相关订单</text>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { orderPage, payOrder, cancelOrder, completeOrder } from '../../api/index.js'
import { ORDER_STATUS } from '../../config/index.js'
import { formatPrice, formatDateTime } from '../../utils/format.js'
import store from '../../store/index.js'
import { refreshOrderBadge } from '../../utils/badge.js'

const statuses = [
  { label: '全部', value: null },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已取消', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已退款', value: 4 }
]

const isLogin = ref(false)
const list = ref([])
const currentStatus = ref(null)
const current = ref(1)
const size = 10
const noMore = ref(false)
const loadingMore = ref(false)

onShow(() => {
  isLogin.value = store.isLogin()
  if (isLogin.value) {
    resetAndLoad()
  }
})

function resetAndLoad() {
  current.value = 1
  list.value = []
  noMore.value = false
  loadList()
}

function switchStatus(v) {
  currentStatus.value = v
  resetAndLoad()
}

async function loadList() {
  const params = { current: current.value, size }
  if (currentStatus.value !== null) {
    params.status = currentStatus.value
  }
  try {
    const data = await orderPage(params)
    const records = (data && data.records) || []
    list.value = list.value.concat(records)
    noMore.value = list.value.length >= (data.total || 0)
  } catch (e) {}
}

onReachBottom(() => {
  if (!isLogin.value || noMore.value || loadingMore.value) return
  loadingMore.value = true
  current.value += 1
  loadList().finally(() => {
    loadingMore.value = false
  })
})

function onPay(o) {
  uni.showModal({
    title: '确认支付',
    content: `支付 ¥${formatPrice(o.totalAmount)}？`,
    success: (res) => {
      if (res.confirm) {
        payOrder(o.id).then(() => {
          uni.showToast({ title: '支付成功', icon: 'success' })
          resetAndLoad()
          refreshOrderBadge()
        }).catch(() => {})
      }
    }
  })
}

function onCancel(o) {
  uni.showModal({
    title: '取消订单',
    content: '确定取消该订单吗？',
    success: (res) => {
      if (res.confirm) {
        cancelOrder(o.id).then(() => {
          uni.showToast({ title: '已取消', icon: 'success' })
          resetAndLoad()
          refreshOrderBadge()
        }).catch(() => {})
      }
    }
  })
}

function onComplete(o) {
  uni.showModal({
    title: '确认完成',
    content: '确认订单已完成？完成后即可评价。',
    success: (res) => {
      if (res.confirm) {
        completeOrder(o.id).then(() => {
          uni.showToast({ title: '已完成', icon: 'success' })
          resetAndLoad()
        }).catch(() => {})
      }
    }
  })
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

function goComment(o) {
  uni.navigateTo({ url: `/pages/comment/write?orderId=${o.id}&targetType=${o.orderType}&targetId=${o.targetId}&name=${encodeURIComponent(o.targetName)}` })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/login' })
}
</script>

<style lang="scss" scoped>
.status-bar {
  display: flex;
  background: #ffffff;
  padding: 16rpx 0;
  position: sticky;
  top: 0;
  z-index: 10;
}
.status-item {
  flex: 1;
  text-align: center;
  font-size: 26rpx;
  color: #7A7A6E;
  padding: 10rpx 0;
}
.status-item.active {
  color: #2F4F45;
  font-weight: 600;
  border-bottom: 4rpx solid #2F4F45;
}
.order-list {
  padding-bottom: 20rpx;
}
.order-card {
  padding: 24rpx;
}
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.order-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #33382F;
  flex: 1;
}
.order-status {
  font-size: 24rpx;
  margin-left: 16rpx;
  flex-shrink: 0;
}
.st0 { color: #C9A05A; }
.st1 { color: #2F4F45; }
.st2 { color: #ABA79A; }
.st3 { color: #5A8A66; }
.st4 { color: #A63D40; }
.order-row {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  margin-top: 10rpx;
}
.order-label {
  color: #ABA79A;
}
.order-value {
  color: #5A5F56;
}
.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #EFEBE0;
}
.order-amount {
  font-size: 26rpx;
}
.order-actions {
  display: flex;
}
.action-btn {
  padding: 10rpx 32rpx;
  border: 1rpx solid #2F4F45;
  color: #2F4F45;
  border-radius: 32rpx;
  font-size: 24rpx;
  margin-left: 16rpx;
}
.action-btn.ghost {
  border-color: #D3CFC2;
  color: #7A7A6E;
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
.empty-btn {
  width: 300rpx;
  margin-top: 40rpx;
  border: none;
}
.load-tip {
  text-align: center;
  color: #ABA79A;
  font-size: 24rpx;
  padding: 20rpx 0;
}
</style>
