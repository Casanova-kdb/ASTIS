<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Personal Settings</p>
        <h2>Study Profile</h2>
        <p class="page-copy">
          Set your long-term study habits. Task-specific scoring criteria are managed inside each task.
        </p>
      </div>
    </div>

    <div class="settings-grid settings-grid-single">
      <section class="panel">
        <div class="panel-heading">
          <div>
            <h3>Study profile</h3>
            <p>These settings describe your study habits and are kept separate from task data.</p>
          </div>
        </div>

        <form class="settings-form" @submit.prevent="saveProfile">
          <label>
            Study pace
            <select v-model="profileForm.studyPace" required>
              <option value="SLOW">I usually need more time</option>
              <option value="NORMAL">I work at a normal pace</option>
              <option value="FAST">I usually finish tasks quickly</option>
            </select>
          </label>

          <label>
            Deadline pressure
            <select v-model="profileForm.deadlinePressureTolerance" required>
              <option value="LOW">I get stressed near deadlines</option>
              <option value="MEDIUM">I can handle some deadline pressure</option>
              <option value="HIGH">I work well near deadlines</option>
            </select>
          </label>

          <label>
            Daily study capacity
            <select v-model="profileForm.dailyStudyCapacity" required>
              <option value="LIGHT">Short study blocks</option>
              <option value="MEDIUM">Balanced study blocks</option>
              <option value="HEAVY">Long study blocks</option>
            </select>
          </label>

          <div class="form-row">
            <label>
              Preferred study time
              <select v-model="profileForm.preferredStudyTime" required>
                <option value="MORNING">Morning</option>
                <option value="AFTERNOON">Afternoon</option>
                <option value="EVENING">Evening</option>
                <option value="NIGHT">Night</option>
                <option value="FLEXIBLE">Flexible</option>
              </select>
            </label>

            <label>
              Planning style
              <select v-model="profileForm.planningStyle" required>
                <option value="FLEXIBLE">Flexible</option>
                <option value="BALANCED">Balanced</option>
                <option value="STRICT">Strict</option>
              </select>
            </label>
          </div>

          <p v-if="profileError" class="form-error">{{ profileError }}</p>
          <p v-if="profileSuccess" class="form-success">{{ profileSuccess }}</p>

          <button type="submit" class="primary-button" :disabled="isSavingProfile">
            {{ isSavingProfile ? 'Saving...' : 'Save profile' }}
          </button>
        </form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getApiErrorMessage } from '../services/apiClient'
import { fetchUserProfile, updateUserProfile } from '../services/settingsService'

const profileForm = reactive({
  studyPace: 'NORMAL',
  deadlinePressureTolerance: 'MEDIUM',
  dailyStudyCapacity: 'MEDIUM',
  preferredStudyTime: 'EVENING',
  planningStyle: 'BALANCED'
})

const profileError = ref('')
const profileSuccess = ref('')
const isSavingProfile = ref(false)

onMounted(loadProfile)

async function loadProfile() {
  profileError.value = ''

  try {
    const response = await fetchUserProfile()
    applyProfile(response.data)
  } catch (error) {
    profileError.value = getApiErrorMessage(error)
  }
}

async function saveProfile() {
  isSavingProfile.value = true
  profileError.value = ''
  profileSuccess.value = ''

  try {
    const response = await updateUserProfile({ ...profileForm })
    applyProfile(response.data)
    profileSuccess.value = 'Study profile saved.'
  } catch (error) {
    profileError.value = getApiErrorMessage(error)
  } finally {
    isSavingProfile.value = false
  }
}

function applyProfile(profile) {
  if (!profile) {
    return
  }

  profileForm.studyPace = profile.studyPace
  profileForm.deadlinePressureTolerance = profile.deadlinePressureTolerance
  profileForm.dailyStudyCapacity = profile.dailyStudyCapacity
  profileForm.preferredStudyTime = profile.preferredStudyTime
  profileForm.planningStyle = profile.planningStyle
}

</script>
