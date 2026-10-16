<template>
  <div class="page">
    <div class="page-title">订单查询</div>

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
        <el-button type="primary" style="margin-left: 10px" @click="search">查询</el-button>
        <el-button style="margin-left: 10px" :icon="Download" :loading="exporting" @click="doExport('orders')">
          导出订单
        </el-button>
        <el-button :icon="Download" :loading="exporting" @click="doExport('dishes')">导出菜品</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="orderNo" label="订单编号" width="140" />
        <el-table-column prop="userNickname" label="学生" width="110" />
        <el-table-column prop="merchantName" label="食堂商户" width="130" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">￥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="预计热量" width="110">
          <template #default="{ row }">{{ row.totalCalorie ? row.totalCalorie + ' kcal' : '-' }}</template>
        </el-table-column>
        <el-table-column label="方式" width="100">
          <template #default="{ row }">{{ orderTypeMap[row.orderType] || row.orderType }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagOf(orderStatusMap, row.status).type">{{ tagOf(orderStatusMap, row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" text bg @click="$router.push(`/order/${row.id}`)">详情</el-button>
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
import { Download } from '@element-plus/icons-vue'
import { pageOrders, deleteOrder } from '@/api/business'
import { exportDishes, exportOrders, saveBlob } from '@/api/report'
import { orderStatusMap, orderTypeMap, tagOf } from '@/utils/dict'

const loading = ref(false)
const exporting = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ status: null, orderType: null, page: 1, pageSize: 10 })

const formatTime = (value) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const doExport = async (kind) => {
  exporting.value = true
  try {
    const blob = kind === 'orders' ? await exportOrders() : await exportDishes()
    saveBlob(blob, kind === 'orders' ? '全量订单.xlsx' : '全量菜品.xlsx')
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

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除订单 ${row.orderNo}？明细会一并删除。`, '提示', { type: 'warning' })
  await deleteOrder(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
