<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="(stat, idx) in statCards" :key="idx">
        <el-card shadow="hover" class="stat-card" :class="'stat-' + idx">
          <div class="stat-icon">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">近30天销售额</span>
              <el-radio-group v-model="salesRange" size="small">
                <el-radio-button value="7">近7天</el-radio-button>
                <el-radio-button value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="salesChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">新用户增长趋势</span>
            </div>
          </template>
          <div ref="userChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户角色分布</span>
            </div>
          </template>
          <div ref="memberChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">课程容量 TOP5</span>
            </div>
          </template>
          <div ref="courseChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getPaymentStatistics } from '@/api/payment'
import { getUserStatistics } from '@/api/user'
import { getGoodsList } from '@/api/goods'
import { getCourseList } from '@/api/course'

const salesRange = ref('30')
const salesChartRef = ref(null)
const userChartRef = ref(null)
const memberChartRef = ref(null)
const courseChartRef = ref(null)

let salesChart = null
let userChart = null
let memberChart = null
let courseChart = null

const statCards = ref([
  { icon: 'Money', label: '总销售额 (元)', value: '0.00' },
  { icon: 'User', label: '注册用户数', value: '0' },
  { icon: 'Goods', label: '在售商品数', value: '0' },
  { icon: 'Reading', label: '课程总数', value: '0' }
])

const salesTrend = ref([])
const userTrend = ref([])
const roleCounts = ref({ ADMIN: 0, COACH: 0, USER: 0 })
const topCourses = ref([])

async function loadStats() {
  const [pay, user, goods, course] = await Promise.all([
    getPaymentStatistics().catch(() => null),
    getUserStatistics().catch(() => null),
    getGoodsList({ pageNum: 1, pageSize: 1, status: 1 }).catch(() => null),
    getCourseList({ pageNum: 1, pageSize: 500 }).catch(() => null)
  ])

  statCards.value[0].value = Number(pay?.totalAmount || 0).toFixed(2)
  statCards.value[1].value = Number(user?.total ?? 0)
  statCards.value[2].value = Number(goods?.total ?? 0)
  statCards.value[3].value = Number(course?.total ?? 0)

  salesTrend.value = pay?.trend || []
  userTrend.value = user?.trend || []
  const rc = user?.roleCounts || {}
  roleCounts.value = { ADMIN: Number(rc.ADMIN || 0), COACH: Number(rc.COACH || 0), USER: Number(rc.USER || 0) }
  const courses = course?.rows || []
  topCourses.value = courses.slice(0, 5)

  await nextTick()
  initSalesChart()
  initUserChart()
  initMemberChart()
  initCourseChart()
}

function initSalesChart() {
  if (!salesChartRef.value) return
  salesChart = echarts.init(salesChartRef.value)
  const days = Number(salesRange.value)
  const data = salesTrend.value.slice(-days)
  const option = {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(50,50,50,0.9)', borderColor: 'transparent', textStyle: { color: '#fff' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.map((d) => d.date), axisLine: { lineStyle: { color: '#eee' } }, axisLabel: { color: '#909399' } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f5f5f5' } }, axisLabel: { color: '#909399', formatter: '¥{value}' } },
    series: [
      {
        name: '销售额', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6,
        data: data.map((d) => d.amount),
        lineStyle: { width: 3, color: '#409eff' },
        itemStyle: { color: '#409eff' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.4)' }, { offset: 1, color: 'rgba(64,158,255,0.05)' }]) }
      }
    ]
  }
  salesChart.setOption(option)
}

function initUserChart() {
  if (!userChartRef.value) return
  userChart = echarts.init(userChartRef.value)
  const data = userTrend.value.slice(-30)
  const option = {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(50,50,50,0.9)', borderColor: 'transparent', textStyle: { color: '#fff' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: data.map((d) => d.date), axisLine: { lineStyle: { color: '#eee' } }, axisLabel: { color: '#909399', interval: 3 } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f5f5f5' } }, axisLabel: { color: '#909399' } },
    series: [
      {
        type: 'bar', data: data.map((d) => Number(d.count)), barWidth: '50%',
        itemStyle: { borderRadius: [4, 4, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#67c23a' }, { offset: 1, color: 'rgba(103,194,58,0.4)' }]) }
      }
    ]
  }
  userChart.setOption(option)
}

function initMemberChart() {
  if (!memberChartRef.value) return
  memberChart = echarts.init(memberChartRef.value)
  const option = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, left: 'center' },
    color: ['#f56c6c', '#e6a23c', '#409eff'],
    series: [
      {
        type: 'pie', radius: ['40%', '65%'], center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{d}%' },
        data: [
          { value: roleCounts.value.ADMIN || 0, name: '管理员' },
          { value: roleCounts.value.COACH || 0, name: '教练' },
          { value: roleCounts.value.USER || 0, name: '会员' }
        ]
      }
    ]
  }
  memberChart.setOption(option)
}

function initCourseChart() {
  if (!courseChartRef.value) return
  courseChart = echarts.init(courseChartRef.value)
  const sorted = [...topCourses.value].sort((a, b) => (b.capacity || 0) - (a.capacity || 0))
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f5f5f5' } }, axisLabel: { color: '#909399' } },
    yAxis: { type: 'category', data: sorted.map((c) => c.name), axisLine: { lineStyle: { color: '#eee' } }, axisTick: { show: false }, axisLabel: { color: '#606266' } },
    series: [
      {
        type: 'bar', barWidth: '55%',
        data: sorted.map((c) => ({ value: c.capacity || 0, itemStyle: { color: '#409eff', borderRadius: [0, 4, 4, 0] } })),
        label: { show: true, position: 'right', color: '#606266', formatter: '{c} 人' }
      }
    ]
  }
  courseChart.setOption(option)
}

function handleResize() {
  salesChart && salesChart.resize()
  userChart && userChart.resize()
  memberChart && memberChart.resize()
  courseChart && courseChart.resize()
}

watch(salesRange, () => {
  salesChart && salesChart.dispose()
  initSalesChart()
})

onMounted(async () => {
  await loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  salesChart && salesChart.dispose()
  userChart && userChart.dispose()
  memberChart && memberChart.dispose()
  courseChart && courseChart.dispose()
})
</script>

<style scoped lang="scss">
.dashboard {
  width: 100%;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  border: none;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px;

  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    gap: 16px;
    width: 100%;
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-0 .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-1 .stat-icon { background: linear-gradient(135deg, #11998e, #38ef7d); }
.stat-2 .stat-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-3 .stat-icon { background: linear-gradient(135deg, #fa709a, #fee140); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart {
  width: 100%;
  height: 320px;
}
</style>
