<template>
  <div class="payment-page">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic1"><el-icon :size="26"><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">总交易金额</div>
            <div class="stat-value">¥ {{ Number(stats.totalAmount || 0).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic2"><el-icon :size="26"><Tickets /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">交易笔数</div>
            <div class="stat-value">{{ stats.totalCount ?? 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic3"><el-icon :size="26"><RefreshLeft /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">退款金额</div>
            <div class="stat-value">¥ {{ Number(stats.refundAmount || 0).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic4"><el-icon :size="26"><CircleCheckFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">成功率</div>
            <div class="stat-value">{{ stats.successRate ?? 0 }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="支付流水号">
          <el-input v-model="searchForm.paymentNo" placeholder="请输入流水号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.payType" placeholder="全部" clearable style="width: 140px">
            <el-option label="余额支付" :value="1" />
            <el-option label="支付宝" :value="2" />
            <el-option label="微信支付" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="支付成功" :value="1" />
            <el-option label="支付失败" :value="2" />
            <el-option label="已退款" :value="3" />
          </el-select>
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
          <span class="header-title">支付记录列表</span>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="paymentNo" label="支付流水号" min-width="200">
          <template #default="{ row }"><span class="mono">{{ row.paymentNo }}</span></template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单号" min-width="180">
          <template #default="{ row }">
            <span class="mono">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="140" show-overflow-tooltip />
        <el-table-column label="支付方式" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="payTypeColor(row.payType)" effect="light">{{ payTypeText(row.payType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="{ refund: row.status === 3 }">¥{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="160" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning" link icon="Refund"
              @click="refundPayment(row)"
            >退款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page.pageNum" v-model:page-size="page.pageSize"
        :page-sizes="[10,20,50,100]" :total="page.total"
        layout="total, sizes, prev, pager, next, jumper" background
        class="table-pagination" @size-change="loadData" @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="支付详情" width="560px">
      <div v-if="current" class="pd-wrap">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="支付流水号" :span="2"><span class="mono">{{ current.paymentNo }}</span></el-descriptions-item>
          <el-descriptions-item label="关联订单号" :span="2"><span class="mono">{{ current.orderNo }}</span></el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ current.userId }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payTypeText(current.payType) }}</el-descriptions-item>
          <el-descriptions-item label="支付金额"><span class="amount-lg">¥{{ Number(current.amount).toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="statusType(current.status)" effect="light">{{ statusText(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付时间" :span="2">{{ current.payTime }}</el-descriptions-item>
          <el-descriptions-item label="第三方流水号" :span="2" v-if="current.transactionId"><span class="mono">{{ current.transactionId }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPaymentList, getPaymentDetail, refundPayment as refundApi, getPaymentStatistics } from '@/api/payment'

const loading = ref(false)
const detailVisible = ref(false)
const current = ref(null)

const searchForm = reactive({ paymentNo: '', orderNo: '', payType: '', status: '' })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const stats = reactive({ totalAmount: 0, totalCount: 0, refundAmount: 0, successRate: 0 })

const payTypeMap = { BALANCE: '余额支付', ALIPAY: '支付宝', WECHAT: '微信支付', 1: '余额支付', 2: '支付宝', 3: '微信支付' }
function payTypeText(t) { return payTypeMap[t] || '-' }
function payTypeColor(t) { return { BALANCE: 'success', ALIPAY: 'primary', WECHAT: 'warning', 1: 'success', 2: 'primary', 3: 'warning' }[t] || 'info' }
function statusText(s) { return { 0: '待支付', 1: '支付成功', 2: '支付失败', 3: '已退款' }[s] || '-' }
function statusType(s) { return { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[s] || 'info' }

const tableData = ref([])
async function loadData() {
  loading.value = true
  try {
    const res = await getPaymentList({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      paymentNo: searchForm.paymentNo || undefined,
      orderNo: searchForm.orderNo || undefined,
      payType: searchForm.payType !== '' ? searchForm.payType : undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    tableData.value = res?.rows || []
    page.total = Number(res?.total) || 0
  } finally {
    loading.value = false
  }
}
async function loadStats() {
  try {
    const res = await getPaymentStatistics()
    Object.assign(stats, res || {})
    stats.totalCount = Number(stats.totalCount || 0)
  } catch (e) {}
}
function handleSearch() { page.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { paymentNo: '', orderNo: '', payType: '', status: '' }); handleSearch() }

async function openDetail(row) {
  detailVisible.value = true
  current.value = row
  try {
    current.value = await getPaymentDetail(row.paymentNo)
  } catch (e) {}
}
function refundPayment(row) {
  ElMessageBox.confirm(`确定对流水号「${row.paymentNo}」退款 ¥${Number(row.amount).toFixed(2)} 吗？`, '确认退款', { type: 'warning' })
    .then(async () => {
      await refundApi(row.paymentNo)
      ElMessage.success('退款成功')
      loadData()
      loadStats()
    })
    .catch(() => {})
}

onMounted(() => { loadData(); loadStats() })
</script>

<style scoped lang="scss">
.payment-page { :deep(.el-card) { margin-bottom: 16px; } }
.stat-row { margin-bottom: 8px; }
.stat-card { border: none; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; border-radius: 10px; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.ic1 { background: linear-gradient(135deg, #667eea, #764ba2); }
.ic2 { background: linear-gradient(135deg, #11998e, #38ef7d); }
.ic3 { background: linear-gradient(135deg, #f093fb, #f5576c); }
.ic4 { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 4px; }
.stat-value { font-size: 20px; font-weight: 700; color: #303133; }
.search-card :deep(.el-card__body) { padding: 16px 20px 0; }
.search-form { display: flex; flex-wrap: wrap; gap: 4px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 15px; font-weight: 600; color: #303133; }
.table-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.mono { font-family: Consolas, monospace; font-size: 12px; color: #409eff; }
.refund { color: #f56c6c; text-decoration: line-through; }
.amount-lg { font-size: 18px; font-weight: 700; color: #f56c6c; }
.pd-wrap { padding: 4px; }
</style>
