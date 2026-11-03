<template>
  <el-dialog
    :model-value="modelValue"
    :title="`房型管理 - ${hotel?.name || ''}`"
    width="720px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div style="margin-bottom: 12px">
      <el-button type="success" size="small" @click="openEdit()">新增房型</el-button>
    </div>
    <el-table :data="rooms" v-loading="loading" border size="small">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="房型名" show-overflow-tooltip />
      <el-table-column prop="price" label="单价" width="90" />
      <el-table-column prop="totalCount" label="总房数" width="80" />
      <el-table-column prop="remainingCount" label="剩余" width="80" />
      <el-table-column label="含早" width="70">
        <template #default="{ row }">{{ row.breakfast === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ roomStatus[row.status] || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 房型编辑子弹窗 -->
    <el-dialog
      v-model="editVisible"
      :title="roomForm.id ? '编辑房型' : '新增房型'"
      width="520px"
      append-to-body
      @closed="reset"
    >
      <el-form :model="roomForm" :rules="roomRules" ref="roomFormRef" label-width="100px">
        <el-form-item label="房型名" prop="name">
          <el-input v-model="roomForm.name" placeholder="如：标准大床房" />
        </el-form-item>
        <el-form-item label="单价(元/晚)" prop="price">
          <el-input-number v-model="roomForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原价(元/晚)">
          <el-input-number v-model="roomForm.originalPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总房间数" prop="totalCount">
          <el-input-number v-model="roomForm.totalCount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="剩余房间数">
          <el-input-number v-model="roomForm.remainingCount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="床型">
          <el-input v-model="roomForm.bedType" placeholder="如：1.8m 大床" />
        </el-form-item>
        <el-form-item label="面积">
          <el-input v-model="roomForm.area" placeholder="如：35㎡" />
        </el-form-item>
        <el-form-item label="最多入住">
          <el-input-number v-model="roomForm.maxGuests" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否含早">
          <el-switch v-model="roomForm.breakfast" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="图片">
          <ImageUpload v-model="roomForm.image" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roomForm.description" type="textarea" :rows="2" placeholder="房型描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoomList, createRoom, updateRoom, deleteRoom } from '@/api/hotel'
import ImageUpload from '@/components/ImageUpload.vue'
import { roomStatus } from '@/utils/dict'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  hotel: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const rooms = ref([])

const editVisible = ref(false)
const saving = ref(false)
const roomFormRef = ref()
const roomForm = reactive({})

const roomRules = {
  name: [{ required: true, message: '请输入房型名', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入总房间数', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (v) => {
    if (v && props.hotel?.id) {
      load()
    }
  }
)

async function load() {
  loading.value = true
  try {
    rooms.value = await getRoomList(props.hotel.id)
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.keys(roomForm).forEach(k => delete roomForm[k])
  Object.assign(roomForm, row || { breakfast: 0, status: 1 })
  editVisible.value = true
}

function reset() {
  roomFormRef.value?.resetFields()
}

async function save() {
  await roomFormRef.value.validate()
  saving.value = true
  try {
    if (roomForm.id) {
      await updateRoom(roomForm)
      ElMessage.success('已保存')
    } else {
      await createRoom({ ...roomForm, hotelId: props.hotel.id })
      ElMessage.success('已新增房型')
    }
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确认删除该房型？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteRoom(row.id)
  ElMessage.success('已删除')
  load()
}
</script>
