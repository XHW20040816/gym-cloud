<template>
  <div class="page">
    <h1 class="page-title">限时秒杀</h1>
    <p class="page-subtitle">库存有限，先到先得；下单后请尽快完成支付</p>

    <div v-loading="loadingActivities" class="activity-list">
      <div
        v-for="activity in activities"
        :key="activity.id"
        class="activity"
        :class="{ active: activity.id === selectedId }"
        @click="selectActivity(activity)"
      >
        <div class="activity-name">{{ activity.name }}</div>
        <div class="activity-time">{{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}</div>
        <el-tag size="small" type="danger" effect="plain">限购 {{ activity.limitCount }} 件</el-tag>
      </div>
    </div>
    <el-empty v-if="!loadingActivities && !activities.length" description="暂无秒杀活动" />

    <div v-loading="loadingGoods" class="goods-wrap">
      <div v-if="seckillGoods.length" class="grid">
        <div v-for="item in seckillGoods" :key="item.id" class="goods-card">
          <div class="cover" :style="{ background: palette(item.goodsId) }">
            {{ item.goodsName.slice(0, 1) }}
          </div>
          <div class="goods-info">
            <div class="goods-name">{{ item.goodsName }}</div>
            <div class="price-line">
              <span class="price">{{ Number(item.seckillPrice).toFixed(2) }}</span>
              <span v-if="item.originalPrice" class="origin">¥{{ Number(item.originalPrice).toFixed(2) }}</span>
            </div>
            <el-progress
              :percentage="progress(item)"
              :stroke-width="10"
              :show-text="false"
              status="exception"
            />
            <div class="stock-line">
              <span class="muted">已抢 {{ item.sold || 0 }} / {{ (item.sold || 0) + (item.stock || 0) }}</span>
            </div>
            <el-button
              type="danger"
              :disabled="item.stock <= 0"
              :loading="buyingId === item.id"
              @click="seckill(item)"
            >
              {{ item.stock > 0 ? '立即秒杀' : '已抢光' }}
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!loadingGoods" description="该活动暂无秒杀商品" />
    </div>

    <el-alert
      v-if="lastOrderNo"
      class="result"
      type="success"
      :closable="false"
      show-icon
      :title="`秒杀成功，订单号 ${lastOrderNo}`"
      description="请到「我的订单」完成支付，超时未支付订单会自动取消并回补库存。"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getSeckillActivities,
  getSeckillGoods,
  getGoodsById,
  doSeckill,
  getSeckillResult
} from '@/api'

const activities = ref([])
const selectedId = ref(null)
const seckillGoods = ref([])
const loadingActivities = ref(true)
const loadingGoods = ref(false)
const buyingId = ref(null)
const lastOrderNo = ref('')

const palettes = [
  'linear-gradient(135deg, #ffd3a5, #fd6585)',
  'linear-gradient(135deg, #a8edea, #fed6e3)',
  'linear-gradient(135deg, #fbc2eb, #a6c1ee)',
  'linear-gradient(135deg, #d4fc79, #96e6a1)'
]

function palette(id) {
  return palettes[Number(id || 0) % palettes.length]
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(5, 16)
}

function progress(item) {
  const total = (item.sold || 0) + (item.stock || 0)
  return total > 0 ? Math.round(((item.sold || 0) / total) * 100) : 0
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

async function loadActivities() {
  loadingActivities.value = true
  try {
    activities.value = (await getSeckillActivities()) || []
    if (activities.value.length) {
      selectedId.value = activities.value[0].id
      await loadGoods()
    }
  } finally {
    loadingActivities.value = false
  }
}

async function selectActivity(activity) {
  if (selectedId.value === activity.id) return
  selectedId.value = activity.id
  await loadGoods()
}

async function loadGoods() {
  loadingGoods.value = true
  try {
    const list = (await getSeckillGoods(selectedId.value)) || []
    seckillGoods.value = await Promise.all(
      list.map(async (item) => {
        let goods = null
        try {
          goods = await getGoodsById(item.goodsId)
        } catch (e) {
          goods = null
        }
        return {
          ...item,
          goodsName: goods?.name || `商品 #${item.goodsId}`,
          originalPrice: goods?.price
        }
      })
    )
  } finally {
    loadingGoods.value = false
  }
}

async function seckill(item) {
  buyingId.value = item.id
  try {
    await doSeckill(item.id)
    // 异步创建订单，轮询结果
    let success = false
    for (let i = 0; i < 6; i++) {
      await sleep(800)
      const result = await getSeckillResult(item.id)
      if (result?.success && result.order) {
        success = true
        lastOrderNo.value = result.order.orderNo
        ElMessage.success('秒杀成功')
        break
      }
      if (result?.reason && String(result.reason).includes('售罄')) {
        ElMessage.warning(result.reason)
        break
      }
    }
    if (!success && !lastOrderNo.value) {
      ElMessage.warning('排队中，请稍后到「我的订单」查看结果')
    }
    await loadGoods()
  } catch (e) {
    // 错误提示已在请求拦截器处理
  } finally {
    buyingId.value = null
  }
}

onMounted(loadActivities)
</script>

<style scoped>
.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}

.activity {
  background: #fff;
  border: 2px solid var(--line);
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
  transition: all 0.2s;
}

.activity.active {
  border-color: var(--brand);
  background: linear-gradient(135deg, #fff9f4, #fff);
}

.activity-name {
  font-weight: 600;
}

.activity-time {
  font-size: 12px;
  color: var(--ink-3);
}

.goods-wrap {
  min-height: 160px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.goods-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  overflow: hidden;
}

.cover {
  height: 130px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 44px;
  font-weight: 800;
}

.goods-info {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goods-name {
  font-weight: 600;
  font-size: 15px;
}

.price-line {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price {
  font-size: 24px;
}

.origin {
  color: var(--ink-3);
  text-decoration: line-through;
  font-size: 13px;
}

.stock-line {
  font-size: 12px;
}

.result {
  margin-top: 24px;
}
</style>
