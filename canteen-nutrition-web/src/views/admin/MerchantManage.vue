<template>
  <div class="page">
    <div class="page-title">商户管理</div>

    <el-card>
      <div class="search-bar">
        <el-input v-model="query.name" placeholder="商户名称" clearable style="width: 180px" @keyup.enter="search" />
        <el-select v-model="query.status" placeholder="经营状态" clearable style="width: 140px; margin-left: 10px">
          <el-option label="经营中" :value="1" />
          <el-option label="已停业" :value="0" />
        </el-select>
        <el-button type="primary" style="margin-left: 10px" @click="search">查询</el-button>
        <el-button type="success" :icon="Plus" @click="openDialog()">新增商户</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="商户名称" min-width="130" />
        <el-table-column prop="contact" label="联系人" width="110" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="地址" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="介绍" min-width="180" show-overflow-tooltip />
        <el-table-column prop="stallCount" label="档口数" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagOf(merchantStatusMap, row.status).type">{{ tagOf(merchantStatusMap, row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="{ row }">
            <el-button size="small" text bg @click="openDialog(row)">编辑</el-button>
            <el-button size="small" text bg @click="toggleStatus(row)">{{ row.status === 1 ? '停业' : '恢复' }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商户' : '新增商户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商户名称" prop="name">
          <el-input v-model="form.name" placeholder="如：第一食堂" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contact" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="商户介绍">
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
import { pageMerchants, addMerchant, updateMerchant, deleteMerchant, updateMerchantStatus } from '@/api/organization'
import { merchantStatusMap, tagOf } from '@/utils/dict'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref()
const query = reactive({ name: '', status: null, page: 1, pageSize: 10 })
const emptyForm = () => ({ name: '', contact: '', phone: '', address: '', description: '' })
const form = ref(emptyForm())

const rules = {
  name: [{ required: true, message: '请输入商户名称', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageMerchants(query)
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
  form.value = row ? { ...row } : emptyForm()
  dialogVisible.value = true
}

const save = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.value.id) {
      await updateMerchant(form.value)
    } else {
      await addMerchant(form.value)
    }
    ElMessage.success('保存成功，可在用户管理中为其创建商户账号')
    dialogVisible.value = false
    load()
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  await updateMerchantStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除商户「${row.name}」？`, '提示', { type: 'warning' })
  await deleteMerchant(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
