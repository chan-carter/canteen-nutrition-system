<template>
  <div class="screen">
    <header class="screen-head">
      <div class="head-title">
        <h2>食堂运营数据大屏</h2>
        <span class="sub">全局概览 · 营养健康 · 推荐与订单</span>
      </div>
      <div class="head-right">
        <span class="clock">{{ clock }}</span>
        <el-button size="small" text class="ghost" :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
        <el-button size="small" text class="ghost" :icon="SwitchButton" @click="$router.push('/admin/dashboard')">
          返回看板
        </el-button>
      </div>
    </header>

    <div class="kpi-row" v-loading="loading">
      <div v-for="k in kpis" :key="k.label" class="kpi">
        <span class="k-value">{{ k.value }}</span>
        <span class="k-label">{{ k.label }}</span>
      </div>
    </div>

    <div class="grid">
      <section class="panel">
        <h3 class="panel-title">订单状态分布</h3>
        <EChart v-if="statusOption" :option="statusOption" height="240px" />
      </section>
      <section class="panel">
        <h3 class="panel-title">菜品分类结构</h3>
        <EChart v-if="categoryOption" :option="categoryOption" height="240px" />
      </section>
      <section class="panel span2">
        <h3 class="panel-title">热销菜品 TOP8</h3>
        <EChart v-if="topOption" :option="topOption" height="250px" />
      </section>
      <section class="panel">
        <h3 class="panel-title">热量区间（每份）</h3>
        <EChart v-if="calorieOption" :option="calorieOption" height="230px" />
      </section>
      <section class="panel">
        <h3 class="panel-title">健康与AI使用情况</h3>
        <ul class="mini-list">
          <li v-for="m in miniRows" :key="m.label">
            <span>{{ m.label }}</span>
            <b>{{ m.value }}</b>
          </li>
        </ul>
        <div class="ai-note muted">
          AI 共调用 {{ ai.total || 0 }} 次，成功 {{ ai.successCount || 0 }} 次，平均耗时 {{ ai.avgCostMs || 0 }} ms
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Refresh, SwitchButton } from '@element-plus/icons-vue'
import { getDashboard } from '@/api/report'
import EChart from '@/components/EChart.vue'

const data = ref({})
const loading = ref(false)
const clock = ref('')
let timer = null
let clockTimer = null

const axis = {
  textStyle: { color: '#c8d3e6' },
  axisLine: { lineStyle: { color: '#33405c' } },
  splitLine: { lineStyle: { color: 'rgba(200, 211, 230, 0.12)' } }
}
const palette = ['#f0a441', '#4dd0a2', '#5b9cf8', '#e0707f', '#b78ef0', '#f3d25e']

const global = computed(() => data.value.global || {})
const ai = computed(() => data.value.ai || {})
const health = computed(() => data.value.health || {})

const kpis = computed(() => [
  { label: '注册学生', value: global.value.studentCount ?? 0 },
  { label: '入驻商户', value: global.value.merchantCount ?? 0 },
  { label: '菜品总数', value: global.value.dishCount ?? 0 },
  { label: '累计订单', value: global.value.orderCount ?? 0 },
  { label: '今日订单', value: global.value.todayOrders ?? 0 },
  { label: '今日营收', value: `￥${Number(global.value.todayRevenue ?? 0).toFixed(2)}` },
  { label: '累计营收', value: `￥${Number(global.value.totalRevenue ?? 0).toFixed(2)}` },
  { label: '饮食记录', value: health.value.dietRecordCount ?? 0 }
])

const miniRows = computed(() => [
  { label: '健康档案', value: health.value.profileCount ?? 0 },
  { label: '体重记录', value: health.value.weightLogCount ?? 0 },
  { label: '餐计划条目', value: health.value.mealPlanCount ?? 0 },
  { label: '菜品收藏', value: health.value.favoriteCount ?? 0 },
  { label: 'AI问答消息', value: health.value.chatMessageCount ?? 0 },
  { label: '站内消息', value: health.value.notificationCount ?? 0 }
])

const statusOption = computed(() => {
  const rows = data.value.orderStatusDist || []
  if (!rows.length) return {}
  return {
    color: palette,
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#c8d3e6', fontSize: 11 }, itemWidth: 9, itemHeight: 9 },
    series: [{
      type: 'pie',
      radius: ['42%', '66%'],
      center: ['50%', '44%'],
      label: { color: '#c8d3e6', fontSize: 11, formatter: '{b}\n{c}' },
      data: rows.map((r) => ({ name: r.name, value: r.value }))
    }]
  }
})

const categoryOption = computed(() => {
  const rows = data.value.categoryDist || []
  if (!rows.length) return {}
  return {
    color: palette,
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#c8d3e6', fontSize: 11 } },
    series: [{
      type: 'pie',
      roseType: 'radius',
      radius: ['26%', '62%'],
      center: ['50%', '44%'],
      label: { color: '#c8d3e6', fontSize: 11 },
      data: rows.map((r) => ({ name: r.name, value: r.value }))
    }]
  }
})

const topOption = computed(() => {
  const rows = [...(data.value.topDishes || [])].reverse()
  if (!rows.length) return {}
  return {
    grid: { left: 84, right: 28, top: 12, bottom: 24 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'value', ...axis, axisLabel: { color: '#c8d3e6', fontSize: 11 } },
    yAxis: { type: 'category', data: rows.map((r) => r.name), ...axis, axisLabel: { color: '#c8d3e6', fontSize: 11 } },
    series: [{
      type: 'bar',
      barWidth: 12,
      itemStyle: { borderRadius: [0, 6, 6, 0], color: '#f0a441' },
      label: { show: true, position: 'right', color: '#c8d3e6', fontSize: 11 },
      data: rows.map((r) => r.value)
    }]
  }
})

const calorieOption = computed(() => {
  const row = (data.value.calorieDist || [])[0] || {}
  return {
    color: palette,
    grid: { left: 42, right: 20, top: 20, bottom: 26 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['<200 kcal', '200-400', '>400'], ...axis, axisLabel: { color: '#c8d3e6', fontSize: 11 } },
    yAxis: { type: 'value', ...axis, axisLabel: { color: '#c8d3e6', fontSize: 11 } },
    series: [{
      type: 'bar',
      barWidth: 26,
      itemStyle: { borderRadius: [6, 6, 0, 0], color: '#4dd0a2' },
      data: [row.low || 0, row.mid || 0, row.high || 0]
    }]
  }
})

const load = async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    data.value = res.data || {}
  } finally {
    loading.value = false
  }
}

const tick = () => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  clock.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

onMounted(async () => {
  tick()
  clockTimer = setInterval(tick, 1000)
  timer = setInterval(load, 30000)
  await load()
})

onBeforeUnmount(() => {
  clearInterval(timer)
  clearInterval(clockTimer)
})
</script>

<style scoped>
.screen {
  margin: -18px -20px -28px;
  min-height: 100%;
  padding: 16px 20px 24px;
  color: #e6ecf7;
  background: radial-gradient(900px 320px at 18% -10%, rgba(240, 164, 65, 0.16), transparent 60%),
    linear-gradient(180deg, #151a24, #0f131b);
}

.screen-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(200, 211, 230, 0.14);
}

.screen-head h2 {
  margin: 0;
  font-size: 19px;
  letter-spacing: 1px;
}

.sub {
  font-size: 12px;
  color: #8f9bb2;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.clock {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
  color: #f0c48a;
}

.ghost {
  color: #c8d3e6;
  border-color: rgba(200, 211, 230, 0.28);
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 10px;
  margin: 14px 0;
}

.kpi {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 12px 14px;
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(200, 211, 230, 0.12);
}

.k-value {
  font-size: 20px;
  font-weight: 700;
  color: #ffd9a4;
  font-variant-numeric: tabular-nums;
}

.k-label {
  font-size: 12px;
  color: #93a0b8;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.panel {
  padding: 14px 16px;
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(200, 211, 230, 0.12);
}

.panel.span2 {
  grid-column: span 2;
}

.panel-title {
  margin: 0 0 8px;
  font-size: 13.5px;
  font-weight: 600;
  color: #dbe4f2;
}

.mini-list {
  list-style: none;
  margin: 4px 0 10px;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.mini-list li {
  display: flex;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: rgba(200, 211, 230, 0.06);
  font-size: 12.5px;
  color: #b9c4d8;
}

.mini-list b {
  color: #ffd9a4;
  font-variant-numeric: tabular-nums;
}

.ai-note {
  font-size: 12px;
  color: #8f9bb2;
}

@media (max-width: 1360px) {
  .kpi-row {
    grid-template-columns: repeat(4, 1fr);
  }

  .grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .panel.span2 {
    grid-column: span 2;
  }
}

@media (max-width: 767px) {
  .screen {
    margin: -12px -12px -22px;
    padding: 12px;
  }

  .kpi-row,
  .grid {
    grid-template-columns: 1fr 1fr;
  }

  .panel.span2 {
    grid-column: span 2;
  }

  .screen-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
