<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">消息中心</h2>
        <p class="page-desc">订单状态变更、菜品审核结果等站内通知</p>
      </div>
      <div class="head-actions">
        <el-radio-group v-model="filter" size="small" @change="load">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="unread">未读 {{ unreadCount ? `(${unreadCount})` : '' }}</el-radio-button>
        </el-radio-group>
        <el-button size="small" type="primary" plain :icon="Check" :disabled="!unreadCount" @click="readAll">
          全部标为已读
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="block" v-loading="loading">
      <el-empty v-if="!shown.length && !loading" :image-size="70" description="这里还没有消息" />
      <div
        v-for="item in shown"
        :key="item.id"
        class="msg-row"
        :class="{ 'is-unread': !item.isRead }"
        @click="open(item)"
      >
        <span class="dot" :class="{ hidden: item.isRead }" />
        <div class="msg-main">
          <div class="msg-title">
            {{ item.title }}
            <el-tag size="small" effect="plain" :type="(notificationTypeMap[item.type] || {}).type || 'info'">
              {{ (notificationTypeMap[item.type] || {}).text || item.type }}
            </el-tag>
          </div>
          <div class="msg-content">{{ item.content }}</div>
          <div class="muted msg-time">{{ formatTime(item.createTime) }}</div>
        </div>
        <el-icon v-if="item.type === 'order' && item.bizId" class="arrow"><ArrowRight /></el-icon>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Check } from '@element-plus/icons-vue'
import { listNotifications, readAllNotifications, readNotification, unreadNotifications } from '@/api/interaction'
import { notificationTypeMap } from '@/utils/dict'

const router = useRouter()
const rows = ref([])
const unreadCount = ref(0)
const filter = ref('all')
const loading = ref(false)

const shown = computed(() => (filter.value === 'unread' ? rows.value.filter((item) => !item.isRead) : rows.value))

const formatTime = (value) => (value ? String(value).replace('T', ' ').slice(0, 16) : '')

const load = async () => {
  loading.value = true
  try {
    const res = await listNotifications(50)
    rows.value = res.data || []
    const cnt = await unreadNotifications()
    unreadCount.value = (cnt.data && cnt.data.count) || 0
  } finally {
    loading.value = false
  }
}

const open = async (item) => {
  if (!item.isRead) {
    await readNotification(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  if (item.type === 'order' && item.bizId) {
    router.push(`/order/${item.bizId}`)
  }
}

const readAll = async () => {
  await readAllNotifications()
  ElMessage.success('已全部标记为已读')
  await load()
}

onMounted(load)
</script>

<style scoped>
.block :deep(.el-card__body) {
  padding: 6px 14px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 13px 6px;
  border-bottom: 1px dashed var(--line);
  cursor: pointer;
}

.msg-row:last-child {
  border-bottom: none;
}

.msg-row.is-unread .msg-title {
  font-weight: 700;
}

.dot {
  flex: none;
  width: 7px;
  height: 7px;
  margin-top: 7px;
  border-radius: 50%;
  background: var(--danger);
}

.dot.hidden {
  background: var(--line);
}

.msg-main {
  flex: 1;
  min-width: 0;
}

.msg-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14.5px;
  margin-bottom: 4px;
}

.msg-content {
  font-size: 13px;
  color: var(--ink-soft);
  line-height: 19px;
}

.msg-time {
  margin-top: 4px;
  font-size: 11.5px;
}

.arrow {
  margin-top: 10px;
  color: var(--ink-faint);
}
</style>
