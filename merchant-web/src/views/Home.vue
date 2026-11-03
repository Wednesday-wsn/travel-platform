<template>
  <div>
    <!-- 店铺档案 -->
    <el-card class="profile-card" shadow="never">
      <div class="profile">
        <div class="profile-logo">{{ (profile?.name || '商')[0] }}</div>
        <div class="profile-main">
          <div class="profile-name">
            {{ profile?.name || '未命名商家' }}
            <el-tag :type="merchantTag(profile?.status)" size="small">{{ merchantStatus[profile?.status] || '-' }}</el-tag>
          </div>
          <div class="profile-desc">
            {{ merchantType[profile?.type] || '-' }} · {{ profile?.contactName || '-' }} · {{ profile?.contactPhone || '-' }}
          </div>
          <div class="profile-desc">{{ profile?.address || '-' }}</div>
        </div>
      </div>
    </el-card>

    <!-- 我的酒店 -->
    <div class="section">
      <div class="section-header">
        <h3>我的酒店</h3>
        <el-button type="success" size="small" @click="openHotelEdit()">新增酒店</el-button>
      </div>
      <el-empty v-if="!hotels.length" description="暂无酒店，点击右上角新增" />
      <div v-else class="shop-grid">
        <el-card v-for="h in hotels" :key="h.id" class="shop-card" shadow="hover">
          <div class="cover">
            <img v-if="h.coverImage" :src="h.coverImage" alt="封面" />
            <div v-else class="cover-placeholder">酒店</div>
            <el-tag class="status-tag" :type="tagType(h.status)" size="small">
              {{ resourceStatus[h.status] || '-' }}
            </el-tag>
          </div>
          <div class="info">
            <div class="name">{{ h.name }}</div>
            <div class="meta">
              <span>{{ h.city || '-' }}</span>
              <span>{{ h.starLevel }}星</span>
              <span>评分 {{ h.rating ?? '-' }}</span>
            </div>
          </div>
          <div class="actions">
            <el-button size="small" @click="openHotelEdit(h)">编辑</el-button>
            <el-button size="small" type="primary" @click="openRooms(h)">房型</el-button>
            <el-button v-if="h.status === 1" size="small" type="warning" @click="offHotel(h)">下架</el-button>
            <el-button v-if="h.status === 2 || h.status === 3" size="small" type="success" @click="shelfHotel(h)">申请上架</el-button>
            <el-button size="small" type="danger" @click="delHotel(h)">删除</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 我的餐厅 -->
    <div class="section">
      <div class="section-header">
        <h3>我的餐厅</h3>
        <el-button type="success" size="small" @click="openRestaurantEdit()">新增餐厅</el-button>
      </div>
      <el-empty v-if="!restaurants.length" description="暂无餐厅，点击右上角新增" />
      <div v-else class="shop-grid">
        <el-card v-for="r in restaurants" :key="r.id" class="shop-card" shadow="hover">
          <div class="cover">
            <img v-if="r.coverImage" :src="r.coverImage" alt="封面" />
            <div v-else class="cover-placeholder">餐厅</div>
            <el-tag class="status-tag" :type="tagType(r.status)" size="small">
              {{ resourceStatus[r.status] || '-' }}
            </el-tag>
          </div>
          <div class="info">
            <div class="name">{{ r.name }}</div>
            <div class="meta">
              <span>{{ r.city || '-' }}</span>
              <span>人均 ¥{{ r.avgPrice ?? '-' }}</span>
              <span>评分 {{ r.rating ?? '-' }}</span>
            </div>
          </div>
          <div class="actions">
            <el-button size="small" @click="openRestaurantEdit(r)">编辑</el-button>
            <el-button size="small" type="primary" @click="openDishes(r)">菜品</el-button>
            <el-button v-if="r.status === 1" size="small" type="warning" @click="offRestaurant(r)">下架</el-button>
            <el-button v-if="r.status === 2 || r.status === 3" size="small" type="success" @click="shelfRestaurant(r)">申请上架</el-button>
            <el-button size="small" type="danger" @click="delRestaurant(r)">删除</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 弹窗组件 -->
    <HotelEdit v-model="hotelEditVisible" :hotel="editingHotel" @saved="loadAll" />
    <RestaurantEdit v-model="restaurantEditVisible" :restaurant="editingRestaurant" @saved="loadAll" />
    <RoomManage v-model="roomVisible" :hotel="currentHotel" />
    <DishManage v-model="dishVisible" :restaurant="currentRestaurant" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantProfile } from '@/api/merchant'
import { getHotelPage, shelfHotel as shelfHotelApi, offHotel as offHotelApi, deleteHotel } from '@/api/hotel'
import { getRestaurantPage, shelfRestaurant as shelfRestaurantApi, offRestaurant as offRestaurantApi, deleteRestaurant } from '@/api/restaurant'
import HotelEdit from '@/components/HotelEdit.vue'
import RestaurantEdit from '@/components/RestaurantEdit.vue'
import RoomManage from '@/components/RoomManage.vue'
import DishManage from '@/components/DishManage.vue'
import { resourceStatus, merchantType, merchantStatus, tagType } from '@/utils/dict'

const profile = ref(null)
const hotels = ref([])
const restaurants = ref([])

const hotelEditVisible = ref(false)
const restaurantEditVisible = ref(false)
const editingHotel = ref(null)
const editingRestaurant = ref(null)
const roomVisible = ref(false)
const dishVisible = ref(false)
const currentHotel = ref(null)
const currentRestaurant = ref(null)

function merchantTag(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  if (status === 2) return 'danger'
  return 'info'
}

async function loadProfile() {
  profile.value = await getMerchantProfile()
}

async function loadAll() {
  const [h, r] = await Promise.all([
    getHotelPage({ current: 1, size: 100 }),
    getRestaurantPage({ current: 1, size: 100 })
  ])
  hotels.value = h.records
  restaurants.value = r.records
}

function openHotelEdit(row) {
  editingHotel.value = row || null
  hotelEditVisible.value = true
}

function openRestaurantEdit(row) {
  editingRestaurant.value = row || null
  restaurantEditVisible.value = true
}

function openRooms(row) {
  currentHotel.value = row
  roomVisible.value = true
}

function openDishes(row) {
  currentRestaurant.value = row
  dishVisible.value = true
}

async function offHotel(row) {
  if (!(await confirm('确认下架该酒店？下架后游客端将不可见。'))) return
  await offHotelApi(row.id)
  ElMessage.success('已下架')
  loadAll()
}

async function shelfHotel(row) {
  if (!(await confirm('确认申请上架？提交后将由管理员审核。'))) return
  await shelfHotelApi(row.id)
  ElMessage.success('已提交上架申请')
  loadAll()
}

async function delHotel(row) {
  if (!(await confirm('确认删除该酒店？删除后不可恢复。'))) return
  await deleteHotel(row.id)
  ElMessage.success('已删除')
  loadAll()
}

async function offRestaurant(row) {
  if (!(await confirm('确认下架该餐厅？下架后游客端将不可见。'))) return
  await offRestaurantApi(row.id)
  ElMessage.success('已下架')
  loadAll()
}

async function shelfRestaurant(row) {
  if (!(await confirm('确认申请上架？提交后将由管理员审核。'))) return
  await shelfRestaurantApi(row.id)
  ElMessage.success('已提交上架申请')
  loadAll()
}

async function delRestaurant(row) {
  if (!(await confirm('确认删除该餐厅？删除后不可恢复。'))) return
  await deleteRestaurant(row.id)
  ElMessage.success('已删除')
  loadAll()
}

async function confirm(msg) {
  try {
    await ElMessageBox.confirm(msg, '提示', { type: 'warning' })
    return true
  } catch {
    return false
  }
}

onMounted(() => {
  loadProfile()
  loadAll()
})
</script>

<style scoped>
.profile-card {
  margin-bottom: 24px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(120deg, #16a34a 0%, #0e7a3d 100%);
}
.profile-card :deep(.el-card__body) {
  padding: 24px;
}
.profile {
  display: flex;
  gap: 16px;
  align-items: center;
}
.profile-logo {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.profile-name {
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  color: #fff;
}
.profile-desc {
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
  line-height: 1.6;
}
.section {
  margin-top: 8px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.section-header h3 {
  margin: 0;
  font-size: 16px;
  padding-left: 12px;
  position: relative;
}
.section-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: #16a34a;
}
.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}
.cover {
  position: relative;
  height: 140px;
  border-radius: 6px;
  overflow: hidden;
  background: var(--art-gray-200);
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--art-gray-400);
  font-size: 20px;
}
.status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}
.info {
  margin: 12px 0;
}
.name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}
.meta {
  display: flex;
  gap: 10px;
  color: var(--art-gray-500);
  font-size: 12px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.actions .el-button {
  margin-left: 0;
}
</style>
