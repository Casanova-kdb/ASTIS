<template>
  <section class="page-stack">
    <PageHeader
      title="Handbook import"
      description="Upload a module handbook, review extracted assessment details, and confirm task drafts."
    />

    <div class="handbook-grid">
      <section class="panel handbook-upload-panel">
        <div class="panel-heading">
          <div>
            <h3>Upload handbook</h3>
            <p>PDF and DOCX files are processed temporarily. Only confirmed tasks are saved.</p>
          </div>
        </div>

        <form class="task-form" @submit.prevent="handleParse">
          <label class="file-upload-control">
            <input
              class="visually-hidden"
              type="file"
              accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              required
              @change="handleFileChange"
            />
            <FileUp :size="24" aria-hidden="true" />
            <span>
              <strong>{{ selectedFile ? selectedFile.name : 'Choose a PDF or DOCX file' }}</strong>
              <small>Maximum file size: 10 MB</small>
            </span>
          </label>

          <p v-if="parseError" class="form-error" role="alert">{{ parseError }}</p>
          <p v-if="parseSuccess" class="form-success" role="status">{{ parseSuccess }}</p>

          <div class="form-actions">
            <button type="submit" class="primary-button button-with-icon" :disabled="isParsing || !selectedFile">
              <LoaderCircle v-if="isParsing" :size="17" class="spinning" aria-hidden="true" />
              <FileSearch v-else :size="17" aria-hidden="true" />
              {{ isParsing ? 'Parsing' : 'Parse handbook' }}
            </button>
          </div>
        </form>

        <dl v-if="parseResult" class="parser-summary">
          <div>
            <dt>Provider</dt>
            <dd>{{ parseResult.provider }}</dd>
          </div>
          <div>
            <dt>File</dt>
            <dd>{{ parseResult.filename }}</dd>
          </div>
          <div>
            <dt>Characters</dt>
            <dd>{{ parseResult.extractedCharacterCount }}</dd>
          </div>
        </dl>

        <p v-if="parseResult?.fallbackReason" class="form-warning">
          {{ parseResult.fallbackReason }}
        </p>

        <details v-if="parseResult?.extractedTextPreview" class="text-preview">
          <summary>Extracted text preview</summary>
          <p>{{ parseResult.extractedTextPreview }}</p>
        </details>
      </section>

      <section class="handbook-drafts-section">
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
                <span class="confidence-score">{{ draft.confidenceScore }}%</span>
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

              <p v-if="draft.deadlineMissing" class="form-error" role="alert">
                The parser could not find a clear deadline. Please add one before creating this task.
              </p>
              <p v-if="draft.error" class="form-error" role="alert">{{ draft.error }}</p>
              <p v-if="draft.created" class="form-success" role="status">Task created successfully.</p>

              <div class="form-actions">
                <button type="submit" class="primary-button button-with-icon" :disabled="draft.isSaving || draft.created">
                  <LoaderCircle v-if="draft.isSaving" :size="17" class="spinning" aria-hidden="true" />
                  <CheckCircle2 v-else :size="17" aria-hidden="true" />
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
import { CheckCircle2, FileSearch, FileUp, LoaderCircle } from '@lucide/vue'
import { ref } from 'vue'
import PageHeader from '../components/layout/PageHeader.vue'
import { getApiErrorMessage } from '../services/apiClient'
import { parseHandbook } from '../services/handbookService'
import { createTask } from '../services/taskService'

const priorities = ['LOW', 'MEDIUM', 'HIGH']
const maxHandbookFileSizeBytes = 10 * 1024 * 1024

const selectedFile = ref(null)
const isParsing = ref(false)
const parseError = ref('')
const parseSuccess = ref('')
const parseResult = ref(null)
const drafts = ref([])

function handleFileChange(event) {
  const file = event.target.files?.[0] || null

  if (file && file.size > maxHandbookFileSizeBytes) {
    selectedFile.value = null
    event.target.value = ''
    parseError.value = 'Handbook file must be 10MB or smaller.'
    parseSuccess.value = ''
    return
  }

  selectedFile.value = file
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
