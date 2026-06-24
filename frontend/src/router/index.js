import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import TasksView from '../views/TasksView.vue'
import RecommendationsView from '../views/RecommendationsView.vue'

const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: DashboardView
  },
  {
    path: '/tasks',
    name: 'tasks',
    component: TasksView
  },
  {
    path: '/recommendations',
    name: 'recommendations',
    component: RecommendationsView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
