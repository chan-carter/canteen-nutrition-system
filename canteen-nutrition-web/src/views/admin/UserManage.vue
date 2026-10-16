<template>
  <div class="page">
    <div class="page-title">用户管理</div>

    <el-card>
      <el-form :inline="true" class="search-bar">
        <el-form-item label="账号">
          <el-input v-model="query.username" placeholder="模糊查询" clearable style="width: 160px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable style="width: 130px">
            <el-option label="学生" :value="1" />
            <el-option label="食堂商户" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button type="success" :icon="Plus" @click="openDialog()">新增用户</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="nickname" label="昵称" width="130" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="row.role === 3 ? 'danger' : row.role === 2 ? 'warning' : 'primary'">
              {{ roleMap[row.role] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="所属商户" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="healthGoal" label="健康目标" width="110" />
        <el-table-column prop="preference" label="饮食偏好" min-width="130" show-overflow-tooltip />
        <el-table-column prop="taboo" label="忌口" min-width="110" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="{ row }">
            <el-button size="small" text bg @click="openDialog(row)">编辑</el-button>
            <el-button size="small" text bg @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="不少于6位" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" :disabled="!!form.id" style="width: 100%">
            <el-option label="学生" :value="1" />
            <el-option label="食堂商户" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.role === 2" label="所属商户" prop="merchantId">
          <el-select v-model="form.merchantId" placeholder="请选择商户" style="width: 100%">
            <el-option v-for="m in merchants" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <template v-if="form.role === 1">
          <el-form-item label="健康目标">
            <el-select v-model="form.healthGoal" clearable style="width: 100%">
              <el-option v-for="item in healthGoals" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="饮食偏好">
            <el-input v-model="form.preference" />
          </el-form-item>
          <el-form-item label="忌口">
            <el-input v-model="form.taboo" />
          </el-form-item>
        </template>
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
import { pageUsers, addUser, updateUser, deleteUser, updateUserStatus, pageMerchants } from '@/api/organization'
import { roleMap, healthGoals } from '@/utils/dict'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const merchants = ref([])
const dialogVisible = ref(false)
const formRef = ref()

const query = reactive({ username: '', role: null, status: null, page: 1, pageSize: 10 })
const emptyForm = () => ({
  username: '',
  password: '',
  nickname: '',
  role: 1,
  merchantId: null,
  phone: '',
  healthGoal: '',
  preference: '',
  taboo: ''
})
const form = ref(emptyForm())

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  merchantId: [{ required: true, message: '商户账号必须绑定商户', trigger: 'change' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await pageUsers(query)
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
  form.value = row ? { ...row, password: '' } : emptyForm()
  dialogVisible.value = true
}

const save = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (form.value.id) {
      await updateUser(form.value)
    } else {
      await addUser(form.value)
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
  await updateUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load()
}

const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除账号「${row.username}」？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  load()
  try {
    const res = await pageMerchants({ page: 1, pageSize: 100 })
    merchants.value = res.data.rows || []
  } catch (e) {
    merchants.value = []
  }
})
</script>
