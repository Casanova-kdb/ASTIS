<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Planning Prototype</p>
        <h2>Study Dashboard</h2>
        <p class="page-copy">
          Review your study workload, completion progress, and current recommended focus.
        </p>
      </div>

      <button type="button" class="secondary-button" :disabled="isLoading" @click="loadDashboard">
        Refresh
      </button>
    </div>

    <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

    <div v-if="isLoading" class="empty-state">Loading dashboard...</div>

    <template v-else>
      <section class="metric-grid">
        <article class="metric-card">
          <span>Total tasks</span>
          <strong>{{ summary.totalTaskCount }}</strong>
        </article>

        <article class="metric-card">
          <span>Completed</span>
          <strong>{{ summary.completedTaskCount }}</strong>
        </article>

        <article class="metric-card">
          <span>Pending</span>
          <strong>{{ pendingTaskCount }}</strong>
        </article>

        <article class="metric-card">
          <span>Overdue</span>
          <strong>{{ summary.overdueTaskCount }}</strong>
        </article>
      </section>

      <section class="dashboard-grid">
        <article class="panel progress-panel">
          <div class="panel-heading">
            <div>
              <h3>Completion rate</h3>
              <p>Current task completion progress based on your stored tasks.</p>
            </div>
          </div>

          <div class="progress-value">{{ formattedCompletionRate }}</div>
          <div class="progress-track" aria-hidden="true">
            <div class="progress-fill" :style="{ width: progressWidth }"></div>
          </div>
        </article>

        <article class="panel focus-panel">
          <div class="panel-heading">
            <div>
              <h3>Today focus</h3>
              <p>The highest ranked task from the recommendation engine.</p>
            </div>
          </div>

          <div v-if="topRecommendation" class="focus-card">
            <div class="task-item-main">
              <div>
                <span class="rank-badge">#{{ topRecommendation.rankPosition }}</span>
                <h4>{{ topRecommendation.title }}</h4>
                <p>{{ topRecommendation.reason }}</p>
              </div>
              <span :class="['status-pill', riskTone(topRecommendation.delayRisk)]">
                {{ formatLabel(topRecommendation.delayRisk) }} risk
              </span>
            </div>

            <div class="task-meta">
              <span>{{ topRecommendation.taskType }}</span>
              <span :class="['priority-pill', priorityTone(topRecommendation.priority)]">
                {{ formatLabel(topRecommendation.priority) }}
              </span>
              <span>Score {{ formatScore(topRecommendation.priorityScore) }}</span>
              <span>Due {{ formatDateTime(topRecommendation.deadline) }}</span>
            </div>
          </div>

          <div v-else class="empty-state">
            No recommended task yet. Create tasks to generate a study focus.
          </div>
        </article>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getApiErrorMessage } from '../services/apiClient'
import { fetchAnalyticsSummary } from '../services/analyticsService'
import { fetchRecommendedTasks } from '../services/recommendationService'

const isLoading = ref(false)
const errorMessage = ref('')
const recommendations = ref([])

const summary = reactive({
  totalTaskCount: 0,
  completedTaskCount: 0,
  overdueTaskCount: 0,
  completionRate: 0
})

const pendingTaskCount = computed(() => {
  return Math.max(summary.totalTaskCount - summary.completedTaskCount, 0)
})

const formattedCompletionRate = computed(() => {
  return `${formatScore(summary.completionRate)}%`
})

const progressWidth = computed(() => {
  const value = Math.min(Math.max(summary.completionRate, 0), 100)
  return `${value}%`
})

const topRecommendation = computed(() => recommendations.value[0] || null)

onMounted(loadDashboard)

async function loadDashboard() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const [summaryResponse, recommendationResponse] = await Promise.all([
      fetchAnalyticsSummary(),
      fetchRecommendedTasks()
    ])

    Object.assign(summary, summaryResponse.data || {})
    recommendations.value = recommendationResponse.data || []
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

function formatScore(value) {
  return Number(value || 0).toFixed(1)
}

function formatDateTime(value) {
  if (!value) {
    return 'No deadline'
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

function formatLabel(value) {
  return String(value || '')
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function priorityTone(priority) {
  return {
    LOW: 'tone-low',
    MEDIUM: 'tone-medium',
    HIGH: 'tone-high'
  }[priority]
}

function riskTone(risk) {
  return {
    LOW: 'tone-low',
    MEDIUM: 'tone-medium',
    HIGH: 'tone-high'
  }[risk]
}
</script>
