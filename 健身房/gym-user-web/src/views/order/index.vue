<template>
  <div class="page">
    <h1 class="page-title">我的订单</h1>
    <p class="page-subtitle">查看订单状态，完成支付或取消未支付订单</p>

    <div v-loading="loading" class="order-list">
      <div v-for="order in orders" :key="order.orderNo" class="order panel">
        <div class="order-head">
          <div>
            <span class="order-no">订单号 {{ order.orderNo }}</span>
            <span class="muted create-time">{{ formatTime(order.createTime) }}</span>
          </div>
          <el-tag :type="statusInfo(order.status).type" effect="light">
            {{ statusInfo(order.status).text }}
          </el-tag>
        </div>

        <div class="order-body">
          <div class="order-info">
            <span class="amount">¥{{ Number(order.payAmount).toFixed(2) }}</span>
            <span class="muted">{{ order.deliveryInfo || '到店自提' }}</span>
          </div>
          <div class="order-actions">
            <el-button size="small" @click="showDetail(order)">查看明细</el-button>
            <el-button
              v-if="isUnpaid(order)"
              size="small"
              @click="cancel(order)"
            >
              取消订单
            </el-button>
            <el-button
              v-if="isUnpaid(order)"
              size="small"
              type="primary"
              @click="openPay(order)"
            >
              去支付
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !orders.length" description="还没有订单，去挑点好物吧" />
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

    <PayDialog v-model="payVisible" :order-no="currentOrderNo" :amount="currentAmount" @success="load" />

    <el-dialog v-model="detailVisible" title="订单明细" width="520px">
      <template v-if="detail">
        <div class="detail-line">
          <span class="muted">订单号</span>
          <span>{{ detail.order?.orderNo }}</span>
        </div>
        <div class="detail-line">
          <span class="muted">实付金额</span>
          <span class="price">{{ Number(detail.order?.payAmount || 0).toFixed(2) }}</span>
        </div>
        <el-table :data="detail.items || []" size="small" class="detail-table">
          <el-table-column prop="goodsName" label="商品" />
          <el-table-column prop="price" label="单价" width="90" />
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column prop="subtotal" label="小计" width="90" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PayDialog from '@/components/PayDialog.vue'
import { getMyOrders, getOrderDetail, cancelOrder } from '@/api'

const orders = ref([])
const loading = ref(true)
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

const payVisible = ref(false)
const currentOrderNo = ref('')
const currentAmount = ref(0)

const detailVisible = ref(false)
const detail = ref(null)

const statusMap = {
  UNPAID: { text: '未支付', type: 'warning' },
  PAID: { text: '已支付', type: 'success' },
  SHIPPED: { text: '已发货', type: 'primary' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
  REFUNDED: { text: '已退款', type: 'danger' },
  0: { text: '未支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
  5: { text: '已退款', type: 'danger' }
}

function statusInfo(status) {
  return statusMap[status] || { text: String(status ?? '-'), type: 'info' }
}

function isUnpaid(order) {
  return order.status === 'UNPAID' || order.status === 0
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

async function load() {
  loading.value = true
  try {
    const page = await getMyOrders({ pageNum: pageNum.value, pageSize })
    orders.value = page?.rows || []
    total.value = Number(page?.total) || 0
  } finally {
    loading.value = false
  }
}

async function openPay(order) {
  currentOrderNo.value = order.orderNo
  currentAmount.value = order.payAmount
  payVisible.value = true
}

async function cancel(order) {
  await ElMessageBox.confirm(`确认取消订单 ${order.orderNo} 吗？`, '取消订单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '不取消',
    type: 'warning'
  }).catch(() => Promise.reject('cancel'))

  try {
    await cancelOrder(order.orderNo)
    ElMessage.success('订单已取消')
    load()
  } catch (e) {
    // 错误提示已在请求拦截器处理
  }
}

async function showDetail(order) {
  detail.value = null
  detailVisible.value = true
  try {
    detail.value = await getOrderDetail(order.orderNo)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 160px;
}

.order {
  padding: 16px 20px;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--line);
}

.order-no {
  font-weight: 600;
  font-size: 14px;
}

.create-time {
  font-size: 12px;
  margin-left: 12px;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  gap: 12px;
  flex-wrap: wrap;
}

.order-info {
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.amount {
  color: var(--brand);
  font-size: 20px;
  font-weight: 700;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-line {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 6px 0;
}

.detail-table {
  margin-top: 12px;
}
</style>
