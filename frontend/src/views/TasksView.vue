<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Task Management</p>
        <h2>Academic Tasks</h2>
        <p class="page-copy">
          Create study tasks, track deadlines, and update progress from one workspace.
        </p>
      </div>

      <label class="filter-control">
        Status
        <select v-model="selectedStatus" @change="loadTasks">
          <option value="">All</option>
          <option v-for="status in statuses" :key="status" :value="status">
            {{ formatLabel(status) }}
          </option>
        </select>
      </label>
    </div>

    <div class="task-grid">
      <section class="panel">
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

          <p v-if="formError" class="form-error">{{ formError }}</p>

          <div class="form-actions">
            <button type="submit" class="primary-button" :disabled="isSaving">
              {{ isSaving ? 'Saving...' : editingTaskId ? 'Save changes' : 'Create task' }}
            </button>
            <button v-if="editingTaskId" type="button" class="secondary-button" @click="resetForm">
              Cancel
            </button>
          </div>
        </form>
      </section>

      <section class="panel">
        <div class="panel-heading">
          <div>
            <h3>Task list</h3>
            <p>{{ tasks.length }} task{{ tasks.length === 1 ? '' : 's' }} shown</p>
          </div>
          <button type="button" class="secondary-button" :disabled="isLoading" @click="loadTasks">
            Refresh
          </button>
        </div>

        <p v-if="listError" class="form-error">{{ listError }}</p>

        <div v-if="isLoading" class="empty-state">Loading tasks...</div>

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

            <div class="task-meta">
              <span>{{ task.taskType }}</span>
              <span :class="['priority-pill', priorityTone(task.priority)]">
                {{ formatLabel(task.priority) }}
              </span>
              <span>Due {{ formatDateTime(task.deadline) }}</span>
              <span>{{ task.estimatedHours ?? 0 }}h</span>
            </div>

            <div class="task-actions">
              <select
                :value="task.status"
                :disabled="updatingTaskId === task.id"
                @change="handleStatusChange(task, $event.target.value)"
              >
                <option v-for="status in statuses" :key="status" :value="status">
                  {{ formatLabel(status) }}
                </option>
              </select>

              <button type="button" class="secondary-button" @click="startEdit(task)">Edit</button>
              <button type="button" class="danger-button" :disabled="deletingTaskId === task.id" @click="handleDelete(task)">
                {{ deletingTaskId === task.id ? 'Deleting...' : 'Delete' }}
              </button>
            </div>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
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

const tasks = ref([])
const selectedStatus = ref('')
const isLoading = ref(false)
const isSaving = ref(false)
const updatingTaskId = ref(null)
const deletingTaskId = ref(null)
const editingTaskId = ref(null)
const listError = ref('')
const formError = ref('')

const form = reactive({
  title: '',
  description: '',
  taskType: 'Assignment',
  priority: 'MEDIUM',
  deadline: getDefaultDeadline(),
  estimatedHours: 1
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
  isSaving.value = true

  try {
    const payload = buildPayload()

    if (editingTaskId.value) {
      await updateTask(editingTaskId.value, payload)
    } else {
      await createTask(payload)
    }

    resetForm()
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

  try {
    await updateTaskStatus(task.id, status)
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

  try {
    await deleteTask(task.id)

    if (editingTaskId.value === task.id) {
      resetForm()
    }

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
  formError.value = ''
}

function resetForm() {
  editingTaskId.value = null
  form.title = ''
  form.description = ''
  form.taskType = 'Assignment'
  form.priority = 'MEDIUM'
  form.deadline = getDefaultDeadline()
  form.estimatedHours = 1
  formError.value = ''
}

function buildPayload() {
  return {
    title: form.title,
    description: form.description,
    taskType: form.taskType,
    priority: form.priority,
    deadline: form.deadline,
    estimatedHours: form.estimatedHours || 0
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
