import { createRouter, createWebHistory } from 'vue-router'
import { verifySession } from '@/services/authService'
import MainLayout from '@/components/Navbars/AdminNavbar.vue'
import Login from '@/views/auth/Login.vue'
import Dashboard from '@/views/admin/Dashboard.vue'
import Components from '@/views/admin/Components.vue'
import Tests from '@/views/admin/Tests.vue'
import Users from '@/views/admin/Users.vue'
import Audit from '@/views/admin/Audit.vue'

const routes = [
  { path: '/', component: Login },

  {
    path: '/admin',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'tests', component: Tests },
      { path: 'components', component: Components },
      { path: 'users', component: Users },
      { path: 'audit', component: Audit }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {

  if (!to.matched.some(r => r.meta.requiresAuth)) {
    return next()
  }

  try {
    await verifySession()
    next()
  } catch (error) {
    next('/')
  }
})

export default router
