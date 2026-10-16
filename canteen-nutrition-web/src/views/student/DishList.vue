<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">菜品浏览</h2>
        <p class="page-desc">共 {{ total }} 个在售菜品，营养数据由 AI 分析得出，仅供参考</p>
      </div>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-bar">
        <el-form-item label="食堂">
          <el-select v-model="query.merchantId" placeholder="全部食堂" clearable style="width: 150px" @change="onMerchantChange">
            <el-option v-for="m in merchants" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="档口">
          <el-select v-model="query.stallId" placeholder="全部档口" clearable style="width: 160px">
            <el-option v-for="s in stalls" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 130px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.name" placeholder="菜品名称/食材" clearable style="width: 170px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div v-loading="loading" class="list-area">
      <el-empty v-if="!dishes.length && !loading" description="暂无符合条件的菜品" />
      <div class="card-grid">
        <el-card v-for="dish in dishes" :key="dish.id" class="dish-card" shadow="never" :body-style="{ padding: '0' }">
          <div class="cover">
            <img v-if="dish.image" :src="dish.image" :alt="dish.name" loading="lazy" />
            <div v-else class="cover-fallback" :style="coverStyle(dish)">{{ dish.name.charAt(0) }}</div>
            <span v-if="dish.calorie" class="kcal-badge">{{ dish.calorie }} kcal</span>
          </div>
          <div class="info">
            <div class="name">{{ dish.name }}</div>
            <div class="muted line">{{ dish.stallName }} · {{ dish.categoryName }}</div>
            <div class="muted line ingredients">食材：{{ dish.ingredients || '未录入' }}</div>
            <div class="tags">
              <span v-if="dish.protein" class="chip is-green">蛋白 {{ dish.protein }}g</span>
              <span v-if="dish.fat" class="chip">脂肪 {{ dish.fat }}g</span>
              <span v-if="dish.carbohydrate" class="chip">碳水 {{ dish.carbohydrate }}g</span>
              <span v-if="!dish.nutritionFlag" class="chip">暂无营养数据</span>
            </div>
            <div class="footer">
              <span class="price">￥{{ dish.price }}</span>
              <div class="ops">
                <el-button size="small" text bg @click="$router.push(`/student/dish/${dish.id}`)">详情</el-button>
                <el-button size="small" type="primary" :icon="ShoppingCart" @click="addCart(dish)">加入</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[9, 15, 30]"
          layout="total, sizes, prev, pager, next"
          background
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
import { Search, Refresh, ShoppingCart } from '@element-plus/icons-vue'
import { pageDishes } from '@/api/dish'
import { pageMerchants } from '@/api/organization'
import { pageStalls, listEnabledCategories } from '@/api/basic'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const dishes = ref([])
const total = ref(0)
const merchants = ref([])
const stalls = ref([])
const categories = ref([])

const query = reactive({
  merchantId: null,
  stallId: null,
  categoryId: null,
  name: '',
  page: 1,
  pageSize: 9
})

const load = async () => {
  loading.value = true
  try {
    const res = await pageDishes(query)
    dishes.value = res.data.rows || []
    total.value = res.data.total || 0
  } catch (e) {
    dishes.value = []
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.page = 1
  load()
}

const reset = () => {
  query.merchantId = null
  query.stallId = null
  query.categoryId = null
  query.name = ''
  search()
  loadStalls()
}

const loadStalls = async (merchantId) => {
  try {
    const res = await pageStalls({ merchantId: merchantId || null, page: 1, pageSize: 100 })
    stalls.value = res.data.rows || []
  } catch (e) {
    stalls.value = []
  }
}

const onMerchantChange = (value) => {
  query.stallId = null
  loadStalls(value)
}

const addCart = (dish) => {
  userStore.addToCart(dish)
  ElMessage.success(`已加入购物车：${dish.name}`)
}

// 无图菜品的占位色：同分类色相接近，避免整屏同一个橙色
const coverStyle = (dish) => {
  const hue = ((dish.categoryId || 0) * 53 + (dish.name.charCodeAt(0) % 23) * 7 + 18) % 360
  return { background: `linear-gradient(135deg, hsl(${hue} 82% 66%), hsl(${(hue + 26) % 360} 76% 49%))` }
}

onMounted(async () => {
  load()
  try {
    const [m, c] = await Promise.all([pageMerchants({ page: 1, pageSize: 100 }), listEnabledCategories()])
    merchants.value = m.data.rows || []
    categories.value = c.data || []
  } catch (e) {
    // 字典加载失败不影响菜品查询
  }
  loadStalls()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.list-area {
  min-height: 220px;
}

.dish-card {
  overflow: hidden;
}

.dish-card:hover {
  transform: translateY(-3px);
  border-color: #f3d9bd;
  box-shadow: var(--shadow-lg) !important;
}

.dish-card:hover .cover img {
  transform: scale(1.06);
}

.cover {
  position: relative;
  height: 162px;
  overflow: hidden;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 44px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
  background: linear-gradient(135deg, #f6c15f 0%, #ef8b2c 100%);
}

.kcal-badge {
  position: absolute;
  right: 10px;
  bottom: 10px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(31, 36, 48, 0.62);
  backdrop-filter: blur(4px);
}

.info {
  padding: 13px 14px 14px;
}

.name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.line {
  line-height: 18px;
}

.ingredients {
  height: 18px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0 12px;
  min-height: 22px;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 11px;
  border-top: 1px dashed var(--line);
}

.price {
  font-size: 17px;
}

.ops {
  display: flex;
  gap: 6px;
}

/* 手机上卡片是单列铺满，封面按比例给高，否则会被压成一条横幅 */
@media (max-width: 767px) {
  .cover {
    height: 200px;
  }
}
</style>
