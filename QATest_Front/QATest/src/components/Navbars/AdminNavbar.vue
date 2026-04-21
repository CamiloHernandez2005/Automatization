<template>
  <div class="flex h-screen text-sm font-sans">
    <!-- Sidebar -->
   <aside
  :class="[
    'shadow-xl flex flex-col',
    'transition-[width,transform] ease-in-out duration-500',
    isCollapsed ? 'w-16' : 'w-60',
    'bg-slate-800 text-white',
  ]"
  @mouseenter="handleMouseEnter"
  @mouseleave="handleMouseLeave"
>
  <!-- 🔹 Parte superior: título -->
  <div
    :class="[
      'flex items-center border-b border-slate-700 py-4',
      isCollapsed ? 'justify-center' : 'px-3',
    ]"
  >
    <i class="pi pi-box text-xl"></i>
    <span v-if="!isCollapsed" class="ml-3 text-lg font-semibold tracking-wide"> AXEXO </span>
  </div>

  <!-- 🔹 Links Sidebar -->
  <nav class="flex-1 overflow-y-auto mt-2 space-y-1">
    <RouterLink
      to="/admin/dashboard"
      :class="[
        'flex items-center py-2 hover:bg-slate-700 rounded transition-all duration-300 ease-in-out',
        isCollapsed ? 'justify-center' : 'px-3',
      ]"
    >
      <i class="pi pi-home text-lg"></i>
      <span v-if="!isCollapsed" class="ml-3">Dashboard</span>
    </RouterLink>

      <RouterLink
      to="/admin/components"
      :class="[
        'flex items-center py-2 hover:bg-slate-700 rounded transition-all duration-300 ease-in-out',
        isCollapsed ? 'justify-center' : 'px-3',
      ]"
    >
      <i class="pi pi-globe text-lg"></i>
      <span v-if="!isCollapsed" class="ml-3">Ports and connections</span>
    </RouterLink>

        <RouterLink
      to="/admin/daily_notes"
      :class="[
        'flex items-center py-2 hover:bg-slate-700 rounded transition-all duration-300 ease-in-out',
        isCollapsed ? 'justify-center' : 'px-3',
      ]"
    >
      <i class="pi pi-file text-lg"></i>
      <span v-if="!isCollapsed" class="ml-3">Daily Notes</span>
    </RouterLink>



      <RouterLink
      to="/admin/users"
      :class="[
        'flex items-center py-2 hover:bg-slate-700 rounded transition-all duration-300 ease-in-out',
        isCollapsed ? 'justify-center' : 'px-3',
      ]"
    >
      <i class="pi pi-user text-lg"></i>
      <span v-if="!isCollapsed" class="ml-3">Users</span>
    </RouterLink>


    <RouterLink
      to="/admin/tests"
      :class="[
        'flex items-center py-2 hover:bg-slate-700 rounded transition-all duration-300 ease-in-out',
        isCollapsed ? 'justify-center' : 'px-3',
      ]"
    >
      <i class="pi pi-search text-lg"></i>
      <span v-if="!isCollapsed" class="ml-3">Tests</span>
    </RouterLink>


      <RouterLink
      to="/admin/audit"
      :class="[
        'flex items-center py-2 hover:bg-slate-700 rounded transition-all duration-300 ease-in-out',
        isCollapsed ? 'justify-center' : 'px-3',
      ]"
    >
      <i class="pi pi-file text-lg"></i>
      <span v-if="!isCollapsed" class="ml-3">Audit</span>
    </RouterLink>

    <button
      @click="handleLogout"
      :class="[
        'flex items-center w-full py-2 hover:bg-slate-700 rounded transition-all duration-300 ease-in-out',
        isCollapsed ? 'justify-center' : 'px-3',
      ]"
    >
      <i class="pi pi-sign-out text-lg"></i>
      <span v-if="!isCollapsed" class="ml-3">Exit</span>
    </button>
  </nav>

  <!-- 🔹 Footer del Sidebar -->
  <div
    class="border-t border-slate-700 p-3 flex flex-col gap-2"
    :class="[isCollapsed ? 'items-center' : '']"
  >

    <!-- Settings -->
    <button
      class="flex items-center gap-2 py-2 px-3 rounded hover:bg-slate-700 transition-all"
    >
      <i class="pi pi-cog"></i>
      <span v-if="!isCollapsed">Settings</span>
    </button>

    <!-- Ayuda -->
    <button
      class="flex items-center gap-2 py-2 px-3 rounded hover:bg-slate-700 transition-all"
    >
      <i class="pi pi-info-circle"></i>
      <span v-if="!isCollapsed">About</span>
    </button>
  </div>
</aside>


    <!-- Área principal -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Header con Menubar -->
      <div class="shadow flex items-center h-14 bg-slate-800 text-white">
        <Menubar class="flex-1 border-none bg-transparent text-white transition-alltext-sm">
          <template #start>
            <!-- Botón de colapso con mismo estilo que los items del sidebar -->
            <button
              @click="toggleCollapse"
              class="flex items-center px-3 py-2 hover:bg-slate-200 rounded transition-all duration-300 ease-in-out mr-3"
            >
              <i :class="isCollapsed ? 'pi pi-bars' : 'pi pi-times'" class="text-lg"></i>
            </button>

            <!-- Título Header -->
            <span class="text-lg font-semibold tracking-wide">AXEXO</span>
          </template>

          <template #end>
            <div class="flex items-center gap-2">
              <Avatar
                image="https://primefaces.org/cdn/primevue/images/avatar/amyelsner.png"
                shape="circle"
              />
            </div>
          </template>
        </Menubar>
      </div>

      <!-- Contenido dinámico -->
      <main class="flex-1 overflow-y-auto p-4 bg-slate-100">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import Menubar from 'primevue/menubar'
import Avatar from 'primevue/avatar'
import { useRouter } from "vue-router";
import { logout } from '@/services/authService'

const isCollapsed = ref(true)
const manualOpen = ref(false)
const router = useRouter();

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
  manualOpen.value = !isCollapsed.value
}

const handleMouseEnter = () => {
  if (isCollapsed.value && !manualOpen.value) {
    isCollapsed.value = false
  }
}

const handleMouseLeave = () => {
  if (!manualOpen.value) {
    isCollapsed.value = true
  }
}

const handleLogout = async () => {
  await logout();
  router.push("/");
};
</script>
