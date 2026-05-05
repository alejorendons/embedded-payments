<template>
  <div class="bg-white rounded-lg shadow p-6">
    <h2 class="text-2xl font-bold text-gray-900 mb-6">Contact Information</h2>

    <form @submit.prevent="handleSubmit" class="space-y-6">
      <!-- Contact Name -->
      <div>
        <label for="contactName" class="block text-sm font-medium text-gray-700">
          Contact Name
        </label>
        <input
          id="contactName"
          v-model="form.contactName"
          type="text"
          required
          class="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="John Doe"
        />
        <p v-if="errors.contactName" class="mt-1 text-sm text-red-600">
          {{ errors.contactName }}
        </p>
      </div>

      <!-- Contact Email -->
      <div>
        <label for="contactEmail" class="block text-sm font-medium text-gray-700">
          Contact Email
        </label>
        <input
          id="contactEmail"
          v-model="form.contactEmail"
          type="email"
          required
          class="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
          placeholder="john@example.com"
        />
        <p v-if="errors.contactEmail" class="mt-1 text-sm text-red-600">
          {{ errors.contactEmail }}
        </p>
      </div>

      <!-- Success/Error Messages -->
      <div v-if="merchantStore.error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        {{ merchantStore.error }}
      </div>

      <!-- Submit Button -->
      <button
        type="submit"
        :disabled="merchantStore.isLoading"
        class="bg-indigo-600 text-white px-6 py-2 rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
      >
        {{ merchantStore.isLoading ? 'Saving...' : 'Save Changes' }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useMerchantStore } from '@/stores/merchant'
import { useNotificationStore } from '@/stores/notifications'

const merchantStore = useMerchantStore()
const notificationStore = useNotificationStore()

const form = reactive({
  contactName: '',
  contactEmail: '',
})

const errors = ref({
  contactName: '',
  contactEmail: '',
})

onMounted(() => {
  if (merchantStore.current) {
    form.contactName = merchantStore.current.contact_name || ''
    form.contactEmail = merchantStore.current.contact_email || ''
  }
})

function validateEmail(email: string): boolean {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

async function handleSubmit() {
  errors.value = { contactName: '', contactEmail: '' }

  if (!form.contactName) {
    errors.value.contactName = 'Contact name is required'
    return
  }

  if (!form.contactEmail) {
    errors.value.contactEmail = 'Contact email is required'
    return
  }

  if (!validateEmail(form.contactEmail)) {
    errors.value.contactEmail = 'Please enter a valid email'
    return
  }

  try {
    await merchantStore.updateContact(form.contactName, form.contactEmail)
    notificationStore.success('Contact information updated successfully')
  } catch (error) {
    notificationStore.error('Failed to update contact information')
  }
}
</script>

