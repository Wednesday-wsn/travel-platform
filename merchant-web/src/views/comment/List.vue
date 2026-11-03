<template>
  <div>
    <el-card>
      <el-form inline>
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
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ commentTargetType[row.targetType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="targetName" label="店铺/商品" show-overflow-tooltip />
        <el-table-column label="评分" width="150">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column prop="reply" label="我的回复" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reply || '-' }}</template>
        </el-table-column>
        <el-table-column label="申诉状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.appealStatus !== null && row.appealStatus !== undefined" :type="appealTag(row.appealStatus)">
              {{ appealStatus[row.appealStatus] || '-' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleReply(row)">回复</el-button>
            <el-button
              v-if="row.appealStatus === null || row.appealStatus === undefined || row.appealStatus === 2"
              size="small"
              type="warning"
              @click="handleAppeal(row)"
            >申诉</el-button>
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
import { getCommentPage, replyComment, appealComment } from '@/api/comment'
import { commentTargetType, appealStatus, val } from '@/utils/dict'

const query = reactive({ current: 1, size: 10, targetType: null })
const list = ref([])
const total = ref(0)
const loading = ref(false)

function appealTag(status) {
  if (status === 0) return 'warning'
  return 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getCommentPage({ current: query.current, size: query.size, targetType: val(query.targetType) })
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
  query.current = 1
  fetchList()
}

async function handleReply(row) {
  let reply
  try {
    const { value } = await ElMessageBox.prompt('请输入回复内容', '回复评价', {
      inputValue: row.reply || '',
      inputPattern: /\S+/,
      inputErrorMessage: '回复内容不能为空'
    })
    reply = value
  } catch {
    return
  }
  await replyComment(row.id, reply)
  ElMessage.success('回复成功')
  fetchList()
}

async function handleAppeal(row) {
  let reason
  try {
    const { value } = await ElMessageBox.prompt('请填写申诉理由，管理员审核通过后将撤销该评价', '申诉撤销评价', {
      inputPattern: /\S+/,
      inputErrorMessage: '申诉理由不能为空'
    })
    reason = value
  } catch {
    return
  }
  await appealComment(row.id, reason)
  ElMessage.success('申诉已提交，等待管理员审核')
  fetchList()
}

onMounted(fetchList)
</script>
