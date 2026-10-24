<template>
  <div class="course-page">
    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="课程管理" name="course">
        <el-card shadow="never" class="search-card">
          <el-form :inline="true" :model="courseSearch" class="search-form">
            <el-form-item label="课程名">
              <el-input v-model="courseSearch.name" placeholder="请输入课程名" clearable @keyup.enter="loadCourses" />
            </el-form-item>
            <el-form-item label="难度">
              <el-select v-model="courseSearch.level" placeholder="全部" clearable style="width: 120px">
                <el-option label="初级" value="1" />
                <el-option label="中级" value="2" />
                <el-option label="高级" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="教练">
              <el-select v-model="courseSearch.coachId" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" />
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
            <el-table-column label="封面" width="90" align="center">
              <template #default="{ row }">
                <el-image :src="row.cover || placeholder" fit="cover" style="width:56px;height:56px;border-radius:6px">
                  <template #error><div class="ph"><el-icon><Picture /></el-icon></div></template>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="课程名称" min-width="140" />
            <el-table-column label="教练" width="100">
              <template #default="{ row }">{{ coachName(row.coachId) }}</template>
            </el-table-column>
            <el-table-column label="难度" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="levelType(row.level)" effect="light">{{ levelText(row.level) }}</el-tag>
              </template>
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
                  @change="(v) => ElMessage.success(`已${v === 1 ? '开启' : '关闭'}：${row.name}`)"
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
              <el-date-picker v-model="scheduleSearch.date" type="date" placeholder="选择日期" style="width:160px" />
            </el-form-item>
            <el-form-item label="教练">
              <el-select v-model="scheduleSearch.coachId" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
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
            <el-table-column label="课程" min-width="140">
              <template #default="{ row }">{{ courseName(row.courseId) }}</template>
            </el-table-column>
            <el-table-column label="教练" width="100">
              <template #default="{ row }">{{ coachName(row.coachId) }}</template>
            </el-table-column>
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column label="时间段" width="160" align="center">
              <template #default="{ row }">
                <el-tag type="primary" effect="plain">{{ row.startTime }} - {{ row.endTime }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="classroom" label="教室" width="100" align="center" />
            <el-table-column label="容量/已报" width="110" align="center">
              <template #default="{ row }">
                <span :class="{ full: row.enrolled >= row.capacity }">{{ row.enrolled }}/{{ row.capacity }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enrolled >= row.capacity ? 'danger' : 'success'" effect="light">
                  {{ row.enrolled >= row.capacity ? '已满' : '可预约' }}
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
    <el-dialog v-model="courseDialogVisible" :title="courseIsEdit ? '编辑课程' : '新增课程'" width="600px" destroy-on-close @close="courseFormRef=null">
      <el-form ref="courseFormRef" :model="courseForm" :rules="courseRules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="14"><el-form-item label="课程名" prop="name"><el-input v-model="courseForm.name" /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="教练" prop="coachId"><el-select v-model="courseForm.coachId" style="width:100%"><el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="难度" prop="level"><el-select v-model="courseForm.level" style="width:100%"><el-option label="初级" :value="1"/><el-option label="中级" :value="2"/><el-option label="高级" :value="3"/></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="时长(分)" prop="duration"><el-input-number v-model="courseForm.duration" :min="15" :max="240" :step="15" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="容量" prop="capacity"><el-input-number v-model="courseForm.capacity" :min="1" :max="200" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="价格(元)" prop="price"><el-input-number v-model="courseForm.price" :min="0" :precision="2" :step="10" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态" prop="status"><el-radio-group v-model="courseForm.status"><el-radio :value="1">开启</el-radio><el-radio :value="0">关闭</el-radio></el-radio-group></el-form-item></el-col>
        </el-row>
        <el-form-item label="课程封面">
          <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" @change="(f) => courseForm.cover = URL.createObjectURL(f.raw)">
            <el-button size="small" type="primary" icon="Upload">上传封面</el-button>
            <span v-if="courseForm.cover" class="ml-4">已选择</span>
          </el-upload>
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
        <el-form-item label="教练" prop="coachId">
          <el-select v-model="scheduleForm.coachId" style="width:100%"><el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" /></el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="排期日期" prop="date"><el-date-picker v-model="scheduleForm.date" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="选择日期" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="教室" prop="classroom"><el-input v-model="scheduleForm.classroom" placeholder="如：A101" /></el-form-item></el-col>
        </el-row>
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

const activeTab = ref('course')
const placeholder = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'
const submitLoading = ref(false)
const courseLoading = ref(false)
const scheduleLoading = ref(false)

const coachList = [
  { id: 1, name: '张教练' }, { id: 2, name: '李教练' }, { id: 3, name: '王教练' },
  { id: 4, name: '刘教练' }, { id: 5, name: '陈教练' }
]
const courseNames = ['瑜伽', '动感单车', '力量训练', '游泳', '私教训练', '普拉提', '搏击操', 'HIIT', '拉丁舞', '尊巴']

const allCourses = ref([])
function genCourses() {
  const list = []
  for (let i = 1; i <= 28; i++) {
    list.push({
      id: i,
      name: courseNames[(i - 1) % courseNames.length] + (i > courseNames.length ? ' ' + Math.ceil(i / courseNames.length) : ''),
      coachId: ((i - 1) % coachList.length) + 1,
      level: ((i - 1) % 3) + 1,
      duration: [45, 60, 90, 120][i % 4],
      capacity: 10 + ((i * 3) % 30),
      price: Number((50 + Math.random() * 300).toFixed(2)),
      status: i % 13 === 0 ? 0 : 1,
      cover: '',
      description: '专业课程，资深教练带教。',
      createTime: '2025-06-15'
    })
  }
  return list
}
const mockCourses = genCourses()

const mockSchedules = []
for (let i = 1; i <= 45; i++) {
  const cid = ((i - 1) % mockCourses.length) + 1
  const course = mockCourses.find((x) => x.id === cid)
  const d = new Date(2025, 7, (i % 28) + 1)
  const ds = d.toISOString().slice(0, 10)
  const hour = 8 + (i * 2) % 12
  mockSchedules.push({
    id: i, courseId: cid, coachId: course.coachId,
    date: ds,
    startTime: String(hour).padStart(2, '0') + ':00',
    endTime: String(hour + 1).padStart(2, '0') + ':00',
    classroom: 'A' + String(100 + i),
    capacity: course.capacity,
    enrolled: Math.floor(Math.random() * (course.capacity + 5))
  })
}

// === 课程 ===
const courseSearch = reactive({ name: '', level: '', coachId: '' })
const coursePage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const courseList = ref([])
function loadCourses() {
  courseLoading.value = true
  setTimeout(() => {
    let list = mockCourses.filter((c) => {
      if (courseSearch.name && !c.name.includes(courseSearch.name)) return false
      if (courseSearch.level && String(c.level) !== courseSearch.level) return false
      if (courseSearch.coachId && c.coachId !== Number(courseSearch.coachId)) return false
      return true
    })
    allCourses.value = list
    coursePage.total = list.length
    const s = (coursePage.pageNum - 1) * coursePage.pageSize
    courseList.value = list.slice(s, s + coursePage.pageSize)
    courseLoading.value = false
  }, 250)
}
function resetCourseSearch() { Object.assign(courseSearch, { name: '', level: '', coachId: '' }); coursePage.pageNum = 1; loadCourses() }
function coachName(id) { return coachList.find((c) => c.id === id)?.name || '-' }
function courseName(id) { const c = mockCourses.find((x) => x.id === id); return c ? c.name : '-' }
function levelText(l) { return { 1: '初级', 2: '中级', 3: '高级' }[l] }
function levelType(l) { return { 1: 'success', 2: 'warning', 3: 'danger' }[l] }

// === 排期 ===
const scheduleSearch = reactive({ courseId: '', date: '', coachId: '' })
const schedulePage = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const scheduleList = ref([])
function loadSchedules() {
  scheduleLoading.value = true
  setTimeout(() => {
    let list = mockSchedules.filter((s) => {
      if (scheduleSearch.courseId && s.courseId !== Number(scheduleSearch.courseId)) return false
      if (scheduleSearch.date && s.date !== scheduleSearch.date) return false
      if (scheduleSearch.coachId && s.coachId !== Number(scheduleSearch.coachId)) return false
      return true
    })
    schedulePage.total = list.length
    const s = (schedulePage.pageNum - 1) * schedulePage.pageSize
    scheduleList.value = list.slice(s, s + schedulePage.pageSize)
    scheduleLoading.value = false
  }, 250)
}
function resetScheduleSearch() { Object.assign(scheduleSearch, { courseId: '', date: '', coachId: '' }); schedulePage.pageNum = 1; loadSchedules() }

// === 课程 Dialog ===
const courseDialogVisible = ref(false)
const courseIsEdit = ref(false)
const courseFormRef = ref(null)
const courseForm = reactive({ id: null, name: '', coachId: null, level: 1, duration: 60, capacity: 20, price: 0, status: 1, cover: '', description: '' })
const courseRules = {
  name: [{ required: true, message: '请输入课程名', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  level: [{ required: true, message: '请选择难度', trigger: 'change' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}
function openCourseDialog(row) {
  courseIsEdit.value = !!row
  if (row) Object.assign(courseForm, JSON.parse(JSON.stringify(row)))
  else Object.assign(courseForm, { id: null, name: '', coachId: null, level: 1, duration: 60, capacity: 20, price: 0, status: 1, cover: '', description: '' })
  courseDialogVisible.value = true
}
async function submitCourse() {
  if (!courseFormRef.value) return
  await courseFormRef.value.validate((v) => {
    if (!v) return
    submitLoading.value = true
    setTimeout(() => {
      if (courseIsEdit.value) {
        const idx = mockCourses.findIndex((c) => c.id === courseForm.id)
        if (idx > -1) Object.assign(mockCourses[idx], courseForm)
        ElMessage.success('编辑成功')
      } else {
        mockCourses.unshift({ ...courseForm, id: Date.now(), createTime: new Date().toISOString().slice(0,10) })
        ElMessage.success('新增成功')
      }
      submitLoading.value = false
      courseDialogVisible.value = false
      loadCourses()
    }, 350)
  })
}
function deleteCourse(row) {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'error' })
    .then(() => { const i = mockCourses.findIndex((c) => c.id === row.id); if (i > -1) mockCourses.splice(i, 1); ElMessage.success('删除成功'); loadCourses() })
    .catch(() => {})
}

// === 排期 Dialog ===
const scheduleDialogVisible = ref(false)
const scheduleIsEdit = ref(false)
const scheduleFormRef = ref(null)
const scheduleForm = reactive({ id: null, courseId: null, coachId: null, date: '', startTime: '', endTime: '', classroom: '', capacity: 20 })
const scheduleRules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  classroom: [{ required: true, message: '请输入教室', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}
function openScheduleDialog(row) {
  scheduleIsEdit.value = !!row
  if (row) Object.assign(scheduleForm, JSON.parse(JSON.stringify(row)))
  else Object.assign(scheduleForm, { id: null, courseId: null, coachId: null, date: '', startTime: '', endTime: '', classroom: '', capacity: 20 })
  scheduleDialogVisible.value = true
}
async function submitSchedule() {
  if (!scheduleFormRef.value) return
  await scheduleFormRef.value.validate((v) => {
    if (!v) return
    submitLoading.value = true
    setTimeout(() => {
      if (scheduleIsEdit.value) {
        const i = mockSchedules.findIndex((s) => s.id === scheduleForm.id)
        if (i > -1) Object.assign(mockSchedules[i], { ...scheduleForm, enrolled: mockSchedules[i].enrolled || 0 })
        ElMessage.success('编辑成功')
      } else {
        mockSchedules.unshift({ ...scheduleForm, id: Date.now(), enrolled: 0 })
        ElMessage.success('新增成功')
      }
      submitLoading.value = false
      scheduleDialogVisible.value = false
      loadSchedules()
    }, 350)
  })
}
function deleteSchedule(row) {
  ElMessageBox.confirm('确定删除此排期吗？', '提示', { type: 'error' })
    .then(() => { const i = mockSchedules.findIndex((s) => s.id === row.id); if (i > -1) mockSchedules.splice(i, 1); ElMessage.success('删除成功'); loadSchedules() })
    .catch(() => {})
}

onMounted(() => { loadCourses(); loadSchedules() })
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
.ph { width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #c0c4cc; border-radius: 6px; }
.ml-4 { margin-left: 12px; }
.full { color: #f56c6c; font-weight: 600; }
</style>
