<template>
   <div class="px-4 w-full mt-2">
    <!-- 🔹 Top cards -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
      <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
        <span class="text-gray-500">Servers</span>
        <span class="text-2xl font-bold">24</span>
      </div>
      <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
        <span class="text-gray-500">Regions</span>
        <span class="text-2xl font-bold">12</span>
      </div>
      <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
        <span class="text-gray-500">Active Users</span>
        <span class="text-2xl font-bold">1,248</span>
      </div>
      <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
        <span class="text-gray-500">Errors</span>
        <span class="text-2xl font-bold text-red-500">7</span>
      </div>
    </div>

    <!-- 🔹 Charts section -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Line Chart -->
      <div class="p-6 bg-white rounded-xl shadow-md">
        <h3 class="text-lg font-semibold mb-4">Server Requests</h3>
        <Chart type="line" :data="lineData" :options="chartOptions" class="h-72" />
      </div>

      <!-- Bar Chart -->
      <div class="p-6 bg-white rounded-xl shadow-md">
        <h3 class="text-lg font-semibold mb-4">Errors by Region</h3>
        <Chart type="bar" :data="barData" :options="chartOptions" class="h-72" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Chart from 'primevue/chart'

const lineData = ref({
  labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
  datasets: [
    {
      label: 'Requests',
      data: [120, 150, 180, 100, 200, 170],
      borderColor: '#3B82F6',
      tension: 0.4,
      fill: false
    }
  ]
})

const barData = ref({
  labels: ['US-East', 'US-West', 'EU-Central', 'Asia'],
  datasets: [
    {
      label: 'Errors',
      data: [3, 1, 2, 1],
      backgroundColor: ['#EF4444', '#F59E0B', '#10B981', '#3B82F6']
    }
  ]
})

const chartOptions = ref({
  plugins: {
    legend: {
      labels: {
        color: '#374151'
      }
    }
  },
  scales: {
    x: {
      ticks: { color: '#6B7280' },
      grid: { color: '#E5E7EB' }
    },
    y: {
      ticks: { color: '#6B7280' },
      grid: { color: '#E5E7EB' }
    }
  }
})
</script>
