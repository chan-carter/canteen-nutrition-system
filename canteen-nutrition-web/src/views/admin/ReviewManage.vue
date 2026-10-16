<template>
  <div class="page">
    <div class="page-title">评价管理</div>

    <el-card>
      <div class="search-bar">
        <el-select v-model="query.dishId" placeholder="按菜品筛选" clearable filterable style="width: 200px" @change="search">
          <el-option v-for="d in dishes" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-select v-model="query.rating" placeholder="评分" clearable style="width: 120px; margin-left: 10px" @change="search">
          <el-option v-for="n in [5, 4, 3, 2, 1]" :key="n" :label="`${n} 星`" :value="n" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px; margin-left: 10px" @change="search">
          <el-option label="正常" :value="1" />
          <el-option label="已屏蔽" :value="0" />
        </el-select>
        <el-button type="primary" style="margin-left: 10px" @click="search">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="nickname" label="学生" width="110" />
        <el-table-column prop="dishName" label="菜品" min-width="120" />
        <el-table-column label="评分" width="150">
          <template #default="{ row }"><el-rate :model-value="row.rating" disabled size="small" /></template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '已屏蔽' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" text bg @click="toggleStatus(row)">{{ row.status === 1 ? '屏蔽' : '恢复' }}</el-button>
            <el-button size="small" text type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageReviews, updateReviewStatus, deleteReview } from '@/api/business'
import { pageDishes } from '@/api/dish'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dishes = ref([])
const query = reactive({ dishId: null, rating: null, status: null, page: 1, pageSize: 10 })

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

const toggleStatus = async (row) => {
  await updateReviewStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm('确认删除该条评价？', '提示', { type: 'warning' })
  await deleteReview(row.id)
  ElMessage.success('删除成功')
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
