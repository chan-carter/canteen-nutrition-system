<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的订单</h2>
        <div class="page-desc">按下单状态分类查看，待接单状态可自行取消</div>
      </div>
    </div>

    <el-card shadow="never" class="tab-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待接单" name="1" />
        <el-tab-pane label="已接单" name="2" />
        <el-tab-pane label="待取餐" name="5" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>

      <el-table v-loading="loading" :data="rows">
        <el-table-column label="订单编号" width="150">
          <template #default="{ row }"><span class="order-no">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="merchantName" label="食堂商户" width="120" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }"><span class="price">￥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column label="预计热量" width="110">
          <template #default="{ row }">{{ row.totalCalorie ? row.totalCalorie + ' kcal' : '-' }}</template>
        </el-table-column>
        <el-table-column label="方式" width="100">
          <template #default="{ row }">{{ orderTypeMap[row.orderType] || row.orderType }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagOf(orderStatusMap, row.status).type" effect="light" round>
              {{ tagOf(orderStatusMap, row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="取餐码" width="90">
          <template #default="{ row }">
            <span v-if="row.pickupCode" class="pickup-code">{{ row.pickupCode }}</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="下单时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" text bg @click="$router.push(`/order/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" text type="danger" @click="cancel(row)">取消</el-button>
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
import { pageOrders, updateOrderStatus } from '@/api/business'
import { orderStatusMap, orderTypeMap, tagOf } from '@/utils/dict'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const activeTab = ref('all')
const query = reactive({ status: null, page: 1, pageSize: 10 })

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const load = async () => {
  loading.value = true
  try {
    const res = await pageOrders(query)
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
  } catch (e) {
    rows.value = []
  } finally {
    loading.value = false
  }
}

const onTabChange = (name) => {
  query.status = name === 'all' ? null : Number(name)
  query.page = 1
  load()
}

const cancel = async (row) => {
  await ElMessageBox.confirm(`确认取消订单 ${row.orderNo}？`, '提示', { type: 'warning' })
  await updateOrderStatus(row.id, 4)
  ElMessage.success('订单已取消')
  load()
}

onMounted(load)
</script>

<style scoped>
.tab-card :deep(.el-tabs__header) {
  margin-bottom: 4px;
}

.order-no {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 12.5px;
  color: var(--ink-soft);
}

.pickup-code {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  color: var(--brand-strong);
}
</style>
