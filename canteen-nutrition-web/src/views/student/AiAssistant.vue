<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">AI 营养问答</h2>
        <p class="page-desc">结合你的健康档案与当天饮食记录回答营养问题</p>
      </div>
      <div>
        <el-tag v-if="lastSource" size="small" :type="lastSource === 'AI模型' ? 'success' : 'warning'" effect="light">
          {{ lastSource }}
        </el-tag>
        <el-button size="small" text bg :icon="RefreshLeft" @click="reset">新建会话</el-button>
      </div>
    </div>

    <el-alert
      title="回答由大模型基于你的档案与饮食记录生成，属营养建议参考，不能替代医生或注册营养师的意见。"
      type="info"
      :closable="false"
      show-icon
      class="tip"
    />

    <el-card shadow="never" class="chat-card">
      <div ref="scroller" class="chat-body" v-loading="loadingHistory">
        <div v-if="!messages.length" class="hello">
          <el-icon :size="26"><ChatDotRound /></el-icon>
          <div>
            <div class="hello-title">可以问我点什么</div>
            <div class="hello-sub">例如今天热量够不够、减脂期怎么点餐、某个菜品营养如何</div>
          </div>
        </div>
        <div v-for="(m, index) in messages" :key="index" class="bubble" :class="m.role">
          <span class="avatar">{{ m.role === 'user' ? (userStore.nickname || '我').charAt(0) : 'AI' }}</span>
          <div class="content">
            <div class="text">{{ m.content }}</div>
            <div v-if="m.meta" class="meta muted">{{ m.meta }}</div>
          </div>
        </div>
        <div v-if="sending" class="bubble assistant">
          <span class="avatar">AI</span>
          <div class="content"><div class="text typing">正在思考…</div></div>
        </div>
      </div>

      <div class="quick">
        <el-button v-for="q in quicks" :key="q" size="small" text bg :disabled="sending" @click="send(q)">
          {{ q }}
        </el-button>
      </div>

      <div class="sender">
        <el-input
          v-model="draft"
          type="textarea"
          :rows="2"
          resize="none"
          maxlength="300"
          show-word-limit
          placeholder="输入你的营养问题，回车发送"
          @keydown.enter.exact.prevent="send()"
        />
        <el-button type="primary" :icon="Promotion" :loading="sending" @click="send()">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { ChatDotRound, Promotion, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiChat, aiHistory } from '@/api/health'
import { useUserStore } from '@/stores/user'

const SESSION_KEY = 'ai_session_id'
const userStore = useUserStore()

const messages = ref([])
const draft = ref('')
const sending = ref(false)
const loadingHistory = ref(false)
const lastSource = ref('')
const scroller = ref()

const quicks = ['今天的热量超标了吗？', '减脂期晚餐怎么搭配？', '帮我看看高蛋白的菜品', '我最近体重变化正常吗？']

const scrollBottom = async () => {
  await nextTick()
  if (scroller.value) scroller.value.scrollTop = scroller.value.scrollHeight
}

const loadHistory = async () => {
  const sessionId = localStorage.getItem(SESSION_KEY)
  if (!sessionId) return
  loadingHistory.value = true
  try {
    const res = await aiHistory(sessionId)
    messages.value = (res.data || []).map((item) => ({
      role: item.role,
      content: item.content,
      meta: item.role === 'assistant' && item.costMs ? `模型 ${item.modelName || '-'} · 耗时 ${item.costMs}ms` : ''
    }))
  } catch (e) {
    messages.value = []
  } finally {
    loadingHistory.value = false
    scrollBottom()
  }
}

const send = async (text) => {
  const content = (text || draft.value).trim()
  if (!content || sending.value) return
  messages.value.push({ role: 'user', content })
  draft.value = ''
  sending.value = true
  scrollBottom()
  try {
    const res = await aiChat({ sessionId: localStorage.getItem(SESSION_KEY) || '', message: content })
    const data = res.data || {}
    if (data.sessionId) localStorage.setItem(SESSION_KEY, data.sessionId)
    lastSource.value = data.source || ''
    messages.value.push({
      role: 'assistant',
      content: data.reply || '暂时没有回复，请稍后再试',
      meta: `${data.source || ''} · ${data.costMs || 0}ms`
    })
    if (data.success === false) ElMessage.warning('大模型未返回结果，本次使用内置规则作答')
  } finally {
    sending.value = false
    scrollBottom()
  }
}

const reset = () => {
  localStorage.removeItem(SESSION_KEY)
  messages.value = []
  lastSource.value = ''
  ElMessage.success('已开始新会话')
}

onMounted(loadHistory)
</script>

<style scoped>
.tip {
  margin-bottom: 14px;
  border-radius: var(--radius-sm);
}

.chat-card :deep(.el-card__body) {
  padding: 14px;
}

.chat-body {
  min-height: 320px;
  max-height: 46vh;
  overflow-y: auto;
  padding: 4px 4px 10px;
}

.hello {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 40px 10px;
  color: var(--ink-faint);
}

.hello-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink-soft);
}

.bubble {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.bubble.user {
  flex-direction: row-reverse;
}

.avatar {
  flex: none;
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #f7b95c, var(--brand));
}

.bubble.user .avatar {
  background: linear-gradient(135deg, #6ba5f9, #3f7fe0);
}

.content {
  max-width: 74%;
}

.text {
  padding: 9px 13px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 21px;
  white-space: pre-wrap;
  word-break: break-word;
  background: var(--surface-sunken);
  border: 1px solid var(--line);
}

.bubble.user .text {
  background: var(--brand-soft);
  border-color: #f7dcc0;
}

.meta {
  margin-top: 4px;
  font-size: 11px;
}

.typing {
  color: var(--ink-faint);
}

.quick {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 0;
  border-top: 1px dashed var(--line);
}

.sender {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

@media (max-width: 767px) {
  .content {
    max-width: 82%;
  }

  .sender {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
