<template>
  <el-card>
    <el-tabs v-model="activeTab">
      <!-- 轮播图 -->
      <el-tab-pane label="轮播图管理" name="banner">
        <div style="margin-bottom: 12px">
          <el-button type="success" @click="openBannerCreate">新增轮播图</el-button>
        </div>
        <el-table :data="bannerList" v-loading="bannerLoading" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column label="图片" width="120">
            <template #default="{ row }">
              <el-image :src="row.image" style="width: 80px; height: 40px" fit="cover" :preview-src-list="[row.image]" />
            </template>
          </el-table-column>
          <el-table-column label="跳转类型" width="100">
            <template #default="{ row }">{{ linkTypeText(row.linkType) }}</template>
          </el-table-column>
          <el-table-column prop="linkId" label="跳转ID" width="90" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ contentStatus[row.status] || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="openBannerEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleBannerDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 公告 -->
      <el-tab-pane label="公告管理" name="notice">
        <div style="margin-bottom: 12px">
          <el-button type="success" @click="openNoticeCreate">新增公告</el-button>
        </div>
        <el-table :data="noticeList" v-loading="noticeLoading" border>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="content" label="内容" show-overflow-tooltip />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ contentStatus[row.status] || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="170" />
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="openNoticeEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleNoticeDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 轮播图对话框 -->
    <el-dialog v-model="bannerDialog" :title="bannerForm.id ? '编辑轮播图' : '新增轮播图'" width="480px">
      <el-form :model="bannerForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="bannerForm.title" placeholder="标题" />
        </el-form-item>
        <el-form-item label="图片URL" required>
          <el-input v-model="bannerForm.image" placeholder="图片 URL" />
        </el-form-item>
        <el-form-item label="跳转类型">
          <el-select v-model="bannerForm.linkType" style="width: 100%">
            <el-option label="无跳转" :value="0" />
            <el-option label="酒店" :value="1" />
            <el-option label="景点" :value="2" />
            <el-option label="餐厅" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="跳转ID">
          <el-input-number v-model="bannerForm.linkId" :min="0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="bannerForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="bannerForm.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBanner">保存</el-button>
      </template>
    </el-dialog>

    <!-- 公告对话框 -->
    <el-dialog v-model="noticeDialog" :title="noticeForm.id ? '编辑公告' : '新增公告'" width="520px">
      <el-form :model="noticeForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="noticeForm.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="noticeForm.content" type="textarea" :rows="4" placeholder="公告内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="noticeForm.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeDialog = false">取消</el-button>
        <el-button type="primary" @click="submitNotice">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getBannerList, createBanner, updateBanner, deleteBanner,
  getNoticeList, createNotice, updateNotice, deleteNotice
} from '@/api/content'
import { contentStatus } from '@/utils/dict'

const activeTab = ref('banner')

// 轮播图
const bannerList = ref([])
const bannerLoading = ref(false)
const bannerDialog = ref(false)
const bannerForm = reactive({ id: null, title: '', image: '', linkType: 0, linkId: null, sort: 0, status: 1 })

// 公告
const noticeList = ref([])
const noticeLoading = ref(false)
const noticeDialog = ref(false)
const noticeForm = reactive({ id: null, title: '', content: '', status: 1 })

const linkTypeMap = { 0: '无', 1: '酒店', 2: '景点', 3: '餐厅' }
function linkTypeText(type) {
  return linkTypeMap[type] || '-'
}

async function fetchBanners() {
  bannerLoading.value = true
  try {
    bannerList.value = await getBannerList()
  } finally {
    bannerLoading.value = false
  }
}

async function fetchNotices() {
  noticeLoading.value = true
  try {
    noticeList.value = await getNoticeList()
  } finally {
    noticeLoading.value = false
  }
}

function openBannerCreate() {
  Object.assign(bannerForm, { id: null, title: '', image: '', linkType: 0, linkId: null, sort: 0, status: 1 })
  bannerDialog.value = true
}

function openBannerEdit(row) {
  Object.assign(bannerForm, row)
  bannerDialog.value = true
}

async function submitBanner() {
  if (!bannerForm.image) {
    ElMessage.warning('请输入图片 URL')
    return
  }
  if (bannerForm.id) {
    await updateBanner(bannerForm)
  } else {
    await createBanner(bannerForm)
  }
  ElMessage.success('保存成功')
  bannerDialog.value = false
  fetchBanners()
}

async function handleBannerDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该轮播图？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteBanner(row.id)
  ElMessage.success('已删除')
  fetchBanners()
}

function openNoticeCreate() {
  Object.assign(noticeForm, { id: null, title: '', content: '', status: 1 })
  noticeDialog.value = true
}

function openNoticeEdit(row) {
  Object.assign(noticeForm, row)
  noticeDialog.value = true
}

async function submitNotice() {
  if (!noticeForm.title || !noticeForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  if (noticeForm.id) {
    await updateNotice(noticeForm)
  } else {
    await createNotice(noticeForm)
  }
  ElMessage.success('保存成功')
  noticeDialog.value = false
  fetchNotices()
}

async function handleNoticeDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该公告？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteNotice(row.id)
  ElMessage.success('已删除')
  fetchNotices()
}

onMounted(() => {
  fetchBanners()
  fetchNotices()
})
</script>
