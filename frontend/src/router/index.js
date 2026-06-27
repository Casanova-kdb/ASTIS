import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../services/authService'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import TasksView from '../views/TasksView.vue'
import RecommendationsView from '../views/RecommendationsView.vue'
import RegisterView from '../views/RegisterView.vue'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: {
      layout: 'auth',
      guestOnly: true
    }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: {
      layout: 'auth',
      guestOnly: true
    }
  },
  {
    path: '/',
    name: 'dashboard',
    component: DashboardView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/tasks',
    name: 'tasks',
    component: TasksView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/recommendations',
    name: 'recommendations',
    component: RecommendationsView,
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const loggedIn = isAuthenticated()

  if (to.meta.requiresAuth && !loggedIn) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (to.meta.guestOnly && loggedIn) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
