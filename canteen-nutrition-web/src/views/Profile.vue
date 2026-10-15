<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">个人中心</h2>
        <div class="page-desc">健康目标与忌口会直接参与智能推荐打分</div>
      </div>
    </div>

    <el-card shadow="never" class="banner">
      <div class="banner-inner">
        <div class="big-avatar">{{ (info.nickname || info.username || '?').charAt(0) }}</div>
        <div class="banner-main">
          <div class="line1">
            <span class="nick">{{ info.nickname || info.username }}</span>
            <el-tag size="small" effect="light" round>{{ roleMap[info.role] }}</el-tag>
            <el-tag v-if="info.merchantName" size="small" type="info" effect="plain" round>
              {{ info.merchantName }}
            </el-tag>
          </div>
          <div class="line2 muted">登录账号 {{ info.username }}</div>
          <div v-if="info.role === 1" class="line3">
            <span v-if="info.healthGoal" class="chip is-hot">目标 · {{ info.healthGoal }}</span>
            <span v-if="info.preference" class="chip">偏好 · {{ info.preference }}</span>
            <span v-if="info.taboo" class="chip">忌口 · {{ info.taboo }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-head"><span>账号资料</span><span class="muted">保存后立即生效</span></div>
          </template>
          <el-form ref="infoRef" :model="info" label-width="90px">
            <el-form-item label="登录账号">
              <el-input :value="info.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>{{ roleMap[info.role] }}</el-tag>
              <span v-if="info.merchantName" class="muted" style="margin-left: 8px">{{ info.merchantName }}</span>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname" :rules="[{ required: true, message: '请输入昵称' }]">
              <el-input v-model="info.nickname" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="info.phone" />
            </el-form-item>
            <template v-if="info.role === 1">
              <el-form-item label="健康目标">
                <el-select v-model="info.healthGoal" placeholder="请选择" clearable>
                  <el-option v-for="item in healthGoals" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="饮食偏好">
                <el-input v-model="info.preference" placeholder="如：清淡、少辣" />
              </el-form-item>
              <el-form-item label="忌口">
                <el-input v-model="info.taboo" placeholder="如：香菜、牛肉，将用于推荐过滤" />
              </el-form-item>
            </template>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveInfo">保存资料</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-head"><span>修改密码</span></div>
          </template>
          <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-width="104px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwd.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="不少于6位" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirm">
              <el-input v-model="pwd.confirm" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="changing" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, updatePassword } from '@/api/auth'
import { healthGoals, roleMap } from '@/utils/dict'

const userStore = useUserStore()
const infoRef = ref()
const pwdRef = ref()
const saving = ref(false)
const changing = ref(false)
const info = ref({})
const pwd = ref({ oldPassword: '', newPassword: '', confirm: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwd.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const saveInfo = async () => {
  const valid = await infoRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateProfile({
      id: info.value.id,
      nickname: info.value.nickname,
      phone: info.value.phone,
      healthGoal: info.value.healthGoal,
      preference: info.value.preference,
      taboo: info.value.taboo
    })
    userStore.setInfo(info.value)
    ElMessage.success('资料已更新，推荐结果将按新的健康目标计算')
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return
  changing.value = true
  try {
    await updatePassword({ oldPassword: pwd.value.oldPassword, newPassword: pwd.value.newPassword })
    ElMessage.success('密码修改成功')
    pwd.value = { oldPassword: '', newPassword: '', confirm: '' }
    pwdRef.value.resetFields()
  } catch (e) {
    // 提示由拦截器处理
  } finally {
    changing.value = false
  }
}

onMounted(async () => {
  await userStore.loadInfo()
  info.value = { ...userStore.info }
})
</script>

<style scoped>
.banner {
  margin-bottom: 16px;
  overflow: hidden;
}

.banner :deep(.el-card__body) {
  background: linear-gradient(120deg, #fff7ee, #ffffff 55%);
}

.banner-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.big-avatar {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  flex: none;
  border-radius: 18px;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #f6b95f, #ef8b2c);
  box-shadow: 0 6px 16px rgba(240, 140, 46, 0.32);
}

.banner-main {
  min-width: 0;
}

.line1 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nick {
  font-size: 18px;
  font-weight: 700;
}

.line2 {
  margin-bottom: 8px;
}

.line3 {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.form-card {
  margin-bottom: 16px;
}

.card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.tip {
  margin-top: 4px;
  border-radius: var(--radius-sm);
}
</style>
