import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Notification {
  id: string
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  duration?: number
}

export const useNotificationStore = defineStore('notifications', () => {
  const notifications = ref<Notification[]>([])

  function add(message: string, type: Notification['type'] = 'info', duration = 3000) {
    const id = Math.random().toString(36).substring(7)
    const notification: Notification = { id, message, type, duration }
    notifications.value.push(notification)

    if (duration) {
      setTimeout(() => {
        remove(id)
      }, duration)
    }

    return id
  }

  function remove(id: string) {
    const index = notifications.value.findIndex((n) => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  function success(message: string) {
    return add(message, 'success')
  }

  function error(message: string) {
    return add(message, 'error', 5000)
  }

  function warning(message: string) {
    return add(message, 'warning')
  }

  function info(message: string) {
    return add(message, 'info')
  }

  return {
    notifications,
    add,
    remove,
    success,
    error,
    warning,
    info,
  }
})

