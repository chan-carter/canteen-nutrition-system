<template>
  <el-container class="layout">
    <el-aside
      :width="isMobile ? '252px' : collapse ? '68px' : '216px'"
      :class="['aside', { 'is-mobile': isMobile, 'is-open': navOpen }]"
    >
      <div class="logo">
        <span class="logo-mark"><el-icon :size="20"><Bowl /></el-icon></span>
        <span v-show="!collapse" class="logo-text">
          食堂营养推荐
          <small>校园食堂点餐与营养分析</small>
        </span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="collapse" router class="side-menu">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
      <div v-show="!collapse" class="aside-foot">{{ roleMap[userStore.info.role] || '' }} · v1.0.0</div>
    </el-aside>

    <div v-if="isMobile && navOpen" class="nav-mask" @click="navOpen = false" />

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button text class="icon-btn" @click="toggleNav">
            <el-icon :size="18">
              <Expand v-if="isMobile ? !navOpen : collapse" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <div class="head-title">
            <span class="role-name">{{ roleMap[userStore.info.role] || '' }}工作台</span>
            <span class="crumb">{{ currentTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <el-popover
            placement="bottom-end"
            :width="noticeWidth"
            trigger="click"
            popper-class="notice-popover"
          >
            <template #reference>
              <el-badge :value="badgeCount" :hidden="!badgeCount" class="notice-badge">
                <el-button text class="icon-btn"><el-icon :size="18"><Bell /></el-icon></el-button>
              </el-badge>
            </template>
            <el-tabs v-model="noticeTab" class="notice-tabs">
              <el-tab-pane :label="`站内消息${msgUnread ? '(' + msgUnread + ')' : ''}`" name="message">
                <el-empty v-if="!messages.length" :image-size="52" description="暂无站内消息" />
                <div v-for="item in messages" :key="item.id" class="notice-item" @click="openMessage(item)">
                  <div class="notice-title">
                    <i v-if="!item.isRead" class="dot" />
                    {{ item.title }}
                    <span class="chip" :class="item.isRead ? '' : 'is-unread'">
                      {{ (notificationTypeMap[item.type] || {}).text || '通知' }}
                    </span>
                  </div>
                  <div class="muted">{{ item.content }}</div>
                  <div class="muted notice-time">{{ formatTime(item.createTime) }}</div>
                </div>
                <div v-if="msgUnread" class="notice-foot">
                  <el-button size="small" type="primary" plain :loading="reading" @click="readMessages">
                    全部标为已读
                  </el-button>
                </div>
              </el-tab-pane>
              <el-tab-pane :label="`公告${unread.length ? '(' + unread.length + ')' : ''}`" name="announce">
                <div class="notice-head">
                  <span>{{ showAll ? '全部公告' : '未读公告' }}</span>
                  <el-button text size="small" @click="showAll = !showAll">
                    {{ showAll ? '只看未读' : '查看全部' }}
                  </el-button>
                </div>
                <el-empty v-if="!noticeList.length" :image-size="52" :description="showAll ? '暂无公告' : '没有未读公告'" />
                <div v-for="item in noticeList" :key="item.id" class="notice-item">
                  <div class="notice-title">
                    <i v-if="isUnread(item.id)" class="dot" />
                    {{ item.title }}
                  </div>
                  <div class="muted">{{ item.content }}</div>
                  <div class="muted notice-time">{{ formatTime(item.createTime) }}</div>
                </div>
                <div v-if="unread.length" class="notice-foot">
                  <el-button size="small" type="primary" plain :loading="reading" @click="markRead">全部标为已读</el-button>
                </div>
              </el-tab-pane>
            </el-tabs>
            <div class="notice-foot notice-all">
              <el-button text size="small" @click="router.push('/notifications')">前往消息中心</el-button>
            </div>
          </el-popover>

          <router-link v-if="userStore.isStudent" to="/student/cart" class="cart-link">
            <el-badge :value="userStore.cartCount" :hidden="!userStore.cartCount">
              <el-button text class="icon-btn"><el-icon :size="18"><ShoppingCart /></el-icon></el-button>
            </el-badge>
          </router-link>

          <el-dropdown @command="onCommand">
            <span class="user-box">
              <el-avatar :size="30" class="avatar">{{ (userStore.nickname || 'U').charAt(0) }}</el-avatar>
              <span class="user-name">{{ userStore.nickname }}</span>
              <el-icon class="muted"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { listPublished, listUnreadAnnouncements, readAllAnnouncements } from '@/api/business'
import { listNotifications, readAllNotifications, readNotification, unreadNotifications } from '@/api/interaction'
import { notificationTypeMap, roleMap } from '@/utils/dict'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapse = ref(false)
const isMobile = ref(window.innerWidth < 768)
const navOpen = ref(false)
const unread = ref([])
const published = ref([])
const showAll = ref(false)
const reading = ref(false)
const messages = ref([])
const msgUnread = ref(0)
const noticeTab = ref('message')

const badgeCount = computed(() => unread.value.length + msgUnread.value)

const noticeList = computed(() => (showAll.value ? published.value : unread.value))
const noticeWidth = computed(() => (isMobile.value ? Math.min(320, window.innerWidth - 16) : 360))
const isUnread = (id) => unread.value.some((item) => item.id === id)
const formatTime = (value) => (value ? value.replace('T', ' ').slice(5, 16) : '')

const toggleNav = () => {
  if (isMobile.value) {
    navOpen.value = !navOpen.value
  } else {
    collapse.value = !collapse.value
  }
}

const onViewportChange = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    navOpen.value = false
  }
}

const loadUnread = async () => {
  try {
    const res = await listUnreadAnnouncements()
    unread.value = res.data || []
  } catch (e) {
    unread.value = []
  }
}

const loadMessages = async () => {
  try {
    const res = await listNotifications(10)
    messages.value = res.data || []
    const cnt = await unreadNotifications()
    msgUnread.value = (cnt.data && cnt.data.count) || 0
  } catch (e) {
    messages.value = []
    msgUnread.value = 0
  }
}

const readMessages = async () => {
  reading.value = true
  try {
    await readAllNotifications()
    await loadMessages()
    ElMessage.success('站内消息已全部标记为已读')
  } finally {
    reading.value = false
  }
}

const openMessage = async (item) => {
  if (item.isRead) {
    return
  }
  await readNotification(item.id)
  item.isRead = 1
  msgUnread.value = Math.max(0, msgUnread.value - 1)
  if (item.type === 'order' && item.bizId) {
    router.push(`/order/${item.bizId}`)
  }
}

const menus = computed(() => {
  const role = userStore.info.role
  const layout = router.getRoutes().find((item) => item.path === '/')
  const children = layout && layout.children ? layout.children : []
  return children
    .filter((item) => item.meta && item.meta.title && !item.meta.hidden && (!item.meta.roles || item.meta.roles.includes(role)))
    .map((item) => ({
      path: '/' + item.path.replace(/^\//, ''),
      title: item.meta.title,
      icon: item.meta.icon,
      // 消息中心排在业务菜单之后、账号设置之前
      weight: /profile$/.test(item.path) ? 2 : item.path.includes('notifications') ? 1 : 0
    }))
    .sort((a, b) => a.weight - b.weight)
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

const profilePath = () => {
  const role = userStore.info.role
  return role === 1 ? '/student/profile' : role === 2 ? '/merchant/profile' : '/admin/profile'
}

const onCommand = async (command) => {
  if (command === 'profile') {
    router.push(profilePath())
    return
  }
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}

const loadPublished = async () => {
  try {
    const res = await listPublished()
    published.value = res.data || []
  } catch (e) {
    published.value = []
  }
}

const markRead = async () => {
  reading.value = true
  try {
    await readAllAnnouncements()
    showAll.value = false
    await loadUnread()
    ElMessage.success('公告已全部标记为已读')
  } finally {
    reading.value = false
  }
}

watch(showAll, async (value) => {
  if (value && !published.value.length) {
    await loadPublished()
  }
})

// 换页后抽屉不该留在屏幕上
watch(() => route.path, () => {
  navOpen.value = false
})

onMounted(async () => {
  window.addEventListener('resize', onViewportChange)
  await userStore.loadInfo()
  await Promise.all([loadUnread(), loadMessages()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onViewportChange)
})
</script>

<style scoped>
.layout {
  height: 100%;
}

/* ---------------- 侧栏 ---------------- */

.aside {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, var(--sidebar-from), var(--sidebar-to));
  transition: width 0.24s cubic-bezier(0.4, 0, 0.2, 1), transform 0.24s cubic-bezier(0.4, 0, 0.2, 1);
  overflow-x: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 62px;
  flex: none;
  padding: 0 14px;
}

.logo-mark {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  flex: none;
  border-radius: 11px;
  color: #fff;
  background: linear-gradient(135deg, #f7b95c, var(--brand));
  box-shadow: 0 6px 16px rgba(240, 140, 46, 0.35);
}

.logo-text {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.3px;
}

.logo-text small {
  font-size: 11px;
  font-weight: 400;
  color: #8b93a3;
  margin-top: 2px;
}

.side-menu {
  flex: 1;
  padding: 6px 10px;
  background: transparent;
  border-right: none;
}

.side-menu :deep(.el-menu-item) {
  height: 44px;
  margin-bottom: 4px;
  border-radius: 10px;
  color: #c5cbd6;
  background: transparent;
  transition: background 0.18s, color 0.18s;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

.side-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  font-weight: 600;
  background: linear-gradient(90deg, rgba(240, 140, 46, 0.95), rgba(240, 140, 46, 0.55));
  box-shadow: 0 6px 16px rgba(240, 140, 46, 0.28);
}

.side-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff;
}

.aside-foot {
  flex: none;
  padding: 12px 16px;
  font-size: 11.5px;
  color: #6d7482;
  white-space: nowrap;
}

/* ---------------- 顶栏 ---------------- */

.header {
  position: sticky;
  top: 0;
  z-index: 10;
  height: 62px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.head-title {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.role-name {
  font-weight: 600;
  font-size: 14.5px;
}

.crumb {
  font-size: 12px;
  color: var(--ink-faint);
}

.icon-btn {
  width: 34px;
  height: 34px;
  padding: 0;
  border-radius: 10px;
}

.icon-btn:hover {
  background: var(--surface-sunken);
}

.cart-link {
  display: flex;
  align-items: center;
}

.notice-badge {
  margin-right: 2px;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 999px;
  cursor: pointer;
  outline: none;
  transition: background 0.18s;
}

.user-box:hover {
  background: var(--surface-sunken);
}

.avatar {
  background: linear-gradient(135deg, #f7b95c, var(--brand));
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

/* ---------------- 主体 ---------------- */

.main {
  padding: 18px 20px 28px;
  background: radial-gradient(1100px 320px at 12% -8%, rgba(240, 140, 46, 0.07), transparent 60%), var(--surface-sunken);
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.notice-item {
  padding: 9px 0;
  border-bottom: 1px dashed var(--line);
  cursor: pointer;
}

.notice-item .chip {
  font-size: 11px;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-tabs :deep(.el-tabs__header) {
  margin-bottom: 4px;
}

.notice-all {
  justify-content: center;
  border-top: none;
  padding-top: 4px;
}

.notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  margin-bottom: 4px;
}

.notice-title .dot {
  width: 6px;
  height: 6px;
  flex: none;
  border-radius: 50%;
  background: var(--danger);
}

.notice-time {
  margin-top: 4px;
  font-size: 11.5px;
}

.notice-foot {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  margin-top: 4px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* ---------------- 手机端（<768px）：侧栏改为抽屉，顶栏收窄 ---------------- */

@media (max-width: 767px) {
  .aside.is-mobile {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    z-index: 2300;
    transform: translateX(-102%);
    box-shadow: none;
  }

  .aside.is-mobile.is-open {
    transform: none;
    box-shadow: 0 0 44px rgba(8, 12, 20, 0.34);
  }

  .nav-mask {
    position: fixed;
    inset: 0;
    z-index: 2250;
    background: rgba(15, 20, 28, 0.42);
  }

  .header {
    height: 54px;
    padding: 0 14px 0 6px;
  }

  .header-left,
  .header-right {
    gap: 6px;
  }

  .role-name {
    font-size: 14px;
  }

  .crumb,
  .user-name {
    display: none;
  }

  .main {
    padding: 12px 12px 22px;
  }

  .notice-popover {
    max-width: calc(100vw - 20px);
  }
}
</style>
