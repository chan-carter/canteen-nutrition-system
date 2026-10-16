<template>
  <div class="page">
    <div class="page-title">商户资料</div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card header="基本信息">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-form-item label="商户名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="联系人">
              <el-input v-model="form.contact" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="form.address" />
            </el-form-item>
            <el-form-item label="商户介绍">
              <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="经营状态">
              <el-tag :type="tagOf(merchantStatusMap, form.status).type">{{ tagOf(merchantStatusMap, form.status).text }}</el-tag>
              <span class="muted" style="margin-left: 8px">经营状态由管理员维护</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="save">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card header="本商户菜品销量Top10">
          <EChart :option="chartOption" height="320px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchant, updateMerchant } from '@/api/organization'
import { getDishSalesData } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { merchantStatusMap, tagOf } from '@/utils/dict'
import EChart from '@/components/EChart.vue'

const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)
const form = ref({})
const sales = ref([])

const rules = {
  name: [{ required: true, message: '请输入商户名称', trigger: 'blur' }]
}

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 70, right: 20, top: 34, bottom: 60 },
  xAxis: { type: 'category', data: sales.value.map((item) => item.name), axisLabel: { interval: 0, rotate: 30 } },
  yAxis: { type: 'value', name: '销量(份)', minInterval: 1 },
  series: [
    {
      type: 'bar',
      barMaxWidth: 32,
      itemStyle: { color: '#ef8b2c', borderRadius: [4, 4, 0, 0] },
      data: sales.value.map((item) => item.value)
    }
  ]
}))

const load = async () => {
  const merchantId = userStore.info.merchantId
  if (!merchantId) {
    ElMessage.warning('当前账号未绑定商户，请联系管理员处理')
    return
  }
  const res = await getMerchant(merchantId)
  form.value = res.data
  try {
    const salesRes = await getDishSalesData()
    sales.value = salesRes.data || []
  } catch (e) {
    sales.value = []
  }
}

const save = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateMerchant(form.value)
    ElMessage.success('商户资料已更新')
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await userStore.loadInfo()
  load()
})
</script>
