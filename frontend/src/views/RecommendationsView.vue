<template>
  <section class="page-stack">
    <PageHeader
      title="Recommendations"
      description="Review task order, delay risk, ranking reasons, and study advice."
    >
      <template #actions>
        <button
          type="button"
          class="icon-button"
          aria-label="Refresh recommendations"
          title="Refresh recommendations"
          :disabled="isLoading"
          @click="loadRecommendations"
        >
          <RefreshCw :size="18" :class="{ spinning: isLoading }" aria-hidden="true" />
        </button>
      </template>
    </PageHeader>

    <section class="panel advice-panel">
      <div class="panel-heading">
        <div>
          <h3>AI study advice</h3>
          <p>Generated from your current recommended task list.</p>
        </div>

        <span v-if="advice" :class="['provider-pill', advice.fallback ? 'tone-medium' : 'tone-low']">
          {{ advice.fallback ? 'Fallback' : advice.provider || 'AI' }}
        </span>
      </div>

      <p v-if="adviceError" class="form-error" role="alert">{{ adviceError }}</p>
      <div v-else-if="isLoadingAdvice" class="advice-skeleton" aria-label="Loading study advice" aria-live="polite">
        <span class="skeleton-block"></span>
        <span class="skeleton-block"></span>
        <span class="skeleton-block skeleton-short"></span>
      </div>
      <div v-else-if="advice" class="advice-content">
        <p>{{ advice.advice }}</p>
        <div class="advice-meta">
          <span>{{ advice.model || 'No model' }}</span>
          <span>Generated {{ formatDateTime(advice.generatedAt) }}</span>
          <span>{{ advice.basedOnTasks?.length || 0 }} task{{ advice.basedOnTasks?.length === 1 ? '' : 's' }} used</span>
        </div>
      </div>
      <div v-else class="empty-state">
        No AI advice yet. Create tasks first to generate a study plan.
      </div>
    </section>

    <section class="recommendation-section">
      <div class="panel-heading">
        <div>
          <h3>Ranked tasks</h3>
          <p>{{ recommendations.length }} recommendation{{ recommendations.length === 1 ? '' : 's' }} shown</p>
        </div>
      </div>

      <p v-if="recommendationError" class="form-error" role="alert">{{ recommendationError }}</p>
      <div v-if="isLoadingRecommendations" class="recommendation-list" aria-label="Loading recommendations" aria-live="polite">
        <span v-for="index in 3" :key="index" class="skeleton-block skeleton-recommendation"></span>
      </div>

      <div v-else-if="recommendations.length === 0" class="empty-state">
        No recommendation result yet. Add tasks with deadlines to generate ranking.
      </div>

      <div v-else class="recommendation-list">
        <article v-for="task in recommendations" :key="task.taskId" class="recommendation-item">
          <div class="recommendation-rank">#{{ task.rankPosition }}</div>

          <div class="recommendation-body">
            <div class="task-item-main">
              <div>
                <h4>{{ task.title }}</h4>
                <p>{{ task.reason }}</p>
              </div>

              <span :class="['status-pill', riskTone(task.delayRisk)]">
                {{ formatLabel(task.delayRisk) }} risk
              </span>
            </div>

            <div class="recommendation-facts">
              <div>
                <span>Priority score</span>
                <strong>{{ formatScore(task.priorityScore) }}</strong>
              </div>
              <div>
                <span>Estimated hours</span>
                <strong>{{ task.estimatedHours ?? 0 }}h</strong>
              </div>
              <div>
                <span>Deadline</span>
                <strong>{{ formatDateTime(task.deadline) }}</strong>
              </div>
            </div>

            <div class="recommendation-labels">
              <span>{{ task.taskType }}</span>
              <span :class="['priority-pill', priorityTone(task.priority)]">
                {{ formatLabel(task.priority) }}
              </span>
              <span>{{ formatLabel(task.status) }}</span>
            </div>

            <details class="recommendation-explanation">
              <summary>Why this task is ranked here</summary>
              <div class="recommendation-explanation-content">
                <div>
                <strong>Why this task is ranked here</strong>
                <ul>
                  <li v-for="factor in explanationFactors(task)" :key="factor">{{ formatFactor(factor) }}</li>
                </ul>
                </div>
                <p>
                  <strong>Delay risk:</strong>
                  {{ task.delayRiskReason || task.reason }}
                </p>
              </div>
            </details>
          </div>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup>
import { RefreshCw } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import PageHeader from '../components/layout/PageHeader.vue'
import { getApiErrorMessage } from '../services/apiClient'
import { fetchRecommendedTasks, fetchStudyAdvice } from '../services/recommendationService'

const recommendations = ref([])
const advice = ref(null)
const isLoadingRecommendations = ref(false)
const isLoadingAdvice = ref(false)
const recommendationError = ref('')
const adviceError = ref('')

const isLoading = computed(() => isLoadingRecommendations.value || isLoadingAdvice.value)

onMounted(loadRecommendations)

async function loadRecommendations() {
  await Promise.all([
    loadRecommendedTasks(),
    loadStudyAdvice()
  ])
}

async function loadRecommendedTasks() {
  isLoadingRecommendations.value = true
  recommendationError.value = ''

  try {
    const response = await fetchRecommendedTasks()
    recommendations.value = response.data || []
  } catch (error) {
    recommendationError.value = getApiErrorMessage(error)
  } finally {
    isLoadingRecommendations.value = false
  }
}

async function loadStudyAdvice() {
  isLoadingAdvice.value = true
  adviceError.value = ''

  try {
    const response = await fetchStudyAdvice()
    advice.value = response.data || null
  } catch (error) {
    adviceError.value = getApiErrorMessage(error)
  } finally {
    isLoadingAdvice.value = false
  }
}

function formatScore(value) {
  return Number(value || 0).toFixed(1)
}

function formatDateTime(value) {
  if (!value) {
    return 'No date'
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

function explanationFactors(task) {
  return task.explanationFactors?.length ? task.explanationFactors : [task.reason]
}

function formatFactor(value) {
  if (!value) {
    return 'No additional explanation available.'
  }

  return value.charAt(0).toUpperCase() + value.slice(1)
}
</script>
