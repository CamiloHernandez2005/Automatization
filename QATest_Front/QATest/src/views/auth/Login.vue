<template>
  <div class="relative min-h-screen flex flex-col bg-slate-800">
    <!-- 🔹 Navbar (forzamos z menor que el overlay) -->
    <div class="relative z-10">
      <AuthNavbar />
    </div>

    <!-- 🔹 Fondo FIXED encima de todo (z alto). pointer-events-none deja pasar clicks -->
    <div
      class="fixed inset-0 z-50 pointer-events-none bg-no-repeat bg-cover"
      :style="{ backgroundImage: `url(${registerBg2})` }"
      aria-hidden="true"
    ></div>

    <!-- 🔹 Contenido principal (queda visualmente debajo del fondo) -->
    <main class="relative z-0 flex-grow flex items-center justify-center">
      <div class="container mx-auto px-4">
        <div class="w-full lg:w-4/12 px-4 mx-auto">
          <div
            class="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-slate-200 border-0"
          >
            <!-- Header -->
            <div class="rounded-t mb-0 px-6 py-6">
              <div class="text-center mb-3">
                <h6 class="text-slate-600 text-sm font-bold">Sign in</h6>
              </div>
              <hr class="mt-4 border-b border-slate-300" />
            </div>

            <!-- Formulario -->
            <div class="flex-auto px-4 lg:px-10 py-10 pt-0">
              <form @submit.prevent="handleLogin">
                <!-- Email -->
                <div class="relative w-full mb-3">
                  <label class="block uppercase text-slate-600 text-xs font-bold mb-2" for="email">
                    Email
                  </label>
                  <input
                    v-model="email"
                    id="email"
                    type="email"
                    class="px-3 py-3 placeholder-slate-400 text-slate-700 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                    placeholder="Email"
                    required
                  />
                </div>

                <!-- Password -->
                <div class="relative w-full mb-3">
                  <label
                    class="block uppercase text-slate-600 text-xs font-bold mb-2"
                    for="password"
                  >
                    Password
                  </label>
                  <input
                    v-model="password"
                    id="password"
                    type="password"
                    class="px-3 py-3 placeholder-slate-400 text-slate-700 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150"
                    placeholder="Password"
                    required
                  />
                </div>

                <!-- Remember me -->
                <div class="mb-4">
                  <label class="inline-flex items-center cursor-pointer">
                    <input
                      id="customCheckLogin"
                      type="checkbox"
                      class="w-5 h-5 text-slate-700 rounded border-slate-300 focus:ring-slate-500"
                    />
                    <span class="ml-2 text-sm font-semibold text-slate-600"> Remember me </span>
                  </label>
                </div>

                <p v-if="error" class="error">{{ error }}</p>

                <!-- Sign In -->
                <div class="text-center">
                  <button
                    class="bg-slate-800 text-white text-sm font-bold uppercase px-6 py-3 rounded shadow hover:shadow-lg w-full ease-linear transition-all duration-150"
                    type="submit"
                  >
                    Sign In
                  </button>
                </div>

                <!-- Google -->
                <div class="text-center mt-3">
                  <button
                    class="bg-white text-slate-700 text-sm font-bold uppercase px-6 py-3 rounded shadow hover:shadow-lg w-full ease-linear transition-all duration-150 inline-flex items-center justify-center"
                    type="button"
                  >
                    <img alt="google" class="w-5 h-5 mr-2" :src="google" />
                    Sign in with Google
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- Links adicionales -->
          <div class="flex flex-wrap mt-6 relative">
            <div class="w-1/2"></div>
            <div class="w-1/2 text-right">
              <a href="javascript:void(0)" class="text-slate-200">
                <small>Forgot password?</small>
              </a>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <div class="relative z-0">
      <AuthFootbar />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AuthNavbar from '@/components/Navbars/AuthNavbar.vue'
import AuthFootbar from '@/components/Footers/FooterSmall.vue'
import registerBg2 from '@/assets/img/register_bg_2.png'
import google from '@/assets/img/google.svg'
import { useRouter } from 'vue-router'

import { login } from '@/services/authService'

const email = ref('')
const password = ref('')
const error = ref(null)
const router = useRouter()

const handleLogin = async () => {
  error.value = null

  try {
    const res = await login(email.value, password.value)
    router.push('/admin/dashboard')
  } catch (err) {
    error.value = 'Invalid credentials :('
  }
}
</script>

<style scoped>
.error {
  color: red;
}
</style>
