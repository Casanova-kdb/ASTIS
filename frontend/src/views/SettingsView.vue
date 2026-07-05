<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <p class="eyebrow">Personal Settings</p>
        <h2>Study Profile</h2>
        <p class="page-copy">
          Manage your account details and long-term study habits.
        </p>
      </div>
    </div>

    <div class="settings-grid">
      <section class="panel">
        <div class="panel-heading">
          <div>
            <h3>Account settings</h3>
            <p>View your login identity and update display name or password.</p>
          </div>
        </div>

        <form class="settings-form" @submit.prevent="saveAccount">
          <div class="readonly-grid">
            <label>
              Username
              <input :value="accountForm.username" type="text" disabled />
            </label>

            <label>
              Email
              <input :value="accountForm.email" type="email" disabled />
            </label>
          </div>

          <label>
            Display name
            <input v-model.trim="accountForm.displayName" type="text" maxlength="80" required />
          </label>

          <p v-if="accountError" class="form-error">{{ accountError }}</p>
          <p v-if="accountSuccess" class="form-success">{{ accountSuccess }}</p>

          <button type="submit" class="primary-button" :disabled="isSavingAccount">
            {{ isSavingAccount ? 'Saving...' : 'Save account' }}
          </button>
        </form>

        <form class="settings-form password-form" @submit.prevent="savePassword">
          <div>
            <h4>Password</h4>
            <p>Use your current password to set a new one.</p>
          </div>

          <label>
            Current password
            <input v-model="passwordForm.currentPassword" type="password" autocomplete="current-password" required />
          </label>

          <label>
            New password
            <input v-model="passwordForm.newPassword" type="password" autocomplete="new-password" minlength="8" maxlength="72" required />
          </label>

          <p v-if="passwordError" class="form-error">{{ passwordError }}</p>
          <p v-if="passwordSuccess" class="form-success">{{ passwordSuccess }}</p>

          <button type="submit" class="secondary-button" :disabled="isSavingPassword">
            {{ isSavingPassword ? 'Updating...' : 'Update password' }}
          </button>
        </form>
      </section>

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
import { changePassword, fetchCurrentUser, updateCurrentUser } from '../services/userService'

const accountForm = reactive({
  username: '',
  email: '',
  displayName: ''
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: ''
})

const profileForm = reactive({
  studyPace: 'NORMAL',
  deadlinePressureTolerance: 'MEDIUM',
  dailyStudyCapacity: 'MEDIUM',
  preferredStudyTime: 'EVENING',
  planningStyle: 'BALANCED'
})

const accountError = ref('')
const accountSuccess = ref('')
const passwordError = ref('')
const passwordSuccess = ref('')
const profileError = ref('')
const profileSuccess = ref('')
const isSavingAccount = ref(false)
const isSavingPassword = ref(false)
const isSavingProfile = ref(false)

onMounted(loadSettings)

async function loadSettings() {
  await Promise.all([
    loadAccount(),
    loadProfile()
  ])
}

async function loadAccount() {
  accountError.value = ''

  try {
    const response = await fetchCurrentUser()
    applyAccount(response.data)
  } catch (error) {
    accountError.value = getApiErrorMessage(error)
  }
}

async function loadProfile() {
  profileError.value = ''

  try {
    const response = await fetchUserProfile()
    applyProfile(response.data)
  } catch (error) {
    profileError.value = getApiErrorMessage(error)
  }
}

async function saveAccount() {
  isSavingAccount.value = true
  accountError.value = ''
  accountSuccess.value = ''

  try {
    const response = await updateCurrentUser({
      displayName: accountForm.displayName
    })
    applyAccount(response.data)
    accountSuccess.value = 'Account details saved.'
  } catch (error) {
    accountError.value = getApiErrorMessage(error)
  } finally {
    isSavingAccount.value = false
  }
}

async function savePassword() {
  isSavingPassword.value = true
  passwordError.value = ''
  passwordSuccess.value = ''

  try {
    await changePassword({ ...passwordForm })
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordSuccess.value = 'Password updated.'
  } catch (error) {
    passwordError.value = getApiErrorMessage(error)
  } finally {
    isSavingPassword.value = false
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

function applyAccount(user) {
  if (!user) {
    return
  }

  accountForm.username = user.username
  accountForm.email = user.email
  accountForm.displayName = user.displayName || user.username
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
