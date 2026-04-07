<template>
  <div class="px-4 w-full mt-2">
    <div class="card bg-white shadow-lg rounded-2xl p-6">


      <!-- 🔹 TITLE -->
      <div>
        <h2 class="text-2xl font-bold text-gray-800">
          {{ activeSprint }}
        </h2>
        <p class="text-gray-600">Sprint board overview</p>
      </div>

        <!-- 🔹 SPRINT BAR -->
      <div class="flex items-center gap-2 mb-4">

        <!-- ☰ MENU -->
        <Button
          icon="pi pi-bars"
          class="p-button-text p-button-rounded"
          @click="toggleSprintMenu"
        />

        <!-- ➕ ADD -->
        <Button
          icon="pi pi-plus"
          class="p-button-text p-button-rounded text-green-600"
          @click="showAddSprintDialog = true"
        />

        <!-- TABS -->
        <TabView
          v-model:activeIndex="activeIndex"
          class="flex-1"
        >
          <TabPanel
            v-for="sprint in sprints"
            :key="sprint"
            :header="sprint"
          />
        </TabView>
      </div>

      <!-- 🔹 TABLE -->
      <DataTable
        :value="ticketsBySprint(activeSprint)"
        dataKey="id"
        paginator
        :rows="5"
        class="rounded-xl"
        tableStyle="min-width: 60rem"
      >
        <Column field="ticket" header="Ticket" />
        <Column field="description" header="Description" />
                <!-- COMPONENTS LIST -->
        <Column header="Components">
          <template #body="slotProps">
            <ul class="list-disc ml-4">
              <li
                v-for="component in slotProps.data.details.components"
                :key="component"
              >
                {{ component }}
              </li>
            </ul>
          </template>
        </Column>

        <Column field="stakeholder" header="Stakeholder" />
        <Column field="responsible" header="QA" />



        <Column field="status" header="Delivery Status" />

        <!-- ACTIONS -->
        <Column header="Actions">
          <template #body="slotProps">
            <Button
              icon="pi pi-eye"
              class="p-button-text p-button-info"
              @click="openDetail(slotProps.data)"
            />
          </template>
        </Column>
      </DataTable>

      <!-- 🔹 MENU SPRINTS -->
      <OverlayPanel ref="sprintMenu">
        <ul class="space-y-2 min-w-[220px]">
          <li
            v-for="(sprint, index) in sprints"
            :key="sprint"
            class="flex justify-between items-center p-2 rounded cursor-pointer hover:bg-gray-100"
            @click="selectSprint(index)"
          >
            <span>{{ sprint }}</span>
            <i
              v-if="index === activeIndex"
              class="pi pi-check text-green-500"
            />
          </li>
        </ul>
      </OverlayPanel>

      <!-- 🔹 ADD SPRINT MODAL -->
      <Dialog
        v-model:visible="showAddSprintDialog"
        header="Create new Sprint"
        modal
        :style="{ width: '25rem' }"
      >
        <div class="space-y-4">
          <InputText
            v-model="newSprintName"
            placeholder="SPRINT 2026D"
            class="w-full"
          />

          <div class="flex justify-end gap-2">
            <Button
              label="Cancel"
              class="p-button-text"
              @click="showAddSprintDialog = false"
            />
            <Button
              label="Create"
              class="p-button-success"
              @click="createSprint"
            />
          </div>
        </div>
      </Dialog>

      <!-- 🔹 DETAIL MODAL -->
      <Dialog
        v-model:visible="detailDialog"
        header="Ticket Detail"
        modal
        :style="{ width: '40rem' }"
      >
        <div v-if="selectedTicket" class="space-y-3">
          <p><strong>Ticket:</strong> {{ selectedTicket.ticket }}</p>

          <div>
            <strong>Regions:</strong>
            <ul class="list-disc ml-5">
              <li v-for="r in selectedTicket.details.regions" :key="r">{{ r }}</li>
            </ul>
          </div>



          <div>
            <strong>Scripts:</strong>
            <ul class="list-disc ml-5">
              <li v-for="s in selectedTicket.details.scripts" :key="s">{{ s }}</li>
            </ul>
          </div>

          <div>
            <strong>Notes:</strong>
            <p class="text-gray-600">{{ selectedTicket.details.notes }}</p>
          </div>
        </div>
      </Dialog>

    </div>
  </div>
</template>
<script setup>
import { ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import OverlayPanel from 'primevue/overlaypanel'

const activeIndex = ref(0)
const sprintMenu = ref(null)

const showAddSprintDialog = ref(false)
const newSprintName = ref('')

const detailDialog = ref(false)
const selectedTicket = ref(null)

const sprints = ref([
  'SPRINT 2026A',
  'SPRINT 2026B',
  'SPRINT 2026C'
])

const tickets = ref([
  {
    id: 1,
    sprint: 'SPRINT 2026A',
    ticket: 'QA-101',
    description: 'Validate login flow',
    responsible: 'Camilo',
    stakeholder: 'Joe Charles',
    status: 'Delivered',
    details: {
      regions: ['LATAM'],
      components: ['Auth', 'UI'],
      scripts: ['login.spec.js'],
      notes: 'All OK'
    }
  },
  {
    id: 2,
    sprint: 'SPRINT 2026B',
    ticket: 'QA-201',
    description: 'Orders API tests',
    responsible: 'Maria',
    status: 'In Progress',
    details: {
      regions: ['EU'],
      components: ['Orders API', 'Payments'],
      scripts: ['orders.postman.json'],
      notes: 'Pending'
    }
  }
])

const activeSprint = computed(() => sprints.value[activeIndex.value])

const ticketsBySprint = (sprint) =>
  tickets.value.filter(t => t.sprint === sprint)

// 🔹 Menu
const toggleSprintMenu = (event) => {
  sprintMenu.value.toggle(event)
}

const selectSprint = (index) => {
  activeIndex.value = index
  sprintMenu.value.hide()
}

// 🔹 Create sprint
const createSprint = () => {
  if (!newSprintName.value) return
  sprints.value.push(newSprintName.value.toUpperCase())
  activeIndex.value = sprints.value.length - 1
  newSprintName.value = ''
  showAddSprintDialog.value = false
}

// 🔹 Detail modal
const openDetail = (ticket) => {
  selectedTicket.value = ticket
  detailDialog.value = true
}
</script>
