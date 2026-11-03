<template>
  <el-dialog
    :model-value="modelValue"
    :title="`菜品管理 - ${restaurant?.name || ''}`"
    width="720px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div style="margin-bottom: 12px">
      <el-button type="success" size="small" @click="openEdit()">新增菜品</el-button>
    </div>
    <el-table :data="dishes" v-loading="loading" border size="small">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="菜品名" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="90" />
      <el-table-column prop="sales" label="销量" width="80" />
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

    <!-- 菜品编辑子弹窗 -->
    <el-dialog
      v-model="editVisible"
      :title="dishForm.id ? '编辑菜品' : '新增菜品'"
      width="520px"
      append-to-body
      @closed="reset"
    >
      <el-form :model="dishForm" :rules="dishRules" ref="dishFormRef" label-width="90px">
        <el-form-item label="菜品名" prop="name">
          <el-input v-model="dishForm.name" placeholder="菜品名" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="dishForm.category" placeholder="如：招牌菜 / 热菜 / 凉菜" />
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="dishForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片">
          <ImageUpload v-model="dishForm.image" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="dishForm.description" type="textarea" :rows="2" placeholder="菜品描述" />
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
import { getDishList, createDish, updateDish, deleteDish } from '@/api/restaurant'
import ImageUpload from '@/components/ImageUpload.vue'
import { roomStatus } from '@/utils/dict'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  restaurant: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const dishes = ref([])

const editVisible = ref(false)
const saving = ref(false)
const dishFormRef = ref()
const dishForm = reactive({})

const dishRules = {
  name: [{ required: true, message: '请输入菜品名', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (v) => {
    if (v && props.restaurant?.id) {
      load()
    }
  }
)

async function load() {
  loading.value = true
  try {
    dishes.value = await getDishList(props.restaurant.id)
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.keys(dishForm).forEach(k => delete dishForm[k])
  Object.assign(dishForm, row || { status: 1 })
  editVisible.value = true
}

function reset() {
  dishFormRef.value?.resetFields()
}

async function save() {
  await dishFormRef.value.validate()
  saving.value = true
  try {
    if (dishForm.id) {
      await updateDish(dishForm)
      ElMessage.success('已保存')
    } else {
      await createDish({ ...dishForm, restaurantId: props.restaurant.id })
      ElMessage.success('已新增菜品')
    }
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确认删除该菜品？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteDish(row.id)
  ElMessage.success('已删除')
  load()
}
</script>
