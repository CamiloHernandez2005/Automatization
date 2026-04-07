import PrimeVue from 'primevue/config';
import ToastService from 'primevue/toastservice';
import Aura from '@primevue/themes/aura'; // preset disponible
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import '@/assets/styles/main.css';
import 'primeicons/primeicons.css';
import '@fortawesome/fontawesome-free/css/all.min.css'



import App from './App.vue';
import router from './router';

const app = createApp(App);

app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.my-app-dark' // si usas modo oscuro dinámico
    }
  }
});

app.use(ToastService);
app.use(createPinia());
app.use(router);

app.mount('#app');
