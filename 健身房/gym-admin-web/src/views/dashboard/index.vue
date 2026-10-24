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
            <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
              <el-icon><component :is="stat.trend > 0 ? 'Top' : 'Bottom'" /></el-icon>
              {{ Math.abs(stat.trend) }}%
              <span class="trend-label">较上月</span>
            </div>
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
                <el-radio-button value="90">近90天</el-radio-button>
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
              <el-tag type="success" effect="light">月累计：+1,256</el-tag>
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
              <span class="card-title">会员类型占比</span>
            </div>
          </template>
          <div ref="memberChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">热门课程 TOP5</span>
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

const salesRange = ref('30')
const salesChartRef = ref(null)
const userChartRef = ref(null)
const memberChartRef = ref(null)
const courseChartRef = ref(null)

let salesChart = null
let userChart = null
let memberChart = null
let courseChart = null

const statCards = [
  { icon: 'Money', label: '总销售额 (元)', value: '¥ 856,432', trend: 12.5 },
  { icon: 'User', label: '注册用户数', value: '12,856', trend: 8.3 },
  { icon: 'Goods', label: '在售商品数', value: '268', trend: -2.1 },
  { icon: 'Reading', label: '本月课程数', value: '356', trend: 15.7 }
]

function genDates(days) {
  const arr = []
  const today = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    arr.push(`${d.getMonth() + 1}/${d.getDate()}`)
  }
  return arr
}

function genSalesData(days) {
  return Array.from({ length: days }, () => Math.round(8000 + Math.random() * 15000))
}

function genUserData(days) {
  return Array.from({ length: days }, () => Math.round(20 + Math.random() * 80))
}

function initSalesChart() {
  if (!salesChartRef.value) return
  salesChart = echarts.init(salesChartRef.value)
  const days = Number(salesRange.value)
  const option = {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(50,50,50,0.9)', borderColor: 'transparent', textStyle: { color: '#fff' } },
    legend: { data: ['销售额', '目标'], right: 10, top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: genDates(days), axisLine: { lineStyle: { color: '#eee' } }, axisLabel: { color: '#909399' } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f5f5f5' } }, axisLabel: { color: '#909399', formatter: '¥{value}' } },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: genSalesData(days),
        lineStyle: { width: 3, color: '#409eff' },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.4)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' }
          ])
        }
      },
      {
        name: '目标',
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, type: 'dashed', color: '#67c23a' },
        data: Array.from({ length: days }, () => 15000)
      }
    ]
  }
  salesChart.setOption(option)
}

function initUserChart() {
  if (!userChartRef.value) return
  userChart = echarts.init(userChartRef.value)
  const days = 30
  const option = {
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(50,50,50,0.9)', borderColor: 'transparent', textStyle: { color: '#fff' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: genDates(days), axisLine: { lineStyle: { color: '#eee' } }, axisLabel: { color: '#909399', interval: 3 } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f5f5f5' } }, axisLabel: { color: '#909399' } },
    series: [
      {
        type: 'bar',
        data: genUserData(days),
        barWidth: '50%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: 'rgba(103,194,58,0.4)' }
          ])
        }
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
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399'],
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{d}%' },
        data: [
          { value: 4280, name: '月卡会员' },
          { value: 3650, name: '季卡会员' },
          { value: 2980, name: '年卡会员' },
          { value: 1250, name: '终身会员' },
          { value: 696, name: '非会员' }
        ]
      }
    ]
  }
  memberChart.setOption(option)
}

function initCourseChart() {
  if (!courseChartRef.value) return
  courseChart = echarts.init(courseChartRef.value)
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f5f5f5' } }, axisLabel: { color: '#909399' } },
    yAxis: {
      type: 'category',
      data: ['瑜伽课', '动感单车', '力量训练', '游泳课', '私教训练'],
      axisLine: { lineStyle: { color: '#eee' } },
      axisTick: { show: false },
      axisLabel: { color: '#606266' }
    },
    series: [
      {
        type: 'bar',
        barWidth: '55%',
        data: [
          { value: 1856, itemStyle: { color: '#409eff', borderRadius: [0, 4, 4, 0] } },
          { value: 2145, itemStyle: { color: '#67c23a', borderRadius: [0, 4, 4, 0] } },
          { value: 2680, itemStyle: { color: '#e6a23c', borderRadius: [0, 4, 4, 0] } },
          { value: 3256, itemStyle: { color: '#f56c6c', borderRadius: [0, 4, 4, 0] } },
          { value: 4120, itemStyle: { color: '#909399', borderRadius: [0, 4, 4, 0] } }
        ],
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
  initSalesChart()
})

onMounted(async () => {
  await nextTick()
  initSalesChart()
  initUserChart()
  initMemberChart()
  initCourseChart()
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
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;

  &.up { color: #67c23a; }
  &.down { color: #f56c6c; }
}

.trend-label {
  color: #c0c4cc;
  margin-left: 4px;
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
