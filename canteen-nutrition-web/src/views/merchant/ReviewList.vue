<template>
  <div class="page">
    <div class="page-title">学生评价</div>

    <el-card>
      <div class="search-bar">
        <el-select v-model="query.dishId" placeholder="按菜品筛选" clearable filterable style="width: 200px" @change="search">
          <el-option v-for="d in dishes" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-select v-model="query.rating" placeholder="评分" clearable style="width: 120px; margin-left: 10px" @change="search">
          <el-option v-for="n in [5, 4, 3, 2, 1]" :key="n" :label="`${n} 星`" :value="n" />
        </el-select>
        <el-button type="primary" style="margin-left: 10px" @click="search">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column v-if="isMobile" label="评价" min-width="150" class-name="review-cell">
          <template #default="{ row }">
            <div class="review-head">
              <span class="who">{{ row.nickname || '学生' }}</span>
              <span class="muted">· {{ row.dishName }}</span>
            </div>
            <div class="review-content">{{ row.content }}</div>
            <div class="muted small">{{ formatTime(row.createTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="nickname" label="学生" width="110" />
        <el-table-column v-if="!isMobile" prop="dishName" label="菜品" min-width="120" />
        <el-table-column label="评分" :width="isMobile ? 108 : 160">
          <template #default="{ row }"><el-rate :model-value="row.rating" disabled size="small" /></template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="content" label="评价内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" :width="isMobile ? 68 : 90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '已屏蔽' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :pager-count="isMobile ? 3 : 5"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageReviews } from '@/api/business'
import { pageDishes } from '@/api/dish'
import { useMobile } from '@/composables/useMobile'

const { isMobile } = useMobile()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dishes = ref([])
const query = reactive({ dishId: null, rating: null, page: 1, pageSize: 10 })

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const load = async () => {
  loading.value = true
  try {
    const res = await pageReviews(query)
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

onMounted(async () => {
  load()
  try {
    const res = await pageDishes({ page: 1, pageSize: 100 })
    dishes.value = res.data.rows || []
  } catch (e) {
    dishes.value = []
  }
})
</script>

<style scoped>
.review-head {
  line-height: 18px;
  margin-bottom: 2px;
}

.who {
  font-weight: 600;
}

.review-content {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  line-height: 18px;
  color: var(--ink-soft);
}

.small {
  font-size: 11.5px;
}
</style>
