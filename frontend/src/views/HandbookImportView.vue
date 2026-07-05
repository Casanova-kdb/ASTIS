<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">AI Import</p>
        <h2>Handbook Parser</h2>
        <p class="page-copy">
          Upload a module handbook and turn assessment details into editable task drafts.
        </p>
      </div>
    </div>

    <div class="handbook-grid">
      <section class="panel">
        <div class="panel-heading">
          <div>
            <h3>Upload handbook</h3>
            <p>PDF and DOCX files are processed temporarily. Only confirmed tasks are saved.</p>
          </div>
        </div>

        <form class="task-form" @submit.prevent="handleParse">
          <label>
            Module handbook
            <input
              type="file"
              accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              required
              @change="handleFileChange"
            />
          </label>

          <p v-if="selectedFile" class="muted-text">
            Selected: {{ selectedFile.name }}
          </p>

          <p v-if="parseError" class="form-error">{{ parseError }}</p>
          <p v-if="parseSuccess" class="form-success">{{ parseSuccess }}</p>

          <div class="form-actions">
            <button type="submit" class="primary-button" :disabled="isParsing || !selectedFile">
              {{ isParsing ? 'Parsing...' : 'Parse handbook' }}
            </button>
          </div>
        </form>

        <div v-if="parseResult" class="parser-summary">
          <div>
            <span>Provider</span>
            <strong>{{ parseResult.provider }}</strong>
          </div>
          <div>
            <span>File</span>
            <strong>{{ parseResult.filename }}</strong>
          </div>
          <div>
            <span>Characters</span>
            <strong>{{ parseResult.extractedCharacterCount }}</strong>
          </div>
        </div>

        <details v-if="parseResult?.extractedTextPreview" class="text-preview">
          <summary>Extracted text preview</summary>
          <p>{{ parseResult.extractedTextPreview }}</p>
        </details>
      </section>

      <section class="panel">
        <div class="panel-heading">
          <div>
            <h3>Task drafts</h3>
            <p>{{ drafts.length }} draft{{ drafts.length === 1 ? '' : 's' }} ready for review</p>
          </div>
        </div>

        <div v-if="!drafts.length" class="empty-state">
          Upload a handbook to generate editable task drafts.
        </div>

        <div v-else class="draft-list">
          <article v-for="draft in drafts" :key="draft.localId" class="draft-item">
            <form class="task-form" @submit.prevent="handleCreateDraft(draft)">
              <div class="draft-heading">
                <span class="rank-badge">{{ draft.confidenceScore }}%</span>
                <div>
                  <h4>{{ draft.title || 'Untitled draft' }}</h4>
                  <p>{{ draft.sourceEvidence }}</p>
                </div>
              </div>

              <label>
                Title
                <input v-model.trim="draft.title" type="text" maxlength="120" required />
              </label>

              <label>
                Description
                <textarea v-model.trim="draft.description" rows="4" />
              </label>

              <div class="form-row">
                <label>
                  Task type
                  <input v-model.trim="draft.taskType" type="text" maxlength="50" required />
                </label>

                <label>
                  Priority
                  <select v-model="draft.priority" required>
                    <option value="" disabled>Select priority</option>
                    <option v-for="priority in priorities" :key="priority" :value="priority">
                      {{ formatLabel(priority) }}
                    </option>
                  </select>
                </label>
              </div>

              <div class="form-row">
                <label>
                  Deadline
                  <input v-model="draft.deadline" type="datetime-local" required />
                </label>

                <label>
                  Estimated hours
                  <input v-model.number="draft.estimatedHours" type="number" min="0" step="0.5" />
                </label>
              </div>

              <p v-if="draft.deadlineMissing" class="form-error">
                The parser could not find a clear deadline. Please add one before creating this task.
              </p>
              <p v-if="draft.error" class="form-error">{{ draft.error }}</p>
              <p v-if="draft.created" class="form-success">Task created successfully.</p>

              <div class="form-actions">
                <button type="submit" class="primary-button" :disabled="draft.isSaving || draft.created">
                  {{ draft.isSaving ? 'Creating...' : draft.created ? 'Created' : 'Create task' }}
                </button>
              </div>
            </form>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { getApiErrorMessage } from '../services/apiClient'
import { parseHandbook } from '../services/handbookService'
import { createTask } from '../services/taskService'

const priorities = ['LOW', 'MEDIUM', 'HIGH']

const selectedFile = ref(null)
const isParsing = ref(false)
const parseError = ref('')
const parseSuccess = ref('')
const parseResult = ref(null)
const drafts = ref([])

function handleFileChange(event) {
  selectedFile.value = event.target.files?.[0] || null
  parseError.value = ''
  parseSuccess.value = ''
}

async function handleParse() {
  if (!selectedFile.value) {
    return
  }

  isParsing.value = true
  parseError.value = ''
  parseSuccess.value = ''
  parseResult.value = null
  drafts.value = []

  try {
    const response = await parseHandbook(selectedFile.value)
    parseResult.value = response.data
    drafts.value = (response.data?.drafts || []).map(toEditableDraft)
    parseSuccess.value = `Generated ${drafts.value.length} task draft${drafts.value.length === 1 ? '' : 's'}.`
  } catch (error) {
    parseError.value = getApiErrorMessage(error)
  } finally {
    isParsing.value = false
  }
}

async function handleCreateDraft(draft) {
  draft.error = ''

  if (!draft.deadline) {
    draft.error = 'Deadline is required before creating a task.'
    return
  }

  draft.isSaving = true

  try {
    await createTask({
      title: draft.title,
      description: draft.description,
      taskType: draft.taskType,
      priority: draft.priority,
      deadline: draft.deadline,
      estimatedHours: optionalNumber(draft.estimatedHours),
      gradeWeight: optionalNumber(draft.gradeWeight),
      difficultyLevel: optionalNumber(draft.difficultyLevel),
      deadlineFlexibility: optionalNumber(draft.deadlineFlexibility),
      personalImportance: optionalNumber(draft.personalImportance)
    })

    draft.created = true
  } catch (error) {
    draft.error = getApiErrorMessage(error)
  } finally {
    draft.isSaving = false
  }
}

function toEditableDraft(draft, index) {
  return {
    localId: `${Date.now()}-${index}`,
    title: draft.title || '',
    description: draft.description || '',
    taskType: draft.taskType || '',
    priority: draft.priority || '',
    deadline: toDateTimeInputValue(draft.deadline),
    deadlineMissing: Boolean(draft.deadlineMissing),
    estimatedHours: draft.estimatedHours ?? '',
    gradeWeight: draft.gradeWeight ?? null,
    difficultyLevel: draft.difficultyLevel ?? null,
    deadlineFlexibility: draft.deadlineFlexibility ?? null,
    personalImportance: draft.personalImportance ?? null,
    confidenceScore: Number(draft.confidenceScore ?? 50),
    sourceEvidence: draft.sourceEvidence || 'No source evidence available.',
    isSaving: false,
    created: false,
    error: ''
  }
}

function optionalNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return null
  }

  return Number(value)
}

function toDateTimeInputValue(value) {
  if (!value) {
    return ''
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return ''
  }

  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return offsetDate.toISOString().slice(0, 16)
}

function formatLabel(value) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}
</script>
