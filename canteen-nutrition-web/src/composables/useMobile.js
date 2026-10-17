import { onBeforeUnmount, onMounted, ref } from 'vue'

// 断点需与 main.css、AppLayout 的 768px 保持一致
const QUERY = '(max-width: 767px)'

/**
 * 后台表格页在窄屏下要换一套列（合并次要字段、折叠操作按钮），
 * 用 matchMedia 而不是 window.innerWidth，旋转和缩放窗口时会自动跟随。
 */
export function useMobile() {
  const isMobile = ref(window.matchMedia(QUERY).matches)
  const onChange = (e) => {
    isMobile.value = e.matches
  }

  onMounted(() => window.matchMedia(QUERY).addEventListener('change', onChange))
  onBeforeUnmount(() => window.matchMedia(QUERY).removeEventListener('change', onChange))

  return { isMobile }
}
