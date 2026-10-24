<template>
  <div class="seckill-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.name" placeholder="请输入活动名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="未开始" value="0" />
            <el-option label="进行中" value="1" />
            <el-option label="已结束" value="2" />
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
          <span class="header-title">秒杀活动列表</span>
          <el-button type="primary" icon="Plus" @click="openDialog(null)">新增活动</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="活动名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="总库存" width="90" align="center">
          <template #default="{ row }">{{ row.totalStock ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="每人限购" width="90" align="center">
          <template #default="{ row }">{{ row.limitCount ?? 1 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link icon="VideoPlay" @click="toggleStatus(row, 1)">开启</el-button>
            <el-button v-if="row.status === 1" type="warning" link icon="VideoPause" @click="toggleStatus(row, 2)">关闭</el-button>
            <el-button type="primary" link icon="Goods" @click="openGoods(row)">商品</el-button>
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

    <!-- 活动 Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秒杀活动' : '新增秒杀活动'" width="560px" destroy-on-close @close="formRef = null">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="如：五一限时秒杀" />
        </el-form-item>
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
            <el-form-item label="总库存" prop="totalStock">
              <el-input-number v-model="form.totalStock" :min="1" :step="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每人限购" prop="limitCount">
              <el-input-number v-model="form.limitCount" :min="1" :max="100" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">未开始</el-radio>
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 活动商品 Drawer -->
    <el-drawer v-model="goodsDrawerVisible" :size="720" :title="`活动商品管理 - ${currentActivity?.name || ''}`" destroy-on-close>
      <div class="goods-drawer">
        <div class="drawer-toolbar">
          <el-button type="primary" icon="Plus" @click="openGoodsDialog(null)">新增秒杀商品</el-button>
          <el-button icon="Refresh" @click="loadActivityGoods">刷新</el-button>
        </div>
        <el-table :data="activityGoods" border stripe v-loading="goodsLoading" style="width:100%">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div class="goods-cell">
                <el-image :src="goodsCover(row.goodsId) || placeholder" fit="cover" style="width:40px;height:40px;border-radius:4px;margin-right:8px" />
                <span>{{ goodsName(row.goodsId) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="原价" width="100" align="right">
            <template #default="{ row }">
              <span class="origin-price">¥{{ Number(goodsPrice(row.goodsId)).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="秒杀价" width="110" align="right">
            <template #default="{ row }">
              <span class="seckill-price">¥{{ Number(row.seckillPrice).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="90" align="center" />
          <el-table-column prop="sold" label="已售" width="90" align="center" />
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link icon="Edit" @click="openGoodsDialog(row)">编辑</el-button>
              <el-button type="danger" link icon="Delete" @click="deleteActivityGoods(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-drawer>

    <!-- 秒杀商品 Dialog -->
    <el-dialog v-model="goodsDialogVisible" :title="goodsIsEdit ? '编辑秒杀商品' : '新增秒杀商品'" width="480px" destroy-on-close @close="goodsFormRef = null">
      <el-form ref="goodsFormRef" :model="goodsForm" :rules="goodsRules" label-width="90px">
        <el-form-item label="商品" prop="goodsId">
          <el-select v-model="goodsForm.goodsId" placeholder="请选择商品" filterable style="width:100%">
            <el-option v-for="g in goodsList" :key="g.id" :label="g.name + ' (库存:' + g.stock + ')'" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="秒杀价" prop="seckillPrice">
          <el-input-number v-model="goodsForm.seckillPrice" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="秒杀库存" prop="stock">
          <el-input-number v-model="goodsForm.stock" :min="1" :step="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="goodsForm.sort" :min="0" :step="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="goodsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitGoods">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSeckillActivityList, createSeckillActivity, updateSeckillActivity, deleteSeckillActivity, updateSeckillActivityStatus, getSeckillGoodsList, createSeckillGoods, updateSeckillGoods, deleteSeckillGoods } from '@/api/seckill'
import { getGoodsList } from '@/api/goods'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const placeholder = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

const goodsList = ref([])

const searchForm = reactive({ name: '', status: '' })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const tableData = ref([])

function statusText(s) { return { 0: '未开始', 1: '进行中', 2: '已结束' }[s] || '-' }
function statusType(s) { return { 0: 'info', 1: 'success', 2: 'warning' }[s] || 'info' }
function formatTime(v) { return v ? String(v).replace('T', ' ').slice(0, 19) : '-' }
function goodsName(id) { return goodsList.value.find((g) => g.id === id)?.name || ('商品#' + id) }
function goodsCover(id) { return goodsList.value.find((g) => g.id === id)?.cover || '' }
function goodsPrice(id) { return goodsList.value.find((g) => g.id === id)?.price || 0 }

async function loadGoods() {
  try {
    const res = await getGoodsList({ pageNum: 1, pageSize: 1000 })
    goodsList.value = res?.rows || []
  } catch (e) {}
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSeckillActivityList({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      name: searchForm.name || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    tableData.value = res?.rows || []
    page.total = Number(res?.total) || 0
  } finally {
    loading.value = false
  }
}
function handleSearch() { page.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { name: '', status: '' }); handleSearch() }

const form = reactive({
  id: null, name: '', timeRange: [], totalStock: 100, limitCount: 1, status: 0
})
const rules = {
  name: [{ required: true, message: '请输入活动名', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  totalStock: [{ required: true, message: '请输入总库存', trigger: 'blur' }],
  limitCount: [{ required: true, message: '请输入限购数量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, {
      id: row.id, name: row.name,
      timeRange: [formatTime(row.startTime), formatTime(row.endTime)],
      totalStock: row.totalStock ?? 100, limitCount: row.limitCount ?? 1, status: row.status
    })
  } else {
    Object.assign(form, { id: null, name: '', timeRange: [], totalStock: 100, limitCount: 1, status: 0 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (v) => {
    if (!v) return
    submitLoading.value = true
    try {
      const payload = {
        id: form.id,
        name: form.name,
        startTime: (form.timeRange[0] || '').replace(' ', 'T'),
        endTime: (form.timeRange[1] || '').replace(' ', 'T'),
        totalStock: form.totalStock,
        limitCount: form.limitCount,
        status: form.status
      }
      if (isEdit.value) {
        await updateSeckillActivity(payload)
        ElMessage.success('编辑成功')
      } else {
        await createSeckillActivity(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

function toggleStatus(row, status) {
  const label = status === 1 ? '开启' : '关闭'
  ElMessageBox.confirm(`确定${label}活动「${row.name}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await updateSeckillActivityStatus(row.id, status)
      ElMessage.success(label + '成功')
      loadData()
    })
    .catch(() => {})
}
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'error' })
    .then(async () => {
      await deleteSeckillActivity(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

// === 活动商品 ===
const goodsDrawerVisible = ref(false)
const goodsDialogVisible = ref(false)
const goodsIsEdit = ref(false)
const goodsLoading = ref(false)
const currentActivity = ref(null)
const activityGoods = ref([])
const goodsFormRef = ref(null)
const goodsForm = reactive({ id: null, goodsId: null, seckillPrice: 0, stock: 50, sort: 0 })
const goodsRules = {
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

function openGoods(row) {
  currentActivity.value = row
  goodsDrawerVisible.value = true
  loadActivityGoods()
}
async function loadActivityGoods() {
  if (!currentActivity.value) return
  goodsLoading.value = true
  try {
    activityGoods.value = (await getSeckillGoodsList(currentActivity.value.id)) || []
  } finally {
    goodsLoading.value = false
  }
}
function openGoodsDialog(row) {
  goodsIsEdit.value = !!row
  if (row) {
    Object.assign(goodsForm, { id: row.id, goodsId: row.goodsId, seckillPrice: row.seckillPrice, stock: row.stock, sort: row.sort ?? 0 })
  } else {
    Object.assign(goodsForm, { id: null, goodsId: null, seckillPrice: 0, stock: 50, sort: 0 })
  }
  goodsDialogVisible.value = true
}
async function submitGoods() {
  if (!goodsFormRef.value) return
  await goodsFormRef.value.validate(async (v) => {
    if (!v) return
    submitLoading.value = true
    try {
      const payload = {
        id: goodsForm.id,
        activityId: currentActivity.value.id,
        goodsId: goodsForm.goodsId,
        seckillPrice: goodsForm.seckillPrice,
        stock: goodsForm.stock,
        sort: goodsForm.sort
      }
      if (goodsIsEdit.value) {
        await updateSeckillGoods(payload)
        ElMessage.success('编辑成功')
      } else {
        await createSeckillGoods({ ...payload, sold: 0 })
        ElMessage.success('新增成功')
      }
      goodsDialogVisible.value = false
      loadActivityGoods()
    } finally {
      submitLoading.value = false
    }
  })
}
function deleteActivityGoods(row) {
  ElMessageBox.confirm(`确定删除该秒杀商品「${goodsName(row.goodsId)}」吗？`, '提示', { type: 'error' })
    .then(async () => {
      await deleteSeckillGoods(row.id)
      ElMessage.success('删除成功')
      loadActivityGoods()
    })
    .catch(() => {})
}

onMounted(() => { loadGoods(); loadData() })
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
.goods-drawer { padding-bottom: 20px; }
.drawer-toolbar { display: flex; gap: 8px; margin-bottom: 14px; }
</style>
