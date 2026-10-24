<template>
  <div class="seckill-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.name" placeholder="请输入活动名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="关联商品">
          <el-select v-model="searchForm.goodsId" placeholder="全部商品" clearable style="width:180px" filterable>
            <el-option v-for="g in goodsList" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="未开始" value="0" />
            <el-option label="进行中" value="1" />
            <el-option label="已结束" value="2" />
            <el-option label="已关闭" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
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
          <span class="header-title">秒杀活动列表</span>
          <el-button type="primary" icon="Plus" @click="openDialog(null)">新增活动</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="关联商品" min-width="160">
          <template #default="{ row }">
            <div class="goods-cell">
              <el-image :src="row.goodsCover || placeholder" fit="cover" style="width:40px;height:40px;border-radius:4px;margin-right:8px" />
              <span>{{ goodsName(row.goodsId) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="原价/秒杀价" width="150" align="center">
          <template #default="{ row }">
            <div>
              <div class="origin-price">¥{{ Number(row.originalPrice).toFixed(2) }}</div>
              <div class="seckill-price">¥{{ Number(row.seckillPrice).toFixed(2) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存/已秒" width="110" align="center">
          <template #default="{ row }">{{ row.sold }}/{{ row.stock }}</template>
        </el-table-column>
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">
            <el-icon><Clock /></el-icon> {{ row.startTime }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" width="160">
          <template #default="{ row }">
            <el-icon><Warning /></el-icon> {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0 || row.status === 2" type="success" link icon="VideoPlay" @click="toggleStatus(row, 1)">开启</el-button>
            <el-button v-if="row.status === 1" type="warning" link icon="VideoPause" @click="toggleStatus(row, 3)">关闭</el-button>
            <el-button type="primary" link icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秒杀活动' : '新增秒杀活动'" width="640px" destroy-on-close @close="formRef = null">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="如：五一限时秒杀" />
        </el-form-item>
        <el-form-item label="关联商品" prop="goodsId">
          <el-select v-model="form.goodsId" placeholder="请选择要秒杀的商品" filterable style="width:100%" @change="onGoodsChange">
            <el-option v-for="g in goodsList" :key="g.id" :label="g.name + ' (库存:' + g.stock + ', 价格:¥' + g.price + ')'" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" disabled style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="秒杀价" prop="seckillPrice">
              <el-input-number v-model="form.seckillPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="秒杀库存" prop="stock">
              <el-input-number v-model="form.stock" :min="1" :step="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            range-separator="至"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="每人限购" prop="limitNum">
              <el-input-number v-model="form.limitNum" :min="1" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">草稿</el-radio>
                <el-radio :value="1">发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填，活动介绍或注意事项" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const placeholder = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

const goodsNames = ['乳清蛋白粉 5磅', '肌酸粉 300g', 'BCAA支链氨基酸', '专业跑步鞋', '瑜伽垫 6mm', '哑铃套装 20kg', '筋膜枪', '蛋白棒 12支装', '左旋肉碱', '健身手套']
const goodsList = goodsNames.map((n, i) => ({
  id: i + 1, name: n, stock: 50 + i * 20, price: Number((59.9 + i * 40).toFixed(2)), cover: ''
}))

const searchForm = reactive({ name: '', goodsId: '', status: '', dateRange: [] })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const mockActs = []
const actNames = ['春季大促秒杀', '会员专享秒杀', '新品首发秒杀', '周末限时秒杀', '节日特惠秒杀', '店庆狂欢秒杀', '周年庆秒杀', '暑期大放送', '双11预热秒杀', '年终钜惠秒杀']
for (let i = 1; i <= 35; i++) {
  const gid = ((i - 1) % goodsList.length) + 1
  const g = goodsList.find((x) => x.id === gid)
  const start = new Date(2025, 7, ((i - 1) % 28) + 1, 9 + (i % 6))
  const end = new Date(start.getTime() + 2 * 3600 * 1000)
  const now = new Date()
  let st
  if (end < now) st = 2
  else if (start > now) st = 0
  else st = 1
  if (i % 9 === 0) st = 3
  const stock = 20 + (i * 7) % 180
  mockActs.push({
    id: i,
    name: actNames[(i - 1) % actNames.length] + ' #' + i,
    goodsId: gid,
    goodsCover: '',
    originalPrice: g.price,
    seckillPrice: Number((g.price * (0.4 + Math.random() * 0.3)).toFixed(2)),
    stock,
    sold: st === 0 ? 0 : Math.floor(Math.random() * stock),
    startTime: formatDT(start),
    endTime: formatDT(end),
    status: st,
    limitNum: 1 + (i % 3),
    description: '限时秒杀，先到先得！'
  })
}
function formatDT(d) {
  const p = (n) => String(n).padStart(2, '0')
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds())
}

const tableData = ref([])
function statusText(s) { return { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已关闭' }[s] }
function statusType(s) { return { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] }
function goodsName(id) { return goodsList.find((g) => g.id === id)?.name || '-' }

function loadData() {
  loading.value = true
  setTimeout(() => {
    let list = mockActs.filter((a) => {
      if (searchForm.name && !a.name.includes(searchForm.name)) return false
      if (searchForm.goodsId && a.goodsId !== Number(searchForm.goodsId)) return false
      if (searchForm.status !== '' && String(a.status) !== searchForm.status) return false
      return true
    })
    page.total = list.length
    const s = (page.pageNum - 1) * page.pageSize
    tableData.value = list.slice(s, s + page.pageSize)
    loading.value = false
  }, 300)
}
function handleSearch() { page.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { name: '', goodsId: '', status: '', dateRange: [] }); handleSearch() }

const form = reactive({
  id: null, name: '', goodsId: null, originalPrice: 0, seckillPrice: 0,
  stock: 50, timeRange: [], status: 0, limitNum: 1, description: '', sold: 0
})
const rules = {
  name: [{ required: true, message: '请输入活动名', trigger: 'blur' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  limitNum: [{ required: true, message: '请输入限购数量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function onGoodsChange(id) {
  const g = goodsList.find((x) => x.id === id)
  if (g) {
    form.originalPrice = g.price
    if (!isEdit.value) {
      form.seckillPrice = Number((g.price * 0.6).toFixed(2))
      form.stock = Math.min(g.stock, 100)
    }
  }
}

function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, JSON.parse(JSON.stringify(row)))
    form.timeRange = [row.startTime, row.endTime]
  } else {
    Object.assign(form, {
      id: null, name: '', goodsId: null, originalPrice: 0, seckillPrice: 0,
      stock: 50, timeRange: [], status: 0, limitNum: 1, description: '', sold: 0
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((v) => {
    if (!v) return
    submitLoading.value = true
    setTimeout(() => {
      const data = { ...form, startTime: form.timeRange[0], endTime: form.timeRange[1] }
      if (isEdit.value) {
        const i = mockActs.findIndex((a) => a.id === form.id)
        if (i > -1) Object.assign(mockActs[i], data)
        ElMessage.success('编辑成功')
      } else {
        mockActs.unshift({ ...data, id: Date.now(), sold: 0 })
        ElMessage.success('新增成功')
      }
      submitLoading.value = false
      dialogVisible.value = false
      loadData()
    }, 350)
  })
}

function toggleStatus(row, status) {
  const label = status === 1 ? '开启' : '关闭'
  ElMessageBox.confirm(`确定${label}活动「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(() => { row.status = status; ElMessage.success(label + '成功'); loadData() })
    .catch(() => {})
}
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'error' })
    .then(() => { const i = mockActs.findIndex((a) => a.id === row.id); if (i > -1) mockActs.splice(i, 1); ElMessage.success('删除成功'); loadData() })
    .catch(() => {})
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.seckill-page { :deep(.el-card) { margin-bottom: 16px; } }
.search-card :deep(.el-card__body) { padding: 16px 20px 0; }
.search-form { display: flex; flex-wrap: wrap; gap: 4px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 15px; font-weight: 600; color: #303133; }
.table-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.goods-cell { display: flex; align-items: center; }
.origin-price { font-size: 12px; color: #909399; text-decoration: line-through; }
.seckill-price { font-size: 14px; color: #f56c6c; font-weight: 600; }
</style>
