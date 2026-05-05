<template>
  <div class="fixed bottom-4 right-4 space-y-2 z-50">
    <transition-group name="list">
      <div
        v-for="notification in notificationStore.notifications"
        :key="notification.id"
        :class="[
          'px-4 py-3 rounded-lg shadow-lg text-white max-w-md',
          notification.type === 'success' && 'bg-green-600',
          notification.type === 'error' && 'bg-red-600',
          notification.type === 'warning' && 'bg-yellow-600',
          notification.type === 'info' && 'bg-blue-600',
        ]"
      >
        {{ notification.message }}
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { useNotificationStore } from '@/stores/notifications'

const notificationStore = useNotificationStore()
</script>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>

