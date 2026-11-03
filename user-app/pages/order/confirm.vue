<template>
  <view class="page">
    <view class="target card">
      <text class="target-name">{{ targetName }}</text>
      <text class="target-sub">{{ type === 1 ? '酒店预订' : '景点门票' }}</text>
    </view>

    <!-- 选择房型/票型 -->
    <view class="card">
      <view class="section-title">选择{{ type === 1 ? '房型' : '票型' }}</view>
      <view class="item" v-for="it in items" :key="it.id" @click="selectItem(it)">
        <view class="item-info">
          <text class="item-name">{{ it.name }}</text>
          <text class="item-sub">{{ itemSub(it) }}</text>
        </view>
        <view class="item-right">
          <text class="item-price"><text class="price">¥{{ formatPrice(it.price) }}</text></text>
          <view class="check" :class="{ checked: selected && selected.id === it.id }">
            {{ selected && selected.id === it.id ? '✓' : '' }}
          </view>
        </view>
      </view>
      <text v-if="!items.length" class="empty-inline">暂无可预订项</text>
    </view>

    <!-- 数量 -->
    <view class="card" v-if="selected">
      <view class="row">
        <text class="row-label">数量</text>
        <view class="stepper">
          <view class="step-btn" @click="changeQty(-1)">－</view>
          <text class="step-num">{{ quantity }}</text>
          <view class="step-btn" @click="changeQty(1)">＋</view>
        </view>
      </view>
    </view>

    <!-- 日期 -->
    <view class="card" v-if="selected">
      <view v-if="type === 1">
        <picker mode="date" :value="checkInDate" @change="(e) => (checkInDate = e.detail.value)">
          <view class="row"><text class="row-label">入住日期</text><text class="row-value">{{ checkInDate }}</text></view>
        </picker>
        <picker mode="date" :value="checkOutDate" @change="(e) => (checkOutDate = e.detail.value)">
          <view class="row"><text class="row-label">离店日期</text><text class="row-value">{{ checkOutDate }}</text></view>
        </picker>
      </view>
      <view v-else>
        <picker mode="date" :value="visitDate" @change="(e) => (visitDate = e.detail.value)">
          <view class="row"><text class="row-label">游玩日期</text><text class="row-value">{{ visitDate }}</text></view>
        </picker>
      </view>
    </view>

    <!-- 联系人 -->
    <view class="card">
      <view class="section-title">联系人信息</view>
      <view class="row">
        <text class="row-label">姓名</text>
        <input class="row-input" v-model="contactName" placeholder="请输入联系人" />
      </view>
      <view class="row">
        <text class="row-label">手机号</text>
        <input class="row-input" v-model="contactPhone" type="number" placeholder="请输入手机号" />
      </view>
    </view>

    <!-- 备注 -->
    <view class="card">
      <view class="section-title">备注</view>
      <input class="row-input" v-model="remark" placeholder="选填" />
    </view>

    <view class="bottom-space"></view>

    <view class="bottom-bar">
      <view class="total">
        合计：<text class="price">¥{{ totalAmount }}</text>
      </view>
      <view class="submit-btn" @click="submit">提交订单</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { hotelDetail, spotDetail, createOrder, getProfile } from '../../api/index.js'
import { formatPrice } from '../../utils/format.js'
import { refreshOrderBadge } from '../../utils/badge.js'

const type = ref(1)
const id = ref(null)
const targetName = ref('')
const items = ref([])
const selected = ref(null)
const quantity = ref(1)
const checkInDate = ref('')
const checkOutDate = ref('')
const visitDate = ref('')
const contactName = ref('')
const contactPhone = ref('')
const remark = ref('')

const totalAmount = computed(() => {
  if (!selected.value) return '0'
  return formatPrice(Number(selected.value.price) * quantity.value)
})

onLoad((options) => {
  type.value = Number(options.type)
  id.value = Number(options.id)
  checkInDate.value = fmtDate(0)
  checkOutDate.value = fmtDate(1)
  visitDate.value = fmtDate(1)
  loadDetail()
  loadProfile()
})

function fmtDate(offset) {
  const d = new Date()
  d.setDate(d.getDate() + offset)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function loadDetail() {
  try {
    if (type.value === 1) {
      const data = await hotelDetail(id.value)
      targetName.value = data.info.name
      items.value = data.rooms || []
    } else {
      const data = await spotDetail(id.value)
      targetName.value = data.info.name
      items.value = data.tickets || []
    }
  } catch (e) {}
}

function loadProfile() {
  getProfile()
    .then((p) => {
      contactName.value = p.nickname || p.username || ''
      contactPhone.value = p.phone || ''
    })
    .catch(() => {})
}

function itemSub(it) {
  if (type.value === 1) {
    return `${it.bedType || ''} · ${it.area || ''} · 可住${it.maxGuests}人`
  }
  return it.description || `${it.validDays ? it.validDays + '天内有效' : ''}`
}

function selectItem(it) {
  selected.value = it
  quantity.value = 1
}

function changeQty(delta) {
  const next = quantity.value + delta
  if (next < 1) return
  quantity.value = next
}

function submit() {
  if (!selected.value) return uni.showToast({ title: '请选择' + (type.value === 1 ? '房型' : '票型'), icon: 'none' })
  if (!contactName.value) return uni.showToast({ title: '请填写联系人', icon: 'none' })
  if (!contactPhone.value) return uni.showToast({ title: '请填写手机号', icon: 'none' })

  const item = {
    itemType: type.value === 1 ? 1 : 2,
    itemId: selected.value.id,
    quantity: quantity.value
  }
  if (type.value === 1) {
    item.checkInDate = checkInDate.value
    item.checkOutDate = checkOutDate.value
  } else {
    item.visitDate = visitDate.value
  }

  const payload = {
    orderType: type.value,
    targetId: id.value,
    contactName: contactName.value,
    contactPhone: contactPhone.value,
    remark: remark.value,
    items: [item]
  }

  createOrder(payload)
    .then(() => {
      uni.showToast({ title: '下单成功', icon: 'success' })
      refreshOrderBadge()
      setTimeout(() => {
        uni.switchTab({ url: '/pages/order/list' })
      }, 600)
    })
    .catch(() => {})
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
.item-info {
  flex: 1;
  overflow: hidden;
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
.item-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.item-price .price {
  font-size: 30rpx;
}
.check {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #D3CFC2;
  border-radius: 50%;
  margin-left: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 24rpx;
}
.check.checked {
  background: linear-gradient(135deg, #2F4F45, #22352F);
  border-color: transparent;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #EFEBE0;
}
.row:last-child {
  border-bottom: none;
}
.row-label {
  font-size: 28rpx;
  color: #33382F;
}
.row-value {
  font-size: 28rpx;
  color: #7A7A6E;
}
.row-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  margin-left: 20rpx;
}
.stepper {
  display: flex;
  align-items: center;
}
.step-btn {
  width: 56rpx;
  height: 56rpx;
  line-height: 52rpx;
  text-align: center;
  border: 1rpx solid #D3CFC2;
  border-radius: 8rpx;
  color: #5A5F56;
  font-size: 32rpx;
}
.step-num {
  min-width: 80rpx;
  text-align: center;
  font-size: 30rpx;
}
.empty-inline {
  color: #ABA79A;
  font-size: 26rpx;
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
  align-items: center;
  padding: 20rpx 24rpx;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.06);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.total {
  flex: 1;
  font-size: 28rpx;
}
.total .price {
  font-size: 36rpx;
}
.submit-btn {
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 45%, #22352F 100%);
  color: #ffffff;
  padding: 0 56rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 30rpx;
}
</style>
