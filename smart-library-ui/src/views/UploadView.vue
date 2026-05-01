<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const token = localStorage.getItem('jwt')

const selectedFile = ref(null)
const title = ref('')
const licenseType = ref('OPEN_ACCESS')
const uploading = ref(false)
const error = ref(null)

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    title.value = file.name.replace(/\.[^/.]+$/, "")
  }
}

const uploadFile = async () => {
  if (!selectedFile.value || !title.value) {
    error.value = "Будь ласка, оберіть файл та вкажіть назву."
    return
  }

  uploading.value = true
  error.value = null

  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('title', title.value)
  formData.append('licenseType', licenseType.value)

  try {
    const response = await fetch('http://localhost:8080/api/v1/library/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData,
    })

    if (!response.ok) throw new Error(`Помилка сервера: ${response.status}`)

    alert('Файл успішно завантажено та оброблено ШІ!')
    router.push('/')

  } catch (err) {
    error.value = err.message
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="upload-container">
    <button @click="router.back()" class="btn-back">⬅ Назад до каталогу</button>

    <div class="upload-card">
      <h2>Завантажити новий документ</h2>
      <p class="subtitle">ШІ автоматично проаналізує файл та створить анотацію.</p>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <div class="form-group">
        <label>PDF Файл <span class="required">*</span></label>
        <div class="file-drop-area">
          <input type="file" @change="handleFileChange" accept=".pdf" id="file-upload" class="file-input-hidden"/>
          <label for="file-upload" class="file-label">
            <span class="file-icon"></span>
            <span class="file-name">{{ selectedFile ? selectedFile.name : 'Натисніть, щоб обрати PDF файл' }}</span>
          </label>
        </div>
      </div>

      <div class="form-group">
        <label>Назва роботи <span class="required">*</span></label>
        <input v-model="title" type="text" class="form-input" placeholder="Введіть назву..." />
      </div>

      <div class="form-group">
        <label>Тип ліцензії</label>
        <select v-model="licenseType" class="form-input">
          <option value="OPEN_ACCESS">Відкритий доступ (Open Access)</option>
          <option value="INSTITUTIONAL">Локальна мережа (Інституційний)</option>
          <option value="RESTRICTED">Суворо обмежений доступ</option>
        </select>
      </div>

      <button @click="uploadFile" :disabled="uploading || !selectedFile || !title" class="btn btn-primary btn-submit">
        {{ uploading ? 'ШІ аналізує файл...' : 'Завантажити в бібліотеку' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.upload-container { max-width: 600px; margin: 2rem auto; padding: 0 1rem; }
.btn-back { background: none; border: none; color: #2a5298; cursor: pointer; font-weight: 600; margin-bottom: 1rem; padding: 0; }
.upload-card { background: white; padding: 2.5rem; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }
.upload-card h2 { margin-top: 0; color: #2c3e50; }
.subtitle { color: #666; margin-bottom: 2rem; }
.form-group { margin-bottom: 1.5rem; }
.form-group label { display: block; font-weight: 600; margin-bottom: 0.5rem; color: #333; }
.required { color: #e53935; }
.form-input { width: 100%; padding: 12px 15px; border: 1px solid #ddd; border-radius: 8px; font-size: 1rem; font-family: inherit; box-sizing: border-box; }
.form-input:focus { border-color: #2a5298; outline: none; }

.file-drop-area { border: 2px dashed #bbdefb; border-radius: 8px; padding: 2rem; text-align: center; background-color: #f8fbff; transition: all 0.3s; }
.file-drop-area:hover { border-color: #2a5298; background-color: #f1f7ff; }
.file-input-hidden { display: none; }
.file-label { cursor: pointer; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.file-icon { font-size: 3rem; }
.file-name { color: #1976d2; font-weight: 500; }

.btn-submit { width: 100%; padding: 14px; font-size: 1.1rem; margin-top: 1rem; }
.btn-submit:disabled { background-color: #a0b4d4; cursor: not-allowed; }
.alert-error { padding: 1rem; background-color: #fde8e8; color: #c62828; border-radius: 8px; margin-bottom: 1.5rem; }
</style>