<template>
  <div class="page">
    <h1 class="page-title">健身餐</h1>
    <p class="page-subtitle">低脂高蛋白，训练后补充能量</p>

    <el-alert
      class="tip"
      type="info"
      :closable="false"
      show-icon
      title="健身餐支持线上下单"
      description="下单并完成支付后，凭订单号到店取餐。"
    />

    <div v-loading="loading" class="card-grid">
      <div v-for="food in foods" :key="food.id" class="food-card">
        <div class="cover">
          <span>{{ food.name?.slice(0, 1) }}</span>
        </div>
        <div class="info">
          <div class="name">{{ food.name }}</div>
          <div class="desc">{{ food.description }}</div>
          <div class="macros">
            <span>{{ food.calories }} kcal</span>
            <span>蛋白 {{ food.protein }}g</span>
            <span>碳水 {{ food.carbs }}g</span>
            <span>脂肪 {{ food.fat }}g</span>
          </div>
          <div class="buy-row">
            <span class="price">{{ Number(food.price).toFixed(2) }}</span>
            <el-input-number v-model="quantity[food.id]" :min="1" :max="20" size="small" />
            <el-button type="primary" size="small" :loading="buyingId === food.id" @click="buy(food)">
              立即购买
            </el-button>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && !foods.length" description="暂无健身餐" />

    <PayDialog v-model="payVisible" :order-no="currentOrderNo" :amount="currentAmount" @success="onPaid" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PayDialog from '@/components/PayDialog.vue'
import { getFoods } from '@/api'
import { createGoodsOrder } from '@/utils/buy'

const foods = ref([])
const loading = ref(true)
const quantity = reactive({})
const buyingId = ref(null)

const payVisible = ref(false)
const currentOrderNo = ref('')
const currentAmount = ref(0)

async function buy(food) {
  buyingId.value = food.id
  try {
    const { orderNo, amount } = await createGoodsOrder(
      food.id,
      quantity[food.id] || 1,
      3,
      '到店取餐'
    )
    currentOrderNo.value = orderNo
    currentAmount.value = amount
    payVisible.value = true
  } catch (e) {
    // 错误提示已在请求拦截器处理
  } finally {
    buyingId.value = null
  }
}

function onPaid() {
  ElMessage.success('支付完成，凭订单号到店取餐')
}

onMounted(async () => {
  try {
    foods.value = (await getFoods()) || []
    foods.value.forEach((food) => {
      quantity[food.id] = 1
    })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.tip {
  margin-bottom: 20px;
}

.food-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  overflow: hidden;
}

.cover {
  height: 130px;
  background: linear-gradient(135deg, #d4fc79, #96e6a1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.92);
  font-size: 44px;
  font-weight: 800;
}

.info {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name {
  font-weight: 600;
  font-size: 15px;
}

.desc {
  font-size: 12px;
  color: var(--ink-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.macros {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.macros span {
  font-size: 11px;
  color: var(--ink-2);
  background: var(--bg);
  border-radius: 8px;
  padding: 2px 8px;
}

.buy-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.buy-row .price {
  font-size: 20px;
}
</style>
