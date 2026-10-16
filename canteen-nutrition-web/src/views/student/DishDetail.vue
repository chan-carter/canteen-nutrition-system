<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div class="head-left">
        <el-button text :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <div>
          <h2 class="page-title">菜品详情</h2>
          <div class="page-desc">营养数据来自AI分析或内置食材估算表，仅供参考</div>
        </div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="side-card">
          <div class="cover" :style="coverStyle">
            <img v-if="dish.image" :src="dish.image" :alt="dish.name" />
            <div v-else class="cover-text">{{ dish.name ? dish.name.charAt(0) : '' }}</div>
          </div>

          <div class="title-row">
            <h3 class="dish-name">{{ dish.name }}</h3>
            <span class="price">￥{{ dish.price }}</span>
          </div>

          <div class="meta-chips">
            <span class="chip">{{ dish.stallName }}</span>
            <span class="chip">{{ dish.categoryName }}</span>
            <span class="chip is-hot">已售 {{ dish.sales }} 份</span>
          </div>

          <div v-if="allergens.length" class="allergen-row">
            <span class="k">过敏原</span>
            <span
              v-for="a in allergens"
              :key="a.id"
              class="allergen"
              :class="{ 'is-taboo': isTaboo(a.name) }"
            >
              <el-icon><WarningFilled /></el-icon>{{ a.name }}
            </span>
          </div>
          <div v-else class="allergen-row">
            <span class="k">过敏原</span>
            <span class="muted">未标注</span>
          </div>

          <div class="meta-block">
            <div class="meta-line">
              <span class="k">主要食材</span>
              <span class="v">{{ dish.ingredients || '-' }}</span>
            </div>
            <div class="meta-line">
              <span class="k">菜品介绍</span>
              <span class="v">{{ dish.description || '-' }}</span>
            </div>
          </div>

          <div class="actions">
            <el-input-number v-model="quantity" :min="1" :max="20" size="small" />
            <el-button type="primary" :icon="ShoppingCart" class="buy" @click="addCart(quantity)">加入购物车</el-button>
            <el-button
              :icon="StarFilled"
              :type="favorited ? 'warning' : 'default'"
              :plain="!favorited"
              circle
              @click="toggleFavorite"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="nutri-card">
          <template #header>
            <div class="card-head">
              <span>AI营养分析</span>
              <span v-if="nutrition" class="chip is-green">{{ nutrition.source }}</span>
            </div>
          </template>
          <template v-if="nutrition">
            <div class="nutrition-grid">
              <div v-for="m in nutrientMetrics" :key="m.label" class="metric" :class="m.tone">
                <span class="value">{{ m.value }}</span>
                <span class="unit">{{ m.unit }}</span>
                <span class="label">{{ m.label }}</span>
              </div>
            </div>
            <p class="analysis">{{ nutrition.analysisText }}</p>
            <el-alert
              title="数值由AI模型或内置食材估算表计算，为估算值，仅供参考，不作为专业营养诊断依据。"
              type="info"
              :closable="false"
              show-icon
              class="disclaim"
            />
          </template>
          <el-empty v-else description="该菜品暂无营养分析数据，可提示食堂商户发起营养分析" :image-size="90" />
        </el-card>

        <el-card shadow="never" class="review-card">
          <template #header>
            <div class="card-head">
              <span>学生评价</span>
              <span class="muted">{{ reviews.length }} 条</span>
            </div>
          </template>
          <el-empty v-if="!reviews.length" description="暂无评价" :image-size="80" />
          <div v-for="item in reviews" :key="item.id" class="review-item">
            <div class="avatar">{{ item.nickname ? item.nickname.charAt(0) : '学' }}</div>
            <div class="review-body">
              <div class="review-head">
                <span class="review-user">{{ item.nickname }}</span>
                <el-rate :model-value="item.rating" disabled size="small" />
                <span class="muted time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="review-text">{{ item.content }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, ArrowLeft, StarFilled, WarningFilled } from '@element-plus/icons-vue'
import { getDish, getNutrition } from '@/api/dish'
import { pageReviews } from '@/api/business'
import { addFavorite, cancelFavorite, getDishAllergens, listFavorites } from '@/api/interaction'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const dish = ref({})
const nutrition = ref(null)
const reviews = ref([])
const quantity = ref(1)
const allergens = ref([])
const favorited = ref(false)

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const coverStyle = computed(() => {
  const hue = ((dish.value.categoryId || 0) * 53 + (dish.value.name ? dish.value.name.charCodeAt(0) % 23 : 3) * 7 + 18) % 360
  return { background: `linear-gradient(135deg, hsl(${hue} 82% 66%), hsl(${(hue + 26) % 360} 76% 49%))` }
})

const nutrientMetrics = computed(() => {
  const n = nutrition.value || {}
  return [
    { label: '热量', unit: 'kcal', value: n.calorie ?? '-', tone: 'tone-amber' },
    { label: '蛋白质', unit: 'g', value: n.protein ?? '-', tone: 'tone-green' },
    { label: '脂肪', unit: 'g', value: n.fat ?? '-', tone: 'tone-red' },
    { label: '碳水', unit: 'g', value: n.carbohydrate ?? '-', tone: 'tone-blue' },
    { label: '膳食纤维', unit: 'g', value: n.fiber ?? '-', tone: 'tone-teal' },
    { label: '钠', unit: 'mg', value: n.sodium ?? '-', tone: 'tone-gray' }
  ]
})

const addCart = (count) => {
  userStore.addToCart({ ...dish.value, calorie: nutrition.value?.calorie ?? dish.value.calorie }, count)
  ElMessage.success(`已加入 ${count} 份 ${dish.value.name}`)
}

// 学生资料里的「忌口」是自由文本，命中即标红提示
const isTaboo = (name) => String(userStore.info.taboo || '').includes(name)

const toggleFavorite = async () => {
  if (favorited.value) {
    await cancelFavorite(dish.value.id)
    favorited.value = false
    ElMessage.success('已取消收藏')
  } else {
    await addFavorite(dish.value.id)
    favorited.value = true
    ElMessage.success('已加入收藏')
  }
}

onMounted(async () => {
  const id = route.params.id
  loading.value = true
  try {
    const res = await getDish(id)
    dish.value = res.data
    if (dish.value.nutritionFlag) {
      const nutritionRes = await getNutrition(id)
      nutrition.value = nutritionRes.data
    }
    const reviewRes = await pageReviews({ dishId: id, status: 1, page: 1, pageSize: 10 })
    reviews.value = reviewRes.data.rows || []
    const [allergenRes, favoriteRes] = await Promise.all([getDishAllergens(id), listFavorites()])
    allergens.value = allergenRes.data || []
    favorited.value = (favoriteRes.data || []).some((item) => item.dishId === Number(id))
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.head-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.side-card,
.nutri-card,
.review-card {
  margin-bottom: 16px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.cover {
  position: relative;
  height: 240px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 14px;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-text {
  font-size: 76px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
}

.title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.dish-name {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.title-row .price {
  font-size: 22px;
}

.meta-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0 14px;
}

.meta-block {
  border-top: 1px dashed var(--el-border-color-lighter);
  padding-top: 4px;
}

.allergen-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin: -4px 0 14px;
  font-size: 12.5px;
}

.allergen-row .k {
  color: var(--ink-faint);
}

.allergen {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #fff1f0;
  border: 1px solid #ffd8d5;
  color: #c4453d;
}

.allergen.is-taboo {
  background: var(--danger);
  border-color: var(--danger);
  color: #fff;
  font-weight: 600;
}

.meta-line {
  display: flex;
  gap: 10px;
  padding: 7px 0;
  font-size: 13px;
  line-height: 20px;
}

.meta-line .k {
  flex: none;
  width: 66px;
  color: var(--ink-faint);
}

.meta-line .v {
  color: var(--ink-soft);
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.actions .buy {
  flex: 1;
  font-weight: 650;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.metric {
  padding: 12px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--el-border-color-lighter);
  background: var(--surface-sunken);
  text-align: center;
}

.metric .value {
  display: block;
  font-size: 21px;
  font-weight: 700;
  line-height: 26px;
  font-variant-numeric: tabular-nums;
}

.metric .unit {
  font-size: 11px;
  color: var(--ink-faint);
}

.metric .label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-soft);
}

.metric.tone-amber .value { color: #d97317; }
.metric.tone-green .value { color: #2fa36b; }
.metric.tone-red .value { color: #e35d5d; }
.metric.tone-blue .value { color: #4b6cb7; }
.metric.tone-teal .value { color: #1f9ea3; }
.metric.tone-gray .value { color: #6b7686; }

.analysis {
  line-height: 23px;
  margin: 0 0 12px;
  color: var(--ink-soft);
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  background: var(--brand-soft);
}

.disclaim {
  border-radius: var(--radius-sm);
}

.review-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.review-item:last-child {
  border-bottom: none;
}

.avatar {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  flex: none;
  border-radius: 50%;
  font-weight: 650;
  color: #fff;
  background: linear-gradient(135deg, #f6b95f, #ef8b2c);
}

.review-body {
  flex: 1;
  min-width: 0;
}

.review-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.review-user {
  font-weight: 600;
}

.review-head .time {
  margin-left: auto;
}

.review-text {
  color: var(--ink-soft);
  line-height: 21px;
}

@media (max-width: 768px) {
  .cover {
    height: 180px;
  }

  .nutrition-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
