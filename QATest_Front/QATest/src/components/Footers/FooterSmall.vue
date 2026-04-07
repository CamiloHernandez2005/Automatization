<template>
  <footer
    class="pb-6"
    :class="[absolute ? 'absolute bottom-0 w-full bg-slate-800' : 'relative']"
  >
    <div class="container mx-auto px-4">
      <hr class="mb-6 border-slate-600" />
      <div class="flex flex-wrap items-center md:justify-between justify-center">
        <!-- Copy -->
        <div class="w-full md:w-4/12 px-4 text-center md:text-left">
          <p class="text-sm text-slate-400 font-medium">
            © AXEXO {{ date }}
          </p>
        </div>

        <!-- Links -->
        <div class="w-full md:w-8/12 px-4">
          <ul class="flex flex-wrap justify-center md:justify-end gap-4">
            <li>
              <a
                href="https://emida.com/"
                target="_blank"
                class="text-sm text-white font-medium hover:text-slate-300 transition-colors"
              >
                About Us
              </a>
            </li>
            <li>
              <a
                href="https://emida.com/terms-conditions/"
                target="_blank"
                class="text-sm text-white font-medium hover:text-slate-300 transition-colors"
              >
                Terms & Conditions
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  props: {
    absolute: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      date: new Date().getFullYear(),
    }
  },
}
</script>
