<template>
  <div class="page">
    <div class="page-title">菜品审核与管理</div>

    <el-card>
      <el-form :inline="true" class="search-bar">
        <el-form-item label="菜品">
          <el-input v-model="query.name" placeholder="名称或食材" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="商户">
          <el-select v-model="query.merchantId" placeholder="全部" clearable style="width: 150px" @change="onMerchantChange">
            <el-option v-for="m in merchants" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="档口">
          <el-select v-model="query.stallId" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="s in stalls" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="query.auditStatus" placeholder="全部" clearable style="width: 130px">
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="onlyWaiting">只看待审核</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="菜品" min-width="120" />
        <el-table-column prop="categoryName" label="分类" width="90" />
        <el-table-column prop="stallName" label="档口" width="130" />
        <el-table-column label="价格" width="90">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="ingredients" label="主要食材" min-width="180" show-overflow-tooltip />
        <el-table-column label="营养" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.nutritionFlag" type="success" size="small">{{ row.calorie }} kcal</el-tag>
            <el-tag v-else type="warning" size="small">待分析</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核" width="110">
          <template #default="{ row }">
            <el-tag :type="tagOf(auditStatusMap, row.auditStatus).type" size="small">
              {{ tagOf(auditStatusMap, row.auditStatus).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核意见" min-width="120" show-overflow-tooltip />
        <el-table-column label="上架" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="() => toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" text bg @click="openAudit(row)">审核</el-button>
            <el-button size="small" text bg @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="auditVisible" title="菜品审核" width="460px">
      <el-descriptions :column="1" border style="margin-bottom: 12px">
        <el-descriptions-item label="菜品">{{ current.name }}</el-descriptions-item>
        <el-descriptions-item label="档口">{{ current.stallName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ current.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="价格">￥{{ current.price }}</el-descriptions-item>
        <el-descriptions-item label="主要食材">{{ current.ingredients }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio-button :value="1">通过</el-radio-button>
            <el-radio-button :value="2">驳回</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="驳回时建议说明原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitAudit">提交审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="修改菜品基础数据" width="520px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="菜品名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.categoryId" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="editForm.price" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="主要食材">
          <el-input v-model="editForm.ingredients" />
        </el-form-item>
        <el-form-item label="菜品介绍">
          <el-input v-model="editForm.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageDishes, updateDish, deleteDish, updateDishStatus, auditDish } from '@/api/dish'
import { pageMerchants } from '@/api/organization'
import { pageStalls, listEnabledCategories } from '@/api/basic'
import { auditStatusMap, tagOf } from '@/utils/dict'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const merchants = ref([])
const stalls = ref([])
const categories = ref([])
const auditVisible = ref(false)
const editVisible = ref(false)
const current = ref({})
const editForm = ref({})
const auditForm = reactive({ auditStatus: 1, auditRemark: '' })

const query = reactive({
  name: '',
  merchantId: null,
  stallId: null,
  categoryId: null,
  auditStatus: null,
  status: null,
  page: 1,
  pageSize: 10
})

const load = async () => {
  loading.value = true
  try {
    const res = await pageDishes(query)
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

const onlyWaiting = () => {
  query.auditStatus = 0
  search()
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

const openAudit = (row) => {
  current.value = row
  auditForm.auditStatus = row.auditStatus === 2 ? 2 : 1
  auditForm.auditRemark = row.auditRemark || ''
  auditVisible.value = true
}

const submitAudit = async () => {
  saving.value = true
  try {
    await auditDish(current.value.id, { ...auditForm })
    ElMessage.success('审核已提交')
    auditVisible.value = false
    load()
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    saving.value = false
  }
}

const openEdit = (row) => {
  editForm.value = { ...row }
  editVisible.value = true
}

const submitEdit = async () => {
  saving.value = true
  try {
    await updateDish(editForm.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  await updateDishStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除菜品「${row.name}」？`, '提示', { type: 'warning' })
  await deleteDish(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  load()
  loadStalls()
  try {
    const [m, c] = await Promise.all([pageMerchants({ page: 1, pageSize: 100 }), listEnabledCategories()])
    merchants.value = m.data.rows || []
    categories.value = c.data || []
  } catch (e) {
    // 字典加载失败不影响列表
  }
})
</script>
