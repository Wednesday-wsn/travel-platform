<template>
  <div class="login">
    <!-- 左侧品牌区（Art Design Pro 原版登录布局） -->
    <div class="left-wrap">
      <div class="logo">
        <div class="logo-mark">
          <el-icon><Location /></el-icon>
        </div>
        <span class="title">旅游服务平台</span>
      </div>
      <img class="left-bg" :src="leftBg" alt="" />
      <img class="left-img" :src="leftImg" alt="" />
      <div class="text-wrap">
        <h1>酒店 · 景点 · 餐饮 一站式管理</h1>
        <p>统一资源审核 · 数据可视化 · 商家与用户管理</p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="right-wrap">
      <div class="login-wrap">
        <div class="form">
          <h3 class="title">管理端登录</h3>
          <p class="sub-title">欢迎回来，请登录您的账号</p>
          <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>
            <el-form-item style="margin-top: 30px">
              <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import leftBg from '@/assets/img/login/lf_bg.png'
import leftImg from '@/assets/img/login/lf_icon2.png'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  box-sizing: border-box;
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* ---- 左侧 ---- */
.left-wrap {
  position: relative;
  box-sizing: border-box;
  width: 50vw;
  height: 100%;
  padding: 20px;
  overflow: hidden;
  background: #fafbfc;
}
.logo {
  position: relative;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  background: var(--el-color-primary);
}
.title {
  font-size: 20px;
  font-weight: 400;
  color: #f9f9f9;
}
.left-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* 右侧渐隐：让背景图自身淡出，与右侧表单无缝过渡，避免硬边线 */
  -webkit-mask-image: linear-gradient(to right, #000 60%, transparent 100%);
  mask-image: linear-gradient(to right, #000 60%, transparent 100%);
}
.left-img {
  position: relative;
  z-index: 10;
  display: block;
  width: 560px;
  margin: auto;
  margin-top: 15vh;
}
.text-wrap {
  position: absolute;
  bottom: 80px;
  width: 100%;
  text-align: center;
}
.text-wrap h1 {
  font-size: 26px;
  font-weight: 400;
  color: #f9f9f9;
}
.text-wrap p {
  margin-top: 10px;
  font-size: 14px;
  color: #c4cada;
}

/* ---- 右侧 ---- */
.right-wrap {
  position: relative;
  flex: 1;
  height: 100%;
}
.login-wrap {
  position: absolute;
  inset: 0;
  width: 440px;
  height: 520px;
  margin: auto;
}
.form {
  box-sizing: border-box;
  height: 100%;
  padding: 40px 0;
}
.form .title {
  margin-left: -2px;
  font-size: 34px;
  font-weight: 600;
  color: var(--art-text-gray-900) !important;
}
.sub-title {
  margin-top: 10px;
  font-size: 14px;
  color: var(--art-text-gray-500) !important;
}
.form :deep(.el-input) {
  height: 46px;
}
.form :deep(.el-input__wrapper) {
  height: 46px;
}
.login-btn {
  width: 100%;
  height: 46px;
  color: #fff;
  border: 0;
  font-size: 15px;
  letter-spacing: 6px;
}
@media only screen and (max-width: 1180px) {
  .left-wrap {
    display: none;
  }
  .right-wrap {
    margin: auto;
  }
  .login-wrap {
    position: relative;
    width: 440px;
    height: auto;
  }
  .form {
    margin-top: 10vh;
  }
}
</style>
