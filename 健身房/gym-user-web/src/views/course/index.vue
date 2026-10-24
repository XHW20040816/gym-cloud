<template>
  <div class="page">
    <h1 class="page-title">团课预约</h1>
    <p class="page-subtitle">选择合适的排期，一键预约（每场每人限约一次）</p>

    <div v-loading="loading" class="course-list">
      <div v-for="course in courses" :key="course.id" class="course panel">
        <div class="course-head">
          <div>
            <div class="course-title">{{ course.name }}</div>
            <div class="course-meta">
              <el-tag size="small" type="warning" effect="plain">{{ course.category }}</el-tag>
              <span class="muted">{{ course.duration }} 分钟</span>
              <span class="price">{{ Number(course.price).toFixed(2) }}</span>
            </div>
          </div>
          <el-tag size="small" effect="plain">容量 {{ course.capacity }} 人</el-tag>
        </div>
        <p class="course-desc">{{ course.description }}</p>

        <div class="schedule-list">
          <div v-for="schedule in schedulesOf(course.id)" :key="schedule.id" class="schedule">
            <div class="schedule-time">
              <div class="date">{{ schedule.classDate }}</div>
              <div class="time">{{ schedule.startTime }} - {{ schedule.endTime }}</div>
            </div>
            <div class="schedule-left">
              <span class="muted">已约 {{ schedule.enrollmentCount }} / {{ schedule.capacity }}</span>
              <el-progress
                :percentage="capacityPercent(schedule)"
                :stroke-width="8"
                :show-text="false"
                class="progress"
              />
            </div>
            <el-button
              :type="isFull(schedule) ? 'info' : 'primary'"
              size="small"
              :disabled="isFull(schedule)"
              :loading="enrollingId === schedule.id"
              @click="enroll(schedule)"
            >
              {{ isFull(schedule) ? '已约满' : '预约' }}
            </el-button>
          </div>
          <el-empty v-if="!schedulesOf(course.id).length" description="暂无排期" :image-size="60" />
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && !courses.length" description="暂无课程" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoursePage, getSchedulePage, enrollSchedule } from '@/api'

const courses = ref([])
const schedules = ref([])
const loading = ref(true)
const enrollingId = ref(null)

function schedulesOf(courseId) {
  return schedules.value.filter((s) => s.courseId === courseId && s.status === 1)
}

function isFull(schedule) {
  return (schedule.enrollmentCount || 0) >= (schedule.capacity || 0)
}

function capacityPercent(schedule) {
  if (!schedule.capacity) return 0
  return Math.min(100, Math.round(((schedule.enrollmentCount || 0) / schedule.capacity) * 100))
}

async function load() {
  loading.value = true
  try {
    const [coursePage, schedulePage] = await Promise.all([
      getCoursePage({ pageNum: 1, pageSize: 50, status: 1 }),
      getSchedulePage({ pageNum: 1, pageSize: 100 })
    ])
    courses.value = coursePage?.rows || []
    schedules.value = schedulePage?.rows || []
  } finally {
    loading.value = false
  }
}

async function enroll(schedule) {
  await ElMessageBox.confirm(
    `确认预约 ${schedule.classDate} ${schedule.startTime} 的课程吗？`,
    '预约确认',
    { confirmButtonText: '确认预约', cancelButtonText: '再想想', type: 'info' }
  ).catch(() => Promise.reject('cancel'))

  enrollingId.value = schedule.id
  try {
    await enrollSchedule(schedule.id)
    ElMessage.success('预约成功')
    await load()
  } catch (e) {
    // 错误提示已在请求拦截器处理
  } finally {
    enrollingId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.course-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.course-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.course-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-desc {
  color: var(--ink-2);
  font-size: 13px;
  margin: 12px 0 16px;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.schedule {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 16px;
  border-radius: 10px;
  background: var(--bg);
}

.schedule-time {
  min-width: 170px;
}

.date {
  font-weight: 600;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: var(--ink-3);
  margin-top: 2px;
}

.schedule-left {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.progress {
  flex: 1;
  max-width: 220px;
}

@media (max-width: 700px) {
  .schedule {
    flex-wrap: wrap;
    gap: 10px;
  }
  .schedule-left {
    width: 100%;
  }
}
</style>
