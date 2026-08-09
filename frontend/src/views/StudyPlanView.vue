<template>
  <section class="page-stack">
    <PageHeader
      title="Study plan"
      description="Review scheduled sessions, remaining workload, and deadline warnings."
    >
      <template #actions>
        <div class="study-plan-controls">
          <label class="filter-control compact-filter">
            Planning window
            <select v-model.number="planningDays" :disabled="isLoading">
              <option :value="3">3 days</option>
              <option :value="7">7 days</option>
              <option :value="14">14 days</option>
            </select>
          </label>

          <button type="button" class="secondary-button button-with-icon" :disabled="isLoading" @click="loadStudyPlan">
            <RefreshCw :size="17" :class="{ spinning: isLoading }" aria-hidden="true" />
            {{ isLoading ? 'Generating' : 'Regenerate' }}
          </button>
        </div>
      </template>
    </PageHeader>

    <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>
    <div v-if="isLoading && !plan" class="study-plan-loading" aria-label="Generating study plan" aria-live="polite">
      <div class="metric-strip skeleton-strip">
        <span v-for="index in 4" :key="index" class="skeleton-block"></span>
      </div>
      <span class="skeleton-block study-plan-skeleton"></span>
    </div>

    <template v-else-if="plan">
      <section class="metric-strip plan-metric-strip" aria-label="Study plan capacity summary">
        <div class="metric-item">
          <span>Scheduled</span>
          <strong>{{ formatHours(plan.totalScheduledHours) }}</strong>
        </div>

        <div class="metric-item">
          <span>Available</span>
          <strong>{{ formatHours(plan.totalAvailableHours) }}</strong>
        </div>

        <div :class="['metric-item', { 'metric-item-danger': Number(plan.totalUnscheduledHours || 0) > 0 }]">
          <span>Unscheduled</span>
          <strong>{{ formatHours(plan.totalUnscheduledHours) }}</strong>
        </div>

        <div class="metric-item">
          <span>Daily capacity</span>
          <strong>{{ formatHours(plan.dailyCapacityHours) }}</strong>
        </div>
      </section>

      <section v-if="plan.warnings?.length" class="plan-warning-panel" aria-live="polite">
        <div class="panel-heading">
          <div>
            <h3>Planning notices</h3>
            <p>{{ plan.warnings.length }} item{{ plan.warnings.length === 1 ? '' : 's' }} need attention.</p>
          </div>

          <span :class="['status-pill', plan.overloaded ? 'tone-high' : 'tone-medium']">
            {{ plan.overloaded ? 'Capacity risk' : 'Review' }}
          </span>
        </div>

        <ul class="plan-warning-list">
          <li v-for="(warning, index) in plan.warnings" :key="`${warning.code}-${warning.taskId}-${index}`">
            <strong>{{ warningLabel(warning.code) }}</strong>
            <span>{{ warning.message }}</span>
          </li>
        </ul>
      </section>

      <section class="panel study-schedule-panel">
        <div class="panel-heading">
          <div>
            <h3>{{ formatDateRange(plan.startDate, plan.endDate) }}</h3>
            <p>
              {{ formatLabel(plan.preferredStudyTime) }} preference ·
              {{ plan.planningDays }}-day plan
            </p>
          </div>

          <span class="generated-at">{{ formatGeneratedAt(plan.generatedAt) }}</span>
        </div>

        <div v-if="isPlanEmpty" class="empty-state">
          No active workload is available for this planning window.
          <RouterLink class="inline-link" to="/tasks">View tasks</RouterLink>
        </div>

        <div v-else class="study-day-list">
          <article v-for="day in plan.days" :key="day.date" class="study-day">
            <header class="study-day-heading">
              <strong>{{ formatWeekday(day.date) }}</strong>
              <span>{{ formatCalendarDate(day.date) }}</span>
              <small>{{ formatHours(day.totalScheduledHours) }}</small>
            </header>

            <div v-if="day.sessions.length" class="study-session-list">
              <div
                v-for="(session, index) in day.sessions"
                :key="`${day.date}-${session.taskId}-${index}`"
                class="study-session"
              >
                <div class="session-time">
                  <strong>{{ formatTime(session.startTime) }}</strong>
                  <span>{{ formatTime(session.endTime) }}</span>
                </div>

                <div class="session-main">
                  <h4>{{ session.title }}</h4>
                  <div class="task-meta">
                    <span>{{ formatHours(session.durationHours) }}</span>
                    <span>Score {{ formatScore(session.priorityScore) }}</span>
                    <span :class="['status-pill', riskTone(session.delayRisk)]">
                      {{ formatLabel(session.delayRisk) }} risk
                    </span>
                  </div>
                </div>

                <div class="session-deadline">
                  <span>Deadline</span>
                  <strong>{{ formatDeadline(session.deadline) }}</strong>
                </div>
              </div>
            </div>

            <div v-else class="study-day-empty">No session scheduled</div>
          </article>
        </div>
      </section>

      <section v-if="plan.unscheduledTasks?.length" class="panel unscheduled-panel">
        <div class="panel-heading">
          <div>
            <h3>Unscheduled work</h3>
            <p>Tasks or remaining hours that could not fit into this plan.</p>
          </div>
        </div>

        <div class="unscheduled-list">
          <article
            v-for="task in plan.unscheduledTasks"
            :key="`${task.taskId}-${task.reason}`"
            class="unscheduled-item"
          >
            <div>
              <h4>{{ task.title }}</h4>
              <p>{{ unscheduledReason(task.reason) }}</p>
            </div>
            <strong>{{ formatHours(task.remainingHours) }}</strong>
          </article>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup>
import { RefreshCw } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import PageHeader from '../components/layout/PageHeader.vue'
import { getApiErrorMessage } from '../services/apiClient'
import { fetchStudyPlan } from '../services/studyPlanService'

const planningDays = ref(7)
const plan = ref(null)
const isLoading = ref(false)
const errorMessage = ref('')

const isPlanEmpty = computed(() => {
  if (!plan.value) {
    return true
  }

  return Number(plan.value.totalScheduledHours || 0) === 0
    && (plan.value.unscheduledTasks?.length || 0) === 0
})

onMounted(loadStudyPlan)

async function loadStudyPlan() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await fetchStudyPlan(planningDays.value)
    plan.value = response.data || null
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

function formatHours(value) {
  const hours = Number(value || 0)
  const formatted = Number.isInteger(hours) ? hours.toFixed(0) : hours.toFixed(1)
  return `${formatted}h`
}

function formatScore(value) {
  return Number(value || 0).toFixed(1)
}

function parseDate(value) {
  return new Date(`${value}T00:00:00`)
}

function formatWeekday(value) {
  return new Intl.DateTimeFormat('en', {
    weekday: 'long'
  }).format(parseDate(value))
}

function formatCalendarDate(value) {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric'
  }).format(parseDate(value))
}

function formatDateRange(startDate, endDate) {
  return `${formatCalendarDate(startDate)} – ${formatCalendarDate(endDate)}`
}

function formatTime(value) {
  return String(value || '').slice(0, 5) || '--:--'
}

function formatDeadline(value) {
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

function formatGeneratedAt(value) {
  if (!value) {
    return 'Generated now'
  }

  return `Generated ${new Intl.DateTimeFormat('en', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))}`
}

function formatLabel(value) {
  return String(value || '')
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function riskTone(risk) {
  return {
    LOW: 'tone-low',
    MEDIUM: 'tone-medium',
    HIGH: 'tone-high'
  }[risk] || 'tone-pending'
}

function warningLabel(code) {
  return {
    DEADLINE_PASSED: 'Deadline passed',
    INSUFFICIENT_CAPACITY: 'Capacity shortage',
    OUTSIDE_PLANNING_WINDOW: 'Outside planning window',
    DEFAULT_ESTIMATE_USED: 'Default estimate'
  }[code] || formatLabel(code)
}

function unscheduledReason(reason) {
  return {
    DEADLINE_PASSED: 'The deadline has already passed.',
    INSUFFICIENT_CAPACITY: 'Not enough study capacity is available before the deadline.',
    OUTSIDE_PLANNING_WINDOW: 'The remaining work continues beyond this planning window.'
  }[reason] || 'This work could not be scheduled.'
}
</script>
