<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">智能推荐</h2>
        <div class="page-desc">结合健康目标、饮食偏好与忌口信息打分排序，结果仅供参考</div>
      </div>
      <el-tag v-if="userStore.info.healthGoal" type="success" effect="light" round>
        当前目标：{{ userStore.info.healthGoal }}
      </el-tag>
    </div>

    <el-alert
      title="推荐结果依据健康目标、饮食偏好、忌口信息与AI营养分析数据计算，用于辅助选择菜品，不替代专业营养建议。"
      type="info"
      :closable="false"
      show-icon
      class="tip"
    />

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="健康目标">
          <el-select v-model="query.healthGoal" placeholder="按账号资料" clearable style="width: 140px">
            <el-option v-for="item in healthGoals" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select v-model="query.categoryId" placeholder="不限" clearable style="width: 130px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单份最高热量">
          <el-input-number v-model="query.maxCalories" :min="50" :max="2000" :step="50" style="width: 130px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">重新计算</el-button>
          <el-button :icon="Refresh" :loading="refreshing" @click="saveRecommendations">保存推荐记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading">
      <el-empty v-if="!rows.length && !loading" description="没有符合条件的菜品，请先完善菜品营养分析数据或放宽筛选条件" />
      <article v-for="(item, index) in rows" :key="item.dishId" class="rec-card">
        <span class="rank" :class="rankClass(index)">
          {{ (query.page - 1) * query.pageSize + index + 1 }}
        </span>

        <div class="cover" :style="coverStyle(item)">
          <img v-if="item.image" :src="item.image" :alt="item.dishName" />
          <span v-else>{{ item.dishName.charAt(0) }}</span>
        </div>

        <div class="rec-main">
          <div class="rec-head">
            <span class="rec-name">{{ item.dishName }}</span>
            <span class="chip">{{ item.stallName }}</span>
            <span class="price">￥{{ item.price }}</span>
          </div>
          <div class="tags">
            <span class="chip is-hot">{{ item.calorie }} kcal</span>
            <span class="chip is-green">蛋白 {{ item.protein }}g</span>
            <span class="chip">脂肪 {{ item.fat }}g</span>
            <span class="chip">碳水 {{ item.carbohydrate ?? '-' }}g</span>
            <span v-if="item.avgRating" class="chip">★ 评分 {{ item.avgRating }}</span>
          </div>
          <div class="reason">
            <el-icon><ChatLineSquare /></el-icon>
            <span>{{ item.reason }}</span>
          </div>
          <div class="muted ingredients">主要食材：{{ item.ingredients }}</div>
        </div>

        <aside class="rec-side">
          <el-progress
            type="dashboard"
            :percentage="Math.round(Number(item.score)) || 0"
            :width="76"
            :stroke-width="8"
            :color="scoreColor"
          >
            <template #default>
              <span class="score-num">{{ item.score }}</span>
              <span class="score-unit">分</span>
            </template>
          </el-progress>
          <div class="rec-actions">
            <el-button size="small" text bg @click="$router.push(`/student/dish/${item.dishId}`)">详情</el-button>
            <el-button size="small" type="primary" :icon="ShoppingCart" @click="addCart(item)">加入</el-button>
          </div>
          <div class="feedback">
            <el-button
              size="small"
              text
              :type="feedbackState[item.dishId] === 1 ? 'success' : 'info'"
              :icon="CaretTop"
              @click="sendFeedback(item, 1)"
            >
              合口味
            </el-button>
            <el-button
              size="small"
              text
              :type="feedbackState[item.dishId] === -1 ? 'danger' : 'info'"
              :icon="CaretBottom"
              @click="sendFeedback(item, -1)"
            >
              不喜欢
            </el-button>
          </div>
        </aside>
      </article>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, ShoppingCart, ChatLineSquare, CaretTop, CaretBottom } from '@element-plus/icons-vue'
import { getRecommendations, refreshRecommendations } from '@/api/business'
import { sendRecommendFeedback } from '@/api/interaction'
import { listEnabledCategories } from '@/api/basic'
import { useUserStore } from '@/stores/user'
import { healthGoals } from '@/utils/dict'

const userStore = useUserStore()
const loading = ref(false)
const refreshing = ref(false)
const rows = ref([])
const total = ref(0)
const categories = ref([])

const query = reactive({ healthGoal: null, categoryId: null, maxCalories: 500, page: 1, pageSize: 5 })

const rankClass = (index) => (index < 3 ? `is-top-${index + 1}` : 'is-rest')

// 无图菜品用分类色渐变占位，同一分类的占位色一致，页面不会出现成片同色块
const coverStyle = (item) => {
  const hue = ((item.categoryId || 0) * 53 + (item.dishName.charCodeAt(0) % 23) * 7 + 18) % 360
  return { background: `linear-gradient(135deg, hsl(${hue} 82% 66%), hsl(${(hue + 26) % 360} 76% 49%))` }
}

const scoreColor = (percentage) => (percentage >= 80 ? '#2fa36b' : percentage >= 60 ? '#f08c2e' : '#9aa3b2')

const load = async () => {
  loading.value = true
  try {
    const res = await getRecommendations(query)
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
  } catch (e) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.page = 1
  load()
}

const saveRecommendations = async () => {
  refreshing.value = true
  try {
    const res = await refreshRecommendations()
    ElMessage.success(`已保存 ${res.data.count} 条推荐记录，管理员可在数据看板查看推荐使用情况`)
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    refreshing.value = false
  }
}

const addCart = (item) => {
  userStore.addToCart(item)
  ElMessage.success(`已加入购物车：${item.dishName}`)
}

const feedbackState = reactive({})

// 反馈会写进 recommend_feedback 并清掉推荐缓存，重新加载即可看到口味权重变化
const sendFeedback = async (item, action) => {
  await sendRecommendFeedback({ dishId: item.dishId, action })
  feedbackState[item.dishId] = action
  ElMessage.success(action === 1 ? '已记录偏好，会多推荐同类菜品' : '已记录，这类菜品会减少推荐')
  await load()
}

onMounted(async () => {
  query.healthGoal = userStore.info.healthGoal || null
  load()
  try {
    const res = await listEnabledCategories()
    categories.value = res.data || []
  } catch (e) {
    categories.value = []
  }
})
</script>

<style scoped>
.tip {
  margin-bottom: 14px;
  border-radius: var(--radius);
}

.filter-card {
  margin-bottom: 16px;
  background: var(--surface);
}

.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}

.rec-card {
  position: relative;
  display: grid;
  grid-template-columns: 104px minmax(0, 1fr) 120px;
  gap: 16px;
  align-items: center;
  padding: 16px 18px 16px 30px;
  margin-bottom: 12px;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.22s ease, transform 0.22s ease, border-color 0.22s ease;
}

.rec-card:hover {
  transform: translateY(-2px);
  border-color: #f7dcc0;
  box-shadow: var(--shadow);
}

/* 排名徽章压在卡片左边缘，前三名用主色渐变 */
.rank {
  position: absolute;
  left: -10px;
  top: 18px;
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  background: #b7bfcb;
  box-shadow: 0 2px 6px rgba(24, 32, 48, 0.18);
}

.rank.is-top-1 {
  background: linear-gradient(135deg, #f0a441, #e0761b);
}

.rank.is-top-2 {
  background: linear-gradient(135deg, #a8b6c8, #7d8ca1);
}

.rank.is-top-3 {
  background: linear-gradient(135deg, #d2a276, #b07a4c);
}

.cover {
  display: grid;
  place-items: center;
  width: 104px;
  height: 82px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  color: rgba(255, 255, 255, 0.92);
  font-size: 28px;
  font-weight: 700;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rec-main {
  min-width: 0;
}

.rec-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.rec-name {
  font-size: 16px;
  font-weight: 650;
}

.rec-head .price {
  margin-left: auto;
  font-size: 16px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 9px;
}

.reason {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 7px 11px;
  margin-bottom: 6px;
  border-radius: var(--radius-sm);
  background: var(--brand-soft);
  border-left: 3px solid var(--brand);
  color: #8a5a22;
  font-size: 13px;
  line-height: 19px;
}

.reason .el-icon {
  margin-top: 2px;
  flex: none;
}

.ingredients {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.rec-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.score-num {
  font-size: 19px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.score-unit {
  display: block;
  font-size: 11px;
  color: var(--ink-faint);
}

.rec-actions {
  display: flex;
  gap: 6px;
}

.feedback {
  display: flex;
  gap: 2px;
}

.feedback .el-button {
  padding: 4px 6px;
  font-size: 12px;
}

@media (max-width: 768px) {
  .rec-card {
    grid-template-columns: 88px minmax(0, 1fr);
  }

  .cover {
    width: 88px;
    height: 70px;
  }

  .rec-side {
    grid-column: 1 / -1;
    flex-direction: row;
    justify-content: space-between;
  }
}

@media (prefers-reduced-motion: reduce) {
  .rec-card:hover {
    transform: none;
  }
}
</style>
