<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(text, value) in resourceStatus" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="景点名称">
          <el-input v-model="query.name" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openCreate">新增景点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="景点名称" show-overflow-tooltip />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="openTime" label="开放时间" width="140" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ resourceStatus[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="openTicket(row)">票型管理</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" @click="handleOff(row)">下架</el-button>
            <el-button v-if="row.status === 3" size="small" type="primary" @click="handleShelf(row)">上架</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </el-card>

    <!-- 景点新增/编辑对话框 -->
    <el-dialog v-model="spotDialog" :title="spotForm.id ? '编辑景点' : '新增景点'" width="600px">
      <el-form :model="spotForm" label-width="90px">
        <el-form-item label="景点名称" required>
          <el-input v-model="spotForm.name" placeholder="请输入景点名称" />
        </el-form-item>
        <el-form-item label="分类ID">
          <el-input-number v-model="spotForm.categoryId" :min="0" />
        </el-form-item>
        <el-form-item label="省 / 市">
          <el-input v-model="spotForm.province" placeholder="省" style="width: 48%" />
          <el-input v-model="spotForm.city" placeholder="市" style="width: 48%; margin-left: 4%" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="spotForm.address" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="开放时间">
          <el-input v-model="spotForm.openTime" placeholder="如 08:00-17:30" />
        </el-form-item>
        <el-form-item label="封面图">
          <div style="display: flex; align-items: center; gap: 12px">
            <el-image
              v-if="spotForm.coverImage"
              :src="spotForm.coverImage"
              fit="cover"
              style="width: 120px; height: 80px; border-radius: 4px; border: 1px solid #dcdfe6"
            />
            <el-upload :show-file-list="false" :http-request="uploadCover" accept="image/*">
              <el-button size="small">{{ spotForm.coverImage ? '重新上传' : '上传图片' }}</el-button>
            </el-upload>
            <el-button v-if="spotForm.coverImage" size="small" @click="spotForm.coverImage = ''">移除</el-button>
          </div>
          <div style="font-size: 12px; color: #909399; margin-top: 4px">支持从电脑选择图片上传</div>
        </el-form-item>
        <el-form-item label="景点介绍">
          <el-input v-model="spotForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="spotDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSpot">保存</el-button>
      </template>
    </el-dialog>

    <!-- 票型管理对话框 -->
    <el-dialog v-model="ticketDialog" :title="`票型管理 · ${currentSpotName}`" width="700px">
      <div style="margin-bottom: 12px">
        <el-button type="success" size="small" @click="openTicketCreate">新增票型</el-button>
      </div>
      <el-table :data="ticketList" border size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="票型名称" />
        <el-table-column prop="price" label="价格(元)" width="100" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="openTicketEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleTicketDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 票型新增/编辑对话框 -->
    <el-dialog v-model="ticketFormDialog" :title="ticketForm.id ? '编辑票型' : '新增票型'" width="480px">
      <el-form :model="ticketForm" label-width="90px">
        <el-form-item label="票型名称" required>
          <el-input v-model="ticketForm.name" placeholder="如 成人票 / 学生票" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="ticketForm.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="ticketForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="有效期(天)">
          <el-input-number v-model="ticketForm.validDays" :min="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="ticketForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ticketFormDialog = false">取消</el-button>
        <el-button type="primary" @click="submitTicket">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSpotPage, createSpot, updateSpot, shelfSpot, offSpot, deleteSpot,
  getTicketList, createTicket, updateTicket, deleteTicket
} from '@/api/spot'
import { resourceStatus, tagType, val } from '@/utils/dict'
import { uploadFile } from '@/api/upload'

const query = reactive({ current: 1, size: 10, status: null, name: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

// 景点表单
const spotDialog = ref(false)
const spotForm = reactive({
  id: null, name: '', categoryId: null, province: '', city: '',
  district: '', address: '', openTime: '', coverImage: '', images: '', description: ''
})

// 票型
const ticketDialog = ref(false)
const ticketFormDialog = ref(false)
const currentSpotId = ref(null)
const currentSpotName = ref('')
const ticketList = ref([])
const ticketForm = reactive({ id: null, spotId: null, name: '', price: 0, stock: 0, validDays: 1, description: '' })

async function fetchList() {
  loading.value = true
  try {
    const data = await getSpotPage({
      current: query.current,
      size: query.size,
      status: val(query.status),
      name: val(query.name)
    })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
  fetchList()
}

function handleReset() {
  query.status = null
  query.name = ''
  query.current = 1
  fetchList()
}

function resetSpotForm() {
  Object.assign(spotForm, {
    id: null, name: '', categoryId: null, province: '', city: '',
    district: '', address: '', openTime: '', coverImage: '', images: '', description: ''
  })
}

function openCreate() {
  resetSpotForm()
  spotDialog.value = true
}

function openEdit(row) {
  Object.assign(spotForm, {
    id: row.id, name: row.name, categoryId: row.categoryId, province: row.province,
    city: row.city, district: row.district, address: row.address, openTime: row.openTime,
    coverImage: row.coverImage, images: row.images, description: row.description
  })
  spotDialog.value = true
}

async function uploadCover({ file }) {
  try {
    spotForm.coverImage = await uploadFile(file)
    ElMessage.success('上传成功')
  } catch {
    // 错误提示已在拦截器统一处理
  }
}

async function submitSpot() {
  if (!spotForm.name) {
    ElMessage.warning('请输入景点名称')
    return
  }
  if (spotForm.id) {
    await updateSpot(spotForm)
  } else {
    await createSpot(spotForm)
  }
  ElMessage.success('保存成功')
  spotDialog.value = false
  fetchList()
}

async function handleShelf(row) {
  await shelfSpot(row.id)
  ElMessage.success('已上架')
  fetchList()
}

async function handleOff(row) {
  await offSpot(row.id)
  ElMessage.success('已下架')
  fetchList()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该景点？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteSpot(row.id)
  ElMessage.success('已删除')
  fetchList()
}

async function openTicket(row) {
  currentSpotId.value = row.id
  currentSpotName.value = row.name
  ticketDialog.value = true
  await fetchTickets()
}

async function fetchTickets() {
  ticketList.value = await getTicketList(currentSpotId.value)
}

function openTicketCreate() {
  Object.assign(ticketForm, { id: null, spotId: currentSpotId.value, name: '', price: 0, stock: 0, validDays: 1, description: '' })
  ticketFormDialog.value = true
}

function openTicketEdit(row) {
  Object.assign(ticketForm, {
    id: row.id, spotId: row.spotId, name: row.name, price: row.price,
    stock: row.stock, validDays: row.validDays, description: row.description
  })
  ticketFormDialog.value = true
}

async function submitTicket() {
  if (!ticketForm.name) {
    ElMessage.warning('请输入票型名称')
    return
  }
  if (ticketForm.id) {
    await updateTicket(ticketForm)
  } else {
    await createTicket(ticketForm)
  }
  ElMessage.success('保存成功')
  ticketFormDialog.value = false
  fetchTickets()
}

async function handleTicketDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该票型？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteTicket(row.id)
  ElMessage.success('已删除')
  fetchTickets()
}

onMounted(fetchList)
</script>
