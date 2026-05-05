<template>
  <div class="min-h-screen bg-slate-50">
    <AppLayout>
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-slate-900">Dashboard</h1>
          <p class="text-slate-600 mt-1">Welcome back to your merchant account</p>
        </div>

        <!-- Loading State -->
        <div v-if="merchantStore.isLoading" class="flex justify-center items-center py-16">
          <svg class="animate-spin h-10 w-10 text-slate-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>

        <!-- Error State -->
        <div v-else-if="merchantStore.error" class="bg-white rounded-xl shadow-sm border border-red-200 transition-all duration-200 bg-red-50 p-4 mb-6">
          <div class="flex items-start">
            <svg class="w-5 h-5 text-red-600 mt-0.5 mr-3 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
            <span class="text-red-800">{{ merchantStore.error }}</span>
          </div>
        </div>

        <!-- Dashboard Content -->
        <div v-else-if="merchantStore.current" class="space-y-8">
          <!-- Status Overview -->
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 p-6">
            <div class="flex items-start justify-between mb-6">
              <div>
                <h2 class="text-xl font-semibold text-slate-900">Account Status</h2>
                <p class="text-sm text-slate-600 mt-1">Your merchant account information</p>
              </div>
              <span
                :class="[
                  'badge font-semibold',
                  merchantStore.current.status === 'ACTIVE'
                    ? 'badge-success'
                    : 'badge-error',
                ]"
              >
                {{ merchantStore.current.status }}
              </span>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div class="border-l-2 border-slate-200 pl-4">
                <p class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Business Name</p>
                <p class="text-lg font-semibold text-slate-900 mt-1">{{ merchantStore.current.name }}</p>
              </div>
              <div class="border-l-2 border-slate-200 pl-4">
                <p class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Email</p>
                <p class="text-lg font-semibold text-slate-900 mt-1">{{ merchantStore.current.email }}</p>
              </div>
              <div class="border-l-2 border-slate-200 pl-4">
                <p class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Account ID</p>
                <p class="text-lg font-mono text-slate-900 mt-1">{{ merchantStore.current.id.slice(0, 8) }}...</p>
              </div>
            </div>
          </div>

          <!-- Quick Actions Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <router-link
              to="/settings/contact"
              class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 hover:shadow-md hover:border-slate-300 group p-6 cursor-pointer"
            >
              <div class="flex items-start justify-between mb-3">
                <svg class="w-6 h-6 text-slate-400 group-hover:text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </div>
              <h3 class="font-semibold text-slate-900 mb-1">Contact Info</h3>
              <p class="text-sm text-slate-600">Manage your contact details</p>
            </router-link>

            <router-link
              to="/settings/bank"
              class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 hover:shadow-md hover:border-slate-300 group p-6 cursor-pointer"
            >
              <div class="flex items-start justify-between mb-3">
                <svg class="w-6 h-6 text-slate-400 group-hover:text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 class="font-semibold text-slate-900 mb-1">Bank Account</h3>
              <p class="text-sm text-slate-600">Register banking details</p>
            </router-link>

            <router-link
              to="/settings/profile"
              class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 hover:shadow-md hover:border-slate-300 group p-6 cursor-pointer"
            >
              <div class="flex items-start justify-between mb-3">
                <svg class="w-6 h-6 text-slate-400 group-hover:text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 class="font-semibold text-slate-900 mb-1">Profile</h3>
              <p class="text-sm text-slate-600">View merchant profile</p>
            </router-link>

            <router-link
              to="/transactions"
              class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 hover:shadow-md hover:border-slate-300 group p-6 cursor-pointer"
            >
              <div class="flex items-start justify-between mb-3">
                <svg class="w-6 h-6 text-slate-400 group-hover:text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <h3 class="font-semibold text-slate-900 mb-1">Transactions</h3>
              <p class="text-sm text-slate-600">View payment history</p>
            </router-link>
          </div>

          <!-- Configuration Cards -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="card p-6 border-l-4 border-blue-500">
              <h3 class="font-semibold text-slate-900 mb-4 flex items-center">
                <svg class="w-5 h-5 text-blue-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Contact Information
              </h3>
              <div class="space-y-2">
                <div v-if="merchantStore.current.contact_name" class="text-sm">
                  <span class="text-slate-600">Name:</span>
                  <span class="font-medium text-slate-900 ml-2">{{ merchantStore.current.contact_name }}</span>
                </div>
                <div v-if="merchantStore.current.contact_email" class="text-sm">
                  <span class="text-slate-600">Email:</span>
                  <span class="font-medium text-slate-900 ml-2">{{ merchantStore.current.contact_email }}</span>
                </div>
                <div v-if="!merchantStore.current.contact_name && !merchantStore.current.contact_email" class="text-sm text-slate-500 italic">
                  No contact information configured
                </div>
              </div>
            </div>

            <div class="card p-6 border-l-4 border-emerald-500">
              <h3 class="font-semibold text-slate-900 mb-4 flex items-center">
                <svg class="w-5 h-5 text-emerald-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Bank Account Status
              </h3>
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-slate-600">Status</p>
                  <p class="text-lg font-semibold text-slate-900 mt-1">
                    {{ merchantStore.hasBankAccount ? '✓ Configured' : 'Not configured' }}
                  </p>
                </div>
                <svg
                  v-if="merchantStore.hasBankAccount"
                  class="w-8 h-8 text-emerald-500"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>

          <!-- Account Management -->
          <div class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 p-6">
            <h2 class="text-xl font-semibold text-slate-900 mb-6">Account Management</h2>
            <div v-if="merchantStore.current.status === 'ACTIVE'" class="bg-emerald-50 border border-emerald-200 rounded-lg p-6">
              <div class="flex items-start justify-between">
                <div>
                  <p class="font-semibold text-emerald-900">Account is Active</p>
                  <p class="text-sm text-emerald-800 mt-1">Your merchant account is ready to process payments</p>
                </div>
                <button
                  @click="openDeactivateDialog"
                  :disabled="merchantStore.isLoading"
                  class="px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-slate-100 text-slate-900 hover:bg-slate-200 active:bg-slate-300 disabled:opacity-50 disabled:cursor-not-allowed text-sm ml-4 flex-shrink-0"
                >
                  {{ merchantStore.isLoading ? 'Processing...' : 'Deactivate' }}
                </button>
              </div>
            </div>
            <div v-else class="bg-red-50 border border-red-200 rounded-lg p-6">
              <div class="flex items-start justify-between">
                <div>
                  <p class="font-semibold text-red-900">Account is Inactive</p>
                  <p class="text-sm text-red-800 mt-1">Your merchant account is currently disabled</p>
                </div>
                <button
                  @click="openActivateDialog"
                  :disabled="merchantStore.isLoading"
                  class="px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-slate-900 text-white hover:bg-slate-800 active:bg-slate-950 disabled:opacity-50 disabled:cursor-not-allowed text-sm ml-4 flex-shrink-0"
                >
                  {{ merchantStore.isLoading ? 'Processing...' : 'Activate' }}
                </button>
              </div>
            </div>
          </div>

          <!-- Activate Dialog -->
          <transition
            enter-active-class="transition ease-out duration-200"
            leave-active-class="transition ease-in duration-150"
            enter-from-class="opacity-0"
            leave-to-class="opacity-0"
          >
            <div v-if="showActivateDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
              <div class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 max-w-md w-full p-6 animate-in fade-in zoom-in-95">
                <h3 class="text-xl font-semibold text-slate-900 mb-4">Activate Account</h3>
                <p class="text-slate-600 mb-6">Please provide a reason for activating your merchant account.</p>
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1.5">Activation Reason</label>
                    <textarea
                      v-model="statusChangeForm.reason"
                      rows="3"
                      placeholder="Explain why you want to reactivate your account..."
                      class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent resize-none"
                    />
                  </div>
                  <div v-if="statusChangeError" class="bg-white rounded-xl shadow-sm border border-red-200 transition-all duration-200 bg-red-50 p-3">
                    <p class="text-sm text-red-800">{{ statusChangeError }}</p>
                  </div>
                  <div class="flex gap-3">
                    <button
                      @click="closeStatusDialog"
                      :disabled="merchantStore.isLoading"
                      class="px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-slate-100 text-slate-900 hover:bg-slate-200 active:bg-slate-300 disabled:opacity-50 disabled:cursor-not-allowed flex-1"
                    >
                      Cancel
                    </button>
                    <button
                      @click="handleActivate"
                      :disabled="merchantStore.isLoading || !statusChangeForm.reason.trim()"
                      class="px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-slate-900 text-white hover:bg-slate-800 active:bg-slate-950 disabled:opacity-50 disabled:cursor-not-allowed flex-1"
                    >
                      {{ merchantStore.isLoading ? 'Processing...' : 'Activate' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </transition>

          <!-- Deactivate Dialog -->
          <transition
            enter-active-class="transition ease-out duration-200"
            leave-active-class="transition ease-in duration-150"
            enter-from-class="opacity-0"
            leave-to-class="opacity-0"
          >
            <div v-if="showDeactivateDialog" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
              <div class="bg-white rounded-xl shadow-sm border border-slate-200 transition-all duration-200 max-w-md w-full p-6 animate-in fade-in zoom-in-95">
                <h3 class="text-xl font-semibold text-slate-900 mb-4">Deactivate Account</h3>
                <p class="text-slate-600 mb-6">Deactivating your account will stop payment processing. Please provide a reason.</p>
                <div class="space-y-4">
                  <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1.5">Deactivation Reason</label>
                    <textarea
                      v-model="statusChangeForm.reason"
                      rows="3"
                      placeholder="Explain why you want to deactivate your account..."
                      class="w-full px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 placeholder-slate-400 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent resize-none"
                    />
                  </div>
                  <div v-if="statusChangeError" class="bg-white rounded-xl shadow-sm border border-red-200 transition-all duration-200 bg-red-50 p-3">
                    <p class="text-sm text-red-800">{{ statusChangeError }}</p>
                  </div>
                  <div class="flex gap-3">
                    <button
                      @click="closeStatusDialog"
                      :disabled="merchantStore.isLoading"
                      class="px-4 py-2 rounded-lg font-medium transition-all duration-200 bg-slate-100 text-slate-900 hover:bg-slate-200 active:bg-slate-300 disabled:opacity-50 disabled:cursor-not-allowed flex-1"
                    >
                      Cancel
                    </button>
                    <button
                      @click="handleDeactivate"
                      :disabled="merchantStore.isLoading || !statusChangeForm.reason.trim()"
                      class="btn bg-red-600 text-white hover:bg-red-700 flex-1 active:bg-red-800"
                    >
                      {{ merchantStore.isLoading ? 'Processing...' : 'Deactivate' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </AppLayout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useMerchantStore } from '@/stores/merchant'
import { useNotificationStore } from '@/stores/notifications'
import AppLayout from '@/layouts/AppLayout.vue'
import router from '@/router'

const authStore = useAuthStore()
const merchantStore = useMerchantStore()
const notificationStore = useNotificationStore()

const showActivateDialog = ref(false)
const showDeactivateDialog = ref(false)
const statusChangeError = ref('')
const statusChangeForm = ref({
  reason: '',
})

function openActivateDialog() {
  statusChangeForm.value.reason = ''
  statusChangeError.value = ''
  showActivateDialog.value = true
}

function openDeactivateDialog() {
  statusChangeForm.value.reason = ''
  statusChangeError.value = ''
  showDeactivateDialog.value = true
}

function closeStatusDialog() {
  showActivateDialog.value = false
  showDeactivateDialog.value = false
  statusChangeForm.value.reason = ''
  statusChangeError.value = ''
}

async function handleActivate() {
  if (!statusChangeForm.value.reason.trim()) {
    statusChangeError.value = 'Reason is required'
    return
  }

  try {
    await merchantStore.activate(statusChangeForm.value.reason)
    notificationStore.success('Account activated successfully')
    closeStatusDialog()
  } catch (error) {
    statusChangeError.value = 'Failed to activate account'
    notificationStore.error('Failed to activate account')
  }
}

async function handleDeactivate() {
  if (!statusChangeForm.value.reason.trim()) {
    statusChangeError.value = 'Reason is required'
    return
  }

  try {
    await merchantStore.deactivate(statusChangeForm.value.reason)
    notificationStore.success('Account deactivated successfully')
    closeStatusDialog()
  } catch (error) {
    statusChangeError.value = 'Failed to deactivate account'
    notificationStore.error('Failed to deactivate account')
  }
}

onMounted(async () => {
  await authStore.initializeSession()
  if (!authStore.user) {
    router.push('/login')
    return
  }

  try {
    const merchantId = authStore.user.merchantId || authStore.user.id
    await merchantStore.fetchById(merchantId)
  } catch (_error) {
    notificationStore.error('Could not load merchant data')
  }
})
</script>
