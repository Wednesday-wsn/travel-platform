<template>
  <view class="page">
    <view class="form card">
      <view class="row">
        <text class="row-label">昵称</text>
        <input class="row-input" v-model="form.nickname" placeholder="请输入昵称" />
      </view>
      <view class="row">
        <text class="row-label">手机号</text>
        <input class="row-input" v-model="form.phone" type="number" placeholder="请输入手机号" />
      </view>
      <view class="row">
        <text class="row-label">邮箱</text>
        <input class="row-input" v-model="form.email" placeholder="请输入邮箱" />
      </view>
      <picker :range="genders" range-key="label" :value="genderIndex" @change="onGender">
        <view class="row">
          <text class="row-label">性别</text>
          <text class="row-value">{{ genders[genderIndex].label }} ›</text>
        </view>
      </picker>
      <view class="row" @click="chooseAvatar">
        <text class="row-label">头像</text>
        <view class="avatar-wrap">
          <image v-if="form.avatar" class="avatar-preview" :src="resolveUrl(form.avatar)" mode="aspectFill" />
          <text v-else-if="uploading" class="avatar-tip">上传中…</text>
          <text v-else class="avatar-tip">点击选择图片 ›</text>
        </view>
      </view>
    </view>

    <button class="btn-primary submit" :loading="loading" @click="submit">保 存</button>
  </view>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getProfile, updateProfile } from '../../api/index.js'
import { BASE_URL, resolveUrl } from '../../config/index.js'
import store from '../../store/index.js'

const genders = [
  { value: 0, label: '未知' },
  { value: 1, label: '男' },
  { value: 2, label: '女' }
]

const form = reactive({ nickname: '', phone: '', email: '', gender: 0, avatar: '' })
const loading = ref(false)
const uploading = ref(false)

const genderIndex = computed(() => {
  const idx = genders.findIndex((g) => g.value === form.gender)
  return idx >= 0 ? idx : 0
})

onLoad(() => {
  getProfile()
    .then((p) => {
      form.nickname = p.nickname || ''
      form.phone = p.phone || ''
      form.email = p.email || ''
      form.gender = p.gender || 0
      form.avatar = p.avatar || ''
    })
    .catch(() => {})
})

function onGender(e) {
  form.gender = genders[Number(e.detail.value)].value
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      uploadAvatar(res.tempFilePaths[0])
    }
  })
}

function uploadAvatar(filePath) {
  uploading.value = true
  uni.uploadFile({
    url: BASE_URL + '/api/common/upload',
    filePath,
    name: 'file',
    header: { Authorization: 'Bearer ' + store.getToken() },
    success: (res) => {
      let body = {}
      try {
        body = JSON.parse(res.data)
      } catch (e) {}
      if (body.code === 200) {
        form.avatar = body.data
        uni.showToast({ title: '头像已更新，保存后生效', icon: 'none' })
      } else {
        uni.showToast({ title: body.message || '上传失败', icon: 'none' })
      }
    },
    fail: () => {
      uni.showToast({ title: '上传失败，请重试', icon: 'none' })
    },
    complete: () => {
      uploading.value = false
    }
  })
}

function submit() {
  loading.value = true
  updateProfile({ ...form })
    .then(() => {
      const info = store.getUserInfo() || {}
      info.avatar = form.avatar
      info.name = form.nickname
      store.setUserInfo(info)
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 600)
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}
</script>

<style lang="scss" scoped>
.form {
  padding: 10rpx 32rpx;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #EFEBE0;
}
.row:last-child {
  border-bottom: none;
}
.row-label {
  font-size: 28rpx;
  color: #33382F;
  flex-shrink: 0;
}
.row-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  margin-left: 20rpx;
}
.row-value {
  font-size: 28rpx;
  color: #7A7A6E;
}
.avatar-wrap {
  display: flex;
  align-items: center;
}
.avatar-preview {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #EFEBE0;
}
.avatar-tip {
  font-size: 28rpx;
  color: #7A7A6E;
}
.submit {
  margin-top: 40rpx;
  border: none;
}
</style>
