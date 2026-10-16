<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">购物车</h2>
        <div class="page-desc">下单前请确认数量与预计热量，不同食堂的菜品需分开下单</div>
      </div>
      <el-button v-if="items.length" text type="danger" :icon="Delete" @click="userStore.clearCart()">
        清空购物车
      </el-button>
    </div>

    <el-empty v-if="!items.length" description="购物车为空，先去菜品浏览或智能推荐中添加菜品">
      <el-button type="primary" @click="$router.push('/student/dishes')">去浏览菜品</el-button>
    </el-empty>

    <el-row v-else :gutter="16">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="list-card">
          <ul class="cart-list">
            <li v-for="row in items" :key="row.dishId" class="cart-item">
              <div class="thumb" :style="thumbStyle(row)">{{ row.dishName.charAt(0) }}</div>
              <div class="meta">
                <div class="name">{{ row.dishName }}</div>
                <div class="muted">
                  单价 ￥{{ row.price.toFixed(2) }}
                  <template v-if="row.calorie"> · {{ row.calorie }} kcal/份</template>
                </div>
              </div>
              <div class="ops">
                <el-input-number
                  :model-value="row.quantity"
                  size="small"
                  :min="1"
                  :max="20"
                  @change="(value) => userStore.updateCartQuantity(row.dishId, value)"
                />
                <span class="subtotal price">￥{{ (row.price * row.quantity).toFixed(2) }}</span>
                <el-button size="small" text type="danger" @click="userStore.removeCartItem(row.dishId)">移除</el-button>
              </div>
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="sum-card">
          <template #header>
            <span class="sum-title">费用合计</span>
            <span class="muted">共 {{ userStore.cartCount }} 份</span>
          </template>

          <div class="total-row">
            <span>预计金额</span>
            <span class="total">￥{{ userStore.cartAmount }}</span>
          </div>
          <div class="total-row calorie">
            <span>预计摄入热量</span>
            <span class="total-cal">{{ userStore.cartCalorie }} kcal</span>
          </div>

          <el-divider />

          <el-form label-position="top" class="sum-form">
            <el-form-item label="用餐方式">
              <el-radio-group v-model="form.orderType">
                <el-radio-button value="PICKUP">点餐自取</el-radio-button>
                <el-radio-button value="RESERVE">提前预订</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="如：少辣、不要香菜" />
            </el-form-item>
          </el-form>

          <el-alert
            v-if="crossMerchant"
            title="购物车中包含不同食堂的菜品，需要分食堂分别下单"
            type="warning"
            :closable="false"
            show-icon
            class="warn"
          />

          <el-button type="primary" size="large" class="submit" :loading="submitting" @click="submit">提交订单</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { pageDishes } from '@/api/dish'
import { createOrder } from '@/api/business'

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const form = ref({ orderType: 'PICKUP', remark: '' })
const items = computed(() => userStore.cartItems)

// 购物车只缓存下单需要的字段，没有图片，用菜名首字生成稳定配色
const thumbStyle = (row) => {
  const hue = ((row.dishId || 0) * 47 + (row.dishName.charCodeAt(0) % 23) * 7 + 20) % 360
  return { background: `linear-gradient(135deg, hsl(${hue} 82% 66%), hsl(${(hue + 26) % 360} 76% 49%))` }
}

// 后端要求同一订单的菜品属于同一商户，这里提前给出提示
const dishMerchantMap = ref({})
const crossMerchant = computed(() => {
  const ids = items.value.map((item) => dishMerchantMap.value[item.dishId]).filter((id) => id !== undefined)
  return new Set(ids).size > 1
})

const loadMerchantMap = async () => {
  try {
    const res = await pageDishes({ page: 1, pageSize: 100 })
    const map = {}
    ;(res.data.rows || []).forEach((dish) => {
      map[dish.id] = dish.merchantId
    })
    dishMerchantMap.value = map
  } catch (e) {
    dishMerchantMap.value = {}
  }
}

const submit = async () => {
  if (!items.value.length) {
    ElMessage.warning('购物车为空')
    return
  }
  submitting.value = true
  try {
    const res = await createOrder({
      items: items.value.map((item) => ({ dishId: item.dishId, quantity: item.quantity })),
      orderType: form.value.orderType,
      remark: form.value.remark
    })
    userStore.clearCart()
    ElMessage.success(`下单成功，订单号 ${res.data.orderNo}`)
    router.push(`/order/${res.data.id}`)
  } catch (e) {
    loadMerchantMap()
  } finally {
    submitting.value = false
  }
}

loadMerchantMap()
</script>

<style scoped>
.list-card,
.sum-card {
  border-radius: var(--radius);
}

.cart-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.cart-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.cart-item:first-child {
  padding-top: 2px;
}

.thumb {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  flex: none;
  border-radius: 12px;
  color: #fff;
  font-size: 21px;
  font-weight: 700;
}

.meta {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 3px;
}

.ops {
  display: flex;
  align-items: center;
  gap: 14px;
}

.subtotal {
  width: 76px;
  text-align: right;
  font-size: 15px;
}

.sum-card :deep(.el-card__header) {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.sum-title {
  font-weight: 650;
}

.total-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 5px 0;
  color: var(--ink-soft);
}

.total-row .total {
  font-size: 26px;
  font-weight: 700;
  color: var(--brand-strong);
  font-variant-numeric: tabular-nums;
}

.total-cal {
  font-size: 16px;
  font-weight: 650;
  color: var(--success);
  font-variant-numeric: tabular-nums;
}

.sum-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.warn {
  margin-bottom: 12px;
  border-radius: var(--radius-sm);
}

.submit {
  width: 100%;
  font-weight: 650;
  letter-spacing: 2px;
}

@media (max-width: 768px) {
  .ops {
    flex-direction: column;
    align-items: flex-end;
    gap: 6px;
  }

  .subtotal {
    width: auto;
  }
}
</style>
