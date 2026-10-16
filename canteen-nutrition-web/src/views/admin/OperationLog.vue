<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">操作日志</h2>
        <p class="page-desc">记录后台关键写操作的操作人、入参、耗时与结果，便于追溯</p>
      </div>
      <el-button :icon="Refresh" text bg @click="load">刷新</el-button>
    </div>

    <el-card shadow="never" class="bar">
      <el-form :inline="true">
        <el-form-item label="业务模块">
          <el-select v-model="query.module" placeholder="全部" clearable style="width: 150px" @change="search">
            <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="操作人 / 操作 / 方法" clearable style="width: 200px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="search">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="rows" border>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="detail">
              <div class="detail-line"><span>目标方法</span><code>{{ row.method }}</code></div>
              <div class="detail-line"><span>入参</span><code class="wrap">{{ row.params || '-' }}</code></div>
              <div class="detail-line" v-if="row.errorMsg">
                <span>异常</span><code class="wrap err">{{ row.errorMsg }}</code>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作人" width="120">
          <template #default="{ row }">{{ row.username }}<span class="muted"> #{{ row.userId }}</span></template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="operation" label="操作" min-width="130" />
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.costMs }} ms</template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small" effect="light" round>
              {{ row.success === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="来源IP" width="140" />
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="search"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { pageLogs } from '@/api/report'

const modules = ['菜品管理', '订单管理', '用户管理', '商户管理', '评价管理', '数据导出']

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ module: null, keyword: '', page: 1, pageSize: 20 })

const formatTime = (value) => (value ? String(value).replace('T', ' ').slice(0, 19) : '')

const load = async () => {
  loading.value = true
  try {
    const res = await pageLogs(query)
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.page = 1
  load()
}

const reset = () => {
  query.module = null
  query.keyword = ''
  search()
}

onMounted(load)
</script>

<style scoped>
.bar {
  margin-bottom: 14px;
}

.bar :deep(.el-card__body) {
  padding-bottom: 2px;
}

.detail {
  padding: 6px 14px;
}

.detail-line {
  display: flex;
  gap: 10px;
  margin-bottom: 6px;
  font-size: 12.5px;
}

.detail-line > span {
  flex: none;
  width: 62px;
  color: var(--ink-faint);
}

.detail-line code {
  flex: 1;
  min-width: 0;
  padding: 2px 6px;
  border-radius: 6px;
  background: var(--surface-sunken);
  font-size: 12px;
  color: var(--ink-soft);
  overflow-x: auto;
  white-space: pre;
}

.detail-line code.wrap {
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-line code.err {
  color: var(--danger);
}
</style>
