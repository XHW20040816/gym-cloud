<template>
  <div class="page">
    <h1 class="page-title">个人中心</h1>
    <p class="page-subtitle">账户信息、会员卡与支付记录</p>

    <div class="profile-grid">
      <div class="panel user-panel">
        <div class="avatar">{{ userStore.displayName.slice(0, 1) }}</div>
        <div class="user-name">{{ userStore.displayName }}</div>
        <div class="muted">@{{ userStore.userInfo.username }} · {{ roleText }}</div>
        <div class="stats">
          <div class="stat">
            <div class="stat-num">¥{{ Number(userStore.userInfo.balance || 0).toFixed(2) }}</div>
            <div class="stat-label">账户余额</div>
          </div>
          <div class="stat">
            <div class="stat-num">{{ userStore.userInfo.point || 0 }}</div>
            <div class="stat-label">积分</div>
          </div>
        </div>
      </div>

      <div class="panel">
        <h3 class="block-title">我的会员卡</h3>
        <div v-loading="loadingCards" class="my-cards">
          <div v-for="item in myCards" :key="item.id" class="my-card">
            <span>卡号 #{{ item.cardId }}</span>
            <span class="muted">{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</span>
            <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">
              {{ item.status === 1 ? '生效中' : '已失效' }}
            </el-tag>
          </div>
          <el-empty v-if="!loadingCards && !myCards.length" description="暂无会员卡" :image-size="60" />
        </div>
      </div>
    </div>

    <section class="section">
      <div class="section-head">
        <h2 class="section-title">支付记录</h2>
        <el-button link type="primary" @click="loadPayments">刷新</el-button>
      </div>
      <div v-loading="loadingPayments" class="panel payment-list">
        <div v-for="item in payments" :key="item.paymentNo" class="payment">
          <div>
            <div class="payment-no">{{ item.paymentNo }}</div>
            <div class="muted pay-order">订单 {{ item.orderNo }}</div>
          </div>
          <div class="payment-right">
            <span class="price">{{ Number(item.amount).toFixed(2) }}</span>
            <span class="muted">{{ payTypeText(item.payType) }}</span>
            <el-tag size="small" :type="item.status === 1 ? 'success' : item.status === 2 ? 'danger' : 'warning'">
              {{ payStatusText(item.status) }}
            </el-tag>
          </div>
        </div>
        <el-empty v-if="!loadingPayments && !payments.length" description="暂无支付记录" :image-size="70" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { getMyCards, getMyPayments } from '@/api'

const userStore = useUserStore()

const myCards = ref([])
const payments = ref([])
const loadingCards = ref(false)
const loadingPayments = ref(false)

const roleMap = { ADMIN: '管理员', COACH: '教练', USER: '会员' }
const roleText = computed(() => roleMap[userStore.userInfo.role] || '会员')

const payTypeMap = { BALANCE: '余额支付', ALIPAY: '支付宝', WECHAT: '微信支付', 1: '余额支付', 2: '支付宝', 3: '微信支付' }
const payTypeText = (type) => payTypeMap[type] ?? '-'

function payStatusText(status) {
  if (status === 1) return '成功'
  if (status === 2) return '失败'
  return '处理中'
}

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 10)
}

async function loadCards() {
  loadingCards.value = true
  try {
    myCards.value = (await getMyCards()) || []
  } finally {
    loadingCards.value = false
  }
}

async function loadPayments() {
  loadingPayments.value = true
  try {
    const page = await getMyPayments({ pageNum: 1, pageSize: 20 })
    payments.value = page?.rows || []
  } finally {
    loadingPayments.value = false
  }
}

onMounted(() => {
  userStore.fetchUserInfo().catch(() => {})
  loadCards()
  loadPayments()
})
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
}

.user-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb27a, #ff6a00);
  color: #fff;
  font-size: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.user-name {
  font-size: 18px;
  font-weight: 700;
}

.stats {
  display: flex;
  width: 100%;
  margin-top: 22px;
  border-top: 1px solid var(--line);
  padding-top: 16px;
}

.stat {
  flex: 1;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: var(--brand);
}

.stat-label {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 4px;
}

.block-title {
  margin: 0 0 14px;
  font-size: 16px;
}

.my-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 60px;
}

.my-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  background: var(--bg);
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 13px;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 100px;
}

.payment {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--line);
}

.payment:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.payment-no {
  font-family: Consolas, monospace;
  font-size: 13px;
  font-weight: 600;
}

.pay-order {
  font-size: 12px;
  margin-top: 4px;
}

.payment-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

@media (max-width: 800px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
