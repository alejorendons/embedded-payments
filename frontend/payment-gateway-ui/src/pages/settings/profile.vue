<template>
  <div class="bg-white rounded-lg shadow p-6">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Merchant Profile</h2>

    <div v-if="merchantStore.current" class="space-y-6">
      <!-- Basic Info -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Name</label>
          <p class="text-lg text-gray-900">{{ merchantStore.current.name }}</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
          <p class="text-lg text-gray-900">{{ merchantStore.current.email }}</p>
        </div>
      </div>

      <!-- Status -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">Merchant Status</label>
        <div class="flex items-center gap-4">
          <span
            :class="[
              'px-4 py-2 rounded-full font-semibold',
              merchantStore.current.status === 'ACTIVE'
                ? 'bg-green-100 text-green-800'
                : 'bg-red-100 text-red-800',
            ]"
          >
            {{ merchantStore.current.status }}
          </span>
          <p class="text-sm text-gray-600">
            {{ merchantStore.current.status === 'ACTIVE' ? 'Your merchant account is active' : 'Your merchant account is inactive' }}
          </p>
        </div>
      </div>

      <!-- Contact Info -->
      <div class="border-t pt-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Contact Information</h3>
        <div class="space-y-2">
          <p v-if="merchantStore.current.contact_name">
            <span class="text-sm font-medium text-gray-700">Name:</span>
            <span class="text-gray-900">{{ merchantStore.current.contact_name }}</span>
          </p>
          <p v-if="merchantStore.current.contact_email">
            <span class="text-sm font-medium text-gray-700">Email:</span>
            <span class="text-gray-900">{{ merchantStore.current.contact_email }}</span>
          </p>
          <p v-if="!merchantStore.current.contact_name && !merchantStore.current.contact_email" class="text-gray-600">
            No contact information configured
          </p>
        </div>
      </div>

      <!-- Bank Account -->
      <div class="border-t pt-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">Bank Account</h3>
        <div v-if="merchantStore.hasBankAccount" class="bg-green-50 border border-green-200 rounded p-4">
          <p class="text-green-800">✓ Bank account information is registered and encrypted</p>
        </div>
        <div v-else class="text-gray-600">
          No bank account information registered
        </div>
      </div>

      <!-- Dates -->
      <div class="border-t pt-6 text-sm text-gray-600">
        <p>Created: {{ new Date(merchantStore.current.created_at || merchantStore.current.createdAt!).toLocaleDateString() }}</p>
        <p>Last Updated: {{ new Date(merchantStore.current.updated_at || merchantStore.current.updatedAt!).toLocaleDateString() }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useMerchantStore } from '@/stores/merchant'

const merchantStore = useMerchantStore()
</script>

