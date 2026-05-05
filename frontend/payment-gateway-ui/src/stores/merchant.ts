import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { Merchant } from '@/types'
import { merchantService } from '@/services/merchants'

export const useMerchantStore = defineStore('merchant', () => {
  const current = ref<Merchant | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const hasActiveStatus = computed(() => current.value?.status === 'ACTIVE')
  const hasBankAccount = computed(
    () => !!current.value?.bank_account_data && current.value.bank_account_data !== '[RESTRICTED]'
  )

  async function fetchById(id: string) {
    isLoading.value = true
    error.value = null
    try {
      current.value = await merchantService.getById(id)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to fetch merchant'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function updateContact(contactName: string, contactEmail: string) {
    isLoading.value = true
    error.value = null
    try {
      current.value = await merchantService.updateContact(current.value!.id, {
        contact_name: contactName,
        contact_email: contactEmail,
      })
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to update contact'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function registerBankAccount(
    iban: string,
    routingNumber: string,
    accountHolder: string
  ) {
    isLoading.value = true
    error.value = null
    try {
      current.value = await merchantService.registerBankAccount(
        current.value!.id,
        {
          iban,
          routing_number: routingNumber,
          account_holder_name: accountHolder,
        }
      )
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to register bank account'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function activate(reason: string) {
    isLoading.value = true
    error.value = null
    try {
      current.value = await merchantService.activate(current.value!.id, reason)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to activate merchant'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function deactivate(reason: string) {
    isLoading.value = true
    error.value = null
    try {
      current.value = await merchantService.deactivate(current.value!.id, reason)
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to deactivate merchant'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  return {
    current,
    isLoading,
    error,
    hasActiveStatus,
    hasBankAccount,
    fetchById,
    updateContact,
    registerBankAccount,
    activate,
    deactivate,
  }
})

