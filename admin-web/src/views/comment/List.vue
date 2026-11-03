<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(text, value) in commentStatus" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="query.targetType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(text, value) in commentTargetType" :key="value" :label="text" :value="Number(value)" />
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
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="目标类型" width="100">
          <template #default="{ row }">{{ commentTargetType[row.targetType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="targetName" label="目标名称" show-overflow-tooltip />
        <el-table-column prop="merchantName" label="商家" show-overflow-tooltip />
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ commentStatus[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handlePass(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
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
import { ElMessageBox } from 'element-plus'
import { getCommentPage, deleteComment } from '@/api/comment'
import { auditPass, auditReject } from '@/utils/audit'
import { commentStatus, commentTargetType, tagType, val } from '@/utils/dict'

const query = reactive({ current: 1, size: 10, status: null, targetType: null })
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList() {
  loading.value = true
  try {
    const data = await getCommentPage({
      current: query.current,
      size: query.size,
      status: val(query.status),
      targetType: val(query.targetType)
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
  query.targetType = null
  query.current = 1
  fetchList()
}

async function handlePass(row) {
  if (await auditPass(5, row.id)) fetchList()
}

async function handleReject(row) {
  if (await auditReject(5, row.id)) fetchList()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该评价？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteComment(row.id)
  fetchList()
}

onMounted(fetchList)
</script>
