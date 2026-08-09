<template>
  <section class="page-stack">
    <PageHeader
      title="Dashboard"
      description="Review your workload, completion progress, and current study focus."
    >
      <template #actions>
        <button
          type="button"
          class="icon-button"
          aria-label="Refresh dashboard"
          title="Refresh dashboard"
          :disabled="isLoading || isTrendLoading"
          @click="loadDashboard"
        >
          <RefreshCw :size="18" :class="{ spinning: isLoading || isTrendLoading }" aria-hidden="true" />
        </button>
      </template>
    </PageHeader>

    <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>

    <div v-if="isLoading" class="dashboard-loading" aria-label="Loading dashboard" aria-live="polite">
      <div class="metric-strip skeleton-strip">
        <span v-for="index in 4" :key="index" class="skeleton-block"></span>
      </div>
      <div class="dashboard-grid">
        <span class="skeleton-block skeleton-panel"></span>
        <span class="skeleton-block skeleton-panel"></span>
      </div>
    </div>

    <template v-else>
      <section class="metric-strip" aria-label="Task overview">
        <div class="metric-item">
          <span>Total tasks</span>
          <strong>{{ summary.totalTaskCount }}</strong>
        </div>

        <div class="metric-item metric-item-success">
          <span>Completed</span>
          <strong>{{ summary.completedTaskCount }}</strong>
        </div>

        <div class="metric-item">
          <span>Pending</span>
          <strong>{{ pendingTaskCount }}</strong>
        </div>

        <div class="metric-item metric-item-danger">
          <span>Overdue</span>
          <strong>{{ summary.overdueTaskCount }}</strong>
        </div>
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

          <div v-if="topRecommendation" class="focus-detail">
            <div class="task-item-main">
              <div>
                <span class="focus-rank">Rank {{ topRecommendation.rankPosition }}</span>
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

      <section class="analytics-trend-section">
        <div class="section-heading trend-section-heading">
          <div>
            <h3>Weekly trends</h3>
            <p>Compare completion activity and missed deadlines across recent weeks.</p>
          </div>

          <div class="trend-window-control" aria-label="Analytics reporting window">
            <button
              v-for="weeks in trendWindowOptions"
              :key="weeks"
              type="button"
              :class="['trend-window-button', { active: selectedWeeks === weeks }]"
              :aria-pressed="selectedWeeks === weeks"
              :disabled="isTrendLoading"
              @click="changeTrendWindow(weeks)"
            >
              {{ weeks }} weeks
            </button>
          </div>
        </div>

        <p v-if="trendErrorMessage" class="form-error" role="alert">
          {{ trendErrorMessage }}
        </p>

        <div v-if="isTrendLoading" class="empty-state" aria-live="polite">
          Loading analytics trends...
        </div>

        <template v-else-if="!trendErrorMessage">
          <section class="trend-summary-grid">
            <article class="metric-card trend-summary-card">
              <span>Most delayed task type</span>
              <strong>{{ mostDelayedTaskType }}</strong>
            </article>

            <article class="metric-card trend-summary-card">
              <span>Average estimated hours</span>
              <strong>{{ formattedAverageEstimatedHours }}</strong>
            </article>
          </section>

          <section class="trend-chart-grid">
            <article class="panel trend-panel">
              <div class="panel-heading">
                <div>
                  <h3>Weekly completions</h3>
                  <p>Completion events recorded in each Monday-to-Sunday week.</p>
                </div>
              </div>

              <WeeklyTrendChart
                v-if="hasCompletionData"
                :labels="trendLabels"
                :values="completionValues"
                dataset-label="Completed tasks"
                chart-type="line"
                color="#1f766d"
                aria-label="Weekly completed task trend"
              />
              <div v-else class="empty-state trend-empty-state">
                No completed tasks were recorded in this period.
              </div>
            </article>

            <article class="panel trend-panel">
              <div class="panel-heading">
                <div>
                  <h3>Weekly overdue tasks</h3>
                  <p>Tasks grouped by the week in which their deadline passed.</p>
                </div>
              </div>

              <WeeklyTrendChart
                v-if="hasOverdueData"
                :labels="trendLabels"
                :values="overdueValues"
                dataset-label="Overdue tasks"
                chart-type="bar"
                color="#a44338"
                aria-label="Weekly overdue task trend"
              />
              <div v-else class="empty-state trend-empty-state">
                No overdue tasks were found in this period.
              </div>
            </article>
          </section>
        </template>
      </section>
    </template>
  </section>
</template>

<script setup>
import { RefreshCw } from '@lucide/vue'
import { computed, onMounted, reactive, ref } from 'vue'
import WeeklyTrendChart from '../components/analytics/WeeklyTrendChart.vue'
import PageHeader from '../components/layout/PageHeader.vue'
import { getApiErrorMessage } from '../services/apiClient'
import { fetchAnalyticsSummary, fetchAnalyticsTrends } from '../services/analyticsService'
import { fetchRecommendedTasks } from '../services/recommendationService'

const isLoading = ref(false)
const isTrendLoading = ref(false)
const errorMessage = ref('')
const trendErrorMessage = ref('')
const recommendations = ref([])
const selectedWeeks = ref(8)
const trendWindowOptions = [4, 8, 12]

const summary = reactive({
  totalTaskCount: 0,
  completedTaskCount: 0,
  overdueTaskCount: 0,
  completionRate: 0
})

const trends = reactive({
  generatedAt: null,
  startDate: null,
  endDate: null,
  weeks: 8,
  weeklyTrends: [],
  mostDelayedTaskType: null,
  averageEstimatedHours: 0
})

const pendingTaskCount = computed(() => {
  return Math.max(summary.totalTaskCount - summary.completedTaskCount, 0)
})

const formattedCompletionRate = computed(() => {
  return `${formatScore(summary.completionRate * 100)}%`
})

const progressWidth = computed(() => {
  const value = Math.min(Math.max(summary.completionRate * 100, 0), 100)
  return `${value}%`
})

const topRecommendation = computed(() => recommendations.value[0] || null)

const trendLabels = computed(() => {
  return trends.weeklyTrends.map((week) => formatWeekLabel(week.weekStart))
})

const completionValues = computed(() => {
  return trends.weeklyTrends.map((week) => Number(week.completedCount || 0))
})

const overdueValues = computed(() => {
  return trends.weeklyTrends.map((week) => Number(week.overdueCount || 0))
})

const hasCompletionData = computed(() => completionValues.value.some((value) => value > 0))
const hasOverdueData = computed(() => overdueValues.value.some((value) => value > 0))

const mostDelayedTaskType = computed(() => {
  return trends.mostDelayedTaskType?.taskType
    ? formatLabel(trends.mostDelayedTaskType.taskType)
    : 'No delayed task type'
})

const formattedAverageEstimatedHours = computed(() => {
  return `${Number(trends.averageEstimatedHours || 0).toFixed(1)} hours`
})

onMounted(loadDashboard)

async function loadDashboard() {
  await Promise.all([loadDashboardSummary(), loadAnalyticsTrends()])
}

async function loadDashboardSummary() {
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

async function loadAnalyticsTrends() {
  isTrendLoading.value = true
  trendErrorMessage.value = ''

  try {
    const response = await fetchAnalyticsTrends(selectedWeeks.value)
    Object.assign(trends, response.data || {})
  } catch (error) {
    trends.weeklyTrends = []
    trendErrorMessage.value = getApiErrorMessage(error)
  } finally {
    isTrendLoading.value = false
  }
}

async function changeTrendWindow(weeks) {
  if (selectedWeeks.value === weeks || isTrendLoading.value) {
    return
  }

  selectedWeeks.value = weeks
  await loadAnalyticsTrends()
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

function formatWeekLabel(value) {
  if (!value) {
    return ''
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric'
  }).format(new Date(`${value}T00:00:00`))
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
