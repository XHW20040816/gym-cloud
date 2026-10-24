<template>
  <div class="payment-page">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic1"><el-icon :size="26"><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">总交易金额</div>
            <div class="stat-value">¥ 2,356,890.00</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic2"><el-icon :size="26"><Tickets /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">交易笔数</div>
            <div class="stat-value">12,586</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic3"><el-icon :size="26"><RefreshLeft /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">退款金额</div>
            <div class="stat-value">¥ 45,680.00</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon ic4"><el-icon :size="26"><CircleCheckFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">成功率</div>
            <div class="stat-value">98.6%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="支付流水号">
          <el-input v-model="searchForm.payNo" placeholder="请输入流水号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.payType" placeholder="全部" clearable style="width: 140px">
            <el-option label="微信支付" :value="1" />
            <el-option label="支付宝" :value="2" />
            <el-option label="银行卡" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="支付成功" :value="1" />
            <el-option label="支付失败" :value="2" />
            <el-option label="已退款" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" />
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
          <el-button icon="DataAnalysis" @click="ElMessage.info('统计报表（演示）')">统计报表</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="payNo" label="支付流水号" min-width="200">
          <template #default="{ row }"><span class="mono">{{ row.payNo }}</span></template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单号" min-width="180">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="ElMessage.info('跳转订单详情：' + row.orderNo)">
              {{ row.orderNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="付款人" width="110">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="26" :src="row.avatar">
                <el-icon :size="14"><User /></el-icon>
              </el-avatar>
              <span>{{ row.payer }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="payTypeColor(row.payType)" effect="light">
              <el-icon style="vertical-align: -2px; margin-right: 2px">
                <component :is="payTypeIcon(row.payType)" />
              </el-icon>
              {{ payTypeText(row.payType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="{ refund: row.status === 3 }">¥{{ Number(row.amount).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="手续费" width="100" align="right">
          <template #default="{ row }">¥{{ Number(row.fee).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="160" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
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
          <el-descriptions-item label="支付流水号" :span="2"><span class="mono">{{ current.payNo }}</span></el-descriptions-item>
          <el-descriptions-item label="关联订单号" :span="2"><span class="mono link" @click="ElMessage.info('跳转订单:' + current.orderNo)">{{ current.orderNo }}</span></el-descriptions-item>
          <el-descriptions-item label="付款人">{{ current.payer }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payTypeText(current.payType) }}</el-descriptions-item>
          <el-descriptions-item label="支付金额"><span class="amount-lg">¥{{ Number(current.amount).toFixed(2) }}</span></el-descriptions-item>
          <el-descriptions-item label="手续费">¥{{ Number(current.fee).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="statusType(current.status)" effect="light">{{ statusText(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="交易渠道">{{ channelText(current.payType) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" :span="2">{{ current.payTime }}</el-descriptions-item>
          <el-descriptions-item label="第三方流水号" :span="2" v-if="current.tradeNo"><span class="mono">{{ current.tradeNo }}</span></el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <el-dialog v-model="refundVisible" title="申请退款" width="460px">
      <el-form :model="refundForm" label-width="90px">
        <el-form-item label="原支付金额">¥{{ Number(current?.amount || 0).toFixed(2) }}</el-form-item>
        <el-form-item label="退款金额" required>
          <el-input-number v-model="refundForm.amount" :min="0.01" :max="Number(current?.amount || 0)" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="退款原因" required>
          <el-select v-model="refundForm.reason" style="width:100%" placeholder="请选择原因">
            <el-option label="用户申请退款" value="用户申请" />
            <el-option label="商家协商退款" value="商家协商" />
            <el-option label="支付异常" value="支付异常" />
            <el-option label="其他原因" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款备注">
          <el-input v-model="refundForm.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const detailVisible = ref(false)
const refundVisible = ref(false)
const current = ref(null)

const searchForm = reactive({ payNo: '', orderNo: '', payType: '', status: '', dateRange: [] })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const mockPays = []
const payers = ['张小明', '李雷', '韩梅梅', '王芳', '刘强', '陈静', '杨洋', '赵磊']
for (let i = 1; i <= 86; i++) {
  const t = 1 + (i % 3)
  const s = i % 23 === 0 ? 2 : (i % 17 === 0 ? 3 : 1)
  const amount = Number((39.9 + (i * 7) % 960).toFixed(2))
  const fee = Number((amount * 0.006).toFixed(2))
  const d = new Date(2025, 7, ((i - 1) % 28) + 1, 9 + ((i * 2) % 12), (i * 5) % 60, (i * 3) % 60)
  mockPays.push({
    id: i,
    payNo: 'PAY' + (202508000000000 + i * 13),
    orderNo: 'GYM' + String(2025080000000 + i),
    payer: payers[i % payers.length],
    avatar: '',
    payType: t,
    amount, fee,
    status: s,
    payTime: formatDT(d),
    tradeNo: (t === 1 ? 'wx' : t === 2 ? 'ali' : 'bk') + '202508' + String(i).padStart(10, '0')
  })
}
function formatDT(d) {
  const p = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds())
}
function payTypeText(t) { return { 1: '微信支付', 2: '支付宝', 3: '银行卡' }[t] }
function payTypeColor(t) { return { 1: 'success', 2: 'primary', 3: 'warning' }[t] }
function payTypeIcon(t) { return { 1: 'ChatDotRound', 2: 'Aim', 3: 'CreditCard' }[t] }
function channelText(t) { return { 1: '微信官方支付', 2: '蚂蚁集团支付宝', 3: '银联在线支付' }[t] }
function statusText(s) { return { 1: '支付成功', 2: '支付失败', 3: '已退款' }[s] }
function statusType(s) { return { 1: 'success', 2: 'danger', 3: 'warning' }[s] }

const tableData = ref([])
function loadData() {
  loading.value = true
  setTimeout(() => {
    let list = mockPays.filter((p) => {
      if (searchForm.payNo && !p.payNo.includes(searchForm.payNo)) return false
      if (searchForm.orderNo && !p.orderNo.includes(searchForm.orderNo)) return false
      if (searchForm.payType !== '' && p.payType !== Number(searchForm.payType)) return false
      if (searchForm.status !== '' && p.status !== Number(searchForm.status)) return false
      return true
    })
    page.total = list.length
    const s = (page.pageNum - 1) * page.pageSize
    tableData.value = list.slice(s, s + page.pageSize)
    loading.value = false
  }, 300)
}
function handleSearch() { page.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { payNo: '', orderNo: '', payType: '', status: '', dateRange: [] }); handleSearch() }

function openDetail(row) { current.value = row; detailVisible.value = true }

const refundForm = reactive({ amount: 0, reason: '', remark: '' })
function refundPayment(row) {
  current.value = row
  refundForm.amount = row.amount
  refundForm.reason = ''
  refundForm.remark = ''
  refundVisible.value = true
}
function confirmRefund() {
  if (!refundForm.amount || refundForm.amount <= 0) { ElMessage.warning('请输入退款金额'); return }
  if (!refundForm.reason) { ElMessage.warning('请选择退款原因'); return }
  ElMessageBox.confirm(`确定退款 ¥${refundForm.amount.toFixed(2)} 吗？`, '确认退款', { type: 'warning' })
    .then(() => {
      current.value.status = 3
      ElMessage.success('退款申请已提交')
      refundVisible.value = false
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
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
.link { cursor: pointer; }
.user-cell { display: flex; align-items: center; gap: 6px; }
.refund { color: #f56c6c; text-decoration: line-through; }
.amount-lg { font-size: 18px; font-weight: 700; color: #f56c6c; }
</style>
