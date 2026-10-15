import { defineStore } from 'pinia'
import { login as loginApi, getMyInfo, logout as logoutApi } from '@/api/auth'
import { ROLE_STUDENT } from '@/utils/dict'

const TOKEN_KEY = 'token'
const CART_KEY = 'cart'

const readCart = () => {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY) || '{}')
  } catch (e) {
    return {}
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    info: {},
    // 购物车持久化到本地，避免刷新页面后丢失
    cart: readCart()
  }),
  getters: {
    logged: (state) => !!state.token,
    role: (state) => state.info.role,
    nickname: (state) => state.info.nickname || state.info.username || '',
    isStudent: (state) => state.info.role === ROLE_STUDENT,
    cartItems: (state) => Object.values(state.cart),
    cartCount: (state) => Object.values(state.cart).reduce((sum, item) => sum + item.quantity, 0),
    cartAmount: (state) =>
      Object.values(state.cart)
        .reduce((sum, item) => sum + item.price * item.quantity, 0)
        .toFixed(2),
    cartCalorie: (state) =>
      Object.values(state.cart)
        .reduce((sum, item) => sum + (item.calorie ? item.calorie * item.quantity : 0), 0)
        .toFixed(1)
  },
  actions: {
    async login(form) {
      const res = await loginApi(form)
      this.token = res.data.token
      localStorage.setItem(TOKEN_KEY, this.token)
      await this.loadInfo()
      return res.data
    },
    async loadInfo() {
      if (!this.token) return
      const res = await getMyInfo()
      this.info = res.data || {}
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 令牌已失效时忽略退出接口异常
      }
      this.clear()
    },
    clear() {
      this.token = ''
      this.info = {}
      this.cart = {}
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(CART_KEY)
    },
    persistCart() {
      localStorage.setItem(CART_KEY, JSON.stringify(this.cart))
    },
    setInfo(patch) {
      this.info = { ...this.info, ...patch }
    },
    addToCart(dish, quantity = 1) {
      if (!dish) return
      const key = String(dish.dishId || dish.id)
      const exist = this.cart[key]
      const base = exist || {
        dishId: dish.dishId || dish.id,
        dishName: dish.dishName || dish.name,
        price: Number(dish.price || 0),
        calorie: dish.calorie ? Number(dish.calorie) : null,
        quantity: 0
      }
      base.quantity += quantity
      this.cart[key] = base
      this.persistCart()
    },
    updateCartQuantity(dishId, quantity) {
      const key = String(dishId)
      if (!this.cart[key]) return
      if (quantity <= 0) {
        delete this.cart[key]
      } else {
        this.cart[key].quantity = quantity
      }
      this.persistCart()
    },
    removeCartItem(dishId) {
      delete this.cart[String(dishId)]
      this.persistCart()
    },
    clearCart() {
      this.cart = {}
      this.persistCart()
    }
  }
})
