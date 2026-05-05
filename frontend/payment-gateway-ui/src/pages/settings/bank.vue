<template>
  <div class="bg-white rounded-lg shadow p-6">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Bank Account Information</h2>

    <div v-if="merchantStore.hasBankAccount" class="mb-6 bg-green-50 border border-green-200 rounded p-4">
      <p class="text-green-800">✓ Bank account is registered and encrypted</p>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- IBAN -->
      <div>
        <label for="iban" class="block text-sm font-medium text-gray-700">
          IBAN
        </label>
        <input
          id="iban"
          v-model="form.iban"
          type="text"
          required
          placeholder="ES9121000418450200051332"
          class="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
        />
        <p class="mt-1 text-sm text-gray-500">International Bank Account Number</p>
        <p v-if="errors.iban" class="mt-1 text-sm text-red-600">{{ errors.iban }}</p>
      </div>

      <!-- Routing Number -->
      <div>
        <label for="routingNumber" class="block text-sm font-medium text-gray-700">
          Routing Number (US)
        </label>
        <input
          id="routingNumber"
          v-model="form.routingNumber"
          type="text"
          required
          placeholder="021000021"
          class="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
        />
        <p class="mt-1 text-sm text-gray-500">9-digit routing number</p>
        <p v-if="errors.routingNumber" class="mt-1 text-sm text-red-600">{{ errors.routingNumber }}</p>
      </div>

      <!-- Account Holder Name -->
      <div>
        <label for="accountHolder" class="block text-sm font-medium text-gray-700">
          Account Holder Name
        </label>
        <input
          id="accountHolder"
          v-model="form.accountHolder"
          type="text"
          required
          placeholder="Your Company Name"
          class="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
        />
        <p v-if="errors.accountHolder" class="mt-1 text-sm text-red-600">{{ errors.accountHolder }}</p>
      </div>

      <!-- Security Notice -->
      <div class="bg-blue-50 border border-blue-200 rounded p-4">
        <p class="text-sm text-blue-800">
          🔒 Your bank account information is encrypted using AES-256-GCM and stored securely.
        </p>
      </div>

      <!-- Error Message -->
      <div v-if="merchantStore.error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        {{ merchantStore.error }}
      </div>

      <!-- Submit Button -->
      <button
        type="submit"
        :disabled="merchantStore.isLoading || !merchantStore.hasActiveStatus"
        class="bg-indigo-600 text-white px-6 py-2 rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
      >
        {{ merchantStore.isLoading ? 'Registering...' : 'Register Bank Account' }}
      </button>

      <p v-if="!merchantStore.hasActiveStatus" class="text-sm text-amber-600">
        ⚠️ Your merchant account must be ACTIVE to register bank account details. Please contact support.
      </p>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useMerchantStore } from '@/stores/merchant'
import { useNotificationStore } from '@/stores/notifications'

const merchantStore = useMerchantStore()
const notificationStore = useNotificationStore()

const form = reactive({
  iban: '',
  routingNumber: '',
  accountHolder: '',
})

const errors = ref({
  iban: '',
  routingNumber: '',
  accountHolder: '',
})

function validateIBAN(iban: string): boolean {
  const ibanRegex = /^[A-Z]{2}\d{2}[A-Z0-9]{1,30}$/
  return ibanRegex.test(iban)
}

function validateRoutingNumber(routing: string): boolean {
  return /^\d{9}$/.test(routing)
}

async function handleSubmit() {
  errors.value = { iban: '', routingNumber: '', accountHolder: '' }

  if (!form.iban) {
    errors.value.iban = 'IBAN is required'
    return
  }

  if (!validateIBAN(form.iban)) {
    errors.value.iban = 'Invalid IBAN format'
    return
  }

  if (!form.routingNumber) {
    errors.value.routingNumber = 'Routing number is required'
    return
  }

  if (!validateRoutingNumber(form.routingNumber)) {
    errors.value.routingNumber = 'Routing number must be 9 digits'
    return
  }

  if (!form.accountHolder) {
    errors.value.accountHolder = 'Account holder name is required'
    return
  }

  try {
    await merchantStore.registerBankAccount(
      form.iban,
      form.routingNumber,
      form.accountHolder
    )
    notificationStore.success('Bank account registered successfully')
    form.iban = ''
    form.routingNumber = ''
    form.accountHolder = ''
  } catch (error) {
    notificationStore.error('Failed to register bank account')
  }
}
</script>

