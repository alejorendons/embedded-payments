import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { User } from '@/types'
import { authService } from '@/services/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)
  const isLoading = ref(false)
  const isInitialized = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN')
  const isMerchant = computed(
    () => user.value?.role === 'ROLE_MERCHANT' || user.value?.role === 'ROLE_ADMIN'
  )

  async function login(email: string, password: string) {
    isLoading.value = true
    error.value = null
    try {
      const response = await authService.login(email, password)
      token.value = response.token
      localStorage.setItem('token', response.token)
      user.value = await authService.getCurrentUser()
      isInitialized.value = true
    } catch (err: any) {
      logout(true)
      error.value = err.response?.data?.message || 'Login failed'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function initializeSession() {
    if (isInitialized.value) {
      return
    }

    if (!token.value) {
      isInitialized.value = true
      return
    }

    isLoading.value = true
    error.value = null
    try {
      user.value = await authService.getCurrentUser()
    } catch (_err) {
      logout(true)
    } finally {
      isInitialized.value = true
      isLoading.value = false
    }
  }

  function logout(skipApiCall = false) {
    token.value = null
    user.value = null
    error.value = null
    isInitialized.value = true
    localStorage.removeItem('token')

    if (!skipApiCall) {
      authService.logout()
    }
  }

  return {
    token,
    user,
    isLoading,
    isInitialized,
    error,
    isAuthenticated,
    isAdmin,
    isMerchant,
    login,
    initializeSession,
    logout,
  }
})
