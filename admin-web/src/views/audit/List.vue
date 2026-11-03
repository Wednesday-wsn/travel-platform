<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="审核类型">
          <el-select v-model="query.targetType" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="(text, value) in auditTargetType" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="通过" :value="1" />
            <el-option label="驳回" :value="2" />
          </el-select>
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
        <el-table-column label="审核类型" width="120">
          <template #default="{ row }">{{ auditTargetType[row.targetType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="targetId" label="对象ID" width="90" />
        <el-table-column prop="auditorId" label="审核员ID" width="100" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ statusMap[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="审核意见" show-overflow-tooltip />
        <el-table-column prop="auditTime" label="审核时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0 && row.targetType === 6">
              <el-button size="small" type="success" @click="handlePass(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
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
import { getAuditPage } from '@/api/audit'
import { auditPass, auditReject } from '@/utils/audit'
import { auditTargetType, tagType, val } from '@/utils/dict'

const statusMap = { 0: '待审核', 1: '通过', 2: '驳回' }

const query = reactive({ current: 1, size: 10, targetType: null, status: null })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const data = await getAuditPage({
      current: query.current,
      size: query.size,
      targetType: val(query.targetType),
      status: val(query.status)
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
  query.targetType = null
  query.status = null
  query.current = 1
  fetchList()
}

async function handlePass(row) {
  if (await auditPass(row.targetType, row.targetId)) fetchList()
}

async function handleReject(row) {
  if (await auditReject(row.targetType, row.targetId)) fetchList()
}

onMounted(fetchList)
</script>
