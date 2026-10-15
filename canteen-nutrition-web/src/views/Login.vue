<template>
  <div class="login-page">
    <div class="auth-shell">
      <section class="pitch">
        <span class="blob blob-a"></span>
        <span class="blob blob-b"></span>
        <div class="pitch-inner">
          <div class="pitch-logo">
            <el-icon :size="20"><Bowl /></el-icon>
            <span>食堂营养推荐</span>
          </div>
          <h1>校园食堂菜品<br />营养分析与推荐系统</h1>
          <p class="pitch-sub">按健康目标推荐菜品，用 AI 估算每一份的营养</p>
          <ul class="pitch-list">
            <li><el-icon><DataAnalysis /></el-icon><span>热量·蛋白·脂肪·钠 六项营养指标</span></li>
            <li><el-icon><MagicStick /></el-icon><span>减脂 / 增肌 / 均衡 三类目标匹配</span></li>
            <li><el-icon><Aim /></el-icon><span>忌口自动过滤，偏好参与打分</span></li>
          </ul>
          <div class="pitch-foot">课程设计演示系统 · 菜品与订单数据均为模拟 · 不提供真实交易</div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>登录</h2>
          <p>学生、商户与管理员共用此入口，按角色进入对应工作台</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="onSubmit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="onSubmit">登录</el-button>
        </el-form>

        <div class="links">
          <router-link to="/register">没有账号？学生注册</router-link>
        </div>

        <template v-if="demos.length">
          <el-divider>演示账号 · 密码 123456</el-divider>
          <div class="demo-accounts">
            <el-button v-for="item in demos" :key="item.username" size="small" text bg @click="fill(item.username)">
              {{ item.label }}：{{ item.username }}
            </el-button>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { homePathOf } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 只在开发环境注入演示账号：Vite 构建时 import.meta.env.DEV 会被折成 false，整段连同账号名一起被摇掉
const demos = import.meta.env.DEV
  ? [
      { label: '学生', username: 'zhangsan' },
      { label: '商户', username: 'canteen01' },
      { label: '管理员', username: 'admin' }
    ]
  : []

const fill = (username) => {
  form.value.username = username
  form.value.password = '123456'
}

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await userStore.login(form.value)
    ElMessage.success(`欢迎回来，${data.nickname}`)
    router.replace(route.query.redirect || homePathOf(data.role))
  } catch (e) {
    // 错误提示由响应拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 外壳样式（.login-page / .auth-shell / .pitch / .panel）统一在全局 main.css，注册页共用 */

.submit {
  width: 100%;
  height: 46px;
  font-size: 15px;
  letter-spacing: 2px;
  border: none;
  background: linear-gradient(135deg, #f7a94f, #ef8b2c);
  box-shadow: 0 8px 20px rgba(240, 140, 46, 0.32);
}

.links {
  text-align: right;
  font-size: 13px;
  margin-top: 16px;
}

.links a {
  color: var(--brand-strong);
  text-decoration: none;
  font-weight: 500;
}

.links a:hover {
  text-decoration: underline;
}

:deep(.el-divider__text) {
  font-size: 12px;
  color: var(--ink-faint);
  background: var(--surface);
}

.demo-accounts {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
