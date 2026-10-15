<template>
  <div class="auth-page">
    <div class="auth-shell">
      <section class="pitch">
        <span class="blob blob-a"></span>
        <span class="blob blob-b"></span>
        <div class="pitch-inner">
          <div class="pitch-logo">
            <el-icon :size="20"><Bowl /></el-icon>
            <span>食堂营养推荐</span>
          </div>
          <h1>注册后告诉我<br />你的健康目标</h1>
          <p class="pitch-sub">系统会结合目标、偏好与忌口，为每一餐推荐更合适的菜品</p>
          <ul class="pitch-list">
            <li><el-icon><Aim /></el-icon><span>减脂 / 增肌 / 均衡饮食三档目标</span></li>
            <li><el-icon><CircleClose /></el-icon><span>忌口食材自动过滤，不会推错</span></li>
            <li><el-icon><Star /></el-icon><span>偏好与评分参与推荐打分</span></li>
          </ul>
          <div class="pitch-foot">注册仅创建学生账号，商户与管理员账号请联系管理员开通</div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>学生账号注册</h2>
          <p class="muted">带 * 为必填项</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="账号" prop="username">
              <el-input v-model="form.username" placeholder="登录账号" />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="如：张三" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="不少于6位" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="选填" />
            </el-form-item>
          </div>

          <el-form-item label="验证码" prop="captchaCode">
            <div class="captcha-row">
              <el-input v-model="form.captchaCode" placeholder="图中字符" maxlength="4" />
              <img v-if="captcha.image" :src="captcha.image" class="captcha-img" alt="点击刷新验证码" title="看不清？点击刷新" @click="loadCaptcha" />
              <el-button v-else :loading="captchaLoading" @click="loadCaptcha">获取验证码</el-button>
            </div>
          </el-form-item>

          <el-form-item label="健康目标" prop="healthGoal">
            <el-radio-group v-model="form.healthGoal">
              <el-radio-button v-for="item in healthGoals" :key="item" :value="item">{{ item }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="饮食偏好" prop="preference">
            <el-input v-model="form.preference" placeholder="如：清淡、少辣" />
          </el-form-item>
          <el-form-item label="忌口" prop="taboo">
            <el-input v-model="form.taboo" placeholder="如：香菜、牛肉（顿号分隔）" />
          </el-form-item>

          <div class="actions">
            <el-button type="primary" size="large" :loading="loading" @click="onSubmit">注册</el-button>
            <el-button size="large" @click="router.push('/login')">返回登录</el-button>
          </div>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getCaptcha } from '@/api/auth'
import { healthGoals } from '@/utils/dict'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const captchaLoading = ref(false)
const captcha = ref({ image: '' })
const form = ref({
  username: '',
  nickname: '',
  password: '',
  phone: '',
  healthGoal: '均衡饮食',
  preference: '',
  taboo: '',
  captchaKey: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  captchaCode: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

const loadCaptcha = async () => {
  captchaLoading.value = true
  try {
    const res = await getCaptcha()
    captcha.value = res.data
    form.value.captchaKey = res.data.key
    form.value.captchaCode = ''
  } finally {
    captchaLoading.value = false
  }
}

onMounted(loadCaptcha)

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register(form.value)
    ElMessage.success('注册成功，请登录')
    router.replace('/login')
  } catch (e) {
    // 验证码一次一用，失败后必须换一张
    loadCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 外壳样式（.auth-page / .auth-shell / .pitch / .panel）统一在全局 main.css，登录页共用 */

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 14px;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-img {
  height: 40px;
  width: 120px;
  flex: none;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: border-color 0.18s;
}

.captcha-img:hover {
  border-color: var(--brand);
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}

.actions .el-button {
  flex: 1;
  height: 44px;
  margin: 0;
}

.actions .el-button--primary {
  border: none;
  background: linear-gradient(135deg, #f7a94f, #ef8b2c);
  box-shadow: 0 8px 20px rgba(240, 140, 46, 0.32);
}

@media (max-width: 767px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
