<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape shape1"></div>
      <div class="bg-shape shape2"></div>
      <div class="bg-shape shape3"></div>
    </div>
    <div class="login-box">
      <div class="login-left">
        <div class="brand">
          <el-icon class="brand-icon"><Trophy /></el-icon>
          <h1 class="brand-title">健身房管理系统</h1>
          <p class="brand-subtitle">Gym Management System</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据大屏实时监控</span>
          </div>
          <div class="feature-item">
            <el-icon><User /></el-icon>
            <span>会员用户精细化管理</span>
          </div>
          <div class="feature-item">
            <el-icon><Reading /></el-icon>
            <span>课程排期智能调度</span>
          </div>
          <div class="feature-item">
            <el-icon><Promotion /></el-icon>
            <span>秒杀活动高效运营</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <h2 class="login-title">欢迎登录</h2>
        <p class="login-desc">请输入您的账号和密码</p>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
              <el-link type="primary" @click="handleForgot">忘记密码？</el-link>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>
        <div class="quick-login">
          <div class="quick-btns">
            <el-button size="small" type="success" plain @click="quickLogin('admin', '123456')">
              管理员登录
            </el-button>
            <el-button size="small" type="warning" plain @click="quickLogin('coach', '123456')">
              教练登录
            </el-button>
            <el-button size="small" type="info" plain @click="toUserLogin">
              用户登录
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login({
          username: loginForm.username,
          password: loginForm.password
        })
        ElMessage.success('登录成功')
        router.push('/')
      } catch (e) {
        ElMessage.error(e.message || '登录失败，请检查账号密码')
      } finally {
        loading.value = false
      }
    }
  })
}

function quickLogin(username, password) {
  loginForm.username = username
  loginForm.password = password
  handleLogin()
}

// 跳转到会员端登录页（gym-user-web，端口 5174）
function toUserLogin() {
  window.location.href = 'http://localhost:5174/login'
}

function handleForgot() {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: #fff;
}

.shape1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation: float 8s ease-in-out infinite;
}

.shape2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
  animation: float 10s ease-in-out infinite 1s;
}

.shape3 {
  width: 200px;
  height: 200px;
  top: 40%;
  right: 20%;
  animation: float 12s ease-in-out infinite 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.login-box {
  position: relative;
  width: 900px;
  height: 560px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  overflow: hidden;
}

.login-left {
  width: 420px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand {
  .brand-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }

  .brand-title {
    font-size: 28px;
    margin: 0 0 8px;
    font-weight: 700;
  }

  .brand-subtitle {
    font-size: 14px;
    opacity: 0.8;
    margin: 0;
    letter-spacing: 1px;
  }
}

.features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.9;

  .el-icon {
    font-size: 20px;
  }
}

.login-right {
  flex: 1;
  padding: 60px 50px 30px;
  display: flex;
  flex-direction: column;
}

.login-title {
  font-size: 28px;
  color: #303133;
  margin: 0 0 8px;
  font-weight: 600;
}

.login-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px;
}

.login-form {
  margin-bottom: 24px;
}

.form-options {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;
}

.quick-login {
  border-top: 1px solid #ebeef5;
  padding-top: 18px;

  .quick-btns {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
}
</style>
