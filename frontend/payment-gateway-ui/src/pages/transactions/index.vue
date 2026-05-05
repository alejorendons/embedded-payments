<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Transactions</h1>

      <div class="bg-white rounded-lg shadow">
        <div class="px-6 py-4 border-b">
          <p class="text-sm text-gray-600">View your recent transactions and payment history</p>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">ID</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Customer</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Amount</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Status</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Date</th>
                <th class="px-6 py-3 text-left text-sm font-semibold text-gray-900">Action</th>
              </tr>
            </thead>
            <tbody class="divide-y">
              <tr v-if="!transactions.length" class="hover:bg-gray-50">
                <td colspan="6" class="px-6 py-4 text-center text-gray-600">
                  No transactions found
                </td>
              </tr>
              <tr v-for="transaction in transactions" :key="transaction.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 text-sm text-gray-900">
                  {{ transaction.id.substring(0, 8) }}...
                </td>
                <td class="px-6 py-4 text-sm text-gray-900">
                  {{ transaction.customerEmail }}
                </td>
                <td class="px-6 py-4 text-sm font-semibold text-gray-900">
                  {{ transaction.currency }} {{ transaction.amount.toFixed(2) }}
                </td>
                <td class="px-6 py-4 text-sm">
                  <span
                    :class="[
                      'px-3 py-1 rounded-full text-xs font-semibold',
                      transaction.status === 'COMPLETED'
                        ? 'bg-green-100 text-green-800'
                        : transaction.status === 'PENDING'
                        ? 'bg-yellow-100 text-yellow-800'
                        : 'bg-red-100 text-red-800',
                    ]"
                  >
                    {{ transaction.status }}
                  </span>
                </td>
                <td class="px-6 py-4 text-sm text-gray-600">
                  {{ new Date(transaction.createdAt).toLocaleDateString() }}
                </td>
                <td class="px-6 py-4 text-sm">
                  <router-link
                    :to="`/transactions/${transaction.id}`"
                    class="text-indigo-600 hover:text-indigo-900"
                  >
                    View
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '@/layouts/AppLayout.vue'
import { transactionService } from '@/services/transactions'
import { Transaction } from '@/types'

const transactions = ref<Transaction[]>([])

onMounted(async () => {
  try {
    const response = await transactionService.list()
    transactions.value = response.items
  } catch (error) {
    console.error('Failed to fetch transactions:', error)
  }
})
</script>

