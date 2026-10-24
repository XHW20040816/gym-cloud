<template>
  <div class="order-page">
    <el-card shadow="never" class="tabs-card">
      <el-tabs v-model="activeStatus" class="order-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="">
          <template #label><span class="tab-label"><el-icon><Tickets /></el-icon>全部<el-badge v-if="counts.total" :value="counts.total" class="tab-badge" :max="999" /></span></template>
        </el-tab-pane>
        <el-tab-pane label="待支付" name="0">
          <template #label><span class="tab-label"><el-icon><Clock /></el-icon>待支付<el-badge v-if="counts.pending" :value="counts.pending" class="tab-badge" type="warning" :max="999" /></span></template>
        </el-tab-pane>
        <el-tab-pane label="已支付" name="1">
          <template #label><span class="tab-label"><el-icon><Wallet /></el-icon>已支付<el-badge v-if="counts.paid" :value="counts.paid" class="tab-badge" type="success" :max="999" /></span></template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="2">
          <template #label><span class="tab-label"><el-icon><CircleCheck /></el-icon>已完成<el-badge v-if="counts.done" :value="counts.done" class="tab-badge" type="info" :max="999" /></span></template>
        </el-tab-pane>
        <el-tab-pane label="已取消" name="3">
          <template #label><span class="tab-label"><el-icon><CircleClose /></el-icon>已取消<el-badge v-if="counts.canceled" :value="counts.canceled" class="tab-badge" type="danger" :max="999" /></span></template>
        </el-tab-pane>
      </el-tabs>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable prefix-icon="Tickets" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="下单用户">
          <el-input v-model="searchForm.username" placeholder="用户名/昵称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="手机号" clearable prefix-icon="Phone" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="下单时间">
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
          <span class="header-title">订单列表（共 {{ page.total }} 条）</span>
          <div>
            <el-button icon="Download" @click="ElMessage.success('导出订单（演示）')">导出订单</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%" @row-click="openDetail">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="orderNo" label="订单号" min-width="180">
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
              <span>{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="商品信息" min-width="180">
          <template #default="{ row }">
            <div class="goods-cell">
              <el-image :src="row.goodsCover || placeholder" fit="cover" style="width:40px;height:40px;border-radius:4px;margin-right:8px" />
              <div>
                <div class="goods-name">{{ row.goodsName }}</div>
                <div class="goods-qty">x{{ row.quantity }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="110" align="right">
          <template #default="{ row }"><span class="amount">¥{{ Number(row.amount).toFixed(2) }}</span></template>
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
            <el-button type="primary" link icon="View" @click.stop="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="warning" link icon="CircleClose" @click.stop="cancelOrder(row)">取消</el-button>
            <el-button v-if="row.status === 1" type="success" link icon="CircleCheck" @click.stop="finishOrder(row)">完成</el-button>
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
          <el-descriptions-item label="下单用户">{{ currentOrder.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusType(currentOrder.status)" effect="light">{{ statusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ payTypeText(currentOrder.payType) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="currentOrder.payTime" :span="2">{{ currentOrder.payTime }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="1" border class="mb-16" title="商品信息">
          <el-descriptions-item label="商品">
            <div class="goods-cell">
              <el-image :src="currentOrder.goodsCover || placeholder" fit="cover" style="width:60px;height:60px;border-radius:6px;margin-right:12px" />
              <div>
                <div class="goods-name-lg">{{ currentOrder.goodsName }}</div>
                <div class="muted">单价：¥{{ Number(currentOrder.price).toFixed(2) }} × {{ currentOrder.quantity }}</div>
              </div>
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="2" border class="mb-16" title="金额信息">
          <el-descriptions-item label="商品金额">¥{{ Number(currentOrder.amount).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">-¥{{ Number(currentOrder.discount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="运费">¥{{ Number(currentOrder.shipping || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span class="amount-lg">¥{{ Number(currentOrder.amount).toFixed(2) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="1" border title="收货信息">
          <el-descriptions-item label="收货人">{{ currentOrder.receiver || '张小明' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone || currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ currentOrder.address || '北京市朝阳区建国路88号' }}</el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="drawer-footer">
          <el-button v-if="currentOrder.status === 0" type="warning" @click="cancelOrder(currentOrder)">取消订单</el-button>
          <el-button v-if="currentOrder.status === 1" type="success" @click="finishOrder(currentOrder)">确认完成</el-button>
          <el-button type="primary" @click="drawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const activeStatus = ref('')
const placeholder = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

const searchForm = reactive({ orderNo: '', username: '', phone: '', dateRange: [] })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const goodsList = ['乳清蛋白粉 5磅', '肌酸粉 300g', '瑜伽垫 6mm', '专业跑步鞋', '哑铃套装 20kg', '蛋白棒 12支装', '筋膜枪', 'BCAA支链氨基酸']
const userList = ['张小明', '李雷', '韩梅梅', '王芳', '刘强', '陈静', '杨洋', '赵磊']
const phones = ['13812345678', '13923456789', '13634567890', '13745678901', '13556789012', '13667890123', '13878901234', '13989012345']

const mockOrders = []
for (let i = 1; i <= 68; i++) {
  const d = new Date(2025, 7, ((i - 1) % 28) + 1, 9 + ((i * 3) % 10), (i * 7) % 60, (i * 11) % 60)
  const p = d.getTime() + 10 * 60 * 1000
  const status = i % 5
  mockOrders.push({
    id: i,
    orderNo: 'GYM' + String(2025080000000 + i),
    nickname: userList[i % userList.length],
    avatar: '',
    phone: phones[i % phones.length],
    goodsName: goodsList[i % goodsList.length],
    goodsCover: '',
    quantity: 1 + (i % 3),
    price: Number((49.9 + (i % 10) * 30).toFixed(2)),
    amount: Number(((49.9 + (i % 10) * 30) * (1 + (i % 3))).toFixed(2)),
    discount: Number((Math.random() * 30).toFixed(2)),
    shipping: i % 4 === 0 ? 0 : 10,
    payType: status === 0 ? null : (i % 3 + 1),
    status,
    createTime: formatDT(d),
    payTime: status !== 0 ? formatDT(new Date(p)) : null,
    remark: i % 7 === 0 ? '希望尽快发货' : ''
  })
}
function formatDT(d) {
  const p = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds())
}

const tableData = ref([])
const counts = computed(() => ({
  total: mockOrders.length,
  pending: mockOrders.filter((o) => o.status === 0).length,
  paid: mockOrders.filter((o) => o.status === 1).length,
  done: mockOrders.filter((o) => o.status === 2).length,
  canceled: mockOrders.filter((o) => o.status === 3).length
}))

function statusText(s) { return ['待支付', '已支付', '已完成', '已取消', '已退款'][s] || '-' }
function statusType(s) { return ['warning', 'primary', 'success', 'info', 'danger'][s] || 'info' }
function payTypeText(t) { return { 1: '微信支付', 2: '支付宝', 3: '银行卡' }[t] }
function payTypeColor(t) { return { 1: 'success', 2: 'primary', 3: 'warning' }[t] }

function loadData() {
  loading.value = true
  setTimeout(() => {
    let list = mockOrders.filter((o) => {
      if (activeStatus.value !== '' && String(o.status) !== activeStatus.value) return false
      if (searchForm.orderNo && !o.orderNo.includes(searchForm.orderNo)) return false
      if (searchForm.username && !o.nickname.includes(searchForm.username)) return false
      if (searchForm.phone && !o.phone.includes(searchForm.phone)) return false
      return true
    })
    page.total = list.length
    const s = (page.pageNum - 1) * page.pageSize
    tableData.value = list.slice(s, s + page.pageSize)
    loading.value = false
  }, 300)
}
function handleSearch() { page.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { orderNo: '', username: '', phone: '', dateRange: [] }); handleSearch() }
function handleTabChange() { page.pageNum = 1; loadData() }

const drawerVisible = ref(false)
const currentOrder = ref(null)
function openDetail(row) {
  currentOrder.value = row
  drawerVisible.value = true
}
function copyNo() {
  navigator.clipboard?.writeText(currentOrder.value.orderNo)
  ElMessage.success('订单号已复制')
}
function cancelOrder(row) {
  ElMessageBox.confirm(`确定取消订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
    .then(() => { row.status = 3; ElMessage.success('订单已取消'); loadData() })
    .catch(() => {})
}
function finishOrder(row) {
  ElMessageBox.confirm(`确认订单「${row.orderNo}」已完成吗？`, '提示', { type: 'success' })
    .then(() => { row.status = 2; ElMessage.success('订单已完成'); loadData() })
    .catch(() => {})
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.order-page { :deep(.el-card) { margin-bottom: 16px; } }
.tabs-card :deep(.el-card__body) { padding: 12px 20px 0; }
.order-tabs { margin-bottom: 16px; }
.tab-label { display: inline-flex; align-items: center; gap: 4px; }
.tab-badge { margin-left: 6px; }
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
