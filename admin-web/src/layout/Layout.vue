<template>
  <div class="layout">
    <!-- 左侧菜单（浅色，Art Design Pro DESIGN 主题） -->
    <aside class="menu-left">
      <div class="logo" @click="$router.push('/dashboard')">
        <div class="logo-mark">
          <el-icon><Location /></el-icon>
        </div>
        <span class="logo-text">旅游服务平台</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          router
          class="el-menu-design"
        >
          <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
            <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </aside>

    <!-- 右侧主区域 -->
    <div class="main-area">
      <header class="top-bar">
        <div class="left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="user">
            <span class="avatar">{{ (adminInfo?.name || adminInfo?.username || '管')[0] }}</span>
            <span class="username">{{ adminInfo?.name || adminInfo?.username || '管理员' }}</span>
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
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
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
  { path: '/dashboard', title: '数据统计', icon: 'DataLine' },
  { path: '/merchant', title: '商家管理', icon: 'Shop' },
  { path: '/user', title: '用户管理', icon: 'User' },
  { path: '/hotel', title: '酒店管理', icon: 'OfficeBuilding' },
  { path: '/restaurant', title: '餐厅管理', icon: 'Food' },
  { path: '/spot', title: '景点管理', icon: 'Picture' },
  { path: '/comment', title: '评价管理', icon: 'ChatDotRound' },
  { path: '/category', title: '分类管理', icon: 'Menu' },
  { path: '/content', title: '内容管理', icon: 'Document' },
  { path: '/audit', title: '审核日志', icon: 'List' }
]

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')
const adminInfo = computed(() => userStore.adminInfo)

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
  display: flex;
  height: 100vh;
  background: var(--art-bg-color);
}

/* ---- 左侧菜单（浅色） ---- */
.menu-left {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--art-main-bg-color);
  border-right: 1px solid var(--art-card-border);
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 22px;
  cursor: pointer;
  white-space: nowrap;
}
.logo-mark {
  width: 32px;
  height: 32px;
  min-width: 32px;
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
.menu-scroll {
  flex: 1;
  height: 0;
}

/* 菜单项（Art Design Pro el-menu-design 原样式） */
.el-menu-design {
  border-right: none;
  padding: 8px 0;
}
.el-menu-design :deep(.el-menu-item) {
  width: calc(100% - 16px);
  height: 52px;
  line-height: 52px;
  margin: 0 auto 5px;
  border-radius: 6px;
  color: var(--art-text-gray-700);
  transition: background-color 0s;
}
.el-menu-design :deep(.el-menu-item .el-icon) {
  margin-right: 10px;
  color: #6b6b6b;
}
.el-menu-design :deep(.el-menu-item:hover) {
  background-color: var(--art-gray-100);
}
.el-menu-design :deep(.el-menu-item.is-active) {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}
.el-menu-design :deep(.el-menu-item.is-active .el-icon) {
  color: var(--el-color-primary);
}

/* ---- 右侧主区域 ---- */
.main-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.top-bar {
  height: 60px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: var(--art-main-bg-color);
  border-bottom: 1px solid var(--art-card-border);
}
.breadcrumb :deep(.el-breadcrumb__inner) {
  color: var(--art-gray-500);
  font-weight: 400;
}
.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background-color 0.2s;
  outline: none;
}
.user:hover {
  background-color: var(--art-gray-100);
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
  flex: 1;
  overflow: auto;
  padding: 20px;
}
</style>
