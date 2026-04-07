<template>
  <nav class="fixed top-0 z-50 w-full flex flex-wrap items-center justify-between px-4 py-3 bg-slate-800">
    <div class="container mx-auto flex flex-wrap items-center justify-between">
      <!-- Logo -->
      <div class="flex justify-between items-center w-full lg:w-auto">
        <RouterLink
          class="text-white text-sm font-bold leading-relaxed inline-block mr-4 py-2 whitespace-nowrap uppercase"
          to="/">
          AXEXO
        </RouterLink>
        <!-- Mobile toggle -->
        <button
          class="lg:hidden text-white text-xl p-2 rounded-md hover:bg-slate-700"
          type="button"
          @click="setNavbarOpen"
        >
          <i class="fas fa-bars"></i>
        </button>
      </div>

      <!-- Links -->
      <div
        class="lg:flex flex-grow items-center lg:bg-transparent"
        :class="[navbarOpen ? 'block bg-white rounded shadow-lg mt-2 p-2' : 'hidden']"
      >
        <ul class="flex flex-col lg:flex-row list-none lg:ml-auto">
          <!-- Facebook -->
          <li class="flex items-center">
            <a
              class="text-slate-700 lg:text-white lg:hover:text-slate-300 px-3 py-2 flex items-center text-xs uppercase font-bold"
              href="https://www.facebook.com/emidatechnologies/"
              target="_blank"
            >
              <i class="fab fa-facebook text-lg leading-lg lg:text-slate-300 text-slate-500"></i>
              <span class="lg:hidden ml-2">Share</span>
            </a>
          </li>
          <!-- X / Twitter -->
          <li class="flex items-center">
            <a
              class="text-slate-700 lg:text-white lg:hover:text-slate-300 px-3 py-2 flex items-center text-xs uppercase font-bold"
              href="https://x.com/emidatech"
              target="_blank"
            >
              <i class="fab fa-x-twitter text-lg leading-lg lg:text-slate-300 text-slate-500"></i>
              <span class="lg:hidden ml-2">Tweet</span>
            </a>
          </li>
          <!-- LinkedIn -->
          <li class="flex items-center">
            <a
              class="text-slate-700 lg:text-white lg:hover:text-slate-300 px-3 py-2 flex items-center text-xs uppercase font-bold"
              href="https://www.linkedin.com/company/emidatechnologies"
              target="_blank"
            >
              <i class="fab fa-linkedin text-lg leading-lg lg:text-slate-300 text-slate-500"></i>
              <span class="lg:hidden ml-2">Star</span>
            </a>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  data() {
    return {
      navbarOpen: false,
    }
  },
  methods: {
    setNavbarOpen() {
      this.navbarOpen = !this.navbarOpen
    },
  },
}
</script>
