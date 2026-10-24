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
        <h2>创建账号</h2>
        <p class="muted">注册后即可下单、预约课程、开通会员卡</p>

        <el-form ref="formRef" :model="form" :rules="rules" class="form" @keyup.enter="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" size="large" placeholder="用户名" prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              size="large"
              type="password"
              placeholder="密码（6-20位）"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              size="large"
              type="password"
              placeholder="确认密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item label="角色" prop="role">
            <el-radio-group v-model="form.role">
              <el-radio-button label="USER">普通用户</el-radio-button>
              <el-radio-button label="COACH">教练</el-radio-button>
              <el-radio-button label="ADMIN">管理员</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.role === 'ADMIN'" label="邀请码" prop="inviteCode">
            <el-input
              v-model="form.inviteCode"
              size="large"
              placeholder="请输入管理员邀请码"
              prefix-icon="Key"
              clearable
            />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" size="large" placeholder="昵称（选填）" prefix-icon="Postcard" clearable />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="form.phone" size="large" placeholder="手机号（选填）" prefix-icon="Phone" clearable />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="form.email" size="large" placeholder="邮箱（选填）" prefix-icon="Message" clearable />
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="submit">
            注 册
          </el-button>
        </el-form>

        <div class="demo">
          <span class="muted">已有账号？</span>
          <el-button size="small" link type="primary" @click="goLogin">返回登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'USER',
  inviteCode: '',
  nickname: '',
  phone: '',
  email: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

function goLogin() {
  router.push('/login')
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.role === 'ADMIN' && !form.inviteCode.trim()) {
      ElMessage.warning('请填写管理员邀请码')
      return
    }
    loading.value = true
    try {
      await register({
        username: form.username,
        password: form.password,
        role: form.role,
        inviteCode: form.inviteCode,
        nickname: form.nickname,
        phone: form.phone,
        email: form.email
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (e) {
      ElMessage.error(e.message || '注册失败，请重试')
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
  min-height: 560px;
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
  padding: 40px 48px;
  display: flex;
  flex-direction: column;
}

.form-area h2 {
  margin: 0 0 6px;
  font-size: 24px;
}

.form-area .muted {
  font-size: 13px;
  margin: 0 0 20px;
}

.form {
  margin-bottom: 8px;
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
