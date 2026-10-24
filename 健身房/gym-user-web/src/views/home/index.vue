<template>
  <div class="page">
    <section class="hero">
      <div class="hero-text">
        <h1>练出更好的自己</h1>
        <p>运动补给、团课预约、限时秒杀，一站式健身服务</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="$router.push('/goods')">去逛逛</el-button>
          <el-button size="large" plain @click="$router.push('/seckill')">限时秒杀</el-button>
        </div>
      </div>
      <div class="hero-badge">
        <span class="badge-num">{{ goodsTotal }}</span>
        <span class="badge-label">在售商品</span>
      </div>
    </section>

    <section class="quick-grid">
      <div v-for="item in quickEntries" :key="item.path" class="quick-item" @click="$router.push(item.path)">
        <div class="quick-icon" :style="{ background: item.bg }">
          <component :is="item.icon" />
        </div>
        <div>
          <div class="quick-title">{{ item.title }}</div>
          <div class="quick-desc">{{ item.desc }}</div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-head">
        <h2 class="section-title">热门商品</h2>
        <el-button link type="primary" @click="$router.push('/goods')">查看全部</el-button>
      </div>
      <el-skeleton v-if="loadingGoods" :rows="3" animated />
      <div v-else class="card-grid">
        <ProductCard v-for="goods in goodsList" :key="goods.id" :goods="goods" @buy="buyGoods" />
      </div>
      <el-empty v-if="!loadingGoods && !goodsList.length" description="暂无商品" />
    </section>

    <section class="section">
      <div class="section-head">
        <h2 class="section-title">限时秒杀</h2>
        <el-button link type="primary" @click="$router.push('/seckill')">全部活动</el-button>
      </div>
      <div class="activity-row">
        <div v-for="activity in activities" :key="activity.id" class="activity-card" @click="$router.push('/seckill')">
          <div class="activity-name">{{ activity.name }}</div>
          <div class="activity-time">{{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}</div>
          <el-tag type="danger" effect="dark" size="small">进行中</el-tag>
        </div>
        <el-empty v-if="!activities.length" description="暂无进行中的秒杀" :image-size="60" />
      </div>
    </section>

    <section class="section">
      <div class="section-head">
        <h2 class="section-title">热门课程</h2>
        <el-button link type="primary" @click="$router.push('/course')">全部课程</el-button>
      </div>
      <div class="course-row">
        <div v-for="course in courses" :key="course.id" class="course-card" @click="$router.push('/course')">
          <div class="course-category">{{ course.category }}</div>
          <div class="course-name">{{ course.name }}</div>
          <div class="course-desc">{{ course.description }}</div>
          <div class="course-meta">
            <span class="price">{{ Number(course.price).toFixed(2) }}</span>
            <span class="muted">{{ course.duration }} 分钟</span>
          </div>
        </div>
      </div>
    </section>

    <PayDialog v-model="payVisible" :order-no="currentOrderNo" :amount="currentAmount" @success="onPaid" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/ProductCard.vue'
import PayDialog from '@/components/PayDialog.vue'
import { getGoodsPage, getCoursePage, getSeckillActivities } from '@/api'
import { createGoodsOrder } from '@/utils/buy'

const loadingGoods = ref(true)
const goodsList = ref([])
const goodsTotal = ref(0)
const activities = ref([])
const courses = ref([])

const payVisible = ref(false)
const currentOrderNo = ref('')
const currentAmount = ref(0)

const quickEntries = [
  { title: '限时秒杀', desc: '低价抢好物', path: '/seckill', icon: 'AlarmClock', bg: 'linear-gradient(135deg,#ff9a44,#ff5a00)' },
  { title: '团课预约', desc: '教练带练', path: '/course', icon: 'Reading', bg: 'linear-gradient(135deg,#7ec8ff,#3d8bfd)' },
  { title: '健身餐', desc: '低脂高蛋白', path: '/food', icon: 'Food', bg: 'linear-gradient(135deg,#96e6a1,#4caf50)' },
  { title: '会员卡', desc: '畅享入场', path: '/member', icon: 'CreditCard', bg: 'linear-gradient(135deg,#c3a6ff,#7c4dff)' }
]

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(5, 16)
}

async function load() {
  try {
    const [goodsPage, activityList, coursePage] = await Promise.all([
      getGoodsPage({ pageNum: 1, pageSize: 8, status: 1 }),
      getSeckillActivities(),
      getCoursePage({ pageNum: 1, pageSize: 4, status: 1 })
    ])
    goodsList.value = goodsPage?.rows || []
    goodsTotal.value = goodsPage?.total || 0
    activities.value = activityList || []
    courses.value = coursePage?.rows || []
  } finally {
    loadingGoods.value = false
  }
}

async function buyGoods(goods) {
  if (goods.stock <= 0) {
    ElMessage.warning('该商品已售罄')
    return
  }
  try {
    const { orderNo, amount } = await createGoodsOrder(goods.id, 1, 1)
    currentOrderNo.value = orderNo
    currentAmount.value = amount
    payVisible.value = true
  } catch (e) {
    // 错误提示已在请求拦截器处理
  }
}

function onPaid() {
  ElMessage.success('支付完成，可在「我的订单」查看')
  load()
}

onMounted(load)
</script>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 44px 48px;
  border-radius: 18px;
  color: #fff;
  background: linear-gradient(120deg, #ff9a44 0%, #ff5a00 55%, #d93b00 100%);
  position: relative;
  overflow: hidden;
}

.hero::after {
  content: '';
  position: absolute;
  right: -80px;
  top: -80px;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.hero h1 {
  font-size: 34px;
  margin: 0 0 10px;
  letter-spacing: 2px;
}

.hero p {
  margin: 0 0 24px;
  opacity: 0.85;
  font-size: 14px;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-badge {
  position: relative;
  z-index: 1;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.35);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.badge-num {
  font-size: 40px;
  font-weight: 800;
  line-height: 1;
}

.badge-label {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 8px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.quick-item {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.quick-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(31, 45, 61, 0.07);
}

.quick-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-icon :deep(svg) {
  width: 22px;
  height: 22px;
}

.quick-title {
  font-size: 15px;
  font-weight: 600;
}

.quick-desc {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 2px;
}

.activity-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.activity-card {
  background: linear-gradient(135deg, #fff5eb, #ffe7d6);
  border: 1px solid #ffdfc7;
  border-radius: 12px;
  padding: 18px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.activity-name {
  font-weight: 600;
  font-size: 15px;
}

.activity-time {
  font-size: 12px;
  color: var(--ink-3);
}

.course-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px;
}

.course-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 18px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-category {
  align-self: flex-start;
  font-size: 12px;
  color: var(--brand);
  background: var(--brand-light);
  border-radius: 10px;
  padding: 2px 10px;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
}

.course-desc {
  font-size: 12px;
  color: var(--ink-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

@media (max-width: 900px) {
  .hero {
    padding: 32px 24px;
  }
  .hero-badge {
    display: none;
  }
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
