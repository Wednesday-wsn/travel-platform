<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="订单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(text, value) in orderStatus" :key="value" :label="text" :value="Number(value)" />
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
        <el-table-column prop="orderNo" label="订单号" width="200" show-overflow-tooltip />
        <el-table-column prop="targetName" label="酒店" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="totalAmount" label="金额(元)" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="orderTag(row.status)">{{ orderStatus[row.status] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" type="success" @click="handleFinish(row)">完成</el-button>
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

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="520px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号" :span="2">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="酒店" :span="2">{{ detail.targetName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">￥{{ detail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ orderStatus[detail.status] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ detail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderPage, finishOrder } from '@/api/order'
import { orderStatus, val } from '@/utils/dict'

const query = reactive({ current: 1, size: 10, status: null })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detail = ref({})

function orderTag(status) {
  if (status === 1 || status === 3) return 'success'
  if (status === 0) return 'warning'
  if (status === 2 || status === 4) return 'info'
  return 'info'
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getOrderPage({ current: query.current, size: query.size, status: val(query.status) })
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
  query.current = 1
  fetchList()
}

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

async function handleFinish(row) {
  try {
    await ElMessageBox.confirm('确认将该订单标记为已完成？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await finishOrder(row.id)
  ElMessage.success('已完成')
  fetchList()
}

onMounted(fetchList)
</script>
