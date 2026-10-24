<template>
  <div class="product-card" @click="goDetail">
    <div class="cover" :style="{ background: coverStyle }">
      <span class="cover-char">{{ goods.name?.slice(0, 1) || 'G' }}</span>
      <span v-if="goods.stock <= 0" class="soldout">已售罄</span>
    </div>
    <div class="info">
      <div class="name">{{ goods.name }}</div>
      <div class="desc">{{ goods.description }}</div>
      <div class="meta">
        <span class="price">{{ Number(goods.price).toFixed(2) }}</span>
        <span class="sales">已售 {{ goods.sales || 0 }}</span>
      </div>
      <div class="actions">
        <el-button size="small" plain @click.stop="$emit('buy', goods)">立即购买</el-button>
        <span class="stock">库存 {{ goods.stock ?? 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  goods: { type: Object, required: true }
})
defineEmits(['buy'])

const router = useRouter()

const palettes = [
  'linear-gradient(135deg, #ffd3a5, #fd6585)',
  'linear-gradient(135deg, #a8edea, #fed6e3)',
  'linear-gradient(135deg, #fbc2eb, #a6c1ee)',
  'linear-gradient(135deg, #ffecd2, #fcb69f)',
  'linear-gradient(135deg, #d4fc79, #96e6a1)',
  'linear-gradient(135deg, #c2e9fb, #a1c4fd)',
  'linear-gradient(135deg, #fddb92, #d1fdff)'
]

const coverStyle = computed(() => palettes[Number(props.goods.id || 0) % palettes.length])

function goDetail() {
  router.push(`/goods/${props.goods.id}`)
}
</script>

<style scoped>
.product-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 26px rgba(31, 45, 61, 0.08);
}

.cover {
  position: relative;
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-char {
  font-size: 46px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.soldout {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.info {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.name {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.desc {
  font-size: 12px;
  color: var(--ink-3);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-top: auto;
}

.price {
  font-size: 20px;
}

.sales {
  font-size: 12px;
  color: var(--ink-3);
}

.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.stock {
  font-size: 12px;
  color: var(--ink-3);
}
</style>
