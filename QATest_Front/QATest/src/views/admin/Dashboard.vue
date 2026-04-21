<template>
  <div class="px-4 w-full mt-2">
    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center py-20 text-gray-400">
      <i class="pi pi-spin pi-spinner text-3xl mr-3"></i>
      <span class="text-lg">Loading containers...</span>
    </div>

    <template v-else>
      <!-- Top cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
          <span class="text-gray-500 text-sm">Total Containers</span>
          <span class="text-2xl font-bold mt-1">{{ containers.length }}</span>
        </div>
        <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
          <span class="text-gray-500 text-sm">Running</span>
          <span class="text-2xl font-bold text-emerald-600 mt-1">{{ countByState('running') }}</span>
        </div>
        <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
          <span class="text-gray-500 text-sm">Exited</span>
          <span class="text-2xl font-bold text-red-500 mt-1">{{ countByState('exited') }}</span>
        </div>
        <div class="p-6 bg-white rounded-xl shadow-md flex flex-col items-start">
          <span class="text-gray-500 text-sm">Stopped / Other</span>
          <span class="text-2xl font-bold text-gray-500 mt-1">{{ countOther }}</span>
        </div>
      </div>

      <!-- Charts row 1 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- Doughnut - Status distribution -->
        <div class="p-6 bg-white rounded-xl shadow-md">
          <h3 class="text-lg font-semibold mb-4">Status Distribution</h3>
          <div class="flex items-center justify-center h-72">
            <Chart type="doughnut" :data="doughnutData" :options="doughnutOptions" />
          </div>
        </div>

        <!-- Line - Containers created over time -->
        <div class="p-6 bg-white rounded-xl shadow-md">
          <h3 class="text-lg font-semibold mb-4">Containers Created Over Time</h3>
          <Chart type="line" :data="timelineData" :options="barOptions" class="h-72" />
        </div>
      </div>

      <!-- Charts row 2 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- Stacked bar - Status by category -->
        <div class="p-6 bg-white rounded-xl shadow-md">
          <h3 class="text-lg font-semibold mb-4">Status by Category</h3>
          <Chart type="bar" :data="stackedBarData" :options="stackedOptions" class="h-72" />
        </div>

        <!-- Top images -->
        <div class="p-6 bg-white rounded-xl shadow-md">
          <h3 class="text-lg font-semibold mb-4">Top Images</h3>
          <Chart type="bar" :data="imageBarData" :options="horizontalBarOptions" class="h-72" />
        </div>
      </div>

      <!-- Recent containers table -->
      <div class="p-6 bg-white rounded-xl shadow-md">
        <h3 class="text-lg font-semibold mb-4">Recent Containers</h3>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-2 px-3 text-gray-500 font-medium text-xs uppercase">Name</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium text-xs uppercase">Status</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium text-xs uppercase">Image</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium text-xs uppercase">Ports</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium text-xs uppercase">Created</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in recentContainers" :key="c.name" class="border-b border-gray-100 hover:bg-gray-50">
                <td class="py-2 px-3 font-medium text-gray-700">{{ c.name }}</td>
                <td class="py-2 px-3">
                  <span
                    class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-semibold"
                    :class="badgeClass(c.status)"
                  >
                    <span class="w-1.5 h-1.5 rounded-full" :class="dotClass(c.status)" />
                    {{ stateLabel(c.status) }}
                  </span>
                </td>
                <td class="py-2 px-3 text-gray-500 font-mono text-xs truncate max-w-[200px]" :title="c.image">{{ c.image }}</td>
                <td class="py-2 px-3 text-gray-500 font-mono text-xs">{{ formatPorts(c.ports) }}</td>
                <td class="py-2 px-3 text-gray-400 text-xs">{{ formatDate(c.created) }}</td>
              </tr>
              <tr v-if="!recentContainers.length">
                <td colspan="5" class="py-8 text-center text-gray-400">No containers found</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import Chart from 'primevue/chart'
import { getContainers } from '@/services/containersService'

const containers = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await getContainers()
    containers.value = data.data
  } catch (e) {
    console.error('Error loading containers:', e)
  } finally {
    loading.value = false
  }
})

// -- State helpers --
function stateFromStatus(s = '') {
  s = s.toLowerCase()
  if (s.startsWith('up')) return 'running'
  if (s.includes('exited')) return 'exited'
  if (s.includes('restarting')) return 'restarting'
  if (s.includes('paused')) return 'paused'
  return 'stopped'
}

function stateLabel(s) {
  const state = stateFromStatus(s)
  if (state === 'running') {
    const m = s.match(/^Up\s+(.+)/i)
    return m ? `Running (${m[1].trim()})` : 'Running'
  }
  return state.charAt(0).toUpperCase() + state.slice(1)
}

function badgeClass(s) {
  return {
    running: 'bg-emerald-100 text-emerald-800 border border-emerald-200',
    exited: 'bg-red-50 text-red-700 border border-red-200',
    stopped: 'bg-gray-100 text-gray-600 border border-gray-200',
    restarting: 'bg-yellow-50 text-yellow-700 border border-yellow-200',
    paused: 'bg-amber-50 text-amber-700 border border-amber-200',
  }[stateFromStatus(s)] || 'bg-gray-100 text-gray-600 border border-gray-200'
}

function dotClass(s) {
  return {
    running: 'bg-emerald-500',
    exited: 'bg-red-500',
    stopped: 'bg-gray-400',
    restarting: 'bg-yellow-500',
    paused: 'bg-amber-500',
  }[stateFromStatus(s)] || 'bg-gray-400'
}

function formatPorts(ports) {
  if (!ports) return '—'
  const matches = [...ports.matchAll(/(?:\d+\.\d+\.\d+\.\d+|::):(\d+)->(\d+)/g)]
  if (!matches.length) return '—'
  const seen = new Set()
  const result = []
  for (const m of matches) {
    const pair = `${m[1]}:${m[2]}`
    if (seen.has(pair)) continue
    seen.add(pair)
    result.push(pair)
  }
  return result.join(', ')
}

function formatDate(s) {
  if (!s) return '—'
  const d = new Date(s)
  if (isNaN(d)) return s
  return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function categoryFromName(name) {
  const n = name.toLowerCase()
  if (n.includes('dom')) return 'DOM'
  if (n.includes('mx')) return 'MX'
  if (n.includes('int')) return 'INT'
  if (n.includes('etp')) return 'ETP'
  return 'Other'
}

// -- Counts --
function countByState(state) {
  return containers.value.filter(c => stateFromStatus(c.status) === state).length
}

const countOther = computed(() => {
  return containers.value.filter(c => {
    const s = stateFromStatus(c.status)
    return s !== 'running' && s !== 'exited'
  }).length
})

// -- Recent containers --
const recentContainers = computed(() => {
  return [...containers.value]
    .sort((a, b) => new Date(b.created) - new Date(a.created))
    .slice(0, 10)
})

// -- Chart: Doughnut (status) --
const stateColors = {
  running: '#10B981',
  exited: '#EF4444',
  stopped: '#9CA3AF',
  restarting: '#F59E0B',
  paused: '#F97316',
}

const stateCounts = computed(() => {
  const map = {}
  containers.value.forEach(c => {
    const s = stateFromStatus(c.status)
    map[s] = (map[s] || 0) + 1
  })
  return map
})

const doughnutData = computed(() => {
  const labels = Object.keys(stateCounts.value).map(s => s.charAt(0).toUpperCase() + s.slice(1))
  const data = Object.values(stateCounts.value)
  const colors = Object.keys(stateCounts.value).map(s => stateColors[s] || '#9CA3AF')
  return {
    labels,
    datasets: [{ data, backgroundColor: colors }]
  }
})

const doughnutOptions = ref({
  plugins: { legend: { position: 'bottom', labels: { color: '#374151', padding: 16 } } },
  cutout: '60%',
})

// -- Chart: Bar (by category) --
const timelineData = computed(() => {
  const map = {}
  containers.value.forEach(c => {
    if (!c.created) return
    const d = new Date(c.created)
    if (isNaN(d)) return
    const key = d.toLocaleDateString()
    map[key] = (map[key] || 0) + 1
  })
  const sorted = Object.entries(map).sort((a, b) => new Date(a[0]) - new Date(b[0]))
  return {
    labels: sorted.map(([date]) => date),
    datasets: [{
      label: 'Created',
      data: sorted.map(([, count]) => count),
      borderColor: '#3B82F6',
      backgroundColor: 'rgba(59, 130, 246, 0.1)',
      tension: 0.4,
      fill: true,
    }]
  }
})

// -- Chart: Stacked bar (status by category) --
const statusByCategory = computed(() => {
  const map = {}
  containers.value.forEach(c => {
    const cat = categoryFromName(c.name)
    const s = stateFromStatus(c.status)
    if (!map[cat]) map[cat] = {}
    map[cat][s] = (map[cat][s] || 0) + 1
  })
  return map
})

const stackedBarData = computed(() => {
  const categories = Object.keys(statusByCategory.value)
  const states = [...new Set(containers.value.map(c => stateFromStatus(c.status)))]
  return {
    labels: categories,
    datasets: states.map(s => ({
      label: s.charAt(0).toUpperCase() + s.slice(1),
      data: categories.map(cat => statusByCategory.value[cat]?.[s] || 0),
      backgroundColor: stateColors[s] || '#9CA3AF',
      borderRadius: 4,
    }))
  }
})

// -- Chart: Horizontal bar (top images) --
const imageCounts = computed(() => {
  const map = {}
  containers.value.forEach(c => {
    const img = c.image || 'unknown'
    map[img] = (map[img] || 0) + 1
  })
  return Object.entries(map)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8)
})

const imageBarData = computed(() => ({
  labels: imageCounts.value.map(([img]) => img.length > 30 ? img.slice(0, 30) + '...' : img),
  datasets: [{
    label: 'Containers',
    data: imageCounts.value.map(([, count]) => count),
    backgroundColor: '#8B5CF6',
    borderRadius: 6,
  }]
}))

// -- Chart options --
const barOptions = ref({
  plugins: { legend: { labels: { color: '#374151' } } },
  scales: {
    x: { ticks: { color: '#6B7280' }, grid: { color: '#E5E7EB' } },
    y: { ticks: { color: '#6B7280' }, grid: { color: '#E5E7EB' }, beginAtZero: true },
  }
})

const stackedOptions = ref({
  plugins: { legend: { labels: { color: '#374151' } } },
  scales: {
    x: { stacked: true, ticks: { color: '#6B7280' }, grid: { color: '#E5E7EB' } },
    y: { stacked: true, ticks: { color: '#6B7280' }, grid: { color: '#E5E7EB' }, beginAtZero: true },
  }
})

const horizontalBarOptions = ref({
  indexAxis: 'y',
  plugins: { legend: { display: false } },
  scales: {
    x: { ticks: { color: '#6B7280' }, grid: { color: '#E5E7EB' }, beginAtZero: true },
    y: { ticks: { color: '#6B7280', font: { size: 11 } }, grid: { display: false } },
  }
})
</script>
