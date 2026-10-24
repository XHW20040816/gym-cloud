<template>
  <div class="course-page">
    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="课程管理" name="course">
        <el-card shadow="never" class="search-card">
          <el-form :inline="true" :model="courseSearch" class="search-form">
            <el-form-item label="课程名">
              <el-input v-model="courseSearch.name" placeholder="请输入课程名" clearable @keyup.enter="loadCourses" />
            </el-form-item>
            <el-form-item label="教练">
              <el-select v-model="courseSearch.coachId" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="c in coachList" :key="c.id" :label="c.realName" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="courseSearch.status" placeholder="全部" clearable style="width: 120px">
                <el-option label="启用" value="1" />
                <el-option label="禁用" value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="() => { coursePage.pageNum = 1; loadCourses() }">搜索</el-button>
              <el-button icon="Refresh" @click="resetCourseSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="table-header">
              <span class="header-title">课程列表</span>
              <el-button type="primary" icon="Plus" @click="openCourseDialog(null)">新增课程</el-button>
            </div>
          </template>
          <el-table :data="courseList" border stripe v-loading="courseLoading" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="课程名称" min-width="160" />
            <el-table-column label="教练" width="110">
              <template #default="{ row }">{{ coachName(row.coachId) }}</template>
            </el-table-column>
            <el-table-column label="时长" width="90" align="center">
              <template #default="{ row }">{{ row.duration }} 分钟</template>
            </el-table-column>
            <el-table-column label="容量" width="90" align="center">
              <template #default="{ row }">{{ row.capacity }} 人</template>
            </el-table-column>
            <el-table-column label="价格" width="100" align="right">
              <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status" :active-value="1" :inactive-value="0"
                  inline-prompt active-text="开" inactive-text="关"
                  @change="(v) => updateCourseStatus(row, v)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link icon="Edit" @click="openCourseDialog(row)">编辑</el-button>
                <el-button type="danger" link icon="Delete" @click="deleteCourse(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="coursePage.pageNum" v-model:page-size="coursePage.pageSize"
            :page-sizes="[10,20,50]" :total="coursePage.total"
            layout="total, sizes, prev, pager, next, jumper" background
            class="table-pagination" @size-change="loadCourses" @current-change="loadCourses"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="课程排期" name="schedule">
        <el-card shadow="never" class="search-card">
          <el-form :inline="true" :model="scheduleSearch" class="search-form">
            <el-form-item label="课程">
              <el-select v-model="scheduleSearch.courseId" placeholder="全部课程" clearable style="width: 180px">
                <el-option v-for="c in allCourses" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="排期日期">
              <el-date-picker v-model="scheduleSearch.date" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:160px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="() => { schedulePage.pageNum = 1; loadSchedules() }">搜索</el-button>
              <el-button icon="Refresh" @click="resetScheduleSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="table-header">
              <span class="header-title">排期列表</span>
              <el-button type="primary" icon="Plus" @click="openScheduleDialog(null)">新增排期</el-button>
            </div>
          </template>
          <el-table :data="scheduleList" border stripe v-loading="scheduleLoading" style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column label="课程" min-width="160">
              <template #default="{ row }">{{ courseName(row.courseId) }}</template>
            </el-table-column>
            <el-table-column prop="classDate" label="日期" width="120" />
            <el-table-column label="时间段" width="160" align="center">
              <template #default="{ row }">
                <el-tag type="primary" effect="plain">{{ row.startTime }} - {{ row.endTime }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="容量/已报" width="110" align="center">
              <template #default="{ row }">
                <span :class="{ full: row.enrollmentCount >= row.capacity }">{{ row.enrollmentCount }}/{{ row.capacity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enrollmentCount >= row.capacity ? 'danger' : 'success'" effect="light">
                  {{ row.enrollmentCount >= row.capacity ? '已满' : '可预约' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link icon="Edit" @click="openScheduleDialog(row)">编辑</el-button>
                <el-button type="danger" link icon="Delete" @click="deleteSchedule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="schedulePage.pageNum" v-model:page-size="schedulePage.pageSize"
            :page-sizes="[10,20,50]" :total="schedulePage.total"
            layout="total, sizes, prev, pager, next, jumper" background
            class="table-pagination" @size-change="loadSchedules" @current-change="loadSchedules"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 课程 Dialog -->
    <el-dialog v-model="courseDialogVisible" :title="courseIsEdit ? '编辑课程' : '新增课程'" width="560px" destroy-on-close @close="courseFormRef=null">
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseRules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="14"><el-form-item label="课程名" prop="name"><el-input v-model="courseForm.name" /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="教练" prop="coachId"><el-select v-model="courseForm.coachId" style="width:100%"><el-option v-for="c in coachList" :key="c.id" :label="c.realName" :value="c.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="时长(分)" prop="duration"><el-input-number v-model="courseForm.duration" :min="15" :max="240" :step="15" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="容量" prop="capacity"><el-input-number v-model="courseForm.capacity" :min="1" :max="200" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="价格(元)" prop="price"><el-input-number v-model="courseForm.price" :min="0" :precision="2" :step="10" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="课程简介"><el-input v-model="courseForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCourse">确定</el-button>
      </template>
    </el-dialog>

    <!-- 排期 Dialog -->
    <el-dialog v-model="scheduleDialogVisible" :title="scheduleIsEdit ? '编辑排期' : '新增排期'" width="560px" destroy-on-close @close="scheduleFormRef=null">
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="90px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="scheduleForm.courseId" style="width:100%" placeholder="选择课程">
            <el-option v-for="c in allCourses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排期日期" prop="classDate">
          <el-date-picker v-model="scheduleForm.classDate" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="选择日期" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="开始时间" prop="startTime"><el-time-picker v-model="scheduleForm.startTime" format="HH:mm" value-format="HH:mm" style="width:100%" placeholder="开始" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束时间" prop="endTime"><el-time-picker v-model="scheduleForm.endTime" format="HH:mm" value-format="HH:mm" style="width:100%" placeholder="结束" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="容量" prop="capacity"><el-input-number v-model="scheduleForm.capacity" :min="1" :max="200" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList, createCourse, updateCourse, deleteCourse as deleteCourseApi, updateCourseStatus as updateCourseStatusApi, getScheduleList, createSchedule, updateSchedule, deleteSchedule as deleteScheduleApi, getCoachList } from '@/api/course'

const activeTab = ref('course')
const submitLoading = ref(false)
const courseLoading = ref(false)
const scheduleLoading = ref(false)

const coachList = ref([])
const allCourses = ref([])

async function loadCoaches() {
  try {
    const res = await getCoachList()
    coachList.value = res?.rows || []
  } catch (e) {}
}

function coachName(id) { return coachList.value.find((c) => c.id === id)?.realName || '-' }
function courseName(id) { const c = allCourses.value.find((x) => x.id === id); return c ? c.name : '-' }

// === 课程 ===
const courseSearch = reactive({ name: '', coachId: '', status: '' })
const coursePage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const courseList = ref([])
async function loadCourses() {
  courseLoading.value = true
  try {
    const res = await getCourseList({
      pageNum: coursePage.pageNum,
      pageSize: coursePage.pageSize,
      name: courseSearch.name || undefined,
      coachId: courseSearch.coachId || undefined,
      status: courseSearch.status !== '' ? courseSearch.status : undefined
    })
    courseList.value = res?.rows || []
    coursePage.total = Number(res?.total) || 0
  } finally {
    courseLoading.value = false
  }
}
async function loadAllCourses() {
  try {
    const all = await getCourseList({ pageNum: 1, pageSize: 500 })
    allCourses.value = all?.rows || []
  } catch (e) {}
}
function resetCourseSearch() { Object.assign(courseSearch, { name: '', coachId: '', status: '' }); coursePage.pageNum = 1; loadCourses() }

// === 排期 ===
const scheduleSearch = reactive({ courseId: '', date: '' })
const schedulePage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const scheduleList = ref([])
async function loadSchedules() {
  scheduleLoading.value = true
  try {
    const res = await getScheduleList({
      pageNum: schedulePage.pageNum,
      pageSize: schedulePage.pageSize,
      courseId: scheduleSearch.courseId || undefined,
      startDate: scheduleSearch.date || undefined,
      endDate: scheduleSearch.date || undefined
    })
    scheduleList.value = res?.rows || []
    schedulePage.total = Number(res?.total) || 0
  } finally {
    scheduleLoading.value = false
  }
}
function resetScheduleSearch() { Object.assign(scheduleSearch, { courseId: '', date: '' }); schedulePage.pageNum = 1; loadSchedules() }

// === 课程 Dialog ===
const courseDialogVisible = ref(false)
const courseIsEdit = ref(false)
const courseFormRef = ref(null)
const courseForm = reactive({ id: null, name: '', coachId: null, duration: 60, capacity: 20, price: 0, status: 1, description: '' })
const courseRules = {
  name: [{ required: true, message: '请输入课程名', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
function openCourseDialog(row) {
  courseIsEdit.value = !!row
  if (row) Object.assign(courseForm, JSON.parse(JSON.stringify(row)))
  else Object.assign(courseForm, { id: null, name: '', coachId: null, duration: 60, capacity: 20, price: 0, status: 1, description: '' })
  courseDialogVisible.value = true
}
async function submitCourse() {
  if (!courseFormRef.value) return
  await courseFormRef.value.validate(async (v) => {
    if (!v) return
    submitLoading.value = true
    try {
      const payload = {
        id: courseForm.id, name: courseForm.name, coachId: courseForm.coachId,
        duration: courseForm.duration, capacity: courseForm.capacity, price: courseForm.price,
        status: courseForm.status, description: courseForm.description
      }
      if (courseIsEdit.value) {
        await updateCourse(payload)
        ElMessage.success('编辑成功')
      } else {
        await createCourse(payload)
        ElMessage.success('新增成功')
      }
      courseDialogVisible.value = false
      loadCourses()
      loadAllCourses()
    } finally {
      submitLoading.value = false
    }
  })
}
async function updateCourseStatus(row, v) {
  try {
    await updateCourseStatusApi(row.id, v)
    ElMessage.success(`已${v === 1 ? '启用' : '禁用'}：${row.name}`)
  } catch (e) {
    loadCourses()
  }
}
function deleteCourse(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'error' })
    .then(async () => {
      await deleteCourseApi(row.id)
      ElMessage.success('删除成功')
      loadCourses()
      loadAllCourses()
    })
    .catch(() => {})
}

// === 排期 Dialog ===
const scheduleDialogVisible = ref(false)
const scheduleIsEdit = ref(false)
const scheduleFormRef = ref(null)
const scheduleForm = reactive({ id: null, courseId: null, classDate: '', startTime: '', endTime: '', capacity: 20 })
const scheduleRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  classDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}
function openScheduleDialog(row) {
  scheduleIsEdit.value = !!row
  if (row) Object.assign(scheduleForm, JSON.parse(JSON.stringify(row)))
  else Object.assign(scheduleForm, { id: null, courseId: null, classDate: '', startTime: '', endTime: '', capacity: 20 })
  scheduleDialogVisible.value = true
}
async function submitSchedule() {
  if (!scheduleFormRef.value) return
  await scheduleFormRef.value.validate(async (v) => {
    if (!v) return
    submitLoading.value = true
    try {
      const payload = {
        id: scheduleForm.id, courseId: scheduleForm.courseId, classDate: scheduleForm.classDate,
        startTime: scheduleForm.startTime, endTime: scheduleForm.endTime, capacity: scheduleForm.capacity
      }
      if (scheduleIsEdit.value) {
        await updateSchedule(payload)
        ElMessage.success('编辑成功')
      } else {
        await createSchedule(payload)
        ElMessage.success('新增成功')
      }
      scheduleDialogVisible.value = false
      loadSchedules()
    } finally {
      submitLoading.value = false
    }
  })
}
function deleteSchedule(row) {
  ElMessageBox.confirm('确定删除此排期吗？', '提示', { type: 'error' })
    .then(async () => {
      await deleteScheduleApi(row.id)
      ElMessage.success('删除成功')
      loadSchedules()
    })
    .catch(() => {})
}

onMounted(() => { loadCoaches(); loadCourses(); loadAllCourses(); loadSchedules() })
</script>

<style scoped lang="scss">
.course-page {
  :deep(.el-card) { margin-bottom: 16px; }
  :deep(.main-tabs) {
    .el-tabs__header { background: #fff; padding: 0 16px; margin: 0 0 16px !important; border-radius: 8px; }
  }
}
.search-card :deep(.el-card__body) { padding: 16px 20px 0; }
.search-form { display: flex; flex-wrap: wrap; gap: 4px; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 15px; font-weight: 600; color: #303133; }
.table-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.full { color: #f56c6c; font-weight: 600; }
</style>
