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
              active-value="NORMAL"
              inactive-value="DISABLED"
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
            <el-radio value="NORMAL">启用</el-radio>
            <el-radio value="DISABLED">停用</el-radio>
          </el-radio-group>
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
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, resetPassword } from '@/api/user'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogFormRef = ref(null)
const selectedIds = ref([])

const searchForm = reactive({ username: '', nickname: '', phone: '', role: '', status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const tableData = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await getUserList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      nickname: searchForm.nickname || undefined,
      phone: searchForm.phone || undefined,
      role: searchForm.role || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    tableData.value = res?.rows || []
    pagination.total = Number(res?.total) || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.pageNum = 1; loadData() }
function handleReset() { Object.assign(searchForm, { username: '', nickname: '', phone: '', role: '', status: '' }); handleSearch() }

function roleType(role) { return { ADMIN: 'danger', COACH: 'warning', USER: 'info' }[role] || 'info' }
function roleText(role) { return { ADMIN: '管理员', COACH: '教练', USER: '用户' }[role] || role }

function handleSelectionChange(rows) { selectedIds.value = rows.map((r) => r.id) }

const formData = reactive({
  id: null, username: '', password: '', nickname: '', phone: '', email: '',
  role: 'USER', status: 'NORMAL'
})
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { validator: (_, v, cb) => (!isEdit.value && !v ? cb(new Error('请输入密码')) : cb()), trigger: 'blur' }
  ],
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
    id: null, username: '', password: '', nickname: '', phone: '', email: '',
    role: 'USER', status: 'NORMAL'
  })
}

function handleAdd() { isEdit.value = false; resetForm(); dialogVisible.value = true }
function handleEdit(row) {
  isEdit.value = true
  Object.assign(formData, JSON.parse(JSON.stringify(row)))
  formData.password = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!dialogFormRef.value) return
  await dialogFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        id: formData.id,
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname,
        phone: formData.phone,
        email: formData.email,
        role: formData.role,
        status: formData.status
      }
      if (isEdit.value) {
        delete payload.password
        await updateUser(payload)
        ElMessage.success('编辑成功')
      } else {
        await createUser(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

async function updateStatus(row, val) {
  const status = val === 'NORMAL' ? 1 : 0
  try {
    await updateUserStatus(row.id, status)
    ElMessage.success(`已${status === 1 ? '启用' : '停用'}用户：${row.nickname || row.username}`)
  } catch (e) {
    loadData()
  }
}

function handleResetPwd(row) {
  ElMessageBox.confirm(`确定要重置用户「${row.nickname || row.username}」的密码为 123456 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await resetPassword(row.id)
      ElMessage.success('密码已重置为 123456')
    })
    .catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除用户「${row.nickname || row.username}」吗？`, '删除确认', { type: 'error' })
    .then(async () => {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个用户吗？`, '批量删除', { type: 'error' })
    .then(async () => {
      await Promise.all(selectedIds.value.map((id) => deleteUser(id)))
      selectedIds.value = []
      ElMessage.success('批量删除成功')
      loadData()
    })
    .catch(() => {})
}

function handleExport() {
  if (!tableData.value.length) { ElMessage.warning('暂无数据可导出'); return }
  const headers = ['用户名', '昵称', '手机号', '邮箱', '角色', '状态', '注册时间']
  const rows = tableData.value.map((u) => [
    u.username, u.nickname, u.phone, u.email,
    roleText(u.role), u.status === 'NORMAL' ? '启用' : '停用',
    u.createTime
  ])
  const csv = [headers, ...rows]
    .map((r) => r.map((c) => `"${String(c ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '用户列表.csv'
  link.click()
  URL.revokeObjectURL(link.href)
  ElMessage.success('导出成功')
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
