<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">数据看板</h2>
        <p class="page-desc">用户、菜品、订单与 AI 调用的整体运营概览</p>
      </div>
      <el-button :icon="Refresh" text bg @click="loadAll">刷新数据</el-button>
    </div>

    <el-row :gutter="12" class="metric-row">
      <el-col v-for="item in metrics" :key="item.label" :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="never">
          <span class="stat-icon" :class="item.tone"><el-icon><component :is="item.icon" /></el-icon></span>
          <div>
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col v-for="chart in charts" :key="chart.title" :xs="24" :md="chart.md || 12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-head">
              <el-icon class="chart-icon"><component :is="chart.icon" /></el-icon>
              <span>{{ chart.title }}</span>
            </div>
          </template>
          <EChart :option="chart.option" :height="chart.height || '290px'" />
          <div v-if="chart.tip" class="muted chart-tip">{{ chart.tip }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  getOverview,
  getDishCategoryData,
  getHealthGoalData,
  getOrderData,
  getReviewData,
  getDishSalesData,
  getRecommendHotData,
  getAiCallData
} from '@/api/business'
import EChart from '@/components/EChart.vue'

const overview = ref({})
const categoryData = ref([])
const goalData = ref([])
const orderData = ref([])
const reviewData = ref([])
const salesData = ref([])
const hotData = ref([])
const aiData = ref([])

const metrics = computed(() => [
  { label: '学生用户', value: overview.value.studentCount ?? 0, icon: 'User', tone: 'tone-amber' },
  { label: '食堂商户', value: overview.value.merchantCount ?? 0, icon: 'Shop', tone: 'tone-blue' },
  { label: '菜品总数', value: overview.value.dishCount ?? 0, icon: 'Bowl', tone: 'tone-amber' },
  { label: '在架菜品', value: overview.value.onlineDishCount ?? 0, icon: 'CircleCheck', tone: 'tone-green' },
  { label: '待审核菜品', value: overview.value.waitAuditCount ?? 0, icon: 'Tickets', tone: 'tone-blue' },
  { label: '已分析营养', value: overview.value.nutritionCount ?? 0, icon: 'DataAnalysis', tone: 'tone-green' },
  { label: '订单总数', value: overview.value.orderCount ?? 0, icon: 'List', tone: 'tone-amber' },
  { label: 'AI调用失败', value: overview.value.aiFailCount ?? 0, icon: 'WarningFilled', tone: 'tone-red' }
])

const axisText = { color: '#5b6472', fontSize: 11 }

const pieOption = (data, name) => ({
  color: ['#ef8b2c', '#f7c05a', '#2fa36b', '#4b6cb7', '#8f7ee2', '#e35d5d', '#38b2ac', '#f2856f'],
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, type: 'scroll', textStyle: axisText },
  series: [
    {
      name,
      type: 'pie',
      radius: ['46%', '68%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 6 },
      label: { formatter: '{b}\n{c}', color: '#5b6472', fontSize: 11 },
      data: data.map((item) => ({ name: item.name, value: item.value }))
    }
  ]
})

const lineOption = (data, name, color) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 52, right: 22, top: 24, bottom: 62 },
  xAxis: {
    type: 'category',
    data: data.map((item) => item.name).reverse(),
    axisLabel: { ...axisText, rotate: 35 },
    axisLine: { lineStyle: { color: '#e9ebf0' } },
    axisTick: { show: false }
  },
  yAxis: {
    type: 'value',
    name,
    minInterval: 1,
    nameTextStyle: axisText,
    axisLabel: axisText,
    splitLine: { lineStyle: { color: '#f0f1f5' } }
  },
  series: [
    {
      type: 'line',
      smooth: true,
      symbolSize: 7,
      areaStyle: { opacity: 0.16 },
      itemStyle: { color },
      lineStyle: { width: 3, color },
      data: data.map((item) => item.value).reverse()
    }
  ]
})

const barOption = (data, name, color, rotate = 30) => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 62, right: 22, top: 24, bottom: 70 },
  xAxis: {
    type: 'category',
    data: data.map((item) => item.name),
    axisLabel: { interval: 0, rotate, ...axisText },
    axisLine: { lineStyle: { color: '#e9ebf0' } },
    axisTick: { show: false }
  },
  yAxis: {
    type: 'value',
    name,
    minInterval: 1,
    nameTextStyle: axisText,
    axisLabel: axisText,
    splitLine: { lineStyle: { color: '#f0f1f5' } }
  },
  series: [
    {
      type: 'bar',
      barMaxWidth: 30,
      itemStyle: { borderRadius: [6, 6, 0, 0], color },
      data: data.map((item) => item.value)
    }
  ]
})

const charts = computed(() => [
  { title: '各分类菜品数量分布', icon: 'PieChart', option: pieOption(categoryData.value, '菜品分类') },
  { title: '学生健康目标分布', icon: 'Aim', option: pieOption(goalData.value, '健康目标') },
  { title: '每日订单量', icon: 'TrendCharts', option: lineOption(orderData.value, '订单数', '#2fa36b') },
  {
    title: 'AI营养分析调用情况',
    icon: 'MagicStick',
    height: '260px',
    tip: '调用失败时系统自动降级为内置食材估算表，基础功能不受影响。',
    option: {
      ...barOption(aiData.value, '次数', '#ef8b2c', 0),
      series: [
        {
          type: 'bar',
          barMaxWidth: 60,
          label: { show: true, position: 'top', color: '#5b6472' },
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: (params) => (params.dataIndex === 0 ? '#e35d5d' : '#2fa36b')
          },
          data: aiData.value.map((item) => item.value)
        }
      ]
    }
  },
  { title: '菜品销量Top10', icon: 'Histogram', option: barOption(salesData.value, '销量(份)', '#ef8b2c') },
  { title: '评价评分分布', icon: 'Star', option: pieOption(reviewData.value, '评分') },
  {
    title: '推荐命中菜品Top10',
    icon: 'Aim',
    md: 24,
    height: '250px',
    tip: '统计学生保存在推荐记录表中的菜品出现次数，用于观察推荐算法的实际效果。',
    option: barOption(hotData.value, '推荐次数', '#8f7ee2', 20)
  }
])

const loadAll = async () => {
  const tasks = [
    [getOverview, (v) => (overview.value = v)],
    [getDishCategoryData, (v) => (categoryData.value = v)],
    [getHealthGoalData, (v) => (goalData.value = v)],
    [getOrderData, (v) => (orderData.value = v)],
    [getReviewData, (v) => (reviewData.value = v)],
    [getDishSalesData, (v) => (salesData.value = v)],
    [getRecommendHotData, (v) => (hotData.value = v)],
    [getAiCallData, (v) => (aiData.value = v)]
  ]
  for (const [fn, setter] of tasks) {
    try {
      const res = await fn()
      setter(res.data || [])
    } catch (e) {
      // 单项统计失败不影响其他图表展示
    }
  }
}

onMounted(loadAll)
</script>

<style scoped>
.metric-row {
  margin-bottom: 4px;
}

.stat-card {
  margin-bottom: 12px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow) !important;
}

.stat-icon {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  flex: none;
  border-radius: 13px;
  font-size: 21px;
}

.tone-amber {
  background: var(--brand-soft);
  color: var(--brand-strong);
}

.tone-green {
  background: #eef8f2;
  color: #1f7d51;
}

.tone-blue {
  background: #eef2fb;
  color: #3f6bb0;
}

.tone-red {
  background: #fdeeec;
  color: #cf4b46;
}

.chart-card {
  margin-bottom: 12px;
}

.chart-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.chart-icon {
  color: var(--brand);
}

.chart-tip {
  margin-top: 6px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: var(--surface-sunken);
  line-height: 1.6;
}
</style>
