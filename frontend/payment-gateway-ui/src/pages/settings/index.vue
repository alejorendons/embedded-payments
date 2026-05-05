<template>
  <AppLayout>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-8">Settings</h1>

      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <!-- Settings Menu -->
        <div class="md:col-span-1">
          <div class="bg-white rounded-lg shadow divide-y">
            <router-link
              to="/settings/contact"
              class="block px-4 py-3 hover:bg-gray-50 transition"
              active-class="bg-indigo-50 border-l-4 border-indigo-600"
            >
              Contact Information
            </router-link>
            <router-link
              to="/settings/bank"
              class="block px-4 py-3 hover:bg-gray-50 transition"
              active-class="bg-indigo-50 border-l-4 border-indigo-600"
            >
              Bank Account
            </router-link>
            <router-link
              to="/settings/profile"
              class="block px-4 py-3 hover:bg-gray-50 transition"
              active-class="bg-indigo-50 border-l-4 border-indigo-600"
            >
              Profile
            </router-link>
          </div>
        </div>

        <!-- Settings Content -->
        <div class="md:col-span-3">
          <router-view />
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from '@/layouts/AppLayout.vue'
</script>

