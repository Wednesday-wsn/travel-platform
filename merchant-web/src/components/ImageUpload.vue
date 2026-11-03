<template>
  <div class="image-upload">
    <div v-for="(url, i) in list" :key="i" class="img-item">
      <el-image :src="url" fit="cover" class="thumb" :preview-src-list="list" :initial-index="i" />
      <el-icon class="del" @click="remove(i)"><CircleCloseFilled /></el-icon>
    </div>
    <el-upload
      v-if="list.length < limit"
      :show-file-list="false"
      :http-request="doUpload"
      accept="image/*"
    >
      <div class="upload-trigger">
        <el-icon><Plus /></el-icon>
      </div>
    </el-upload>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { uploadFile } from '@/api/upload'

const props = defineProps({
  modelValue: { type: String, default: '' },
  multiple: { type: Boolean, default: false },
  limit: { type: Number, default: 5 }
})
const emit = defineEmits(['update:modelValue'])

const list = computed(() => (props.modelValue ? props.modelValue.split(',').filter(Boolean) : []))

async function doUpload({ file }) {
  try {
    const url = await uploadFile(file)
    if (props.multiple) {
      emit('update:modelValue', [...list.value, url].join(','))
    } else {
      emit('update:modelValue', url)
    }
  } catch {
    // 错误提示已在拦截器统一处理
  }
}

function remove(i) {
  const arr = [...list.value]
  arr.splice(i, 1)
  emit('update:modelValue', arr.join(','))
}
</script>

<style scoped>
.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.img-item {
  position: relative;
  width: 80px;
  height: 80px;
}
.thumb {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
.del {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 18px;
  color: #f56c6c;
  background: #fff;
  border-radius: 50%;
  cursor: pointer;
}
.upload-trigger {
  width: 80px;
  height: 80px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  font-size: 22px;
  cursor: pointer;
  transition: all 0.2s;
}
.upload-trigger:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
