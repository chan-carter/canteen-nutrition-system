<template>
  <div class="page">
    <div class="page-title">档口管理</div>

    <el-card>
      <div class="search-bar">
        <el-input v-model="query.name" placeholder="档口名称" clearable style="width: 180px" @keyup.enter="search" />
        <el-select v-model="query.status" placeholder="营业状态" clearable style="width: 130px; margin-left: 10px">
          <el-option label="营业中" :value="1" />
          <el-option label="已关闭" :value="0" />
        </el-select>
        <el-button type="primary" style="margin-left: 10px" @click="search">查询</el-button>
        <el-button type="success" :icon="Plus" @click="openDialog()">新增档口</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="档口名称" min-width="120" />
        <el-table-column prop="location" label="位置" min-width="130" />
        <el-table-column prop="businessHours" label="营业时间" min-width="200" />
        <el-table-column prop="dishCount" label="菜品数" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagOf(stallStatusMap, row.status).type">{{ tagOf(stallStatusMap, row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" text bg @click="openDialog(row)">编辑</el-button>
            <el-button size="small" text bg @click="toggleStatus(row)">{{ row.status === 1 ? '关闭' : '恢复营业' }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑档口' : '新增档口'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="档口名称" prop="name">
          <el-input v-model="form.name" placeholder="如：面食窗口" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="如：第一食堂A区1号" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.businessHours" placeholder="如：10:30-13:00 / 16:30-19:00" />
        </el-form-item>
        <el-form-item label="档口介绍">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { pageStalls, addStall, updateStall, deleteStall, updateStallStatus } from '@/api/basic'
import { stallStatusMap, tagOf } from '@/utils/dict'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ name: '', status: null, page: 1, pageSize: 10 })
const form = ref({})

const rules = {
  name: [{ required: true, message: '请输入档口名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageStalls(query)
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

const openDialog = (row) => {
  form.value = row ? { ...row } : { name: '', location: '', businessHours: '', description: '' }
  dialogVisible.value = true
}

const save = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.value.id) {
      await updateStall(form.value)
    } else {
      await addStall(form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  await updateStallStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除档口「${row.name}」？删除后其下菜品将无法归属。`, '提示', { type: 'warning' })
  await deleteStall(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
