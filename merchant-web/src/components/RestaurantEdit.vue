<template>
  <el-dialog
    :model-value="modelValue"
    :title="form.id ? '编辑餐厅' : '新增餐厅'"
    width="620px"
    @update:model-value="emit('update:modelValue', $event)"
    @closed="reset"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
      <el-form-item label="餐厅名称" prop="name">
        <el-input v-model="form.name" placeholder="餐厅名称" />
      </el-form-item>
      <el-form-item label="所在地区">
        <div style="display: flex; gap: 8px; width: 100%">
          <el-input v-model="form.province" placeholder="省" />
          <el-input v-model="form.city" placeholder="市" />
        </div>
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="form.address" placeholder="详细地址" />
      </el-form-item>
      <el-form-item label="人均消费">
        <el-input-number v-model="form.avgPrice" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="营业时间">
        <el-input v-model="form.businessHours" placeholder="如：10:00-22:00" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" placeholder="联系电话" />
      </el-form-item>
      <el-form-item label="封面图">
        <ImageUpload v-model="form.coverImage" />
      </el-form-item>
      <el-form-item label="图片集">
        <ImageUpload v-model="form.images" multiple />
      </el-form-item>
      <el-form-item label="餐厅介绍">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="餐厅介绍" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createRestaurant, updateRestaurant } from '@/api/restaurant'
import ImageUpload from '@/components/ImageUpload.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  restaurant: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const form = reactive({})

const rules = {
  name: [{ required: true, message: '请输入餐厅名称', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      Object.keys(form).forEach(k => delete form[k])
      Object.assign(form, props.restaurant || {})
    }
  }
)

function reset() {
  formRef.value?.resetFields()
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateRestaurant(form)
      ElMessage.success('已保存，重新进入待审核状态')
    } else {
      await createRestaurant(form)
      ElMessage.success('已提交，等待管理员审核上架')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
