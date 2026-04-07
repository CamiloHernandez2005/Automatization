<template>
  <div class="px-4 w-full mt-2">
    <div class="card bg-white shadow-lg rounded-2xl p-6">
      <!-- 🔹 Selector de aplicación -->
      <div class="flex justify-between items-center mb-6 gap-6">
        <Dropdown
          v-model="selectedApp"
          :options="apps"
          optionLabel="label"
          optionValue="value"
          placeholder="Select Application"
          class="w-64"
        >
          <template #option="slotProps">
            <div class="flex items-center gap-2">
              <i :class="slotProps.option.icon"></i>
              <span>{{ slotProps.option.label }}</span>
            </div>
          </template>
          <template #value="slotProps">
            <div class="flex items-center gap-2" v-if="slotProps.value">
              <i :class="apps.find((a) => a.value === slotProps.value)?.icon"></i>
              <span>{{ apps.find((a) => a.value === slotProps.value)?.label }}</span>
            </div>
            <span v-else>Select Application</span>
          </template>
        </Dropdown>

        <!-- 🔹 Selector de región -->
        <Dropdown
          v-if="selectedApp"
          v-model="selectedRegion"
          :options="regions"
          optionLabel="label"
          optionValue="value"
          placeholder="Select Region"
          class="w-64"
        />
      </div>

      <!-- 🔹 Estado vacío -->
      <div
        v-if="!selectedApp || !selectedRegion"
        class="flex flex-col items-center justify-center py-16 text-center text-slate-500 border-2 border-dashed border-slate-300 rounded-xl"
      >
        <i class="pi pi-database text-5xl mb-4 text-slate-400"></i>
        <p class="text-lg font-medium">No application or region selected</p>
        <p class="text-sm text-slate-400 mt-1">Choose an application and region to view data.</p>
      </div>

      <!-- 🔹 Stats + Tabla -->
      <div v-else>
        <!-- Stats resumen -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
          <div class="bg-slate-50 rounded-xl shadow p-4 flex flex-col">
            <span class="text-sm text-gray-500">Total Test</span>
            <span class="text-2xl font-bold mt-1">{{ selectedData.length }}</span>
          </div>
          <div class="bg-slate-50 rounded-xl shadow p-4 flex flex-col">
            <span class="text-sm text-gray-500">Unit tests</span>
            <span class="text-2xl font-bold mt-1">12</span>
          </div>
          <div class="bg-slate-50 rounded-xl shadow p-4 flex flex-col">
            <span class="text-sm text-gray-500">Last Updated</span>
            <span class="text-2xl font-bold mt-1">
              {{ selectedData[0]?.lastUpdated || '-' }}
            </span>
          </div>
        </div>

        <!-- Tabla dinámica -->
        <DataTable
          v-if="selectedData.length"
          :value="selectedData"
          dataKey="id"
          tableStyle="min-width: 60rem"
          paginator
          :rows="5"
          :globalFilterFields="['name', 'result', 'lastUpdated']"
          class="rounded-xl overflow-hidden shadow-md"
          :filters="filters"
        >
          <!-- Header -->
          <template #header>
            <div class="flex flex-wrap justify-between items-center gap-4 w-full">
              <div class="flex gap-2">
                <Button label="Add Record" icon="pi pi-plus" @click="onAdd" />
                <!-- 🔹 Botón Play Global -->
                <Button
                  label="Run All"
                  icon="pi pi-play"
                  class="p-button-success"
                  @click="onRunAll"
                />
              </div>
              <div class="flex items-center gap-2">
                <span class="p-input-icon-left">
                  <InputText placeholder="Search..." class="w-48" />
                </span>
              </div>
            </div>
          </template>

          <!-- Columnas -->
          <Column field="name" header="Name" sortable />
          <Column field="result" header="Result" sortable>
            <template #body="slotProps">
              <span
                :class="[
                  'px-2 py-1 rounded-full text-xs font-semibold',
                  slotProps.data.result === 'Successful' ? 'bg-green-100 text-green-700' : '',
                  slotProps.data.result === 'Error' ? 'bg-red-100 text-red-700' : '',
                  slotProps.data.result === 'Idle' ? 'bg-yellow-100 text-yellow-700' : '',
                ]"
              >
                {{ slotProps.data.result }}
              </span>
            </template>
          </Column>
          <Column field="lastUpdated" header="Last Updated" sortable />

          <!-- 🔹 Acciones -->
          <Column header="Actions" bodyClass="text-center" style="width: 10rem">
            <template #body="slotProps">
              <div class="flex justify-center gap-2">
                <Button
                  icon="pi pi-pencil"
                  class="p-button-rounded p-button-info p-button-sm"
                  @click="onEdit(slotProps.data)"
                />
                <Button
                  icon="pi pi-trash"
                  class="p-button-rounded p-button-danger p-button-sm"
                  @click="onDelete(slotProps.data)"
                />
                <!-- 🔹 Play individual -->
                <Button
                  icon="pi pi-play"
                  class="p-button-rounded p-button-success p-button-sm"
                  @click="onRun(slotProps.data)"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'

// 🔹 Apps disponibles
const apps = [
  { label: 'App A', value: 'appA', icon: 'pi pi-server' },
  { label: 'App B', value: 'appB', icon: 'pi pi-cloud' },
  { label: 'App C', value: 'appC', icon: 'pi pi-cog' },
]

// 🔹 Regiones
const regions = [
  { label: 'US-East', value: 'us-east' },
  { label: 'US-West', value: 'us-west' },
  { label: 'EU', value: 'eu' },
  { label: 'Asia', value: 'asia' },
]

const selectedApp = ref(null)
const selectedRegion = ref(null)

// 🔹 Data simulada
const data = {
  appA: [
    { id: 1, name: 'Server A1', result: 'Successful', lastUpdated: '2025-08-20' },
    { id: 2, name: 'Server A2', result: 'Idle', lastUpdated: '2025-08-22' },
  ],
  appB: [
    { id: 3, name: 'Server B1', result: 'Successful', lastUpdated: '2025-08-19' },
    { id: 4, name: 'Server B2', result: 'Error', lastUpdated: '2025-08-21' },
  ],
  appC: [{ id: 5, name: 'Server C1', result: 'Successful', lastUpdated: '2025-08-18' }],
}

// 🔹 Datos filtrados
const selectedData = computed(() => {
  return selectedApp.value ? data[selectedApp.value] : []
})

// 🔹 Métodos
const onAdd = () => console.log('Add record for', selectedApp.value, selectedRegion.value)
const onEdit = (row) => console.log('Edit row:', row)
const onDelete = (row) => console.log('Delete row:', row)
const onRun = (row) =>
  console.log('Run row:', row, 'App:', selectedApp.value, 'Region:', selectedRegion.value)
const onRunAll = () =>
  console.log('Run ALL for', selectedApp.value, 'Region:', selectedRegion.value, selectedData.value)
</script>
