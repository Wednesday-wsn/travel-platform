<template>
  <div class="register-page">
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <el-card class="register-card">
      <div class="head">
        <div class="head-mark">
          <el-icon><Shop /></el-icon>
        </div>
        <h2 class="title">商家入驻申请</h2>
        <p class="sub">填写信息，提交后由管理员审核</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="登录密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item label="商家名称" prop="name">
          <el-input v-model="form.name" placeholder="如：某某假日酒店" />
        </el-form-item>
        <el-form-item label="商家类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option v-for="(text, value) in merchantType" :key="value" :label="text" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱（选填）" />
        </el-form-item>
        <el-form-item label="经营地址">
          <el-input v-model="form.address" placeholder="经营地址" />
        </el-form-item>
        <el-form-item label="商家简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商家简介（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">提交申请</el-button>
        </el-form-item>
      </el-form>
      <div class="tip">
        已有账号？
        <el-link type="primary" @click="$router.push('/login')">返回登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { merchantRegister } from '@/api/auth'
import { merchantType } from '@/utils/dict'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  type: null,
  contactName: '',
  contactPhone: '',
  email: '',
  address: '',
  description: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次密码输入不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  name: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择商家类型', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const { confirmPassword, ...payload } = form
    await merchantRegister(payload)
    ElMessage.success('申请已提交，请等待管理员审核')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0b0d12 0%, #10131a 45%, #0c1510 100%);
  overflow: hidden;
  padding: 40px 0;
}
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.18;
}
.blob-1 {
  width: 420px;
  height: 420px;
  background: #a7f3d0;
  top: -120px;
  right: -80px;
}
.blob-2 {
  width: 360px;
  height: 360px;
  background: #99f6e4;
  bottom: -120px;
  left: -60px;
}
.register-card {
  position: relative;
  width: 520px;
  max-width: 94vw;
  padding: 12px 12px;
  border-radius: 18px;
  box-shadow: 0 24px 60px rgba(6, 95, 70, 0.18);
}
.head {
  text-align: center;
  margin: 8px 0 20px;
}
.head-mark {
  width: 48px;
  height: 48px;
  border-radius: 13px;
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: 0 6px 16px rgba(22, 163, 74, 0.35);
}
.title {
  text-align: center;
  margin: 0 0 6px;
  color: #1f2937;
  font-size: 20px;
}
.sub {
  text-align: center;
  margin: 0;
  color: #98a2b3;
  font-size: 13px;
}
.submit-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
  letter-spacing: 4px;
}
.tip {
  text-align: center;
  color: #98a2b3;
  font-size: 13px;
}
</style>
