<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div class="head-left">
        <el-button text :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <div>
          <h2 class="page-title">订单详情</h2>
          <div class="page-desc">订单号 {{ order.orderNo || '-' }}</div>
        </div>
      </div>
      <el-tag v-if="order.status" :type="tagOf(orderStatusMap, order.status).type" effect="dark" round size="large">
        {{ tagOf(orderStatusMap, order.status).text }}
      </el-tag>
    </div>

    <el-card shadow="never" class="info-card">
      <el-descriptions :column="3" class="tidy-desc">
        <el-descriptions-item label="下单学生">{{ order.userNickname }}</el-descriptions-item>
        <el-descriptions-item label="食堂商户">{{ order.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="用餐方式">{{ orderTypeMap[order.orderType] || order.orderType }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="接单时间">{{ formatTime(order.acceptTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ formatTime(order.finishTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预计总热量">{{ order.totalCalorie ? order.totalCalorie + ' kcal' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="price">￥{{ order.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-steps v-if="order.status !== 4" :active="stepIndex" align-center finish-status="success" class="flow">
        <el-step title="已提交" description="等待食堂接单" />
        <el-step title="已接单" description="食堂备餐中" />
        <el-step title="待取餐" description="凭取餐码取餐" />
        <el-step title="已完成" description="欢迎评价" />
      </el-steps>

      <div v-if="showPickup" class="pickup">
        <div>
          <div class="pickup-label">取餐码</div>
          <div class="pickup-code">{{ order.pickupCode }}</div>
        </div>
        <div class="pickup-tip muted">
          到「{{ order.merchantName }}」报取餐码取餐{{ order.status === 5 ? '，餐已备好' : '，食堂正在备餐' }}
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="items-card">
      <template #header>
        <div class="card-head">
          <span>菜品明细</span>
          <span class="muted">共 {{ (order.items || []).length }} 项</span>
        </div>
      </template>
      <el-table :data="order.items || []">
        <el-table-column prop="dishName" label="菜品" min-width="140" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="单价" width="100">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="小计" width="110">
          <template #default="{ row }"><span class="price">￥{{ row.amount }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button
              v-if="canReview"
              size="small"
              text
              bg
              @click="openReview(row)"
            >
              评价
            </el-button>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="action-bar">
        <el-button v-if="order.status === 1 && isStudent" type="danger" plain @click="cancel">取消订单</el-button>
        <el-button v-if="nextAction" type="primary" @click="changeStatus(nextAction.status)">{{ nextAction.label }}</el-button>
        <el-button v-if="order.status === 3 && isStudent" type="success" @click="$router.push('/student/orders')">
          返回订单列表
        </el-button>
        <span v-if="!nextAction && order.status === 3" class="muted flow-done">订单已完成，可在明细中评价菜品</span>
        <span v-if="order.status === 4" class="muted flow-done">订单已取消{{ order.pickupCode ? `（取餐码 ${order.pickupCode} 作废）` : '' }}</span>
      </div>
    </el-card>

    <el-dialog v-model="reviewVisible" :title="`评价菜品：${current.dishName || ''}`" width="460px">
      <el-form :model="reviewForm" label-width="70px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" show-text :texts="['很差', '较差', '一般', '较好', '很好']" />
        </el-form-item>
        <el-form-item label="评价">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="口味、份量、营养搭配等方面的反馈"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getOrder, updateOrderStatus, addReview } from '@/api/business'
import { useUserStore } from '@/stores/user'
import { orderStatusMap, orderTypeMap, tagOf } from '@/utils/dict'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const order = ref({})
const reviewVisible = ref(false)
const reviewing = ref(false)
const current = ref({})
const reviewForm = ref({ rating: 5, content: '' })

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 19) : '')

const isStudent = computed(() => userStore.info.role === 1)
const isMerchant = computed(() => userStore.info.role === 2)
const isAdmin = computed(() => userStore.info.role === 3)
const canReview = computed(() => isStudent.value && order.value.status === 3)

const stepIndex = computed(() => ({ 1: 1, 2: 2, 5: 3, 3: 4 }[order.value.status] || 0))
const showPickup = computed(() => !!order.value.pickupCode && [2, 5].includes(order.value.status))

// 按后端状态机只暴露当前合法的一步，避免把已完成订单改回待接单
const nextAction = computed(() => {
  if (!isMerchant.value && !isAdmin.value) return null
  const map = {
    1: { status: 2, label: '接单' },
    2: { status: 5, label: '出餐待取' },
    5: { status: 3, label: '确认完成' }
  }
  return map[order.value.status] || null
})

const load = async () => {
  loading.value = true
  try {
    const res = await getOrder(route.params.id)
    order.value = res.data
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    loading.value = false
  }
}

const cancel = async () => {
  await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
  await updateOrderStatus(order.value.id, 4)
  ElMessage.success('订单已取消')
  load()
}

const changeStatus = async (status) => {
  await updateOrderStatus(order.value.id, status)
  ElMessage.success('订单状态已更新')
  load()
}

const openReview = (item) => {
  current.value = item
  reviewForm.value = { rating: 5, content: '' }
  reviewVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  reviewing.value = true
  try {
    await addReview({
      orderId: order.value.id,
      dishId: current.value.dishId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价提交成功')
    reviewVisible.value = false
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    reviewing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.head-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-card,
.items-card {
  margin-bottom: 16px;
}

.tidy-desc :deep(.el-descriptions__label) {
  width: 108px;
  padding-right: 10px !important;
}

.tidy-desc :deep(.el-descriptions__content) {
  color: var(--ink);
}

.flow {
  margin-top: 22px;
  padding-top: 20px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.flow-done {
  font-size: 12.5px;
}

.pickup {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 18px;
  padding: 14px 18px;
  border-radius: var(--radius);
  background: linear-gradient(120deg, var(--brand-soft), #fff);
  border: 1px solid #f7dcc0;
}

.pickup-label {
  font-size: 12px;
  color: var(--ink-faint);
}

.pickup-code {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 4px;
  color: var(--brand-strong);
  font-variant-numeric: tabular-nums;
}

.pickup-tip {
  font-size: 12.5px;
}

@media (max-width: 767px) {
  .pickup {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
