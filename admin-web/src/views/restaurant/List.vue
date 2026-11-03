<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(text, value) in resourceStatus" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="餐厅名称">
          <el-input v-model="query.name" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="餐厅名称" show-overflow-tooltip />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="avgPrice" label="人均(元)" width="90" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ resourceStatus[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handlePass(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRestaurantPage, shelfRestaurant, offRestaurant, deleteRestaurant } from '@/api/restaurant'
import { auditPass, auditReject } from '@/utils/audit'
import { resourceStatus, tagType, val } from '@/utils/dict'

const query = reactive({ current: 1, size: 10, status: null, name: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const data = await getRestaurantPage({
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

async function handlePass(row) {
  if (await auditPass(4, row.id)) fetchList()
}

async function handleReject(row) {
  if (await auditReject(4, row.id)) fetchList()
}

async function handleShelf(row) {
  await shelfRestaurant(row.id)
  ElMessage.success('已上架')
  fetchList()
}

async function handleOff(row) {
  await offRestaurant(row.id)
  ElMessage.success('已下架')
  fetchList()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该餐厅？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteRestaurant(row.id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(fetchList)
</script>
