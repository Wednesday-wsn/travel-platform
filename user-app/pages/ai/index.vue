<template>
  <view class="page">
    <!-- 聊天区 -->
    <scroll-view scroll-y class="chat-area" :scroll-top="scrollTop" scroll-with-animation>
      <!-- 欢迎卡片（无对话时显示） -->
      <view v-if="!messages.length" class="welcome">
        <view class="welcome-icon">
          <text class="tn-icon-ai-fill"></text>
        </view>
        <text class="welcome-title">你好，我是 AI 旅行助手</text>
        <text class="welcome-sub">可以帮你推荐酒店、景点和美食，也能一键规划专属行程</text>
        <view class="welcome-tags">
          <view class="w-tag"><text class="tn-icon-home-capsule-fill"></text>酒店</view>
          <view class="w-tag"><text class="tn-icon-location-fill"></text>景点</view>
          <view class="w-tag"><text class="tn-icon-food-fill"></text>美食</view>
          <view class="w-tag"><text class="tn-icon-route-fill"></text>行程</view>
        </view>
      </view>

      <!-- 消息气泡 -->
      <view class="msg" v-for="(m, idx) in messages" :key="idx" :class="m.role">
        <view v-if="m.role === 'bot'" class="avatar sm"><text class="tn-icon-ai-fill"></text></view>
        <view class="bubble" :class="{ typing: m.role === 'bot' && !m.content }">
          <text class="bubble-text">{{ m.role === 'bot' && !m.content ? '正在思考…' : m.content }}</text>
          <view v-if="m.role === 'bot' && m.items && m.items.length" class="rec-cards">
            <view
              v-for="it in m.items"
              :key="it.id + '-' + it.type"
              class="rec-card"
              @click="goDetail(it)"
            >
              <view class="rec-info">
                <text class="rec-name">{{ it.name }}</text>
                <text class="rec-sub">{{ it.sub }}</text>
              </view>
              <text class="rec-arrow">查看详情 ›</text>
            </view>
          </view>
        </view>
        <view v-if="m.role === 'user'" class="avatar sm user"><text class="tn-icon-my-fill"></text></view>
      </view>
    </scroll-view>

    <!-- 底部输入面板 -->
    <view class="dock">
      <view class="dock-actions">
        <view class="act-btn" @click="showPlan = true">
          <text class="act-icon tn-icon-route-fill"></text>
          <text>规划行程</text>
        </view>
        <view class="act-btn" @click="openHistory">
          <text class="act-icon tn-icon-history"></text>
          <text>历史记录</text>
        </view>
      </view>
      <view class="input-row">
        <input
          class="input"
          v-model="input"
          placeholder="问我旅行相关的问题…"
          confirm-type="send"
          @confirm="send()"
        />
        <view class="send-btn" :class="{ disabled: loading }" @click="send()">
          <text class="tn-icon-send"></text>
        </view>
      </view>
    </view>

    <!-- 规划行程弹层 -->
    <view class="mask" v-if="showPlan" @click="showPlan = false">
      <view class="sheet" @click.stop>
        <view class="sheet-head">
          <text class="sheet-title">规划你的旅程</text>
          <text class="sheet-close tn-icon-close" @click="showPlan = false"></text>
        </view>
        <view class="plan-row">
          <text class="plan-icon tn-icon-location-fill"></text>
          <input class="plan-input" v-model="planDest" placeholder="目的地，如：苏州" />
        </view>
        <view class="plan-row">
          <text class="plan-icon tn-icon-calendar-fill"></text>
          <picker class="plan-picker-wrap" mode="selector" :range="daysRange" :value="planDays" @change="onDaysChange">
            <view class="plan-picker">{{ daysRange[planDays] }} <text class="plan-arrow">▾</text></view>
          </picker>
        </view>
        <view class="plan-row">
          <text class="plan-icon tn-icon-money-fill"></text>
          <input class="plan-input" v-model="planBudget" type="number" placeholder="预算（元），如：2000" />
        </view>
        <view class="plan-btn" @click="generatePlan">生成行程</view>
      </view>
    </view>

    <!-- 历史记录弹层 -->
    <view class="mask" v-if="showHistory" @click="showHistory = false">
      <view class="sheet" @click.stop>
        <view class="sheet-head">
          <text class="sheet-title">历史记录</text>
          <text class="sheet-close tn-icon-close" @click="showHistory = false"></text>
        </view>
        <scroll-view scroll-y class="sheet-body">
          <view v-if="!sessions.length" class="empty">暂无历史会话</view>
          <view v-for="s in sessions" :key="s.id" class="session">
            <view class="session-main" @click="loadSession(s)">
              <text class="session-title ellipsis">{{ s.title }}</text>
              <text class="session-time">{{ s.time }}</text>
            </view>
            <text class="session-del tn-icon-delete-fill" @click="deleteSession(s.id)"></text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiChat } from '../../api/index.js'
import { aiChatStream } from '../../utils/aiStream.js'
import store from '../../store/index.js'

const SESSION_KEY = 'ai_sessions'
const daysRange = Array.from({ length: 15 }, (_, i) => (i + 1) + ' 天')

const messages = ref([])
const input = ref('')
const loading = ref(false)
const scrollTop = ref(0)

// 行程规划表单
const showPlan = ref(false)
const planDest = ref('')
const planDays = ref(2)
const planBudget = ref('')

// 历史记录弹层
const showHistory = ref(false)
const sessions = ref([])
let sessionId = Date.now().toString()

function scrollBottom() {
  nextTick(() => {
    scrollTop.value += 5000
  })
}

async function send(textArg) {
  const text = (textArg !== undefined ? textArg : input.value).trim()
  if (!text || loading.value) return

  if (!store.isLogin()) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 500)
    return
  }

  const history = messages.value
    .slice(-6)
    .map((m) => ({ role: m.role === 'bot' ? 'assistant' : 'user', content: m.content }))

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  showPlan.value = false
  scrollBottom()

  const payload = { message: text, history }

  try {
    // #ifdef H5
    await doStream(payload)
    // #endif
    // #ifndef H5
    await doNormal(payload)
    // #endif
  } catch (e) {
    // doStream 内部已回退非流式
  } finally {
    loading.value = false
    scrollBottom()
    saveSession()
  }
}

// 流式：逐字追加到 bot 气泡
async function doStream(payload) {
  messages.value.push({ role: 'bot', content: '', items: [] })
  const idx = messages.value.length - 1
  try {
    await aiChatStream(payload, {
      onToken: (t) => {
        messages.value[idx].content += stripMd(t)
        scrollBottom()
      },
      onDone: (items) => {
        messages.value[idx].items = items || []
      }
    })
    if (!messages.value[idx].content) {
      messages.value[idx].content = '抱歉，我没有理解你的问题。'
    }
  } catch (e) {
    // 流式失败 → 移除占位气泡，回退非流式
    messages.value.splice(idx, 1)
    await doNormal(payload)
  }
}

// 非流式（小程序端 / 兜底）
async function doNormal(payload) {
  messages.value.push({ role: 'bot', content: '', items: [] })
  const idx = messages.value.length - 1
  try {
    const data = await aiChat({ message: payload.message, history: payload.history })
    messages.value[idx].content = stripMd(data.reply || '抱歉，我没有理解你的问题。')
    messages.value[idx].items = (data && data.items) || []
  } catch (e) {
    messages.value[idx].content = '抱歉，服务暂时不可用，请稍后再试。'
  }
}

function goDetail(it) {
  const urlMap = { 1: '/pages/hotel/detail', 2: '/pages/spot/detail', 3: '/pages/restaurant/detail' }
  const url = (urlMap[it.type] || '/pages/index/index') + '?id=' + it.id
  uni.navigateTo({ url })
}

function onDaysChange(e) {
  planDays.value = Number(e.detail.value)
}

function generatePlan() {
  const dest = planDest.value.trim()
  if (!dest) {
    uni.showToast({ title: '请填写目的地', icon: 'none' })
    return
  }
  const days = planDays.value + 1
  const budget = planBudget.value.trim()
  let msg = '请帮我规划一次 ' + dest + ' ' + days + ' 天游'
  if (budget) msg += '，预算约 ' + budget + ' 元'
  msg += '。请按天给出行程安排，并推荐当地的景点、酒店和美食。'
  send(msg)
}

// ---------- 历史记录 ----------
function loadSessions() {
  sessions.value = uni.getStorageSync(SESSION_KEY) || []
}

function saveSession() {
  const msgs = messages.value
  const firstUser = msgs.find((m) => m.role === 'user')
  if (!firstUser) return
  const list = uni.getStorageSync(SESSION_KEY) || []
  const idx = list.findIndex((s) => s.id === sessionId)
  const entry = {
    id: sessionId,
    title: String(firstUser.content || '对话').slice(0, 20),
    time: formatTime(new Date()),
    messages: JSON.parse(JSON.stringify(msgs))
  }
  if (idx >= 0) list[idx] = entry
  else list.unshift(entry)
  uni.setStorageSync(SESSION_KEY, list)
  sessions.value = list
}

function openHistory() {
  loadSessions()
  showHistory.value = true
}

function loadSession(s) {
  messages.value = JSON.parse(JSON.stringify(s.messages))
  sessionId = s.id
  showHistory.value = false
  scrollBottom()
}

function deleteSession(id) {
  const list = (uni.getStorageSync(SESSION_KEY) || []).filter((s) => s.id !== id)
  uni.setStorageSync(SESSION_KEY, list)
  sessions.value = list
}

function formatTime(d) {
  const pad = (n) => (n < 10 ? '0' + n : '' + n)
  return (d.getMonth() + 1) + '/' + d.getDate() + ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes())
}

// 过滤 AI 回复里的 Markdown 符号（**、#、##、` 等）
function stripMd(s) {
  return String(s).replace(/[*#`~]/g, '')
}
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F4F2EB;
}

/* ---------- 聊天区 ---------- */
.chat-area {
  flex: 1;
  padding: 24rpx 24rpx 16rpx;
  box-sizing: border-box;
}

/* ---------- 欢迎卡片 ---------- */
.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx 60rpx;
  text-align: center;
}
.welcome-icon {
  width: 140rpx;
  height: 140rpx;
  border-radius: 40rpx;
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 55%, #22352F 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 72rpx;
  box-shadow: 0 16rpx 40rpx rgba(47, 79, 69, 0.30);
  margin-bottom: 36rpx;
}
.welcome-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #2F4F45;
}
.welcome-sub {
  font-size: 26rpx;
  color: #7A7A6E;
  margin-top: 16rpx;
  line-height: 1.6;
  padding: 0 40rpx;
}
.welcome-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 44rpx;
}
.w-tag {
  display: inline-flex;
  align-items: center;
  padding: 0 26rpx;
  height: 60rpx;
  background: #ffffff;
  border: 1rpx solid #E3E0D4;
  border-radius: 30rpx;
  font-size: 24rpx;
  color: #555A4F;
  margin: 0 8rpx 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(31, 41, 55, 0.04);
}
.w-tag text {
  font-size: 28rpx;
  color: #2F4F45;
  margin-right: 8rpx;
}

/* ---------- 消息气泡 ---------- */
.msg {
  display: flex;
  margin-bottom: 28rpx;
  align-items: flex-start;
}
.msg.user {
  flex-direction: row-reverse;
}
.avatar.sm {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 36rpx;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(47, 79, 69, 0.18);
}
.avatar.sm.user {
  background-image: none;
  background: #22352F;
}
.bubble {
  max-width: 70%;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  color: #33382F;
  line-height: 1.6;
  word-break: break-all;
  box-shadow: 0 4rpx 16rpx rgba(31, 41, 55, 0.06);
}
.msg.bot .bubble {
  margin-left: 16rpx;
  border-top-left-radius: 6rpx;
}
.msg.user .bubble {
  margin-right: 16rpx;
  background-image: linear-gradient(135deg, #3A6B5B 0%, #2F4F45 100%);
  color: #ffffff;
  border-top-right-radius: 6rpx;
}
.bubble.typing {
  color: #ABA79A;
}
.bubble-text {
  white-space: pre-wrap;
}

/* ---------- 推荐卡片 ---------- */
.rec-cards {
  margin-top: 16rpx;
}
.rec-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #F9F6EF;
  border: 1rpx solid #E3E0D4;
  border-radius: 14rpx;
  padding: 16rpx 20rpx;
  margin-bottom: 12rpx;
}
.rec-card:last-child {
  margin-bottom: 0;
}
.rec-info {
  flex: 1;
  overflow: hidden;
  margin-right: 10rpx;
}
.rec-name {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #33382F;
}
.rec-sub {
  display: block;
  font-size: 22rpx;
  color: #7A7A6E;
  margin-top: 4rpx;
}
.rec-arrow {
  font-size: 22rpx;
  color: #2F4F45;
  flex-shrink: 0;
}

/* ---------- 底部输入面板 ---------- */
.dock {
  background: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  box-shadow: 0 -8rpx 30rpx rgba(31, 41, 55, 0.08);
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}
.dock-actions {
  display: flex;
  margin-bottom: 16rpx;
}
.act-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  background: #F4F2EB;
  border-radius: 36rpx;
  font-size: 26rpx;
  color: #33382F;
  margin-right: 20rpx;
}
.act-btn:last-child {
  margin-right: 0;
}
.act-icon {
  font-size: 30rpx;
  color: #2F4F45;
  margin-right: 10rpx;
}
.input-row {
  display: flex;
  align-items: center;
}
.input {
  flex: 1;
  height: 76rpx;
  background: #F4F2EB;
  border-radius: 38rpx;
  padding: 0 30rpx;
  font-size: 26rpx;
}
.send-btn {
  width: 76rpx;
  height: 76rpx;
  margin-left: 16rpx;
  border-radius: 50%;
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 34rpx;
  box-shadow: 0 6rpx 16rpx rgba(47, 79, 69, 0.28);
  flex-shrink: 0;
}
.send-btn.disabled {
  opacity: 0.5;
}

/* ---------- 弹层 ---------- */
.mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}
.sheet {
  width: 100%;
  background: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx 32rpx calc(40rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28rpx;
}
.sheet-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #33382F;
}
.sheet-close {
  font-size: 36rpx;
  color: #ABA79A;
}
.sheet-body {
  max-height: 50vh;
}
.empty {
  text-align: center;
  color: #ABA79A;
  font-size: 26rpx;
  padding: 40rpx 0;
}

/* ---------- 规划行程表单 ---------- */
.plan-row {
  display: flex;
  align-items: center;
  height: 88rpx;
  background: #F4F2EB;
  border-radius: 16rpx;
  padding: 0 24rpx;
  margin-bottom: 20rpx;
}
.plan-icon {
  font-size: 32rpx;
  color: #2F4F45;
  margin-right: 16rpx;
  flex-shrink: 0;
}
.plan-input {
  flex: 1;
  height: 100%;
  font-size: 27rpx;
}
.plan-picker-wrap {
  flex: 1;
}
.plan-picker {
  height: 88rpx;
  line-height: 88rpx;
  font-size: 27rpx;
  color: #33382F;
}
.plan-arrow {
  float: right;
  color: #ABA79A;
}
.plan-btn {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background-image: linear-gradient(135deg, #4A6B5F 0%, #2F4F45 100%);
  color: #ffffff;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 600;
  margin-top: 8rpx;
  box-shadow: 0 8rpx 20rpx rgba(47, 79, 69, 0.25);
}

/* ---------- 历史记录列表 ---------- */
.session {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #F0EDE4;
}
.session-main {
  flex: 1;
  overflow: hidden;
}
.session-title {
  display: block;
  font-size: 28rpx;
  color: #33382F;
}
.session-time {
  display: block;
  font-size: 22rpx;
  color: #ABA79A;
  margin-top: 8rpx;
}
.session-del {
  font-size: 36rpx;
  color: #C0BDB2;
  padding: 0 10rpx;
}
.ellipsis {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
