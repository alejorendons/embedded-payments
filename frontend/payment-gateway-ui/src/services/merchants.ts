import api from './api'
import {
  Merchant,
  UpdateContactRequest,
  RegisterBankAccountRequest,
  ChangeMerchantStatusRequest,
} from '@/types'

export const merchantService = {
  getById: async (id: string): Promise<Merchant> => {
    const response = await api.get<Merchant>(`/api/v1/merchants/${id}`)
    return response.data
  },

  updateContact: async (
    id: string,
    data: UpdateContactRequest
  ): Promise<Merchant> => {
    const response = await api.put<Merchant>(
      `/api/v1/merchants/${id}/contact`,
      data
    )
    return response.data
  },

  registerBankAccount: async (
    id: string,
    data: RegisterBankAccountRequest
  ): Promise<Merchant> => {
    const response = await api.put<Merchant>(
      `/api/v1/merchants/${id}/bank-account`,
      data
    )
    return response.data
  },

  activate: async (
    id: string,
    reason: string
  ): Promise<Merchant> => {
    const response = await api.patch<Merchant>(
      `/api/v1/merchants/${id}/activate`,
      { reason }
    )
    return response.data
  },

  deactivate: async (
    id: string,
    reason: string
  ): Promise<Merchant> => {
    const response = await api.patch<Merchant>(
      `/api/v1/merchants/${id}/deactivate`,
      { reason }
    )
    return response.data
  },
}

