<template>
  <div class="login-page">
    <div class="login-card">
      <div class="side">
        <div class="side-logo"><Trophy /></div>
        <h1>健身房会员中心</h1>
        <p class="side-sub">Gym Member Center</p>
        <ul class="side-list">
          <li><Goods /> 精选运动补给与装备</li>
          <li><AlarmClock /> 限时秒杀，先到先得</li>
          <li><Reading /> 团课预约，教练带练</li>
          <li><CreditCard /> 会员卡在线开通</li>
        </ul>
      </div>

      <div class="form-area">
        <h2>欢迎回来</h2>
        <p class="muted">登录后可下单、预约课程、查看会员卡</p>

        <el-form ref="formRef" :model="form" :rules="rules" class="form" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" size="large" placeholder="用户名" prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
            登 录
          </el-button>
        </el-form>

        <div class="demo">
          <span class="muted">还没有账号？</span>
          <el-button size="small" link type="primary" @click="goRegister">立即注册</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function goRegister() {
  router.push('/register')
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login({ username: form.username, password: form.password })
      await userStore.fetchUserInfo()
      ElMessage.success('登录成功')
      const redirect = route.query.redirect
      router.replace(redirect ? decodeURIComponent(redirect) : '/home')
    } catch (e) {
      ElMessage.error(e.message || '登录失败，请检查账号密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: radial-gradient(circle at 15% 20%, #ffe3cc 0, transparent 45%),
    radial-gradient(circle at 85% 80%, #ffd6e7 0, transparent 45%), #f6f8fb;
}

.login-card {
  width: 900px;
  max-width: 100%;
  min-height: 520px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 24px 60px rgba(31, 45, 61, 0.12);
  display: flex;
  overflow: hidden;
}

.side {
  width: 44%;
  padding: 48px 40px;
  color: #fff;
  background: linear-gradient(150deg, #ff9a44 0%, #ff5a00 60%, #e04300 100%);
  display: flex;
  flex-direction: column;
}

.side-logo {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.side-logo :deep(svg),
.side-list :deep(svg) {
  width: 24px;
  height: 24px;
}

.side h1 {
  font-size: 26px;
  margin: 0 0 6px;
}

.side-sub {
  margin: 0 0 32px;
  opacity: 0.75;
  letter-spacing: 2px;
  font-size: 12px;
}

.side-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-size: 14px;
}

.side-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  opacity: 0.92;
}

.form-area {
  flex: 1;
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
}

.form-area h2 {
  margin: 0 0 6px;
  font-size: 24px;
}

.form-area .muted {
  font-size: 13px;
  margin: 0 0 28px;
}

.form {
  margin-bottom: 12px;
}

.submit {
  width: 100%;
  letter-spacing: 6px;
  font-size: 15px;
}

.demo {
  margin-top: auto;
  font-size: 12px;
  display: flex;
  align-items: center;
}

@media (max-width: 760px) {
  .side {
    display: none;
  }
  .form-area {
    padding: 40px 28px;
  }
}
</style>
