<template>
  <div class="order-page">
    <el-card shadow="never" class="tabs-card">
      <el-tabs v-model="activeStatus" class="order-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="">
          <template #label><span class="tab-label"><el-icon><Tickets /></el-icon>全部</span></template>
        </el-tab-pane>
        <el-tab-pane label="待支付" name="0">
          <template #label><span class="tab-label"><el-icon><Clock /></el-icon>待支付</span></template>
        </el-tab-pane>
        <el-tab-pane label="已支付" name="1">
          <template #label><span class="tab-label"><el-icon><Wallet /></el-icon>已支付</span></template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="3">
          <template #label><span class="tab-label"><el-icon><CircleCheck /></el-icon>已完成</span></template>
        </el-tab-pane>
        <el-tab-pane label="已取消" name="4">
          <template #label><span class="tab-label"><el-icon><CircleClose /></el-icon>已取消</span></template>
        </el-tab-pane>
      </el-tabs>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable prefix-icon="Tickets" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="table-header">
          <span class="header-title">订单列表（共 {{ page.total }} 条）</span>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="200">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="下单用户" width="130">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="28" :src="row.avatar">
                <el-icon :size="16"><User /></el-icon>
              </el-avatar>
              <span>{{ row.nickname || row.userId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="商品信息" min-width="180">
          <template #default="{ row }">
            <div class="goods-cell">
              <el-image :src="row.goodsCover || placeholder" fit="cover" style="width:40px;height:40px;border-radius:4px;margin-right:8px" />
              <div>
                <div class="goods-name">{{ row.goodsName || '-' }}</div>
                <div class="goods-qty" v-if="row.quantity">x{{ row.quantity }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="110" align="right">
          <template #default="{ row }"><span class="amount">¥{{ Number(row.payAmount ?? row.totalAmount).toFixed(2) }}</span></template>
        </el-table-column>
        <el-table-column label="支付方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.payType" size="small" effect="plain" :type="payTypeColor(row.payType)">
              {{ payTypeText(row.payType) }}
            </el-tag>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="openDetail(row)">详情</el-button>
            <el-button v-if="isUnpaid(row)" type="warning" link icon="CircleClose" @click="cancelOrder(row)">取消</el-button>
            <el-button v-if="isPaid(row)" type="success" link icon="CircleCheck" @click="finishOrder(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.pageNum" v-model:page-size="page.pageSize"
        :page-sizes="[10,20,50]" :total="page.total"
        layout="total, sizes, prev, pager, next, jumper" background
        class="table-pagination" @size-change="loadData" @current-change="loadData"
      />
    </el-card>

    <!-- 订单详情 Drawer -->
    <el-drawer v-model="drawerVisible" :size="560" title="订单详情" destroy-on-close>
      <div v-if="currentOrder" class="detail-wrap">
        <el-descriptions :column="2" border class="mb-16" title="基础信息">
          <el-descriptions-item label="订单号" :span="2">
            <span class="order-no">{{ currentOrder.orderNo }}</span>
            <el-button type="primary" link icon="DocumentCopy" @click="copyNo">复制</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="下单用户">{{ currentOrder.nickname || currentOrder.userId }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusType(currentOrder.status)" effect="light">{{ statusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payTypeText(currentOrder.payType) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="currentOrder.payTime" :span="2">{{ currentOrder.payTime }}</el-descriptions-item>
          <el-descriptions-item label="订单备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="1" border class="mb-16" title="商品信息">
          <el-descriptions-item label="商品">
            <div v-for="(it, idx) in detailItems" :key="idx" class="goods-cell" style="margin-bottom: 8px">
              <el-image :src="it.goodsCover || placeholder" fit="cover" style="width:48px;height:48px;border-radius:6px;margin-right:12px" />
              <div>
                <div class="goods-name-lg">{{ it.goodsName }}</div>
                <div class="muted">单价：¥{{ Number(it.price).toFixed(2) }} × {{ it.quantity }}</div>
              </div>
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="2" border title="金额信息">
          <el-descriptions-item label="商品总额">¥{{ Number(currentOrder.totalAmount ?? 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span class="amount-lg">¥{{ Number(currentOrder.payAmount ?? 0).toFixed(2) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="drawer-footer">
          <el-button v-if="isUnpaid(currentOrder)" type="warning" @click="cancelOrder(currentOrder)">取消订单</el-button>
          <el-button v-if="isPaid(currentOrder)" type="success" @click="finishOrder(currentOrder)">确认完成</el-button>
          <el-button type="primary" @click="drawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, cancelOrder as cancelOrderApi, updateOrderStatus } from '@/api/order'

const loading = ref(false)
const activeStatus = ref('')
const placeholder = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

const searchForm = reactive({ orderNo: '' })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const tableData = ref([])
const drawerVisible = ref(false)
const currentOrder = ref(null)
const detailItems = ref([])

const statusMap = {
  UNPAID: { text: '待支付', type: 'warning' },
  PAID: { text: '已支付', type: 'success' },
  SHIPPED: { text: '已发货', type: 'primary' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
  REFUNDED: { text: '已退款', type: 'danger' },
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
  5: { text: '已退款', type: 'danger' }
}
function statusText(s) { return statusMap[s]?.text || String(s ?? '-') }
function statusType(s) { return statusMap[s]?.type || 'info' }
function isUnpaid(row) { return row.status === 'UNPAID' || row.status === 0 }
function isPaid(row) { return row.status === 'PAID' || row.status === 1 }

const payTypeMap = { BALANCE: '余额支付', ALIPAY: '支付宝', WECHAT: '微信支付', 1: '余额支付', 2: '支付宝', 3: '微信支付' }
function payTypeText(t) { return payTypeMap[t] || '' }
function payTypeColor(t) { return { BALANCE: 'success', ALIPAY: 'primary', WECHAT: 'warning', 1: 'success', 2: 'primary', 3: 'warning' }[t] || 'info' }

async function loadData() {
  loading.value = true
  try {
    const res = await getOrderList({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      status: activeStatus.value !== '' ? activeStatus.value : undefined,
      orderNo: searchForm.orderNo || undefined
    })
    tableData.value = res?.rows || []
    page.total = Number(res?.total) || 0
  } finally {
    loading.value = false
  }
}
function handleSearch() { page.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { orderNo: '' }); handleSearch() }
function handleTabChange() { page.pageNum = 1; loadData() }

async function openDetail(row) {
  drawerVisible.value = true
  currentOrder.value = row
  detailItems.value = []
  try {
    const res = await getOrderDetail(row.orderNo)
    if (res?.order) currentOrder.value = { ...row, ...res.order }
    detailItems.value = res?.items || []
  } catch (e) {}
}
function copyNo() {
  navigator.clipboard?.writeText(currentOrder.value.orderNo)
  ElMessage.success('订单号已复制')
}
function cancelOrder(row) {
  ElMessageBox.confirm(`确定取消订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await cancelOrderApi(row.orderNo)
      ElMessage.success('订单已取消')
      drawerVisible.value = false
      loadData()
    })
    .catch(() => {})
}
function finishOrder(row) {
  ElMessageBox.confirm(`确认订单「${row.orderNo}」已完成吗？`, '提示', { type: 'success' })
    .then(async () => {
      await updateOrderStatus(row.orderNo, 3)
      ElMessage.success('订单已完成')
      drawerVisible.value = false
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.order-page { :deep(.el-card) { margin-bottom: 16px; } }
.tabs-card :deep(.el-card__body) { padding: 12px 20px 0; }
.order-tabs { margin-bottom: 16px; }
.tab-label { display: inline-flex; align-items: center; gap: 4px; }
.search-form { display: flex; flex-wrap: wrap; gap: 4px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 15px; font-weight: 600; color: #303133; }
.table-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.order-no { font-family: Consolas, monospace; color: #409eff; }
.amount { color: #f56c6c; font-weight: 600; }
.amount-lg { color: #f56c6c; font-weight: 700; font-size: 18px; }
.muted { color: #909399; }
.user-cell { display: flex; align-items: center; gap: 8px; }
.goods-cell { display: flex; align-items: center; }
.goods-name { font-size: 13px; color: #303133; }
.goods-name-lg { font-size: 15px; font-weight: 500; color: #303133; margin-bottom: 4px; }
.goods-qty { font-size: 12px; color: #909399; }
.mb-16 { margin-bottom: 16px; }
.detail-wrap { padding-bottom: 60px; }
.drawer-footer { position: absolute; bottom: 0; left: 0; right: 0; padding: 12px 16px; background: #fff; border-top: 1px solid #ebeef5; display: flex; justify-content: flex-end; gap: 8px; }
</style>
