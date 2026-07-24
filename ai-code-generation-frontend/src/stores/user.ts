import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { userApi } from '@/api/user'
import type { LoginUserVO, UserLoginRequest, UserRegisterRequest } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<LoginUserVO | null>(null)
  const initialized = ref(false)
  const loading = ref(false)

  const isLoggedIn = computed(() => currentUser.value !== null)
  const isAdmin = computed(() => currentUser.value?.userRole === 'admin')

  const fetchLoginUser = async () => {
    loading.value = true

    try {
      currentUser.value = await userApi.getLoginUser()
      return currentUser.value
    } catch {
      currentUser.value = null
      return null
    } finally {
      initialized.value = true
      loading.value = false
    }
  }

  const login = async (data: UserLoginRequest) => {
    const user = await userApi.login(data)
    currentUser.value = user
    initialized.value = true
    return user
  }

  const register = (data: UserRegisterRequest) => userApi.register(data)

  const logout = async () => {
    await userApi.logout()
    currentUser.value = null
    initialized.value = true
  }

  return {
    currentUser,
    initialized,
    loading,
    isLoggedIn,
    isAdmin,
    fetchLoginUser,
    login,
    register,
    logout,
  }
})
