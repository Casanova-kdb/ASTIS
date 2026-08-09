<template>
  <section class="page-stack">
    <PageHeader title="Tasks" description="Create academic work, manage deadlines, and update progress.">
      <template #actions>
        <label class="filter-control compact-filter">
          Status
          <select v-model="selectedStatus" @change="loadTasks">
            <option value="">All tasks</option>
            <option v-for="status in statuses" :key="status" :value="status">
              {{ formatLabel(status) }}
            </option>
          </select>
        </label>
      </template>
    </PageHeader>

    <div class="task-grid">
      <section class="panel task-editor-panel">
        <div class="panel-heading">
          <div>
            <h3>{{ editingTaskId ? 'Edit task' : 'Create task' }}</h3>
            <p>{{ editingTaskId ? 'Update the selected task details.' : 'Add a study task to your plan.' }}</p>
          </div>
        </div>

        <form class="task-form" @submit.prevent="handleSubmit">
          <label>
            Title
            <input v-model.trim="form.title" type="text" maxlength="120" required />
          </label>

          <label>
            Description
            <textarea v-model.trim="form.description" rows="4" />
          </label>

          <div class="form-row">
            <label>
              Task type
              <input v-model.trim="form.taskType" type="text" maxlength="50" required />
            </label>

            <label>
              Priority
              <select v-model="form.priority" required>
                <option v-for="priority in priorities" :key="priority" :value="priority">
                  {{ formatLabel(priority) }}
                </option>
              </select>
            </label>
          </div>

          <div class="form-row">
            <label>
              Deadline
              <input v-model="form.deadline" type="datetime-local" required />
            </label>

            <label>
              Estimated hours
              <input v-model.number="form.estimatedHours" type="number" min="0" step="0.5" />
            </label>
          </div>

          <section class="criteria-panel">
            <div>
              <h4>Scoring criteria</h4>
              <p>These values describe this task only and help ASTIS rank it against your other tasks.</p>
            </div>

            <div
              v-for="criterion in criteriaControls"
              :key="criterion.key"
              class="weight-control"
            >
              <div class="weight-heading">
                <label :for="criterion.key">{{ criterion.label }}</label>
                <strong>{{ form[criterion.key] }}/5</strong>
              </div>

              <input
                :id="criterion.key"
                v-model.number="form[criterion.key]"
                type="range"
                min="1"
                max="5"
                step="1"
              />

              <div class="range-copy">
                <span>{{ criterion.low }}</span>
                <span>{{ criterion.high }}</span>
              </div>
            </div>
          </section>

          <p v-if="formError" class="form-error">{{ formError }}</p>
          <p v-if="successMessage" class="form-success">{{ successMessage }}</p>

          <div class="form-actions">
            <button type="submit" class="primary-button" :disabled="isSaving">
              {{ isSaving ? 'Saving...' : editingTaskId ? 'Save changes' : 'Create task' }}
            </button>
            <button v-if="editingTaskId" type="button" class="secondary-button" @click="resetForm(true)">
              Cancel
            </button>
          </div>
        </form>
      </section>

      <section class="task-list-section">
        <div class="panel-heading">
          <div>
            <h3>Task list</h3>
            <p>{{ tasks.length }} task{{ tasks.length === 1 ? '' : 's' }} shown</p>
          </div>
          <button
            type="button"
            class="icon-button"
            aria-label="Refresh task list"
            title="Refresh task list"
            :disabled="isLoading"
            @click="loadTasks"
          >
            <RefreshCw :size="18" :class="{ spinning: isLoading }" aria-hidden="true" />
          </button>
        </div>

        <p v-if="listError" class="form-error" role="alert">{{ listError }}</p>

        <div v-if="isLoading" class="task-list task-list-loading" aria-label="Loading tasks" aria-live="polite">
          <span v-for="index in 3" :key="index" class="skeleton-block skeleton-task"></span>
        </div>

        <div v-else-if="tasks.length === 0" class="empty-state">
          No tasks yet. Create your first study task to start planning.
        </div>

        <div v-else class="task-list">
          <article v-for="task in tasks" :key="task.id" class="task-item">
            <div class="task-item-main">
              <div>
                <h4>{{ task.title }}</h4>
                <p v-if="task.description">{{ task.description }}</p>
                <p v-else class="muted-text">No description</p>
              </div>

              <span :class="['status-pill', statusTone(task.status)]">
                {{ formatLabel(task.status) }}
              </span>
            </div>

            <div class="task-facts">
              <div>
                <span>Type</span>
                <strong>{{ task.taskType }}</strong>
              </div>
              <div>
                <span>Deadline</span>
                <strong>{{ formatDateTime(task.deadline) }}</strong>
              </div>
              <div>
                <span>Effort</span>
                <strong>{{ task.estimatedHours ?? 0 }}h</strong>
              </div>
              <span :class="['priority-pill', priorityTone(task.priority)]">
                {{ formatLabel(task.priority) }} priority
              </span>
            </div>

            <details class="task-criteria-details">
              <summary>Scoring criteria</summary>
              <dl>
                <div><dt>Grade impact</dt><dd>{{ task.gradeWeight ?? 3 }}/5</dd></div>
                <div><dt>Difficulty</dt><dd>{{ task.difficultyLevel ?? 3 }}/5</dd></div>
                <div><dt>Flexibility</dt><dd>{{ task.deadlineFlexibility ?? 3 }}/5</dd></div>
                <div><dt>Importance</dt><dd>{{ task.personalImportance ?? 3 }}/5</dd></div>
              </dl>
            </details>

            <div class="task-actions">
              <select
                :value="task.status"
                :aria-label="`Update status for ${task.title}`"
                :disabled="updatingTaskId === task.id"
                @change="handleStatusChange(task, $event.target.value)"
              >
                <option v-for="status in statuses" :key="status" :value="status">
                  {{ formatLabel(status) }}
                </option>
              </select>

              <button
                type="button"
                class="icon-button task-action-button"
                :aria-label="`Edit ${task.title}`"
                title="Edit task"
                @click="startEdit(task)"
              >
                <Pencil :size="17" aria-hidden="true" />
              </button>
              <button
                type="button"
                class="icon-button task-action-button danger-icon"
                :aria-label="`Delete ${task.title}`"
                title="Delete task"
                :disabled="deletingTaskId === task.id"
                @click="handleDelete(task)"
              >
                <LoaderCircle v-if="deletingTaskId === task.id" :size="17" class="spinning" aria-hidden="true" />
                <Trash2 v-else :size="17" aria-hidden="true" />
              </button>
            </div>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { LoaderCircle, Pencil, RefreshCw, Trash2 } from '@lucide/vue'
import { onMounted, reactive, ref } from 'vue'
import PageHeader from '../components/layout/PageHeader.vue'
import { getApiErrorMessage } from '../services/apiClient'
import {
  createTask,
  deleteTask,
  fetchTasks,
  updateTask,
  updateTaskStatus
} from '../services/taskService'

const priorities = ['LOW', 'MEDIUM', 'HIGH']
const statuses = ['PENDING', 'IN_PROGRESS', 'COMPLETED']
const criteriaControls = [
  {
    key: 'gradeWeight',
    label: 'Grade impact',
    low: 'Small assessment impact',
    high: 'Major assessment impact'
  },
  {
    key: 'difficultyLevel',
    label: 'Difficulty',
    low: 'Easy task',
    high: 'Difficult task'
  },
  {
    key: 'deadlineFlexibility',
    label: 'Deadline flexibility',
    low: 'Strict deadline',
    high: 'Flexible deadline'
  },
  {
    key: 'personalImportance',
    label: 'Personal importance',
    low: 'Less important to me',
    high: 'Very important to me'
  }
]

const tasks = ref([])
const selectedStatus = ref('')
const isLoading = ref(false)
const isSaving = ref(false)
const updatingTaskId = ref(null)
const deletingTaskId = ref(null)
const editingTaskId = ref(null)
const listError = ref('')
const formError = ref('')
const successMessage = ref('')

const form = reactive({
  title: '',
  description: '',
  taskType: 'Assignment',
  priority: 'MEDIUM',
  deadline: getDefaultDeadline(),
  estimatedHours: 1,
  gradeWeight: 3,
  difficultyLevel: 3,
  deadlineFlexibility: 3,
  personalImportance: 3
})

onMounted(loadTasks)

async function loadTasks() {
  isLoading.value = true
  listError.value = ''

  try {
    const response = await fetchTasks(selectedStatus.value)
    tasks.value = response.data || []
  } catch (error) {
    listError.value = getApiErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

async function handleSubmit() {
  formError.value = ''
  successMessage.value = ''
  isSaving.value = true

  try {
    const payload = buildPayload()
    const isEditing = Boolean(editingTaskId.value)

    if (isEditing) {
      await updateTask(editingTaskId.value, payload)
    } else {
      await createTask(payload)
    }

    resetForm()
    successMessage.value = isEditing ? 'Task updated successfully.' : 'Task created successfully.'
    await loadTasks()
  } catch (error) {
    formError.value = getApiErrorMessage(error)
  } finally {
    isSaving.value = false
  }
}

async function handleStatusChange(task, status) {
  if (status === task.status) {
    return
  }

  updatingTaskId.value = task.id
  listError.value = ''
  successMessage.value = ''

  try {
    await updateTaskStatus(task.id, status)
    successMessage.value = 'Task status updated.'
    await loadTasks()
  } catch (error) {
    listError.value = getApiErrorMessage(error)
  } finally {
    updatingTaskId.value = null
  }
}

async function handleDelete(task) {
  const confirmed = window.confirm(`Delete "${task.title}"?`)

  if (!confirmed) {
    return
  }

  deletingTaskId.value = task.id
  listError.value = ''
  successMessage.value = ''

  try {
    await deleteTask(task.id)

    if (editingTaskId.value === task.id) {
      resetForm()
    }

    successMessage.value = 'Task deleted successfully.'
    await loadTasks()
  } catch (error) {
    listError.value = getApiErrorMessage(error)
  } finally {
    deletingTaskId.value = null
  }
}

function startEdit(task) {
  editingTaskId.value = task.id
  form.title = task.title
  form.description = task.description || ''
  form.taskType = task.taskType
  form.priority = task.priority
  form.deadline = toDateTimeInputValue(task.deadline)
  form.estimatedHours = Number(task.estimatedHours ?? 0)
  form.gradeWeight = Number(task.gradeWeight ?? 3)
  form.difficultyLevel = Number(task.difficultyLevel ?? 3)
  form.deadlineFlexibility = Number(task.deadlineFlexibility ?? 3)
  form.personalImportance = Number(task.personalImportance ?? 3)
  formError.value = ''
  successMessage.value = ''
}

function resetForm(clearSuccess = false) {
  editingTaskId.value = null
  form.title = ''
  form.description = ''
  form.taskType = 'Assignment'
  form.priority = 'MEDIUM'
  form.deadline = getDefaultDeadline()
  form.estimatedHours = 1
  form.gradeWeight = 3
  form.difficultyLevel = 3
  form.deadlineFlexibility = 3
  form.personalImportance = 3
  formError.value = ''
  if (clearSuccess) {
    successMessage.value = ''
  }
}

function buildPayload() {
  return {
    title: form.title,
    description: form.description,
    taskType: form.taskType,
    priority: form.priority,
    deadline: form.deadline,
    estimatedHours: form.estimatedHours || 0,
    gradeWeight: form.gradeWeight,
    difficultyLevel: form.difficultyLevel,
    deadlineFlexibility: form.deadlineFlexibility,
    personalImportance: form.personalImportance
  }
}

function getDefaultDeadline() {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  date.setMinutes(0, 0, 0)
  return toDateTimeInputValue(date)
}

function toDateTimeInputValue(value) {
  const date = value instanceof Date ? value : new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return offsetDate.toISOString().slice(0, 16)
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
  return value
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

function statusTone(status) {
  return {
    PENDING: 'tone-pending',
    IN_PROGRESS: 'tone-progress',
    COMPLETED: 'tone-completed'
  }[status]
}
</script>
