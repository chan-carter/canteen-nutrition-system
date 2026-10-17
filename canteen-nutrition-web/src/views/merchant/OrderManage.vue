<template>
  <div class="page">
    <div class="page-title">订单处理</div>

    <el-card>
      <div class="search-bar">
        <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px" @change="search">
          <el-option label="待接单" :value="1" />
          <el-option label="已接单" :value="2" />
          <el-option label="待取餐" :value="5" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
        <el-select v-model="query.orderType" placeholder="用餐方式" clearable style="width: 140px; margin-left: 10px" @change="search">
          <el-option label="点餐自取" value="PICKUP" />
          <el-option label="提前预订" value="RESERVE" />
        </el-select>
        <el-button type="primary" style="margin-left: 10px" @click="search">刷新</el-button>
        <el-button :icon="Download" :loading="exporting" style="margin-left: 10px" @click="doExport">导出订单</el-button>
        <span class="muted" style="margin-left: 10px">超过30分钟未接单的订单会被系统自动取消并通知学生</span>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column v-if="isMobile" label="订单" min-width="140" class-name="order-cell">
          <template #default="{ row }">
            <div class="order-no">{{ row.orderNo }}</div>
            <div class="muted small">
              {{ row.userNickname || '学生' }} · ￥{{ row.totalAmount }} · {{ orderTypeMap[row.orderType] || row.orderType }}
            </div>
            <div v-if="row.remark" class="muted small">备注：{{ row.remark }}</div>
            <div class="muted small">{{ formatTime(row.createTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="orderNo" label="订单编号" width="140" />
        <el-table-column v-if="!isMobile" prop="userNickname" label="学生" width="110" />
        <el-table-column v-if="!isMobile" label="金额" width="100">
          <template #default="{ row }"><span class="price">￥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="预计热量" width="110">
          <template #default="{ row }">{{ row.totalCalorie ? row.totalCalorie + ' kcal' : '-' }}</template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="方式" width="100">
          <template #default="{ row }">{{ orderTypeMap[row.orderType] || row.orderType }}</template>
        </el-table-column>
        <el-table-column label="状态" :width="isMobile ? 92 : 100">
          <template #default="{ row }">
            <el-tag :type="tagOf(orderStatusMap, row.status).type">{{ tagOf(orderStatusMap, row.status).text }}</el-tag>
            <div v-if="isMobile && row.pickupCode" class="muted small">取餐码 {{ row.pickupCode }}</div>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" label="取餐码" width="90">
          <template #default="{ row }">
            <span v-if="row.pickupCode" class="pickup-code">{{ row.pickupCode }}</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="!isMobile" prop="remark" label="备注" min-width="130" show-overflow-tooltip />
        <el-table-column v-if="!isMobile" label="下单时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 116 : 230" class-name="table-ops">
          <template #default="{ row }">
            <el-button size="small" text bg @click="$router.push(`/order/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" type="primary" @click="changeStatus(row, 2)">接单</el-button>
            <el-button v-if="row.status === 2" size="small" type="warning" @click="changeStatus(row, 5)">出餐</el-button>
            <el-button v-if="row.status === 5" size="small" type="success" @click="changeStatus(row, 3)">完成</el-button>
            <el-button v-if="[1, 2, 5].includes(row.status)" size="small" text type="danger" @click="changeStatus(row, 4)">
              取消
            </el-button>
          </template>
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
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { pageOrders, updateOrderStatus } from '@/api/business'
import { exportOrders, saveBlob } from '@/api/report'
import { useMobile } from '@/composables/useMobile'
import { orderStatusMap, orderTypeMap, tagOf } from '@/utils/dict'

const { isMobile } = useMobile()
const loading = ref(false)
const exporting = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ status: 1, orderType: null, page: 1, pageSize: 10 })

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const doExport = async () => {
  exporting.value = true
  try {
    const blob = await exportOrders()
    saveBlob(blob, '订单数据.xlsx')
    ElMessage.success('导出成功，浏览器已开始下载')
  } finally {
    exporting.value = false
  }
}

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

const search = () => {
  query.page = 1
  load()
}

const changeStatus = async (row, status) => {
  await updateOrderStatus(row.id, status)
  ElMessage.success(`订单已更新为「${tagOf(orderStatusMap, status).text}」`)
  load()
}

onMounted(load)
</script>

<style scoped>
.pickup-code {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
  font-weight: 700;
  color: var(--brand-strong);
}

.order-no {
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 12px;
  font-weight: 600;
  line-height: 18px;
}

.small {
  font-size: 11.5px;
}

.order-cell .small {
  line-height: 17px;
}
</style>
