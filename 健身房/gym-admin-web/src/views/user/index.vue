<template>
  <div class="user-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            prefix-icon="User"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="searchForm.nickname"
            placeholder="请输入昵称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            clearable
            prefix-icon="Phone"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable style="width: 140px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教练" value="COACH" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="启用" value="1" />
            <el-option label="停用" value="0" />
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
          <span class="header-title">用户列表</span>
          <div>
            <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
            <el-button type="danger" icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">
              批量删除
            </el-button>
            <el-button icon="Download" @click="handleExport">导出</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        border
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="roleType(row.role)" effect="light">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="启"
              inactive-text="停"
              @change="(val) => updateStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="160" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
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
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="560px"
      destroy-on-close
      @close="dialogFormRef = null"
    >
      <el-form ref="dialogFormRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教练" value="COACH" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="选填" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { validatePhone, validateEmail } from '@/utils/validate'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogFormRef = ref(null)
const selectedIds = ref([])

const searchForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  role: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const mockUsers = []
const roles = ['ADMIN', 'COACH', 'USER']
const firstNames = ['张', '李', '王', '赵', '刘', '陈', '杨', '黄', '周', '吴']
const lastNames = ['伟', '芳', '娜', '敏', '静', '丽', '强', '磊', '军', '洋']
for (let i = 1; i <= 56; i++) {
  const fn = firstNames[i % firstNames.length]
  const ln = lastNames[(i * 3) % lastNames.length]
  mockUsers.push({
    id: i,
    username: 'user' + i,
    nickname: fn + ln,
    phone: '138' + String(10000000 + i).padStart(8, '0'),
    email: 'user' + i + '@gym.com',
    role: roles[i % 3],
    status: i % 7 === 0 ? 0 : 1,
    avatar: '',
    remark: '',
    createTime: '2025-0' + ((i % 9) + 1) + '-' + String((i % 28) + 1).padStart(2, '0') + ' 10:' + String(i % 60).padStart(2, '0') + ':00'
  })
}

const tableData = ref([])

function loadData() {
  loading.value = true
  setTimeout(() => {
    let list = mockUsers.filter((u) => {
      if (searchForm.username && !u.username.includes(searchForm.username)) return false
      if (searchForm.nickname && !u.nickname.includes(searchForm.nickname)) return false
      if (searchForm.phone && !u.phone.includes(searchForm.phone)) return false
      if (searchForm.role && u.role !== searchForm.role) return false
      if (searchForm.status !== '' && String(u.status) !== searchForm.status) return false
      return true
    })
    pagination.total = list.length
    const start = (pagination.pageNum - 1) * pagination.pageSize
    tableData.value = list.slice(start, start + pagination.pageSize)
    loading.value = false
  }, 300)
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  Object.assign(searchForm, { username: '', nickname: '', phone: '', role: '', status: '' })
  handleSearch()
}

function roleType(role) {
  return { ADMIN: 'danger', COACH: 'warning', USER: 'info' }[role] || 'info'
}
function roleText(role) {
  return { ADMIN: '管理员', COACH: '教练', USER: '用户' }[role] || role
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.id)
}

const formData = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  role: 'USER',
  status: 1,
  remark: ''
})

const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度 3-20 个字符', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: (_, v, cb) => (v && !validatePhone(v) ? cb(new Error('手机号格式不正确')) : cb()), trigger: 'blur' }
  ],
  email: [
    { validator: (_, v, cb) => (v && !validateEmail(v) ? cb(new Error('邮箱格式不正确')) : cb()), trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

function resetForm() {
  Object.assign(formData, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    phone: '',
    email: '',
    role: 'USER',
    status: 1,
    remark: ''
  })
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!dialogFormRef.value) return
  await dialogFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    setTimeout(() => {
      if (isEdit.value) {
        const idx = mockUsers.findIndex((u) => u.id === formData.id)
        if (idx > -1) Object.assign(mockUsers[idx], formData)
        ElMessage.success('编辑成功')
      } else {
        const newUser = { ...formData, id: Date.now(), createTime: new Date().toLocaleString() }
        mockUsers.unshift(newUser)
        ElMessage.success('新增成功')
      }
      submitLoading.value = false
      dialogVisible.value = false
      loadData()
    }, 400)
  })
}

function updateStatus(row, val) {
  ElMessage.success(`已${val === 1 ? '启用' : '停用'}用户：${row.nickname}`)
}

function handleResetPwd(row) {
  ElMessageBox.confirm(`确定要重置用户「${row.nickname}」的密码为 123456 吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('密码重置成功：123456'))
    .catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除用户「${row.nickname}」吗？`, '删除确认', { type: 'error' })
    .then(() => {
      const idx = mockUsers.findIndex((u) => u.id === row.id)
      if (idx > -1) mockUsers.splice(idx, 1)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个用户吗？`, '批量删除', { type: 'error' })
    .then(() => {
      selectedIds.value.forEach((id) => {
        const idx = mockUsers.findIndex((u) => u.id === id)
        if (idx > -1) mockUsers.splice(idx, 1)
      })
      selectedIds.value = []
      ElMessage.success('批量删除成功')
      loadData()
    })
    .catch(() => {})
}

function handleExport() {
  ElMessage.success('导出功能触发（演示）')
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.user-page {
  :deep(.el-card) {
    margin-bottom: 16px;
  }
}

.search-card {
  :deep(.el-card__body) {
    padding: 16px 20px 0;
  }
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
