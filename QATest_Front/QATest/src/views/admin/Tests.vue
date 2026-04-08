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
              <span>{{ slotProps.option.label }}</span>
            </div>
          </template>
          <template #value="slotProps">
            <div class="flex items-center gap-2" v-if="slotProps.value">
              <span>{{ apps.find((a) => a.value === slotProps.value)?.label }}</span>
            </div>
            <span v-else>Select Application</span>
          </template>
        </Dropdown>        
      </div>

      <!-- 🔹 Estado vacío -->
      <div
        v-if="!selectedApp"
        class="flex flex-col items-center justify-center py-16 text-center text-slate-500 border-2 border-dashed border-slate-300 rounded-xl">
        <p class="text-lg font-medium">No application selected</p>
        <p class="text-sm text-slate-400 mt-1">Choose an application to view tests.</p>
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
              {{ selectedData.length ? selectedData[0].lastUpdated : '-' }}
            </span>
          </div>
        </div>

        <!-- Tabla dinámica -->
        <DataTable
          v-if="selectedApp"
          :value="selectedData"
          :loading="loading"
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
                  type= "button"
                  icon="pi pi-play"
                  class="p-button-rounded p-button-success p-button-sm"
                  @click="onRun(slotProps.data)"
                />
                <Dialog
                  v-model:visible="showRunModal"
                  modal
                  header="Run Test"
                  :style="{ width: '500px' }"
                >
                  <div class="flex flex-col gap-4">

                    <InputText v-model="form.username" placeholder="Username" />
                    <InputText v-model="form.password" placeholder="Password" type="password" />
                    <InputText v-model="form.product" placeholder="Product" />
                    <InputText v-model="form.amount" placeholder="Amount" />
                    <InputText v-model="form.clerkId" placeholder="Clerk ID" />
                    <InputText v-model="form.phoneNumber" placeholder="Phone Number" />

                    <InputText v-model="form.productFlowType" placeholder="Product Flow Type" />
                    <InputText v-model="form.carrier" placeholder="Carrier" />
                    <InputText v-model="form.category" placeholder="Category" />

                    <div class="flex gap-2 items-center">
                      <Checkbox v-model="form.phoneNumberEnabled" binary />
                      <label>Phone Number Enabled</label>
                    </div>

                    <div class="flex gap-2 items-center">
                      <Checkbox v-model="form.clerkIdEnabled" binary />
                      <label>Clerk ID Enabled</label>
                    </div>

                  </div>

                  <template #footer>
                    <Button label="Cancel" icon="pi pi-times" @click="showRunModal = false" />
                    <Button type="button" label="Run" icon="pi pi-play" class="p-button-success" @click="submitRun" />
                  </template>
                </Dialog>
                <Dialog
                  v-model:visible="showResultModal"
                  modal
                  header="Test Result"
                  :style="{ width: '600px' }"
                >
                  <div v-if="formattedResult" class="flex flex-col gap-4">

                    <!-- 🔹 Status -->
                    <div class="flex items-center gap-2">
                      <span class="text-lg font-bold text-green-600">✅ Success</span>
                    </div>

                    <!-- 🔹 Card -->
                    <div class="bg-gray-50 rounded-xl p-4 shadow">

                      <div class="mb-2 font-semibold text-gray-700">
                        📋 {{ formattedResult.product }}
                      </div>

                      <div class="grid grid-cols-2 gap-3 text-sm">

                        <div>
                          <span class="font-semibold">Trans ID:</span><br />
                          <span class="text-gray-600">{{ formattedResult.transId || '-' }}</span>
                        </div>

                        <div>
                          <span class="font-semibold">Amount:</span><br />
                          <span class="text-gray-600">{{ formattedResult.amount || '-' }}</span>
                        </div>

                        <div>
                          <span class="font-semibold">Date:</span><br />
                          <span class="text-gray-600">{{ formattedResult.dateTime || '-' }}</span>
                        </div>

                        <div>
                          <span class="font-semibold">Processed:</span><br />
                          <span class="text-gray-600">{{ formattedResult.fecha || '-' }}</span>
                        </div>

                      </div>
                    </div>

                    <!-- 🔹 Raw response collapsible -->
                    <details class="text-xs text-gray-500">
                      <summary class="cursor-pointer">Ver respuesta completa</summary>
                      <pre class="bg-gray-100 p-3 rounded mt-2 overflow-auto">
                  {{ formattedResult.raw }}
                      </pre>
                    </details>

                  </div>

                  <template #footer>
                    <Button label="Close" icon="pi pi-times" @click="showResultModal = false" />
                  </template>
                </Dialog>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>
  </div>
</template>



<script setup>
  import { ref, computed, watch, onMounted  } from 'vue'
  import DataTable from 'primevue/datatable'
  import Dialog from 'primevue/dialog'
  import Checkbox from 'primevue/checkbox'
  import Column from 'primevue/column'
  import Button from 'primevue/button'
  import InputText from 'primevue/inputtext'
  import Dropdown from 'primevue/dropdown'
  import componentService from '@/services/componentService' 
  import testService from '@/services/testService'

const mockTests = [
  { id: 1, componentId: 1, name: 'Test 1', result: 'Successful', lastUpdated: '2026-04-01' },
  { id: 2, componentId: 1, name: 'Test 2', result: 'Error', lastUpdated: '2026-04-02' },
  { id: 3, componentId: 2, name: 'Test 3', result: 'Idle', lastUpdated: '2026-04-03' }
]
const apps = ref([])
const selectedApp = ref(null)

const selectedData = ref([])
const loading = ref(false)

const showRunModal = ref(false)
const selectedTest = ref(null)

const showResultModal = ref(false)
const testResponse = ref(null)

const formattedResult = ref(null)
const isRunning = ref(false)

const form = ref({
  regionId: 1,
  componentId: null,
  username: '',
  password: '',
  product: '',
  phoneNumberEnabled: false,
  phoneNumber: '',
  clerkIdEnabled: false,
  clerkId: '',
  productFlowType: '',
  amount: '',
  carrier: '',
  category: ''
})

  const loadApps = async () => {
    const response = await componentService.getAll()

    apps.value = response.data.map(c => ({
      label: c.name,
      value: c.id
    }))
  }

  const loadRegions = async () => {
    if (!selectedApp.value) return

    try {
      const response = await componentService.getRegionsByComponent(selectedApp.value)

      regions.value = response.data.map(r => ({
        label: r.name,
        value: r.id
      }))

    } catch (error) {
      console.error('Error loading regions', error)
    }
  }

  const loadTests = () => {
    loading.value = true

    setTimeout(() => {
      selectedData.value = mockTests.filter(
        t => t.componentId === selectedApp.value
      )
      loading.value = false
    })
  }
  const onRun = (row) => {
    selectedTest.value = row

    form.value = {
      regionId: 1,
      componentId: selectedApp.value,
      username: '',
      password: '',
      product: '',
      phoneNumberEnabled: false,
      phoneNumber: '',
      clerkIdEnabled: false,
      clerkId: '',
      productFlowType: '',
      amount: '',
      carrier: '',
      category: ''
    }

    showRunModal.value = true
  }
  const submitRun = async () => {
    if (isRunning.value) return // 🔥 evita doble ejecución

    isRunning.value = true

    try {
      const payload = { ...form.value }

      selectedTest.value.result = 'Running...'

      const response = await testService.SalesPortal(payload)
      console.log('🚀 submitRun ejecutado')
      testResponse.value = response.data
      formattedResult.value = formatResponse(response.data)

      showRunModal.value = false

      setTimeout(() => {
        showResultModal.value = true
      }, 0)

      selectedTest.value.result = 'Successful'

    } catch (error) {
      selectedTest.value.result = 'Error'
    } finally {
      isRunning.value = false
    }
  }
  
  const formatResponse = (text) => {
    if (!text) return {}

    const lines = text.split('\n').map(l => l.trim()).filter(Boolean)

    const data = {}

    lines.forEach(line => {
      if (line.includes('Trans ID')) {
        data.transId = line.split(':')[1]?.trim()
      }
      if (line.includes('Amount')) {
        data.amount = line.split(':')[1]?.trim()
      }
      if (line.includes('Date and Time')) {
        data.dateTime = line.split(':')[1]?.trim()
      }
      if (line.includes('FECHA')) {
        data.fecha = line.split(':')[1]?.trim()
      }
      if (line.includes('RTR') || line.includes('Test')) {
        data.product = line
      }
    })

    data.raw = text

    return data
  }

  watch(selectedApp, () => {
    selectedData.value = []
    loadTests()
  })
  
  onMounted(() => {
    loadApps()
  })
</script>
