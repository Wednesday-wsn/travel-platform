<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="正常" :value="1" />
            <el-option label="已禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="请输入" clearable style="width: 180px" />
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
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ userStatus[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" size="small" type="warning" @click="handleDisable(row)">禁用</el-button>
            <el-button v-if="row.status === 0" size="small" type="primary" @click="handleEnable(row)">启用</el-button>
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
import { getUserPage, enableUser, disableUser } from '@/api/user'
import { userStatus, val } from '@/utils/dict'

const query = reactive({ current: 1, size: 10, status: null, username: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const data = await getUserPage({
      current: query.current,
      size: query.size,
      status: val(query.status),
      username: val(query.username)
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
  query.username = ''
  query.current = 1
  fetchList()
}

async function handleEnable(row) {
  await enableUser(row.id)
  ElMessage.success('已启用')
  fetchList()
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm('确认禁用该用户？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await disableUser(row.id)
  ElMessage.success('已禁用')
  fetchList()
}

onMounted(fetchList)
</script>
