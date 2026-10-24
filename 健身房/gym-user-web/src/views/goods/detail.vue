<template>
  <div class="page">
    <el-button link @click="$router.back()">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <div v-loading="loading" class="detail panel">
      <template v-if="goods">
        <div class="cover" :style="{ background: coverStyle }">
          <span>{{ goods.name?.slice(0, 1) }}</span>
        </div>

        <div class="info">
          <h1 class="name">{{ goods.name }}</h1>
          <p class="desc">{{ goods.description }}</p>

          <div class="price-box">
            <span class="price">{{ Number(goods.price).toFixed(2) }}</span>
            <span class="muted">已售 {{ goods.sales || 0 }} 件</span>
          </div>

          <div class="row">
            <span class="label">库存</span>
            <span>{{ goods.stock ?? 0 }} 件</span>
          </div>

          <div class="row">
            <span class="label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(goods.stock || 1, 1)" />
            <span class="total muted">小计 ¥{{ subtotal }}</span>
          </div>

          <div class="buy-actions">
            <el-button
              type="primary"
              size="large"
              :disabled="goods.stock <= 0"
              @click="buy"
            >
              {{ goods.stock > 0 ? '立即购买' : '已售罄' }}
            </el-button>
          </div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="商品不存在" />
    </div>

    <PayDialog v-model="payVisible" :order-no="currentOrderNo" :amount="currentAmount" @success="onPaid" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PayDialog from '@/components/PayDialog.vue'
import { getGoodsById } from '@/api'
import { createGoodsOrder } from '@/utils/buy'

const route = useRoute()
const goods = ref(null)
const loading = ref(true)
const quantity = ref(1)

const payVisible = ref(false)
const currentOrderNo = ref('')
const currentAmount = ref(0)

const palettes = [
  'linear-gradient(135deg, #ffd3a5, #fd6585)',
  'linear-gradient(135deg, #a8edea, #fed6e3)',
  'linear-gradient(135deg, #fbc2eb, #a6c1ee)',
  'linear-gradient(135deg, #ffecd2, #fcb69f)',
  'linear-gradient(135deg, #d4fc79, #96e6a1)',
  'linear-gradient(135deg, #c2e9fb, #a1c4fd)',
  'linear-gradient(135deg, #fddb92, #d1fdff)'
]

const coverStyle = computed(() => palettes[Number(goods.value?.id || 0) % palettes.length])
const subtotal = computed(() => (Number(goods.value?.price || 0) * quantity.value).toFixed(2))

async function load() {
  loading.value = true
  try {
    goods.value = await getGoodsById(route.params.id)
  } finally {
    loading.value = false
  }
}

async function buy() {
  try {
    const { orderNo, amount } = await createGoodsOrder(goods.value.id, quantity.value, 1)
    currentOrderNo.value = orderNo
    currentAmount.value = amount
    payVisible.value = true
  } catch (e) {
    // 错误提示已在请求拦截器处理
  }
}

function onPaid() {
  ElMessage.success('支付完成')
  load()
}

onMounted(load)
</script>

<style scoped>
.detail {
  margin-top: 16px;
  display: flex;
  gap: 36px;
  min-height: 320px;
}

.cover {
  width: 340px;
  height: 340px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cover span {
  font-size: 110px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.9);
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.name {
  font-size: 26px;
  margin: 0;
}

.desc {
  color: var(--ink-2);
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
}

.price-box {
  display: flex;
  align-items: baseline;
  gap: 16px;
  background: var(--brand-light);
  border-radius: 10px;
  padding: 14px 18px;
}

.price-box .price {
  font-size: 32px;
}

.row {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
}

.label {
  color: var(--ink-3);
  width: 48px;
}

.buy-actions {
  margin-top: auto;
}

@media (max-width: 800px) {
  .detail {
    flex-direction: column;
    gap: 20px;
  }
  .cover {
    width: 100%;
    height: 220px;
  }
}
</style>
