<template>
  <AppLayout>
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Back Button -->
      <div class="mb-8">
        <router-link to="/transactions" class="text-indigo-600 hover:text-indigo-900 flex items-center gap-2">
          ← Back to Transactions
        </router-link>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="flex justify-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>

      <!-- Error State -->
      <div
        v-else-if="error"
        class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-6"
      >
        {{ error }}
      </div>

      <!-- Transaction Details -->
      <div v-else-if="transaction" class="space-y-6">
        <!-- Header -->
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h1 class="text-3xl font-bold text-gray-900">Transaction Details</h1>
              <p class="text-gray-600 mt-2">ID: {{ transaction.id }}</p>
            </div>
            <div
              :class="[
                'px-4 py-2 rounded-full font-semibold text-lg',
                transaction.status === 'COMPLETED'
                  ? 'bg-green-100 text-green-800'
                  : transaction.status === 'PENDING'
                  ? 'bg-yellow-100 text-yellow-800'
                  : transaction.status === 'REFUNDED'
                  ? 'bg-blue-100 text-blue-800'
                  : 'bg-red-100 text-red-800',
              ]"
            >
              {{ transaction.status }}
            </div>
          </div>

          <!-- Main Info -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div>
              <p class="text-sm text-gray-600 mb-1">Amount</p>
              <p class="text-2xl font-bold text-gray-900">
                {{ transaction.currency }} {{ transaction.amount.toFixed(2) }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-600 mb-1">Date</p>
              <p class="text-lg font-semibold text-gray-900">
                {{ new Date(transaction.createdAt).toLocaleDateString() }}
              </p>
              <p class="text-sm text-gray-600">
                {{ new Date(transaction.createdAt).toLocaleTimeString() }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-600 mb-1">Last Updated</p>
              <p class="text-lg font-semibold text-gray-900">
                {{ new Date(transaction.updatedAt).toLocaleDateString() }}
              </p>
            </div>
          </div>
        </div>

        <!-- Customer Information -->
        <div class="bg-white rounded-lg shadow p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-4">Customer Information</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <p class="text-sm text-gray-600 mb-1">Email</p>
              <p class="text-lg text-gray-900">{{ transaction.customerEmail }}</p>
            </div>
            <div v-if="transaction.customerName">
              <p class="text-sm text-gray-600 mb-1">Name</p>
              <p class="text-lg text-gray-900">{{ transaction.customerName }}</p>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div v-if="transaction.status === 'COMPLETED'" class="bg-white rounded-lg shadow p-6">
          <h2 class="text-xl font-semibold text-gray-900 mb-4">Actions</h2>
          <button
            @click="openRefundDialog"
            class="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition"
          >
            Process Refund
          </button>
        </div>

        <!-- Refund Dialog -->
        <div v-if="showRefundDialog" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div class="bg-white rounded-lg p-8 max-w-md w-full">
            <h3 class="text-xl font-semibold text-gray-900 mb-4">Process Refund</h3>

            <div class="space-y-4">
              <!-- Refund Amount -->
              <div>
                <label for="refundAmount" class="block text-sm font-medium text-gray-700 mb-2">
                  Refund Amount
                </label>
                <div class="flex items-center">
                  <span class="text-lg text-gray-600 mr-2">{{ transaction.currency }}</span>
                  <input
                    id="refundAmount"
                    v-model.number="refundForm.amount"
                    type="number"
                    step="0.01"
                    :max="transaction.amount"
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                    placeholder="0.00"
                  />
                </div>
                <p class="mt-2 text-sm text-gray-600">Max: {{ transaction.currency }} {{ transaction.amount.toFixed(2) }}</p>
                <p v-if="refundErrors.amount" class="mt-1 text-sm text-red-600">{{ refundErrors.amount }}</p>
              </div>

              <!-- Refund Reason -->
              <div>
                <label for="refundReason" class="block text-sm font-medium text-gray-700 mb-2">
                  Reason
                </label>
                <textarea
                  id="refundReason"
                  v-model="refundForm.reason"
                  rows="3"
                  class="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="Reason for refund..."
                />
                <p v-if="refundErrors.reason" class="mt-1 text-sm text-red-600">{{ refundErrors.reason }}</p>
              </div>

              <!-- Error Message -->
              <div v-if="refundError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                {{ refundError }}
              </div>

              <!-- Buttons -->
              <div class="flex gap-4 pt-4">
                <button
                  @click="closeRefundDialog"
                  :disabled="isProcessingRefund"
                  class="flex-1 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 transition"
                >
                  Cancel
                </button>
                <button
                  @click="handleRefund"
                  :disabled="isProcessingRefund"
                  class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 transition"
                >
                  {{ isProcessingRefund ? 'Processing...' : 'Confirm Refund' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import { transactionService } from '@/services/transactions'
import { useNotificationStore } from '@/stores/notifications'
import type { Transaction } from '@/types'

const route = useRoute()
const notificationStore = useNotificationStore()

const transaction = ref<Transaction | null>(null)
const isLoading = ref(false)
const error = ref('')

const showRefundDialog = ref(false)
const isProcessingRefund = ref(false)
const refundError = ref<string | null>(null)

const refundForm = ref({
  amount: 0,
  reason: '',
})

const refundErrors = ref({
  amount: '',
  reason: '',
})

async function fetchTransaction() {
  isLoading.value = true
  error.value = ''
  try {
    transaction.value = await transactionService.getById(route.params.id as string)
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to load transaction'
  } finally {
    isLoading.value = false
  }
}

function openRefundDialog() {
  if (!transaction.value) return
  refundForm.value.amount = transaction.value.amount
  refundForm.value.reason = ''
  refundErrors.value = { amount: '', reason: '' }
  refundError.value = null
  showRefundDialog.value = true
}

function closeRefundDialog() {
  showRefundDialog.value = false
  refundForm.value = { amount: 0, reason: '' }
  refundErrors.value = { amount: '', reason: '' }
  refundError.value = null
}

function validateRefundForm(): boolean {
  refundErrors.value = { amount: '', reason: '' }

  if (!refundForm.value.amount || refundForm.value.amount <= 0) {
    refundErrors.value.amount = 'Amount must be greater than 0'
  }

  if (transaction.value && refundForm.value.amount > transaction.value.amount) {
    refundErrors.value.amount = `Amount cannot exceed transaction amount (${transaction.value.currency} ${transaction.value.amount.toFixed(2)})`
  }

  if (!refundForm.value.reason.trim()) {
    refundErrors.value.reason = 'Reason is required'
  }

  return !refundErrors.value.amount && !refundErrors.value.reason
}

async function handleRefund() {
  if (!validateRefundForm() || !transaction.value) return

  isProcessingRefund.value = true
  refundError.value = null

  try {
    await transactionService.refund(
      transaction.value.id,
      refundForm.value.amount,
      refundForm.value.reason
    )
    notificationStore.success('Refund processed successfully')
    closeRefundDialog()
    await fetchTransaction()
  } catch (err: any) {
    refundError.value = err.response?.data?.message || 'Failed to process refund'
    notificationStore.error('Failed to process refund')
  } finally {
    isProcessingRefund.value = false
  }
}

onMounted(() => {
  fetchTransaction()
})
</script>

