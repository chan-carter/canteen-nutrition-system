<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的收藏</h2>
        <p class="page-desc">收藏常点的菜品，下单时一键加入购物车</p>
      </div>
      <el-tag type="warning" effect="light" round>{{ rows.length }} 道</el-tag>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!rows.length && !loading" :image-size="80" description="还没有收藏，去菜品列表点星星吧">
        <el-button type="primary" @click="$router.push('/student/dishes')">去逛菜品</el-button>
      </el-empty>

      <div class="fav-grid">
        <article v-for="item in rows" :key="item.dishId" class="fav-card">
          <div class="cover" :style="coverStyle(item)" @click="$router.push(`/student/dish/${item.dishId}`)">
            <img v-if="item.image" :src="item.image" :alt="item.dishName" />
            <span v-else>{{ item.dishName.charAt(0) }}</span>
            <el-tag v-if="item.status !== 1" class="off" type="info" size="small" effect="dark">已下架</el-tag>
          </div>
          <div class="fav-body">
            <div class="fav-name">{{ item.dishName }}</div>
            <div class="muted fav-stall">{{ item.stallName || '-' }}</div>
            <div class="fav-meta">
              <span class="price">￥{{ item.price }}</span>
              <span class="chip is-hot">{{ item.calorie ?? '-' }} kcal</span>
            </div>
            <div class="fav-actions">
              <el-button size="small" type="primary" :icon="ShoppingCart" :disabled="item.status !== 1" @click="addCart(item)">
                加入
              </el-button>
              <el-button size="small" text type="danger" :icon="Star" @click="cancel(item)">取消收藏</el-button>
            </div>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import { cancelFavorite, listFavorites } from '@/api/interaction'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const rows = ref([])
const loading = ref(false)

const coverStyle = (item) => {
  const hue = ((item.dishId || 0) * 37 + (item.dishName.charCodeAt(0) % 23) * 7 + 20) % 360
  return { background: `linear-gradient(135deg, hsl(${hue} 82% 66%), hsl(${(hue + 26) % 360} 76% 49%))` }
}

const load = async () => {
  loading.value = true
  try {
    const res = await listFavorites()
    rows.value = res.data || []
  } finally {
    loading.value = false
  }
}

const addCart = (item) => {
  userStore.addToCart(item)
  ElMessage.success(`已加入购物车：${item.dishName}`)
}

const cancel = async (item) => {
  await cancelFavorite(item.dishId)
  ElMessage.success('已取消收藏')
  await load()
}

onMounted(load)
</script>

<style scoped>
.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
}

.fav-card {
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.22s ease, transform 0.22s ease;
}

.fav-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.cover {
  position: relative;
  display: grid;
  place-items: center;
  height: 128px;
  overflow: hidden;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.92);
  font-size: 30px;
  font-weight: 700;
}

/* 绝对定位才不会被 grid 的自动行按图片内在比例撑高，压住下面的菜名与价格 */
.cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.off {
  position: absolute;
  left: 8px;
  top: 8px;
}

.fav-body {
  padding: 11px 13px 13px;
}

.fav-name {
  font-size: 15px;
  font-weight: 650;
  margin-bottom: 2px;
}

.fav-stall {
  font-size: 12px;
  margin-bottom: 8px;
}

.fav-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.fav-actions {
  display: flex;
  gap: 6px;
  justify-content: space-between;
}
</style>
