<template>
  <div class="flex h-screen bg-slate-50">
    <!-- Sidebar -->
    <div class="w-64 bg-white border-r border-slate-200 flex flex-col">
      <!-- Logo -->
      <div class="p-6 border-b border-slate-200">
        <h1 class="text-xl font-bold text-slate-900">Payment Gateway</h1>
        <p class="text-xs text-slate-500 mt-1">Merchant Dashboard</p>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-4 py-6 space-y-1">
        <router-link
          to="/dashboard"
          class="flex items-center px-4 py-2.5 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors active-nav"
          active-class="bg-slate-100 text-slate-900 font-semibold"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-3m0 0l7-4 7 4M5 9v10a1 1 0 001 1h12a1 1 0 001-1V9m-9 4h4" />
          </svg>
          Dashboard
        </router-link>
        <router-link
          to="/transactions"
          class="flex items-center px-4 py-2.5 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors active-nav"
          active-class="bg-slate-100 text-slate-900 font-semibold"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Transactions
        </router-link>
        <router-link
          to="/settings"
          class="flex items-center px-4 py-2.5 rounded-lg text-slate-700 hover:bg-slate-100 transition-colors active-nav"
          active-class="bg-slate-100 text-slate-900 font-semibold"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          Settings
        </router-link>
      </nav>

      <!-- User Profile -->
      <div class="p-4 border-t border-slate-200 space-y-4">
        <div class="px-2 py-3 rounded-lg bg-slate-50">
          <p class="text-xs text-slate-500 uppercase tracking-wider">Account</p>
          <p class="text-sm font-medium text-slate-900 mt-1 truncate">{{ authStore.user?.email }}</p>
        </div>
        <button
          @click="logout"
          class="w-full px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-slate-100 text-slate-900 hover:bg-slate-200 active:bg-slate-300 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
        >
          Sign out
        </button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <div class="flex-1 overflow-auto">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.active-nav {
  position: relative;
}
</style>

