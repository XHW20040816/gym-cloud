<template>
  <div class="page">
    <h1 class="page-title">会员卡</h1>
    <p class="page-subtitle">开通会员卡，畅享入场与课程权益</p>

    <div v-loading="loading" class="card-grid">
      <div v-for="card in cards" :key="card.id" class="member-card">
        <div class="card-top">
          <span class="card-name">{{ card.name }}</span>
          <span class="card-type">{{ typeText(card.type) }}</span>
        </div>
        <div class="card-price">{{ Number(card.price).toFixed(2) }}</div>
        <div class="card-desc">{{ card.description }}</div>
        <el-button type="primary" :loading="openingId === card.id" @click="open(card)">
          立即开通
        </el-button>
      </div>
    </div>
    <el-empty v-if="!loading && !cards.length" description="暂无可售会员卡" />

    <section class="section">
      <div class="section-head">
        <h2 class="section-title">我的会员卡</h2>
      </div>
      <div v-loading="loadingMine" class="my-cards">
        <div v-for="item in myCards" :key="item.id" class="my-card">
          <div class="my-card-head">
            <span>卡号 #{{ item.cardId }}</span>
            <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">
              {{ item.status === 1 ? '生效中' : '已失效' }}
            </el-tag>
          </div>
          <div class="my-card-line">有效期：{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</div>
          <div class="my-card-line">剩余次数：{{ item.remainTimes > 0 ? item.remainTimes : '不限次' }}</div>
        </div>
        <el-empty v-if="!loadingMine && !myCards.length" description="还没有会员卡，先开通一张吧" :image-size="70" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMemberCards, getMyCards, openMemberCard } from '@/api'

const cards = ref([])
const myCards = ref([])
const loading = ref(true)
const loadingMine = ref(false)
const openingId = ref(null)

const typeMap = { TIMES: '次卡', MONTH: '月卡', QUARTER: '季卡', YEAR: '年卡', 0: '次卡', 1: '月卡', 3: '季卡', 12: '年卡' }
const typeText = (type) => typeMap[type] ?? type

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 10)
}

async function loadCards() {
  loading.value = true
  try {
    const page = await getMemberCards()
    cards.value = page?.rows || []
  } finally {
    loading.value = false
  }
}

async function loadMine() {
  loadingMine.value = true
  try {
    myCards.value = (await getMyCards()) || []
  } finally {
    loadingMine.value = false
  }
}

async function open(card) {
  await ElMessageBox.confirm(`确认开通「${card.name}」吗？`, '开通会员卡', {
    confirmButtonText: '确认开通',
    cancelButtonText: '取消'
  }).catch(() => Promise.reject('cancel'))

  openingId.value = card.id
  try {
    await openMemberCard(card.id)
    ElMessage.success('开通成功')
    await loadMine()
  } catch (e) {
    // 错误提示已在请求拦截器处理
  } finally {
    openingId.value = null
  }
}

onMounted(() => {
  loadCards()
  loadMine()
})
</script>

<style scoped>
.member-card {
  background: linear-gradient(150deg, #2b1c14 0%, #4a2c1c 60%, #6b3a1e 100%);
  border-radius: 14px;
  padding: 22px;
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 220px;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-name {
  font-size: 17px;
  font-weight: 700;
}

.card-type {
  font-size: 12px;
  color: #ffcf9e;
  border: 1px solid rgba(255, 207, 158, 0.6);
  border-radius: 10px;
  padding: 1px 8px;
}

.card-price {
  font-size: 32px;
  font-weight: 800;
  color: #ffcf9e;
}

.card-price::before {
  content: '¥';
  font-size: 0.5em;
  margin-right: 2px;
}

.card-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.7;
  flex: 1;
}

.my-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  min-height: 80px;
}

.my-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.my-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.my-card-line {
  font-size: 12px;
  color: var(--ink-2);
}
</style>
