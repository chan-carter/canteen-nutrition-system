<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">健康档案</h2>
        <p class="page-desc">填写身体信息，系统按 Mifflin-St Jeor 公式推算基础代谢、每日消耗与营养目标</p>
      </div>
      <el-button :icon="Refresh" text bg @click="load">重新读取</el-button>
    </div>

    <el-row :gutter="14">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="block">
          <template #header><span class="block-title">身体信息</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="88px" v-loading="loading">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="用于计算年龄"
              />
            </el-form-item>
            <el-form-item label="身高(cm)" prop="heightCm">
              <el-input-number v-model="form.heightCm" :min="100" :max="230" :precision="1" :step="0.5" />
            </el-form-item>
            <el-form-item label="体重(kg)" prop="weightKg">
              <el-input-number v-model="form.weightKg" :min="25" :max="250" :precision="1" :step="0.5" />
            </el-form-item>
            <el-form-item label="活动量" prop="activityLevel">
              <el-select v-model="form.activityLevel">
                <el-option v-for="a in activityLevels" :key="a.value" :label="a.label" :value="a.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="健康目标" prop="healthGoal">
              <el-radio-group v-model="form.healthGoal">
                <el-radio-button v-for="g in healthGoals" :key="g" :value="g">{{ g }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="submit">保存并计算</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="block">
          <template #header><span class="block-title">计算结果</span></template>
          <el-empty v-if="!profile.id" :image-size="60" description="还没有档案，先填写左侧信息" />
          <template v-else>
            <div class="metric-grid">
              <div class="metric">
                <span class="m-value">{{ bmi }}</span>
                <span class="m-label">BMI {{ bmiLevel }}</span>
              </div>
              <div class="metric">
                <span class="m-value">{{ round(profile.bmr) }}</span>
                <span class="m-label">基础代谢 kcal/天</span>
              </div>
              <div class="metric">
                <span class="m-value">{{ round(profile.tdee) }}</span>
                <span class="m-label">每日总消耗 kcal</span>
              </div>
              <div class="metric is-primary">
                <span class="m-value">{{ round(profile.targetCalorie) }}</span>
                <span class="m-label">{{ profile.healthGoal }}目标热量 kcal</span>
              </div>
            </div>

            <div class="macro-list">
              <div v-for="m in macroRows" :key="m.label" class="macro-row">
                <span class="macro-name">{{ m.label }}</span>
                <el-progress :percentage="m.percent" :color="m.color" :stroke-width="10" :show-text="false" />
                <span class="macro-num">{{ m.gram }} g</span>
              </div>
            </div>
            <div class="muted tip">
              按 {{ profile.healthGoal }}目标分配：蛋白质按体重每公斤计，脂肪占总热量 25%，其余由碳水补齐。数值为估算，仅供参考。
            </div>
          </template>
        </el-card>

        <el-card shadow="never" class="block">
          <template #header><span class="block-title">身高体重速览</span></template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="年龄">{{ age ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="活动量">{{ activityLabel }}</el-descriptions-item>
            <el-descriptions-item label="身高">{{ profile.heightCm || '-' }} cm</el-descriptions-item>
            <el-descriptions-item label="体重">{{ profile.weightKg || '-' }} kg</el-descriptions-item>
          </el-descriptions>
          <div class="muted tip">记录体重变化请到「营养追踪」页面。</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getHealthProfile, saveHealthProfile } from '@/api/health'
import { activityLevels, healthGoals } from '@/utils/dict'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const profile = ref({})

const form = reactive({
  gender: 1,
  birthday: '',
  heightCm: 175,
  weightKg: 65,
  activityLevel: 2,
  healthGoal: userStore.info.healthGoal || '均衡饮食'
})

const rules = {
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  birthday: [{ required: true, message: '请选择出生日期', trigger: 'change' }],
  heightCm: [{ required: true, message: '请填写身高', trigger: 'blur' }],
  weightKg: [{ required: true, message: '请填写体重', trigger: 'blur' }],
  activityLevel: [{ required: true, message: '请选择活动量', trigger: 'change' }],
  healthGoal: [{ required: true, message: '请选择健康目标', trigger: 'change' }]
}

const round = (value) => Math.round(Number(value || 0))
const one = (value) => Number(value || 0).toFixed(1)

const bmi = computed(() => {
  const h = Number(profile.value.heightCm || 0) / 100
  const w = Number(profile.value.weightKg || 0)
  if (!h || !w) return '-'
  return (w / (h * h)).toFixed(1)
})

const bmiLevel = computed(() => {
  const v = Number(bmi.value)
  if (!v) return ''
  if (v < 18.5) return '偏瘦'
  if (v < 24) return '正常'
  if (v < 28) return '偏胖'
  return '肥胖'
})

const age = computed(() => {
  if (!profile.value.birthday) return null
  const b = new Date(profile.value.birthday)
  const now = new Date()
  let a = now.getFullYear() - b.getFullYear()
  if (now.getMonth() < b.getMonth() || (now.getMonth() === b.getMonth() && now.getDate() < b.getDate())) a -= 1
  return a
})

const activityLabel = computed(() => {
  const hit = activityLevels.find((a) => a.value === profile.value.activityLevel)
  return hit ? hit.label : '-'
})

const macroRows = computed(() => {
  const p = profile.value
  const cal = Number(p.targetCalorie || 0)
  if (!cal) return []
  return [
    { label: '蛋白质', gram: one(p.targetProtein), percent: Math.min(100, round((p.targetProtein * 4 / cal) * 100)), color: '#2fa36b' },
    { label: '脂肪', gram: one(p.targetFat), percent: Math.min(100, round((p.targetFat * 9 / cal) * 100)), color: '#f08c2e' },
    { label: '碳水', gram: one(p.targetCarb), percent: Math.min(100, round((p.targetCarb * 4 / cal) * 100)), color: '#4d8ef0' }
  ]
})

const load = async () => {
  loading.value = true
  try {
    const res = await getHealthProfile()
    profile.value = res.data || {}
    if (res.data) {
      Object.assign(form, {
        gender: res.data.gender,
        birthday: res.data.birthday,
        heightCm: Number(res.data.heightCm),
        weightKg: Number(res.data.weightKg),
        activityLevel: res.data.activityLevel,
        healthGoal: res.data.healthGoal
      })
    }
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const res = await saveHealthProfile(form)
    profile.value = res.data || {}
    ElMessage.success('档案已保存，营养目标已重新计算')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.block {
  margin-bottom: 14px;
}

.block-title {
  font-weight: 600;
}

/* 输入类控件统一宽度，右边缘才不会再参差（原来 180/200/100% 混着写） */
.block :deep(.el-form-item__content > .el-input),
.block :deep(.el-form-item__content > .el-select),
.block :deep(.el-form-item__content > .el-input-number),
.block :deep(.el-form-item__content > .el-date-editor) {
  width: 100%;
  max-width: 300px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px;
  border-radius: var(--radius-sm);
  background: var(--surface-sunken);
  border: 1px solid var(--line);
}

.metric.is-primary {
  background: var(--brand-soft);
  border-color: #f7dcc0;
}

.m-value {
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.m-label {
  font-size: 12px;
  color: var(--ink-faint);
}

.macro-row {
  display: grid;
  grid-template-columns: 52px 1fr 62px;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.macro-name {
  font-size: 13px;
  color: var(--ink-soft);
}

.macro-num {
  text-align: right;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.tip {
  margin-top: 6px;
  font-size: 12px;
  line-height: 18px;
}

@media (max-width: 767px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
