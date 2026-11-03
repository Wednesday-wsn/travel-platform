<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(text, value) in merchantStatus" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="商家名称">
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
        <el-table-column prop="name" label="商家名称" show-overflow-tooltip />
        <el-table-column prop="username" label="登录名" width="120" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ merchantType[row.type] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ merchantStatus[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditReason" label="驳回原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handlePass(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
            <el-button v-if="row.status === 1" size="small" type="warning" @click="handleDisable(row)">禁用</el-button>
            <el-button v-if="row.status === 3" size="small" type="primary" @click="handleEnable(row)">启用</el-button>
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
import { getMerchantPage, enableMerchant, disableMerchant } from '@/api/merchant'
import { auditPass, auditReject } from '@/utils/audit'
import { merchantStatus, merchantType, tagType, val } from '@/utils/dict'

const query = reactive({ current: 1, size: 10, status: null, name: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const data = await getMerchantPage({
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
  if (await auditPass(1, row.id)) fetchList()
}

async function handleReject(row) {
  if (await auditReject(1, row.id)) fetchList()
}

async function handleEnable(row) {
  await enableMerchant(row.id)
  ElMessage.success('已启用')
  fetchList()
}

async function handleDisable(row) {
  try {
    await ElMessageBox.confirm('确认禁用该商家？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await disableMerchant(row.id)
  ElMessage.success('已禁用')
  fetchList()
}

onMounted(fetchList)
</script>
