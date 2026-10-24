<template>
  <div class="page">
    <h1 class="page-title">商品</h1>
    <p class="page-subtitle">运动补给、健身装备与服饰</p>

    <div class="panel filters">
      <el-autocomplete
        v-model="keyword"
        :fetch-suggestions="fetchSuggestions"
        placeholder="搜索商品名称，如：蛋白"
        class="search-input"
        clearable
        @select="onSuggestSelect"
        @keyup.enter="search(1)"
      />
      <el-select v-model="categoryId" placeholder="全部分类" clearable class="filter-item" @change="search(1)">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="sort" class="filter-item" placeholder="默认排序" @change="search(1)">
        <el-option label="综合排序" value="_score" />
        <el-option label="销量优先" value="sales" />
        <el-option label="价格从低到高" value="priceAsc" />
        <el-option label="价格从高到低" value="priceDesc" />
      </el-select>
      <el-button type="primary" @click="search(1)">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <div v-loading="loading" class="result">
      <div v-if="goodsList.length" class="card-grid">
        <ProductCard v-for="goods in goodsList" :key="goods.id" :goods="goods" @buy="buyGoods" />
      </div>
      <el-empty v-else-if="!loading" description="没有找到符合条件的商品" />
    </div>

    <div class="pager">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        background
        @current-change="load"
      />
    </div>

    <PayDialog v-model="payVisible" :order-no="currentOrderNo" :amount="currentAmount" @success="onPaid" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/ProductCard.vue'
import PayDialog from '@/components/PayDialog.vue'
import { getGoodsPage, getCategories, searchGoods, suggestGoods } from '@/api'
import { createGoodsOrder } from '@/utils/buy'

const route = useRoute()

const keyword = ref(String(route.query.keyword || ''))
const categoryId = ref(null)
const sort = ref('_score')
const categories = ref([])

const loading = ref(false)
const goodsList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12

const payVisible = ref(false)
const currentOrderNo = ref('')
const currentAmount = ref(0)

function stripHighlight(text) {
  return String(text || '').replace(/<[^>]+>/g, '')
}

async function fetchSuggestions(queryString, cb) {
  if (!queryString) {
    cb([])
    return
  }
  try {
    const list = await suggestGoods(queryString)
    cb((list || []).map((value) => ({ value })))
  } catch (e) {
    cb([])
  }
}

function onSuggestSelect(item) {
  keyword.value = item.value
  search(1)
}

async function load() {
  loading.value = true
  try {
    const hasKeyword = !!keyword.value.trim()
    const hasPriceFilter = false
    if (hasKeyword || hasPriceFilter) {
      const page = await searchGoods({
        keyword: keyword.value.trim() || undefined,
        categoryId: categoryId.value || undefined,
        pageNum: pageNum.value,
        pageSize,
        sort: sort.value
      })
      goodsList.value = (page?.rows || []).map((item) => ({
        ...item,
        name: stripHighlight(item.name),
        description: stripHighlight(item.description)
      }))
      total.value = Number(page?.total) || 0
    } else {
      const page = await getGoodsPage({
        pageNum: pageNum.value,
        pageSize,
        categoryId: categoryId.value || undefined,
        status: 1
      })
      goodsList.value = page?.rows || []
      total.value = Number(page?.total) || 0
    }
  } catch (e) {
    goodsList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function search(page = 1) {
  pageNum.value = page
  load()
}

function reset() {
  keyword.value = ''
  categoryId.value = null
  sort.value = '_score'
  pageNum.value = 1
  load()
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

onMounted(async () => {
  try {
    const page = await getCategories()
    categories.value = page?.rows || []
  } catch (e) {
    categories.value = []
  }
  load()
})
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.search-input {
  width: 320px;
}

.filter-item {
  width: 150px;
}

.result {
  min-height: 200px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .search-input {
    width: 100%;
  }
  .filter-item {
    flex: 1;
    min-width: 120px;
  }
}
</style>
