<template>
  <div class="dashboard">
    <!-- 概览统计卡片 -->
    <div class="stat-grid">
      <div v-for="card in cards" :key="card.key" class="stat-card">
        <div class="stat-icon" :style="{ color: card.color, background: card.bg }">
          <el-icon><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </div>
      </div>
    </div>

    <!-- 订单趋势图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="chart-header">
          <span class="chart-title">近 7 天订单趋势</span>
          <span class="chart-hint">订单量 / 营业额</span>
        </div>
      </template>
      <el-empty v-if="empty" description="暂无订单数据" :image-size="90" />
      <div v-else ref="chartRef" class="chart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getStatsOverview, getOrderTrend } from '@/api/stats'

const chartRef = ref()
const empty = ref(false)
let chart = null

const cards = ref([
  { label: '用户数', key: 'userCount', icon: 'User', color: '#5d87ff', bg: 'rgba(93, 135, 255, 0.16)' },
  { label: '商家数', key: 'merchantCount', icon: 'Shop', color: '#13deb9', bg: 'rgba(19, 222, 185, 0.16)' },
  { label: '酒店数', key: 'hotelCount', icon: 'OfficeBuilding', color: '#ffae1f', bg: 'rgba(255, 174, 31, 0.16)' },
  { label: '景点数', key: 'spotCount', icon: 'Picture', color: '#49beff', bg: 'rgba(73, 190, 255, 0.16)' },
  { label: '餐厅数', key: 'restaurantCount', icon: 'Food', color: '#ff4d4f', bg: 'rgba(255, 77, 79, 0.16)' },
  { label: '订单数', key: 'orderCount', icon: 'Tickets', color: '#539bff', bg: 'rgba(83, 155, 255, 0.16)' },
  { label: '评价数', key: 'commentCount', icon: 'ChatDotRound', color: '#fa896b', bg: 'rgba(250, 137, 107, 0.16)' },
  { label: '营业额(元)', key: 'orderAmount', icon: 'Money', color: '#b48df3', bg: 'rgba(180, 141, 243, 0.16)' }
])

async function loadOverview() {
  const data = await getStatsOverview()
  cards.value.forEach(card => {
    let v = data[card.key] ?? 0
    card.value = typeof v === 'number' ? v.toLocaleString() : v
  })
}

async function loadTrend() {
  const list = await getOrderTrend(7)
  if (!list || !list.length) {
    empty.value = true
    return
  }
  empty.value = false

  const dates = list.map(i => i.date.slice(5))
  const counts = list.map(i => i.count)
  const amounts = list.map(i => Number(i.amount))

  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#161618',
      borderColor: '#363843',
      textStyle: { color: '#f5f5f5' },
      axisPointer: { lineStyle: { color: '#363843' } }
    },
    legend: {
      data: ['订单量', '营业额'],
      left: 'center',
      bottom: 0,
      icon: 'roundRect',
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 16,
      textStyle: { color: '#b5b7c8' }
    },
    grid: { left: 8, right: 8, top: 12, bottom: 40, containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: true,
      axisLine: { lineStyle: { color: '#363843' } },
      axisTick: { show: false },
      axisLabel: { color: '#808290' }
    },
    yAxis: [
      {
        type: 'value',
        axisLabel: { color: '#808290' },
        splitLine: { lineStyle: { color: '#26272f', type: 'dashed' } }
      },
      {
        type: 'value',
        axisLabel: { color: '#808290' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '订单量',
        type: 'bar',
        data: counts,
        barWidth: 18,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#8eabff' },
            { offset: 1, color: '#5d87ff' }
          ])
        }
      },
      {
        name: '营业额',
        type: 'line',
        yAxisIndex: 1,
        data: amounts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        itemStyle: { color: '#13deb9' },
        lineStyle: { width: 3, color: '#13deb9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(19, 222, 185, 0.25)' },
            { offset: 1, color: 'rgba(19, 222, 185, 0)' }
          ])
        }
      }
    ]
  })
}

function handleResize() {
  if (chart) chart.resize()
}

onMounted(async () => {
  await loadOverview()
  await loadTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.dashboard {
  min-height: 100%;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: var(--art-main-bg-color);
  border-radius: 12px;
  border: 1px solid var(--art-card-border);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}
.stat-card:hover {
  box-shadow: var(--art-box-shadow-sm);
  transform: translateY(-2px);
}
.stat-icon {
  width: 48px;
  height: 48px;
  min-width: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.stat-label {
  color: var(--art-gray-500);
  font-size: 13px;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
}
.chart-card {
  margin-top: 16px;
}
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.chart-title {
  font-weight: 600;
  color: var(--art-text-gray-800);
}
.chart-hint {
  color: var(--art-gray-500);
  font-size: 12px;
  font-weight: 400;
}
.chart {
  height: 360px;
}
@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
