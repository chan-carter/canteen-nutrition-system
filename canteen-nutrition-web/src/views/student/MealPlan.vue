<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">周期餐计划</h2>
        <p class="page-desc">按健康目标与推荐算法生成一周搭配，早中晚自动分配，可逐餐采纳下单</p>
      </div>
      <el-tag v-if="userStore.info.healthGoal" type="success" effect="light" round>
        目标：{{ userStore.info.healthGoal }}
      </el-tag>
    </div>

    <el-card shadow="never" class="block">
      <el-form :inline="true">
        <el-form-item label="起始日期">
          <el-date-picker v-model="query.startDate" type="date" value-format="YYYY-MM-DD" :clearable="false" style="width: 150px" />
        </el-form-item>
        <el-form-item label="天数">
          <el-select v-model="query.days" style="width: 100px">
            <el-option v-for="d in [3, 5, 7]" :key="d" :label="`${d} 天`" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="MagicStick" :loading="generating" @click="generate">生成计划</el-button>
          <el-button :icon="Delete" plain @click="clear">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading">
      <el-empty v-if="!days.length && !loading" :image-size="70" description="还没有餐计划，点上方「生成计划」试一试" />

      <section v-for="day in days" :key="day.date" class="day-card">
        <header class="day-head">
          <div>
            <span class="day-date">{{ day.date }}</span>
            <span class="day-week">{{ weekday(day.date) }}</span>
          </div>
          <span class="chip is-hot">{{ day.total }} kcal</span>
        </header>
        <div class="meal-grid">
          <div v-for="meal in mealTypes.slice(0, 3)" :key="meal" class="meal-cell">
            <div class="meal-tag">{{ meal }}</div>
            <template v-if="day.items[meal]">
              <div class="meal-dish">{{ day.items[meal].dishName }}</div>
              <div class="muted meal-cal">{{ num(day.items[meal].plannedCalorie) }} kcal</div>
              <el-button
                size="small"
                :type="day.items[meal].adopted === 1 ? 'success' : 'primary'"
                :plain="day.items[meal].adopted === 1"
                :disabled="day.items[meal].adopted === 1"
                :loading="adopting === day.items[meal].id"
                @click="adopt(day.items[meal])"
              >
                {{ day.items[meal].adopted === 1 ? '已下单' : '加入购物车' }}
              </el-button>
            </template>
            <div v-else class="meal-empty muted">未搭配</div>
          </div>
        </div>
      </section>

      <p class="muted foot-tip">
        计划依据健康目标、推荐打分与忌口信息自动搭配，同一天重复的菜品会自动换下一名，实际热量请以菜品营养数据为准。
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, MagicStick } from '@element-plus/icons-vue'
import { adoptMealPlan, clearMealPlans, generateMealPlan, listMealPlans } from '@/api/health'
import { useUserStore } from '@/stores/user'
import { mealTypes } from '@/utils/dict'

const userStore = useUserStore()
const loading = ref(false)
const generating = ref(false)
const adopting = ref(0)
const rows = ref([])

const tomorrow = () => {
  const d = new Date(Date.now() + 86400000)
  return d.toISOString().slice(0, 10)
}

const query = reactive({ startDate: tomorrow(), days: 7 })

const num = (value) => (value === null || value === undefined ? 0 : Math.round(Number(value)))

const weekday = (date) => '周' + '日一二三四五六'[new Date(date).getDay()]

const days = computed(() => {
  const map = new Map()
  rows.value.forEach((item) => {
    if (!map.has(item.planDate)) {
      map.set(item.planDate, { date: item.planDate, items: {}, total: 0 })
    }
    const day = map.get(item.planDate)
    day.items[item.mealType] = item
    day.total += num(item.plannedCalorie)
  })
  return [...map.values()].sort((a, b) => String(a.date).localeCompare(String(b.date)))
})

const load = async () => {
  loading.value = true
  try {
    const res = await listMealPlans(query)
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}

const generate = async () => {
  generating.value = true
  try {
    const res = await generateMealPlan({ startDate: query.startDate, days: query.days })
    ElMessage.success(`已生成 ${res.data.count} 条餐计划`)
    await load()
  } finally {
    generating.value = false
  }
}

const clear = async () => {
  await ElMessageBox.confirm('确认清空这段时间的餐计划？', '提示', { type: 'warning' })
  await clearMealPlans(query)
  ElMessage.success('已清空')
  await load()
}

const adopt = async (item) => {
  adopting.value = item.id
  try {
    userStore.addToCart({ dishId: item.dishId, dishName: item.dishName, calorie: item.plannedCalorie }, 1)
    await adoptMealPlan(item.id)
    item.adopted = 1
    ElMessage.success(`已加入购物车：${item.dishName}`)
  } finally {
    adopting.value = 0
  }
}

onMounted(load)
</script>

<style scoped>
.block {
  margin-bottom: 14px;
}

.block :deep(.el-card__body) {
  padding-bottom: 2px;
}

.day-card {
  padding: 14px 16px;
  margin-bottom: 12px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
}

.day-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.day-date {
  font-weight: 650;
  font-size: 15px;
}

.day-week {
  margin-left: 8px;
  font-size: 12px;
  color: var(--ink-faint);
}

.meal-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.meal-cell {
  padding: 12px;
  border-radius: var(--radius-sm);
  background: var(--surface-sunken);
  border: 1px solid var(--line);
  min-height: 108px;
}

.meal-tag {
  font-size: 12px;
  color: var(--brand-strong);
  font-weight: 600;
  margin-bottom: 6px;
}

.meal-dish {
  font-weight: 600;
  font-size: 14.5px;
  margin-bottom: 2px;
}

.meal-cal {
  font-size: 12px;
  margin-bottom: 8px;
}

.meal-empty {
  padding: 14px 0;
  text-align: center;
  font-size: 12.5px;
}

.foot-tip {
  margin-top: 4px;
  font-size: 12px;
  line-height: 18px;
}

@media (max-width: 767px) {
  .meal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
