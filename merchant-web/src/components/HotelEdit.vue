<template>
  <el-dialog
    :model-value="modelValue"
    :title="form.id ? '编辑酒店' : '新增酒店'"
    width="620px"
    @update:model-value="emit('update:modelValue', $event)"
    @closed="reset"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
      <el-form-item label="酒店名称" prop="name">
        <el-input v-model="form.name" placeholder="酒店名称" />
      </el-form-item>
      <el-form-item label="所在地区">
        <div style="display: flex; gap: 8px; width: 100%">
          <el-input v-model="form.province" placeholder="省" />
          <el-input v-model="form.city" placeholder="市" />
          <el-input v-model="form.district" placeholder="区" />
        </div>
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="form.address" placeholder="详细地址" />
      </el-form-item>
      <el-form-item label="星级">
        <el-select v-model="form.starLevel" placeholder="请选择" style="width: 100%">
          <el-option v-for="n in 5" :key="n" :label="`${n} 星`" :value="n" />
        </el-select>
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
      <el-form-item label="酒店介绍">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="酒店介绍" />
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
import { createHotel, updateHotel } from '@/api/hotel'
import ImageUpload from '@/components/ImageUpload.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  hotel: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const formRef = ref()
const saving = ref(false)
const form = reactive({})

const rules = {
  name: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      Object.keys(form).forEach(k => delete form[k])
      Object.assign(form, props.hotel || { starLevel: 3 })
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
      await updateHotel(form)
      ElMessage.success('已保存，重新进入待审核状态')
    } else {
      await createHotel(form)
      ElMessage.success('已提交，等待管理员审核上架')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
