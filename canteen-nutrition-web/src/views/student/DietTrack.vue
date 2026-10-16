<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">营养追踪</h2>
        <p class="page-desc">记录每一餐的营养摄入，对照每日目标查看进度与体重变化</p>
      </div>
      <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" :clearable="false" @change="loadAll" />
    </div>

    <el-alert
      v-if="daily.date && !daily.hasProfile"
      title="还没有健康档案，暂时无法给出每日热量与营养素目标，请先到「健康档案」页面填写。"
      type="warning"
      :closable="false"
      show-icon
      class="tip"
    />

    <el-row :gutter="14">
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="block">
          <template #header><span class="block-title">{{ date }} 摄入进度</span></template>
          <div class="ring-wrap">
            <el-progress
              type="circle"
              :percentage="caloriePercent"
              :width="132"
              :stroke-width="12"
              :color="ringColor"
            >
              <template #default>
                <div class="ring-num">{{ num(daily.calorie) }}</div>
                <div class="ring-unit">/ {{ daily.targetCalorie ? num(daily.targetCalorie) : '—' }} kcal</div>
                <div class="ring-sub">{{ daily.recordCount || 0 }} 条记录</div>
              </template>
            </el-progress>
          </div>
          <div class="macro-row" v-for="m in macroBars" :key="m.label">
            <span class="macro-name">{{ m.label }}</span>
            <el-progress :percentage="m.percent" :color="m.color" :stroke-width="8" :show-text="false" />
            <span class="macro-num">{{ num(m.value) }}/{{ m.target ? num(m.target) : '—' }}g</span>
          </div>
          <div class="sodium">
            <span>钠摄入</span>
            <b>{{ num(daily.sodium) }} mg</b>
            <span class="muted">建议不超过 2000 mg</span>
          </div>
        </el-card>

        <el-card shadow="never" class="block">
          <template #header>
            <div class="head-line">
              <span class="block-title">体重趋势</span>
              <el-button size="small" text bg :icon="Plus" @click="weightDialog = true">记体重</el-button>
            </div>
          </template>
          <EChart v-if="weightOption" :option="weightOption" height="190px" />
          <el-empty v-else :image-size="52" description="暂无体重记录" />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="block">
          <template #header>
            <div class="head-line">
              <span class="block-title">饮食日记</span>
              <div>
                <el-button size="small" text bg :icon="Refresh" @click="loadAll">刷新</el-button>
                <el-button size="small" type="primary" :icon="Plus" @click="openDiet">记一笔</el-button>
              </div>
            </div>
          </template>

          <el-empty v-if="!records.length && !loading" :image-size="60" description="这一天还没有记录" />
          <div v-else class="meal-group" v-for="meal in mealTypes" :key="meal">
            <template v-if="grouped[meal].length">
              <div class="meal-head">
                <span class="meal-name">{{ meal }}</span>
                <span class="chip is-hot">{{ sum(grouped[meal], 'calorie') }} kcal</span>
                <span class="chip is-green">蛋白 {{ sum(grouped[meal], 'protein') }}g</span>
                <span class="chip">脂肪 {{ sum(grouped[meal], 'fat') }}g</span>
                <span class="chip">碳水 {{ sum(grouped[meal], 'carbohydrate') }}g</span>
              </div>
              <div v-for="item in grouped[meal]" :key="item.id" class="diet-row">
                <div class="diet-main">
                  <span class="diet-name">{{ item.dishName }}</span>
                  <el-tag size="small" effect="plain" :type="item.source === 'order' ? 'success' : 'info'">
                    {{ dietSources[item.source] || '手动录入' }}
                  </el-tag>
                </div>
                <div class="diet-num muted">
                  {{ num(item.calorie) }} kcal · 蛋白 {{ num(item.protein) }}g · 脂肪 {{ num(item.fat) }}g · 碳水 {{ num(item.carbohydrate) }}g
                </div>
                <el-button size="small" text type="danger" :icon="Delete" @click="removeDiet(item)" />
              </div>
            </template>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dietDialog" title="添加饮食记录" width="480px">
      <el-form :model="dietForm" label-width="88px">
        <el-form-item label="选择菜品">
          <el-select
            v-model="dietForm.dishId"
            filterable
            clearable
            placeholder="从菜品库选择，自动带出营养"
            style="width: 100%"
            @change="pickDish"
          >
            <el-option v-for="d in dishes" :key="d.id" :label="`${d.name}（${d.calorie ?? '未分析'} kcal）`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="dietForm.dishName" placeholder="手动录入时填写，如「美式咖啡」" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-radio-group v-model="dietForm.mealType">
            <el-radio-button v-for="m in mealTypes" :key="m" :value="m">{{ m }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="热量(kcal)">
          <el-input-number v-model="dietForm.calorie" :min="0" :precision="1" style="width: 150px" />
        </el-form-item>
        <el-form-item label="营养素">
          <div class="macro-inputs">
            <el-input-number v-model="dietForm.protein" :min="0" :precision="1" placeholder="蛋白" controls-position="right" />
            <el-input-number v-model="dietForm.fat" :min="0" :precision="1" placeholder="脂肪" controls-position="right" />
            <el-input-number v-model="dietForm.carbohydrate" :min="0" :precision="1" placeholder="碳水" controls-position="right" />
          </div>
        </el-form-item>
        <el-form-item label="钠(mg)">
          <el-input-number v-model="dietForm.sodium" :min="0" :precision="1" style="width: 150px" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="dietForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dietDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveDiet">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="weightDialog" title="记录体重" width="380px">
      <el-form :model="weightForm" label-width="80px">
        <el-form-item label="体重(kg)">
          <el-input-number v-model="weightForm.weightKg" :min="25" :max="250" :precision="1" :step="0.1" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="weightForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="weightForm.note" placeholder="可选，如晨起空腹" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="weightDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveWeight">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Delete } from '@element-plus/icons-vue'
import { addDietRecord, addWeightLog, deleteDietRecord, getDailyIntake, getWeightTrend, listDietRecords } from '@/api/health'
import { pageDishes } from '@/api/dish'
import EChart from '@/components/EChart.vue'
import { dietSources, mealTypes } from '@/utils/dict'

const today = () => new Date().toISOString().slice(0, 10)

const date = ref(today())
const daily = ref({})
const records = ref([])
const dishes = ref([])
const weights = ref([])
const loading = ref(false)
const saving = ref(false)
const dietDialog = ref(false)
const weightDialog = ref(false)

const dietForm = reactive({
  dishId: null, dishName: '', recordDate: today(), mealType: '午餐',
  calorie: 0, protein: 0, fat: 0, carbohydrate: 0, sodium: 0
})
const weightForm = reactive({ weightKg: 65, recordDate: today(), note: '' })

const num = (value) => (value === null || value === undefined ? 0 : Math.round(Number(value) * 10) / 10)
const sum = (list, field) => num(list.reduce((acc, item) => acc + Number(item[field] || 0), 0))

const caloriePercent = computed(() => {
  const target = Number(daily.value.targetCalorie || 0)
  if (!target) return 0
  return Math.min(100, Math.round((Number(daily.value.calorie || 0) / target) * 100))
})

const ringColor = (percent) => (percent >= 100 ? '#e35d5d' : percent >= 80 ? '#f08c2e' : '#2fa36b')

const macroBars = computed(() => {
  const d = daily.value
  const rows = [
    { label: '蛋白质', value: d.protein, target: d.targetProtein, color: '#2fa36b' },
    { label: '脂肪', value: d.fat, target: d.targetFat, color: '#f08c2e' },
    { label: '碳水', value: d.carbohydrate, target: d.targetCarb, color: '#4d8ef0' }
  ]
  return rows.map((r) => ({
    ...r,
    percent: r.target ? Math.min(100, Math.round((Number(r.value || 0) / Number(r.target)) * 100)) : 0
  }))
})

const grouped = computed(() => {
  const map = {}
  mealTypes.forEach((m) => (map[m] = []))
  records.value.forEach((item) => {
    (map[item.mealType] || map['加餐']).push(item)
  })
  return map
})

const weightOption = computed(() => {
  if (!weights.value.length) return null
  const list = [...weights.value].sort((a, b) => String(a.recordDate).localeCompare(String(b.recordDate)))
  return {
    grid: { left: 38, right: 14, top: 18, bottom: 26 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: list.map((w) => String(w.recordDate).slice(5)), axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', scale: true, axisLabel: { fontSize: 11 } },
    series: [{
      type: 'line',
      smooth: true,
      data: list.map((w) => Number(w.weightKg)),
      areaStyle: { opacity: 0.12 },
      lineStyle: { width: 2.4, color: '#f08c2e' },
      itemStyle: { color: '#f08c2e' }
    }]
  }
})

const loadDaily = async () => {
  const res = await getDailyIntake(date.value)
  daily.value = res.data || {}
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await listDietRecords({ date: date.value })
    records.value = res.data || []
  } finally {
    loading.value = false
  }
}

const loadWeights = async () => {
  const res = await getWeightTrend(60)
  weights.value = res.data || []
}

const loadAll = async () => {
  await Promise.all([loadDaily(), loadRecords(), loadWeights()])
}

const openDiet = () => {
  Object.assign(dietForm, {
    dishId: null, dishName: '', recordDate: date.value, mealType: '午餐',
    calorie: 0, protein: 0, fat: 0, carbohydrate: 0, sodium: 0
  })
  dietDialog.value = true
}

const pickDish = (id) => {
  const dish = dishes.value.find((d) => d.id === id)
  if (!dish) return
  Object.assign(dietForm, {
    dishName: dish.name,
    calorie: Number(dish.calorie || 0),
    protein: Number(dish.protein || 0),
    fat: Number(dish.fat || 0),
    carbohydrate: Number(dish.carbohydrate || 0)
  })
}

const saveDiet = async () => {
  if (!dietForm.dishName) {
    ElMessage.warning('请选择菜品或填写名称')
    return
  }
  saving.value = true
  try {
    await addDietRecord({ ...dietForm, dishId: dietForm.dishId || null })
    dietDialog.value = false
    date.value = dietForm.recordDate
    ElMessage.success('已记录')
    await loadAll()
  } finally {
    saving.value = false
  }
}

const removeDiet = async (item) => {
  await ElMessageBox.confirm(`删除记录「${item.dishName}」？`, '提示', { type: 'warning' })
  await deleteDietRecord(item.id)
  ElMessage.success('已删除')
  await loadAll()
}

const saveWeight = async () => {
  saving.value = true
  try {
    await addWeightLog({ ...weightForm })
    weightDialog.value = false
    ElMessage.success('体重已记录')
    await loadWeights()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadAll()
  try {
    const res = await pageDishes({ page: 1, pageSize: 100, status: 1, auditStatus: 1 })
    dishes.value = res.data.rows || []
  } catch (e) {
    dishes.value = []
  }
})
</script>

<style scoped>
.block {
  margin-bottom: 14px;
}

.block-title {
  font-weight: 600;
}

.head-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip {
  margin-bottom: 14px;
  border-radius: var(--radius-sm);
}

.ring-wrap {
  display: flex;
  justify-content: center;
  padding: 6px 0 14px;
}

.ring-num {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.ring-unit {
  font-size: 12px;
  color: var(--ink-faint);
}

.ring-sub {
  margin-top: 3px;
  font-size: 11px;
  color: var(--ink-faint);
}

.macro-row {
  display: grid;
  grid-template-columns: 46px 1fr 84px;
  align-items: center;
  gap: 8px;
  margin-bottom: 9px;
}

.macro-name {
  font-size: 12.5px;
  color: var(--ink-soft);
}

.macro-num {
  font-size: 12px;
  text-align: right;
  color: var(--ink-soft);
  font-variant-numeric: tabular-nums;
}

.sodium {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px dashed var(--line);
  font-size: 12.5px;
}

.meal-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 6px 0 8px;
}

.meal-name {
  font-weight: 650;
  font-size: 14px;
}

.diet-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto 44px;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  background: var(--surface-sunken);
  margin-bottom: 6px;
}

.diet-row:hover {
  background: #fff;
  border: 1px solid var(--line);
}

.diet-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.diet-name {
  font-weight: 600;
  font-size: 13.5px;
}

.diet-num {
  font-size: 12px;
  white-space: nowrap;
}

.macro-inputs {
  display: flex;
  gap: 6px;
  width: 100%;
}

.macro-inputs :deep(.el-input-number) {
  flex: 1;
}

@media (max-width: 767px) {
  .diet-row {
    grid-template-columns: minmax(0, 1fr) 44px;
  }

  .diet-num {
    grid-column: 1 / -1;
    white-space: normal;
  }
}
</style>
