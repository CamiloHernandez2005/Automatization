<template>
  <div class="px-4 w-full mt-2">
    <!-- 🔹 Toast Notifications -->
    <Toast position="top-right" />

    <div class="card bg-white shadow-lg rounded-2xl p-6">
      <DataTable
        :value="users"
        dataKey="id"
        paginator
        :rows="10"
        class="rounded-xl"
        tableStyle="min-width: 50rem"
        :globalFilterFields="['fullName', 'email', 'role']"
        :filters="filters"
        sortMode="multiple"
        removableSort
      >
        <!-- 🔹 Header -->
        <template #header>
          <div class="flex flex-col md:flex-row justify-between items-center gap-4">
            <div>
              <h2 class="text-2xl font-bold text-gray-800">Users Management</h2>
              <p class="text-gray-600">Manage your system users</p>
            </div>
            <div class="flex items-center gap-3">
              <span class="p-input-icon-left">
                <InputText
                  v-model="globalFilter"
                  placeholder="Search users..."
                  class="w-full rounded-lg border-gray-300 focus:border-blue-500"
                />
              </span>
              <Button
                label="Add user"
                icon="pi pi-plus"
                class="p-button-success shadow-md bg-green-500 hover:bg-green-600 border-green-500"
                @click="openNewUser"
              />
            </div>
          </div>
        </template>

        <!-- 🔹 Loading -->
        <template #empty>
          <div class="flex flex-col items-center justify-center py-8">
            <i class="pi pi-users text-4xl text-gray-400 mb-2"></i>
            <p class="text-gray-500">No users found</p>
          </div>
        </template>

        <template #loading>
          <div class="flex items-center justify-center py-8">
            <i class="pi pi-spin pi-spinner text-2xl text-blue-500"></i>
            <span class="ml-2 text-gray-600">Loading users...</span>
          </div>
        </template>

        <!-- 🔹 Columns -->
        <Column field="fullName" header="Full Name" :sortable="true">
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <i class="pi pi-user text-blue-500"></i>
              <span class="font-medium text-gray-800">{{ data.fullName }}</span>
            </div>
          </template>
        </Column>

        <Column field="email" header="Email" :sortable="true">
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <i class="pi pi-envelope text-green-500"></i>
              <span class="text-gray-700">{{ data.email }}</span>
            </div>
          </template>
        </Column>

        <Column field="role" header="Role" :sortable="true">
          <template #body="{ data }">
            <Badge
              :value="data.role"
              :severity="getRoleSeverity(data.role)"
              :class="getRoleBadgeClass(data.role)"
            />
          </template>
        </Column>

        <Column field="status" header="Status" :sortable="true">
          <template #body="{ data }">
            <div class="flex items-center gap-2">
              <i
                class="pi pi-circle-fill text-xs"
                :class="data.status ? 'text-green-500' : 'text-red-500'"
              ></i>
              <span :class="data.status ? 'text-green-600' : 'text-red-600'">
                {{ data.status ? 'Active' : 'Inactive' }}
              </span>
            </div>
          </template>
        </Column>

        <!-- 🔹 Actions Column -->
        <Column header="Actions" style="width: 12rem">
          <template #body="{ data }">
            <div class="flex gap-2">
              <Button
                icon="pi pi-pencil"
                class="p-button-rounded p-button-info p-button-sm shadow-sm bg-blue-500 hover:bg-blue-600 border-blue-500"
                @click="editUser(data)"
                v-tooltip.top="'Edit user'"
              />
              <Button
                icon="pi pi-trash"
                class="p-button-rounded p-button-danger p-button-sm shadow-sm bg-red-500 hover:bg-red-600 border-red-500"
                @click="confirmDeleteUser(data)"
                v-tooltip.top="'Delete user'"
              />
              <Button
                :icon="data.status ? 'pi pi-lock' : 'pi pi-lock-open'"
                :class="`p-button-rounded p-button-sm shadow-sm ${data.status ? 'bg-yellow-500 hover:bg-yellow-600 border-yellow-500' : 'bg-green-500 hover:bg-green-600 border-green-500'}`"
                @click="toggleUserStatus(data)"
                v-tooltip.top="data.status ? 'Deactivate user' : 'Activate user'"
              />
            </div>
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- 🔹 Modal Create / Edit User -->
    <Dialog
      v-model:visible="userDialog"
      :header="userEditMode ? 'Edit User' : 'New User'"
      modal
      :style="{ width: '500px' }"
      class="p-fluid rounded-lg"
    >
      <div class="space-y-4">
        <div class="flex flex-col gap-2">
          <label for="userFullName" class="font-medium text-gray-700">
            Full Name <span class="text-red-500">*</span>
          </label>
          <InputText
            id="userFullName"
            v-model="userForm.fullName"
            autofocus
            :class="{ 'p-invalid border-red-500': userSubmitted && !userForm.fullName }"
            placeholder="Enter full name"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small v-if="userSubmitted && !userForm.fullName" class="text-red-500 text-sm">
            Full name is required.
          </small>
        </div>

        <div class="flex flex-col gap-2">
          <label for="userEmail" class="font-medium text-gray-700">
            Email <span class="text-red-500">*</span>
          </label>
          <InputText
            id="userEmail"
            v-model="userForm.email"
            :class="{ 'p-invalid border-red-500': userSubmitted && !userForm.email }"
            placeholder="Enter email address"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small v-if="userSubmitted && !userForm.email" class="text-red-500 text-sm">
            Email is required.
          </small>
          <small
            v-if="userSubmitted && userForm.email && !isValidEmail(userForm.email)"
            class="text-red-500 text-sm"
          >
            Please enter a valid email address.
          </small>
        </div>

        <div class="flex flex-col gap-2">
          <label for="userRole" class="font-medium text-gray-700">
            Role <span class="text-red-500">*</span>
          </label>
          <Dropdown
            id="userRole"
            v-model="userForm.role"
            :options="roleOptions"
            optionLabel="label"
            optionValue="value"
            placeholder="Select a role"
            :class="{ 'p-invalid border-red-500': userSubmitted && !userForm.role }"
            class="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small v-if="userSubmitted && !userForm.role" class="text-red-500 text-sm">
            Role is required.
          </small>
          <small v-if="userSubmitted && userForm.role" class="text-green-500 text-sm">
            Selected: {{ userForm.role }}
          </small>
        </div>

        <div v-if="!userEditMode" class="flex flex-col gap-2">
          <label for="userPassword" class="font-medium text-gray-700">
            Password <span class="text-red-500">*</span>
          </label>
          <Password
            id="userPassword"
            v-model="userForm.password"
            :feedback="false"
            toggleMask
            :class="{
              'p-invalid border-red-500': userSubmitted && !userEditMode && !userForm.password,
            }"
            placeholder="Enter password"
            class="w-full"
            inputClass="w-full rounded border-gray-300 focus:border-blue-500 focus:ring-blue-500"
          />
          <small
            v-if="userSubmitted && !userEditMode && !userForm.password"
            class="text-red-500 text-sm"
          >
            Password is required for new users.
          </small>
        </div>

        <div class="flex flex-col gap-2">
          <label for="userStatus" class="font-medium text-gray-700"> Status </label>
          <div class="flex items-center gap-3">
            <Checkbox
              id="userStatus"
              v-model="userForm.status"
              :binary="true"
              :trueValue="true"
              :falseValue="false"
              class="rounded border-gray-300"
            />
            <label for="userStatus" class="text-gray-700">
              Active user ({{ userForm.status ? 'Yes' : 'No' }})
            </label>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <Button
            label="Cancel"
            icon="pi pi-times"
            text
            @click="hideUserDialog"
            class="text-gray-600 hover:text-gray-800"
          />
          <Button
            :label="userEditMode ? 'Update' : 'Create'"
            icon="pi pi-check"
            autofocus
            @click="saveUser"
            class="bg-green-500 hover:bg-green-600 border-green-500 text-white"
          />
        </div>
      </template>
    </Dialog>

    <!-- 🔹 Modal Delete User -->
    <Dialog
      v-model:visible="deleteUserDialog"
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
            <span class="font-bold text-blue-600">{{ userForm.fullName }}</span
            >?
          </p>
          <p class="text-gray-700">
            Email: <span class="font-medium">{{ userForm.email }}</span>
          </p>
          <p class="text-red-500 text-sm mt-2 flex items-center gap-1">
            <i class="pi pi-exclamation-circle"></i>
            This action cannot be undone.
          </p>
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <Button
            label="Cancel"
            icon="pi pi-times"
            text
            @click="deleteUserDialog = false"
            class="text-gray-600 hover:text-gray-800"
          />
          <Button
            label="Delete"
            icon="pi pi-trash"
            @click="deleteUser"
            class="bg-red-500 hover:bg-red-600 border-red-500 text-white"
          />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import Password from 'primevue/password'
import Toast from 'primevue/toast'
import Badge from 'primevue/badge'
import { useToast } from 'primevue/usetoast'

// Importar servicio
import UserService from '@/services/userService'

const toast = useToast()

// 🔹 State
const users = ref([])
const loading = ref(false)
const globalFilter = ref('')

// Dialogs
const userDialog = ref(false)
const deleteUserDialog = ref(false)

// Validation states
const userSubmitted = ref(false)

// Edit modes
const userEditMode = ref(false)

// Form model
const userForm = ref({
  id: null,
  fullName: '',
  email: '',
  role: '',
  password: '',
  status: true,
})

// 🔹 Role options for dropdown
const roleOptions = ref([
  { label: 'Admin', value: 'admin' },
  { label: 'User', value: 'user' },
  { label: 'Manager', value: 'manager' },
  { label: 'Viewer', value: 'viewer' },
])

// 🔹 Filtros
const filters = ref({
  global: { value: null, matchMode: 'contains' },
})

// 🔹 Watch para el filtro global
watch(globalFilter, (newValue) => {
  filters.value.global.value = newValue
})

// 🔹 Get role badge class
const getRoleSeverity = (role) => {
  const severityMap = {
    admin: 'danger',
    manager: 'warning',
    user: 'info',
    viewer: 'success',
  }
  return severityMap[role] || 'info'
}

const getRoleBadgeClass = (role) => {
  const classMap = {
    admin: 'bg-red-100 text-red-800 border-red-200',
    manager: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    user: 'bg-blue-100 text-blue-800 border-blue-200',
    viewer: 'bg-green-100 text-green-800 border-green-200',
  }
  return classMap[role] || 'bg-blue-100 text-blue-800 border-blue-200'
}

// 🔹 Email validation
const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 🔹 Debug function para ver datos
const debugData = (data, label = 'Data') => {
  console.log(`🔍 ${label}:`, {
    id: data.id,
    fullName: data.fullName,
    email: data.email,
    role: data.role,
    status: data.status,
    tipoRole: typeof data.role,
    tipoStatus: typeof data.status
  })
  return data
}

// 🔹 Load data
const loadData = async () => {
  loading.value = true
  try {
    const response = await UserService.getAll()
    console.log('📦 Response from API:', response)

    // Asegurar que los datos tengan la estructura correcta
    users.value = Array.isArray(response.data) ? response.data : response

    // Debug: Mostrar primer usuario
    if (users.value.length > 0) {
      debugData(users.value[0], 'First user from API')
    }

  } catch (error) {
    console.error('❌ Error loading users:', error)
    showError('Failed to load users')
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

// 🔹 USER CRUD
const openNewUser = () => {
  userForm.value = {
    id: null,
    fullName: '',
    email: '',
    role: '',
    password: '',
    status: true,
  }
  userEditMode.value = false
  userSubmitted.value = false
  userDialog.value = true
  console.log('➕ Opening new user form')
}

const editUser = (data) => {
  console.log('✏️ Editing user:', data)

  // Debug del dato recibido
  debugData(data, 'User data to edit')

  // Asegurar que los valores sean correctos
  userForm.value = {
    id: data.id || null,
    fullName: data.fullName || '',
    email: data.email || '',
    role: data.role || '', // Asegurar que role sea string
    password: '', // No mostrar password al editar
    status: data.status !== undefined ? Boolean(data.status) : true, // Convertir a boolean
  }

  console.log('📝 Form after setting:', userForm.value)
  console.log('🔘 Role value in form:', userForm.value.role)
  console.log('🔘 Status value in form:', userForm.value.status, typeof userForm.value.status)

  userEditMode.value = true
  userSubmitted.value = false
  userDialog.value = true
}

const hideUserDialog = () => {
  userDialog.value = false
  userSubmitted.value = false
}

const saveUser = async () => {
  userSubmitted.value = true

  // Validations
  if (!userForm.value.fullName) {
    showError('Full name is required')
    return
  }

  if (!userForm.value.email) {
    showError('Email is required')
    return
  }

  if (!isValidEmail(userForm.value.email)) {
    showError('Please enter a valid email address')
    return
  }

  if (!userForm.value.role) {
    showError('Role is required')
    return
  }

  if (!userEditMode.value && !userForm.value.password) {
    showError('Password is required for new users')
    return
  }

  console.log('💾 Saving user data:', userForm.value)

  try {
    // Preparar datos para enviar
    const userData = {
      fullName: userForm.value.fullName,
      email: userForm.value.email,
      role: userForm.value.role,
      status: Boolean(userForm.value.status) // Asegurar boolean
    }

    // Solo agregar password si existe (para creación) o si se cambió (para edición)
    if (userForm.value.password) {
      userData.password = userForm.value.password
    }

    console.log('📤 Data to send:', userData)

    if (userEditMode.value) {
      await UserService.update(userForm.value.id, userData)
      showSuccess(`User "${userForm.value.fullName}" updated successfully`)
    } else {
      await UserService.create(userData)
      showSuccess(`User "${userForm.value.fullName}" created successfully`)
    }

    userDialog.value = false
    await loadData()
  } catch (error) {
    console.error('❌ Error saving user:', error)
    showError(`Failed to ${userEditMode.value ? 'update' : 'create'} user: ${error.message}`)
  }
}

const confirmDeleteUser = (data) => {
  userForm.value = {
    ...data,
    status: Boolean(data.status)
  }
  deleteUserDialog.value = true
}

const deleteUser = async () => {
  try {
    await UserService.delete(userForm.value.id)
    deleteUserDialog.value = false
    showSuccess(`User "${userForm.value.fullName}" deleted successfully`)
    await loadData()
  } catch (error) {
    console.error('❌ Error deleting user:', error)
    showError('Failed to delete user')
  }
}

const toggleUserStatus = async (data) => {
  try {
    const newStatus = !Boolean(data.status)

    await UserService.updateStatus(data.id, newStatus)

    showSuccess(
      `User "${data.fullName}" ${newStatus ? 'activated' : 'deactivated'} successfully`,
    )
    await loadData()
  } catch (error) {
    console.error('❌ Error toggling user status:', error)
    showError('Failed to update user status')
  }
}

// 🔹 Add loading state
watch(userDialog, (newValue) => {
  if (newValue) {
    loading.value = false
  }
})

onMounted(() => {
  console.log('🚀 Mounting Users component')
  loadData()
})
</script>
