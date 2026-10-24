<template>
  <div class="goods-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 160px">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="上架" value="1" />
            <el-option label="下架" value="0" />
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
          <span class="header-title">商品列表</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增商品</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="封面" width="90" align="center">
          <template #default="{ row }">
            <el-image
              :src="row.cover || placeholderImg"
              fit="cover"
              style="width: 56px; height: 56px; border-radius: 6px"
              :preview-src-list="[row.cover || placeholderImg]"
            >
              <template #error>
                <div class="img-placeholder"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ categoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" align="center" />
        <el-table-column prop="sales" label="销量" width="90" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="上"
              inactive-text="下"
              @change="(v) => updateStatus(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link icon="EditPen" @click="handleStock(row)">改库存</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="table-pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="620px"
      destroy-on-close
      @close="formRef = null"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="16">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="formData.categoryId" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="价格(元)" prop="price">
              <el-input-number v-model="formData.price" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="formData.stock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品封面">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleCoverChange"
            accept="image/*"
          >
            <el-image v-if="formData.cover" :src="formData.cover" fit="cover" class="cover-preview" />
            <div v-else class="cover-placeholder">
              <el-icon :size="28"><Plus /></el-icon>
              <div>上传封面</div>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸 400x400，支持 JPG/PNG</div>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入商品描述..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockVisible" title="修改库存" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前商品">{{ formData.name }}</el-form-item>
        <el-form-item label="当前库存">{{ editingStock }}</el-form-item>
        <el-form-item label="新库存">
          <el-input-number v-model="newStock" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const stockVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editingStock = ref(0)
const newStock = ref(0)

const placeholderImg = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'

const categoryList = [
  { id: 1, name: '蛋白粉' },
  { id: 2, name: '肌酸' },
  { id: 3, name: '运动服饰' },
  { id: 4, name: '健身器材' },
  { id: 5, name: '补剂饮料' },
  { id: 6, name: '其他配件' }
]

const searchForm = reactive({ name: '', categoryId: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const goodsNames = ['乳清蛋白粉 5磅', '肌酸粉 300g', 'BCAA支链氨基酸', '运动紧身衣男款', '运动紧身裤女款',
  '专业跑步鞋', '瑜伽垫 6mm', '哑铃套装 20kg', '健身手套', '护膝护腕套装', '泡沫轴', '蛋白棒 12支装',
  '左旋肉碱口服液', '筋膜枪', '健身水杯', '运动头带', '拉力绳套装', '弹力带', '负重背心', '跳绳']

const mockGoods = []
for (let i = 1; i <= 42; i++) {
  mockGoods.push({
    id: i,
    name: goodsNames[i % goodsNames.length] + (i > goodsNames.length ? ' ' + Math.ceil(i / goodsNames.length) : ''),
    categoryId: ((i - 1) % 6) + 1,
    price: Number((Math.random() * 500 + 20).toFixed(2)),
    stock: Math.floor(Math.random() * 500 + 10),
    sales: Math.floor(Math.random() * 2000),
    status: i % 11 === 0 ? 0 : 1,
    cover: '',
    description: '优质商品，健身必备，品质保证。',
    createTime: '2025-0' + ((i % 9) + 1) + '-' + String((i % 28) + 1).padStart(2, '0') + ' 10:00:00'
  })
}

const tableData = ref([])
function categoryName(id) {
  return categoryList.find((c) => c.id === id)?.name || '-'
}

function loadData() {
  loading.value = true
  setTimeout(() => {
    let list = mockGoods.filter((g) => {
      if (searchForm.name && !g.name.includes(searchForm.name)) return false
      if (searchForm.categoryId && g.categoryId !== Number(searchForm.categoryId)) return false
      if (searchForm.status !== '' && String(g.status) !== searchForm.status) return false
      return true
    })
    pagination.total = list.length
    const start = (pagination.pageNum - 1) * pagination.pageSize
    tableData.value = list.slice(start, start + pagination.pageSize)
    loading.value = false
  }, 300)
}

function handleSearch() { pagination.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { name: '', categoryId: '', status: '' }); handleSearch() }

const formData = reactive({
  id: null, name: '', categoryId: null, price: 0, stock: 0,
  status: 1, cover: '', description: ''
})
const formRules = {
  name: [{ required: true, message: '请输入商品名', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
function resetForm() {
  Object.assign(formData, { id: null, name: '', categoryId: null, price: 0, stock: 0, status: 1, cover: '', description: '' })
}
function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }
function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}
function handleCoverChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => { formData.cover = e.target.result }
  reader.readAsDataURL(file.raw)
}
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value) {
        const idx = mockGoods.findIndex((g) => g.id === formData.id)
        if (idx > -1) Object.assign(mockGoods[idx], formData)
        ElMessage.success('编辑成功')
      } else {
        mockGoods.unshift({ ...formData, id: Date.now(), sales: 0, createTime: new Date().toLocaleString() })
        ElMessage.success('新增成功')
      }
      submitLoading.value = false
      dialogVisible.value = false
      loadData()
    }, 400)
  })
}
function updateStatus(row, val) {
  ElMessage.success(`已${val === 1 ? '上架' : '下架'}：${row.name}`)
}
function handleStock(row) {
  Object.assign(formData, row)
  editingStock.value = row.stock
  newStock.value = row.stock
  stockVisible.value = true
}
function confirmStock() {
  const idx = mockGoods.findIndex((g) => g.id === formData.id)
  if (idx > -1) mockGoods[idx].stock = newStock.value
  ElMessage.success('库存修改成功')
  stockVisible.value = false
  loadData()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'error' })
    .then(() => {
      const idx = mockGoods.findIndex((g) => g.id === row.id)
      if (idx > -1) mockGoods.splice(idx, 1)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}
onMounted(loadData)
</script>

<style scoped lang="scss">
.goods-page { :deep(.el-card) { margin-bottom: 16px; } }
.search-card :deep(.el-card__body) { padding: 16px 20px 0; }
.search-form { display: flex; flex-wrap: wrap; gap: 4px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 15px; font-weight: 600; color: #303133; }
.price-text { color: #f56c6c; font-weight: 600; }
.table-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.img-placeholder { width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #c0c4cc; border-radius: 6px; }
.cover-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    transition: border-color .2s;
    &:hover { border-color: #409eff; }
  }
}
.cover-preview { width: 120px; height: 120px; border-radius: 6px; }
.cover-placeholder { width: 120px; height: 120px; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #8c939d; font-size: 12px; gap: 6px; }
.upload-tip { margin-top: 6px; font-size: 12px; color: #909399; }
</style>
