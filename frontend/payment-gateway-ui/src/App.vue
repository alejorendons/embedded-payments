<template>
  <div class="min-h-screen bg-slate-50">
    <div v-if="isLoading" class="flex items-center justify-center h-screen bg-gradient-to-br from-slate-900 to-slate-800">
      <div class="space-y-4 text-center">
        <svg class="animate-spin h-12 w-12 text-white mx-auto" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p class="text-white text-sm font-medium">Loading...</p>
      </div>
    </div>
    <router-view v-else />
    <Notifications />
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import Notifications from '@/components/shared/Notifications.vue'

const authStore = useAuthStore()
const isLoading = computed(() => authStore.isLoading)

onMounted(() => {
  authStore.initializeSession()
})
</script>
