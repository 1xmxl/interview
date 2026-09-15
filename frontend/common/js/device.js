/**
 * 平台检测工具
 * 根据浏览器 UA + 屏幕宽度判断移动端/桌面端
 */
import { ref } from 'vue'

export const isDesktop = ref(false)
export const isMobile = ref(true)

// #ifdef H5
function checkDevice() {
  const ua = navigator.userAgent.toLowerCase()
  const isMobileUA = /android|iphone|ipad|ipod|blackberry|iemobile|opera mini|mobile/i.test(ua)
  const width = window.innerWidth
  // 桌面端：非移动端UA 且 屏幕宽度≥768px
  isDesktop.value = !isMobileUA && width >= 768
  isMobile.value = !isDesktop.value
}
checkDevice()
window.addEventListener('resize', checkDevice)
// #endif

// #ifdef APP-PLUS
isDesktop.value = false
isMobile.value = true
// #endif

// #ifdef MP-WEIXIN
isDesktop.value = false
isMobile.value = true
// #endif
