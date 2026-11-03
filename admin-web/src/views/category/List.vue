<template>
  <div>
    <el-card>
      <el-form inline>
        <el-form-item label="分类类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="(text, value) in categoryType" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button type="success" @click="openCreate">新增分类</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">{{ categoryType[row.type] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序号" width="120" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog" :title="form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="(text, value) in categoryType" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { categoryType, val } from '@/utils/dict'

const query = reactive({ type: null })
const list = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = reactive({ id: null, type: 1, name: '', sort: 0 })

async function fetchList() {
  loading.value = true
  try {
    list.value = await getCategoryList({ type: val(query.type) })
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: null, type: 1, name: '', sort: 0 })
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, type: row.type, name: row.name, sort: row.sort })
  dialog.value = true
}

async function submit() {
  if (!form.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (form.id) {
    await updateCategory(form)
  } else {
    await createCategory(form)
  }
  ElMessage.success('保存成功')
  dialog.value = false
  fetchList()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  fetchList()
}

onMounted(fetchList)
</script>
