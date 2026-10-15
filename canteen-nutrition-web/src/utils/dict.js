/**
 * 字典与常量：与后端 Role/Status 编码保持一致
 */
export const ROLE_STUDENT = 1
export const ROLE_MERCHANT = 2
export const ROLE_ADMIN = 3

export const roleMap = {
  [ROLE_STUDENT]: '学生',
  [ROLE_MERCHANT]: '食堂商户',
  [ROLE_ADMIN]: '管理员'
}

export const healthGoals = ['减脂', '增肌', '均衡饮食']

export const dishStatusMap = {
  1: { text: '上架', type: 'success' },
  0: { text: '下架', type: 'info' }
}

export const auditStatusMap = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '审核通过', type: 'success' },
  2: { text: '审核驳回', type: 'danger' }
}

export const commonStatusMap = {
  1: { text: '正常', type: 'success' },
  0: { text: '禁用', type: 'danger' }
}

export const stallStatusMap = {
  1: { text: '营业中', type: 'success' },
  0: { text: '已关闭', type: 'info' }
}

export const merchantStatusMap = {
  1: { text: '经营中', type: 'success' },
  0: { text: '已停业', type: 'info' }
}

export const orderStatusMap = {
  1: { text: '待接单', type: 'warning' },
  2: { text: '已接单', type: 'primary' },
  5: { text: '待取餐', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' }
}

export const orderTypeMap = {
  PICKUP: '点餐自取',
  RESERVE: '预订'
}

export const mealTypes = ['早餐', '午餐', '晚餐', '加餐']

export const activityLevels = [
  { value: 1, label: '久坐（很少运动）', factor: 1.2 },
  { value: 2, label: '轻度（每周1-3次）', factor: 1.375 },
  { value: 3, label: '中度（每周3-5次）', factor: 1.55 },
  { value: 4, label: '重度（每周6-7次）', factor: 1.725 }
]

export const genderMap = {
  1: '男',
  2: '女'
}

export const notificationTypeMap = {
  order: { text: '订单', type: 'primary' },
  audit: { text: '审核', type: 'warning' },
  announcement: { text: '公告', type: 'success' },
  system: { text: '系统', type: 'info' }
}

export const dietSources = {
  order: '订单同步',
  manual: '手动录入'
}

export function tagOf(map, value) {
  return map[value] || { text: '-', type: 'info' }
}
