<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="nav-inner">
        <div class="logo" @click="$router.push('/home')">
          <div class="logo-mark">
            <el-icon><Shop /></el-icon>
          </div>
          <span class="logo-text">旅游平台 · 商家中心</span>
        </div>
        <div class="menus">
          <div
            v-for="item in menus"
            :key="item.path"
            class="menu-item"
            :class="{ active: activeMenu === item.path }"
            @click="$router.push(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </div>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <span class="avatar">{{ (merchantInfo?.name || merchantInfo?.username || '商')[0] }}</span>
            <span class="username">{{ merchantInfo?.name || merchantInfo?.username || '商家' }}</span>
            <el-icon class="arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { path: '/home', title: '我的店铺', icon: 'Shop' },
  { path: '/order', title: '订单管理', icon: 'Tickets' },
  { path: '/comment', title: '评价管理', icon: 'ChatDotRound' }
]

const activeMenu = computed(() => route.path)
const merchantInfo = computed(() => userStore.merchantInfo)

async function handleCommand(command) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    } catch {
      return
    }
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--art-bg-color);
}
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--art-main-bg-color);
  border-bottom: 1px solid var(--art-card-border);
}
.nav-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  margin-right: 40px;
  white-space: nowrap;
}
.logo-mark {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  background: var(--el-color-primary);
}
.logo-text {
  font-size: 17px;
  font-weight: 600;
  color: var(--art-text-gray-800);
  letter-spacing: 0.5px;
}
.menus {
  display: flex;
  gap: 8px;
  flex: 1;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--art-text-gray-700);
  font-size: 14px;
  transition: background-color 0.2s;
}
.menu-item:hover {
  background: var(--art-gray-100);
}
.menu-item.active {
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  font-weight: 600;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background-color 0.2s;
  outline: none;
  white-space: nowrap;
}
.user-info:hover {
  background: var(--art-gray-100);
}
.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--el-color-primary);
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.username {
  color: var(--art-text-gray-800);
  font-size: 14px;
  font-weight: 500;
}
.arrow {
  color: var(--art-gray-500);
  font-size: 12px;
}
.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
</style>
