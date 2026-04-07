<template>
  <div class="px-4 w-full mt-2">
    <!-- 🔹 Toast Notifications -->
    <Toast position="top-right" />

    <div class="card bg-white shadow-lg rounded-2xl p-6">
      <DataTable
        :value="components"
        dataKey="id"
        v-model:expandedRows="expandedRows"
        paginator
        :rows="10"
        :loading="loading"
        class="rounded-xl"
        tableStyle="min-width: 50rem"
        :globalFilterFields="['name', 'description']"
        :filters="filters"
        sortMode="multiple"
        removableSort
      >
        <!-- 🔹 Header -->
        <template #header>
          <div class="flex flex-col md:flex-row justify-between items-center">
            <div>
              <h2 class="text-2xl font-bold text-gray-800">Components Management</h2>
              <p class="text-gray-600">Manage your components and their regions</p>
            </div>
            <div class="flex items-center gap-3">
              <span class="p-input-icon-left">
                <InputText
                  v-model="globalFilter"
                  placeholder="Search components..."
                  class="w-full rounded-lg border-gray-300 focus:border-blue-500"
                />
              </span>
              <Button
                label="Add component"
                icon="pi pi-plus"
                class="p-button-success shadow-md bg-green-500 hover:bg-green-600 border-green-500"
                @click="openNewComponent"
              />
            </div>
          </div>
        </template>

        <!-- 🔹 Loading -->
        <template #empty>
          <div class="flex flex-col items-center justify-center py-8">
            <i class="pi pi-inbox text-4xl text-gray-400 mb-2"></i>
            <p class="text-gray-500">No components found</p>
          </div>
        </template>

        <template #loading>
          <div class="flex items-center justify-center py-8">
            <i class="pi pi-spin pi-spinner text-2xl text-blue-500"></i>
            <span class="ml-2 text-gray-600">Loading components...</span>
          </div>
        </template>

        <!-- 🔹 Expander -->
        <Column expander style="width: 3rem" />

        <!-- 🔹 Columns -->
        <Column field="name" header="Name" :sortable="true">
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <i class="pi pi-box text-blue-500"></i>
              <span class="font-medium text-gray-800">{{ data.name }}</span>
            </div>
          </template>
        </Column>

        <Column field="description" header="Description" :sortable="true">
          <template #body="{ data }">
            <span class="text-gray-600">{{ data.description || 'No description' }}</span>
          </template>
        </Column>

        <!-- 🔹 Actions Column (Solo visible cuando NO hay filas expandidas) -->
        <Column v-if="!isAnyRowExpanded" header="Actions" style="width: 10rem">
          <template #body="{ data }">
            <div class="flex gap-2">
              <Button
                icon="pi pi-pencil"
                class="p-button-rounded p-button-info p-button-sm shadow-sm bg-blue-500 hover:bg-blue-600 border-blue-500"
                @click="editComponent(data)"
                v-tooltip.top="'Edit component'"
              />
              <Button
                icon="pi pi-trash"
                class="p-button-rounded p-button-danger p-button-sm shadow-sm bg-red-500 hover:bg-red-600 border-red-500"
                @click="confirmDeleteComponent(data)"
                v-tooltip.top="'Delete component'"
              />
            </div>
          </template>
        </Column>

        <!-- 🔹 Expansion -->
        <template #expansion="{ data }">
          <div class="p-6 bg-gray-50 rounded-lg m-2">
            <div
              class="flex flex-col md:flex-row justify-between items-start md:items-center mb-4 gap-3"
            >
              <div>
                <h5 class="font-semibold text-lg text-gray-800 flex items-center gap-2">
                  <i class="pi pi-globe text-blue-500"></i>
                  Regions for <span class="text-blue-600 font-bold">{{ data.name }}</span>
                </h5>
                <p class="text-gray-600 text-sm">Manage regions associated with this component</p>
              </div>
              <Button
                label="Add region"
                icon="pi pi-plus"
                size="small"
                class="p-button-success bg-green-500 hover:bg-green-600 border-green-500"
                @click="openNewRegion(data.id)"
                v-tooltip.top="'Add new region'"
              />
            </div>

            <DataTable
              :value="getRegionsByComponent(data.id)"
              class="rounded-lg"
              responsiveLayout="scroll"
              sortMode="multiple"
              removableSort
            >
              <template #empty>
                <div class="flex flex-col items-center justify-center py-6">
                  <i class="pi pi-map text-3xl text-gray-300 mb-2"></i>
                  <p class="text-gray-500">No regions assigned to this component</p>
                </div>
              </template>

              <Column field="name" header="Region" :sortable="true">
                <template #body="{ data }">
                  <div class="flex items-center gap-2">
                    <i class="pi pi-map-marker text-red-500"></i>
                    <span class="font-medium text-gray-800">{{ data.name }}</span>
                  </div>
                </template>
              </Column>

              <!-- 🔹 Columna para IP -->
              <Column field="link" header="IP Address" :sortable="true">
                <template #body="{ data }">
                  <div class="flex items-center gap-2">
                    <i class="pi pi-server text-blue-500"></i>
                    <a
                      :href="formatIpLink(data.link)"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="text-blue-600 hover:text-blue-800 hover:underline transition-colors duration-200"
                      v-tooltip.top="'Click to open (HTTP)'"
                    >
                      <code
                        class="bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded text-sm font-mono transition-colors duration-200"
                      >
                        {{ data.link }}
                      </code>
                    </a>
                  </div>
                </template>
              </Column>

              <Column field="port" header="Port" :sortable="true">
                <template #body="{ data }">
                  <Badge
                    :value="data.port"
                    severity="info"
                    class="bg-blue-100 text-blue-800 border-blue-200"
                  />
                </template>
              </Column>

              <!-- 🔹 Columna para DNS -->
              <Column field="dns" header="DNS" :sortable="true">
                <template #body="{ data }">
                  <div class="flex items-center gap-2">
                    <i class="pi pi-globe text-green-500"></i>
                    <a
                      v-if="data.dns && data.dns.trim()"
                      :href="formatDnsLink(data.dns)"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="text-green-600 hover:text-green-800 hover:underline transition-colors duration-200"
                      v-tooltip.top="'Click to open (HTTPS)'"
                    >
                      <code
                        class="bg-gray-100 hover:bg-gray-200 px-2 py-1 rounded text-sm font-mono transition-colors duration-200"
                      >
                        {{ data.dns }}
                      </code>
                    </a>
                    <span v-else class="text-gray-400 italic">N/A</span>
                  </div>
                </template>
              </Column>

              <Column header="Actions" style="width: 8rem">
                <template #body="{ data }">
                  <div class="flex gap-2">
                    <Button
                      icon="pi pi-pencil"
                      class="p-button-rounded p-button-info p-button-sm bg-blue-500 hover:bg-blue-600 border-blue-500"
                      @click="editRegion(data)"
                      v-tooltip.top="'Edit region'"
                    />
                    <Button
                      icon="pi pi-trash"
                      class="p-button-rounded p-button-danger p-button-sm bg-red-500 hover:bg-red-600 border-red-500"
                      @click="confirmDeleteRegion(data)"
                      v-tooltip.top="'Delete region'"
                    />
                  </div>
                </template>
              </Column>
            </DataTable>
          </div>
        </template>
      </DataTable>
    </div>

    <!-- 🔹 Modal Create / Edit Component -->
    <Dialog
      v-model:visible="componentDialog"
      :header="componentEditMode ? 'Edit Component' : 'New Component'"
      modal
      :style="{ width: '450px' }"
      class="p-fluid rounded-lg"
    >
      <div class="space-y-4">
        <div class="flex flex-col gap-2">
          <label for="componentName" class="font-medium text-gray-700">
            Name <span class="text-red-500">*</span>
          </label>
          <InputText
            id="componentName"
            v-model="componentForm.name"
            autofocus
            :class="{ 'p-invalid border-red-500': componentSubmitted && !componentForm.name }"
            placeholder="Enter component name"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small v-if="componentSubmitted && !componentForm.name" class="text-red-500 text-sm">
            Name is required.
          </small>
        </div>

        <div class="flex flex-col gap-2">
          <label for="componentDescription" class="font-medium text-gray-700">Description</label>
          <InputText
            id="componentDescription"
            v-model="componentForm.description"
            placeholder="Enter component description"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <Button
            label="Cancel"
            icon="pi pi-times"
            text
            @click="hideComponentDialog"
            class="text-gray-600 hover:text-gray-800"
          />
          <Button
            :label="componentEditMode ? 'Update' : 'Create'"
            icon="pi pi-check"
            autofocus
            @click="saveComponent"
            class="bg-green-500 hover:bg-green-600 border-green-500 text-white"
          />
        </div>
      </template>
    </Dialog>

    <!-- 🔹 Modal Create / Edit Region -->
    <Dialog
      v-model:visible="regionDialog"
      :header="regionEditMode ? 'Edit Region' : 'New Region'"
      modal
      :style="{ width: '450px' }"
      class="p-fluid rounded-lg"
    >
      <div class="space-y-4">
        <div class="flex flex-col gap-2">
          <label for="regionName" class="font-medium text-gray-700">
            Region Name <span class="text-red-500">*</span>
          </label>
          <InputText
            id="regionName"
            v-model="regionForm.name"
            autofocus
            :class="{ 'p-invalid border-red-500': regionSubmitted && !regionForm.name }"
            placeholder="Enter region name"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small v-if="regionSubmitted && !regionForm.name" class="text-red-500 text-sm">
            Region name is required.
          </small>
        </div>

        <div class="flex flex-col gap-2">
          <label for="regionLink" class="font-medium text-gray-700">
            IP Address / URL <span class="text-red-500">*</span>
          </label>
          <InputText
            id="regionLink"
            v-model="regionForm.link"
            :class="{ 'p-invalid border-red-500': regionSubmitted && !regionForm.link }"
            placeholder="192.168.1.1"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small v-if="regionSubmitted && !regionForm.link" class="text-red-500 text-sm">
            IP address or URL is required.
          </small>
          <small v-else class="text-gray-500 text-sm">
            This will be converted to a clickable link
          </small>
        </div>

        <div class="flex flex-col gap-2">
          <label for="regionPort" class="font-medium text-gray-700">
            Port <span class="text-red-500">*</span>
          </label>
          <InputText
            id="regionPort"
            v-model="regionForm.port"
            :class="{ 'p-invalid border-red-500': regionSubmitted && !regionForm.port }"
            placeholder="8080"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small v-if="regionSubmitted && !regionForm.port" class="text-red-500 text-sm">
            Port is required.
          </small>
        </div>

        <div class="flex flex-col gap-2">
          <label for="regionDns" class="font-medium text-gray-700"> DNS </label>
          <InputText
            id="regionDns"
            v-model="regionForm.dns"
            placeholder="https://example.com"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small class="text-gray-500 text-sm"> This will be converted to a clickable link </small>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <Button
            label="Cancel"
            icon="pi pi-times"
            text
            @click="hideRegionDialog"
            class="text-gray-600 hover:text-gray-800"
          />
          <Button
            :label="regionEditMode ? 'Update' : 'Create'"
            icon="pi pi-check"
            autofocus
            @click="saveRegion"
            class="bg-green-500 hover:bg-green-600 border-green-500 text-white"
          />
        </div>
      </template>
    </Dialog>

    <!-- 🔹 Modal Delete Component -->
    <Dialog
      v-model:visible="deleteComponentDialog"
      :style="{ width: '450px' }"
      header="Confirm Delete"
      :modal="true"
      class="rounded-lg"
    >
      <div class="confirmation-content flex items-start gap-4">
        <i class="pi pi-exclamation-triangle text-red-500 text-3xl mt-1"></i>
        <div>
          <p class="text-gray-700">
            Are you sure you want to delete
            <span class="font-bold text-blue-600">{{ componentForm.name }}</span
            >?
          </p>
          <p class="text-red-500 text-sm mt-2 flex items-center gap-1">
            <i class="pi pi-exclamation-circle"></i>
            This action will also delete all associated regions and cannot be undone.
          </p>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <Button
            label="Cancel"
            icon="pi pi-times"
            text
            @click="deleteComponentDialog = false"
            class="text-gray-600 hover:text-gray-800"
          />
          <Button
            label="Delete"
            icon="pi pi-trash"
            @click="deleteComponent"
            class="bg-red-500 hover:bg-red-600 border-red-500 text-white"
          />
        </div>
      </template>
    </Dialog>

    <!-- 🔹 Modal Delete Region -->
    <Dialog
      v-model:visible="deleteRegionDialog"
      :style="{ width: '450px' }"
      header="Confirm Delete"
      :modal="true"
      class="rounded-lg"
    >
      <div class="confirmation-content flex items-start gap-4">
        <i class="pi pi-exclamation-triangle text-red-500 text-3xl mt-1"></i>
        <div>
          <p class="text-gray-700">
            Are you sure you want to delete
            <span class="font-bold text-blue-600">{{ regionForm.name }}</span
            >?
          </p>
          <p class="text-gray-600 text-sm mt-2">This action cannot be undone.</p>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <Button
            label="Cancel"
            icon="pi pi-times"
            text
            @click="deleteRegionDialog = false"
            class="text-gray-600 hover:text-gray-800"
          />
          <Button
            label="Delete"
            icon="pi pi-trash"
            @click="deleteRegion"
            class="bg-red-500 hover:bg-red-600 border-red-500 text-white"
          />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Toast from 'primevue/toast'
import Badge from 'primevue/badge'
import { useToast } from 'primevue/usetoast'

import ComponentService from '@/services/componentService'
import RegionService from '@/services/regionService'

const toast = useToast()

// 🔹 State
const components = ref([])
const regions = ref([])
const expandedRows = ref({})
const loading = ref(false)
const globalFilter = ref('')

// Dialogs
const componentDialog = ref(false)
const regionDialog = ref(false)
const deleteComponentDialog = ref(false)
const deleteRegionDialog = ref(false)

// Validation states
const componentSubmitted = ref(false)
const regionSubmitted = ref(false)

// Edit modes
const componentEditMode = ref(false)
const regionEditMode = ref(false)

// Form models
const componentForm = ref({
  id: null,
  name: '',
  description: '',
})

const regionForm = ref({
  id: null,
  name: '',
  link: '',
  port: '',
  dns: '',
  parentComponentId: null,
})

// 🔹 Computed para verificar si hay filas expandidas
const isAnyRowExpanded = computed(() => {
  return Object.keys(expandedRows.value).length > 0
})

// 🔹 Filtros simplificados
const filters = ref({
  global: { value: null, matchMode: 'contains' },
})

// 🔹 Watch para el filtro global
watch(globalFilter, (newValue) => {
  filters.value.global.value = newValue
})

// 🔹 Función para formatear IP (siempre HTTP)
const formatIpLink = (ip) => {
  if (!ip || ip.trim() === '') return '#'

  ip = ip.trim()

  // Remover protocolo si ya existe
  ip = ip.replace(/^(http:\/\/|https:\/\/)/, '')

  // Si no tiene http://, agregarlo
  if (!ip.startsWith('http://')) {
    ip = `http://${ip}`
  }

  return ip
}

// 🔹 Función para formatear DNS (siempre HTTPS)
const formatDnsLink = (dns) => {
  if (!dns || dns.trim() === '') return '#'

  dns = dns.trim()

  // Remover protocolo si ya existe
  dns = dns.replace(/^(http:\/\/|https:\/\/)/, '')

  // Si no tiene https://, agregarlo
  if (!dns.startsWith('https://')) {
    dns = `https://${dns}`
  }

  return dns
}

// 🔹 Load data
const loadData = async () => {
  loading.value = true
  try {
    const [componentsRes, regionsRes] = await Promise.all([
      ComponentService.getAll(),
      RegionService.getAll(),
    ])
    components.value = componentsRes.data
    regions.value = regionsRes.data
  } catch (error) {
    showError('Failed to load data')
    console.error('Error loading data:', error)
  } finally {
    loading.value = false
  }
}

// 🔹 Toast helpers
const showSuccess = (message) => {
  toast.add({
    severity: 'success',
    summary: 'Success',
    detail: message,
    life: 3000,
    icon: 'pi pi-check-circle',
  })
}

const showError = (message) => {
  toast.add({
    severity: 'error',
    summary: 'Error',
    detail: message,
    life: 4000,
    icon: 'pi pi-exclamation-circle',
  })
}

// 🔹 Regions by component
const getRegionsByComponent = (componentId) =>
  regions.value.filter((r) => r.parentComponentId === componentId)

// 🔹 COMPONENT CRUD
const openNewComponent = () => {
  componentForm.value = { name: '', description: '' }
  componentEditMode.value = false
  componentSubmitted.value = false
  componentDialog.value = true
}

const editComponent = (data) => {
  componentForm.value = { ...data }
  componentEditMode.value = true
  componentSubmitted.value = false
  componentDialog.value = true
}

const hideComponentDialog = () => {
  componentDialog.value = false
  componentSubmitted.value = false
}

const saveComponent = async () => {
  componentSubmitted.value = true

  if (!componentForm.value.name) {
    return
  }

  try {
    if (componentEditMode.value) {
      await ComponentService.update(componentForm.value.id, componentForm.value)
      showSuccess(`Component "${componentForm.value.name}" updated successfully`)
    } else {
      await ComponentService.create(componentForm.value)
      showSuccess(`Component "${componentForm.value.name}" created successfully`)
    }
    componentDialog.value = false
    await loadData()
  } catch (error) {
    showError(`Failed to ${componentEditMode.value ? 'update' : 'create'} component`)
    console.error('Error saving component:', error)
  }
}

const confirmDeleteComponent = (data) => {
  componentForm.value = { ...data }
  deleteComponentDialog.value = true
}

const deleteComponent = async () => {
  try {
    await ComponentService.delete(componentForm.value.id)
    deleteComponentDialog.value = false
    showSuccess(`Component "${componentForm.value.name}" deleted successfully`)
    await loadData()
  } catch (error) {
    showError('Failed to delete component')
    console.error('Error deleting component:', error)
  }
}

// 🔹 REGION CRUD
const openNewRegion = (componentId) => {
  regionForm.value = {
    name: '',
    link: '',
    port: '',
    dns: '',
    parentComponentId: componentId,
  }
  regionEditMode.value = false
  regionSubmitted.value = false
  regionDialog.value = true
}

const editRegion = (data) => {
  regionForm.value = { ...data }
  regionEditMode.value = true
  regionSubmitted.value = false
  regionDialog.value = true
}

const hideRegionDialog = () => {
  regionDialog.value = false
  regionSubmitted.value = false
}

const saveRegion = async () => {
  regionSubmitted.value = true

  if (!regionForm.value.name || !regionForm.value.link || !regionForm.value.port) {
    return
  }

  try {
    if (regionEditMode.value) {
      await RegionService.update(regionForm.value.id, regionForm.value)
      showSuccess(`Region "${regionForm.value.name}" updated successfully`)
    } else {
      await RegionService.create(regionForm.value)
      showSuccess(`Region "${regionForm.value.name}" created successfully`)
    }
    regionDialog.value = false
    await loadData()
  } catch (error) {
    showError(`Failed to ${regionEditMode.value ? 'update' : 'create'} region`)
    console.error('Error saving region:', error)
  }
}

const confirmDeleteRegion = (data) => {
  regionForm.value = { ...data }
  deleteRegionDialog.value = true
}

const deleteRegion = async () => {
  try {
    await RegionService.delete(regionForm.value.id)
    deleteRegionDialog.value = false
    showSuccess(`Region "${regionForm.value.name}" deleted successfully`)
    await loadData()
  } catch (error) {
    showError('Failed to delete region')
    console.error('Error deleting region:', error)
  }
}

onMounted(loadData)
</script>
