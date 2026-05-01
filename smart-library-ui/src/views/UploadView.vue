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
  <div class="p-6 md:p-10 max-w-3xl mx-auto w-full">
    <button @click="router.back()" class="flex items-center text-slate-500 hover:text-rose-700 font-medium mb-6 transition-colors">
      <i class="fas fa-arrow-left mr-2"></i> Назад до каталогу
    </button>

    <div class="bg-white p-8 md:p-10 rounded-2xl shadow-sm border border-slate-200">
      <h2 class="text-3xl font-bold text-slate-900 mb-2">Завантажити новий документ</h2>
      <p class="text-slate-500 mb-8">ШІ автоматично проаналізує файл, створить вектор та анотацію.</p>

      <div v-if="error" class="bg-red-50 text-red-700 p-4 rounded-xl border border-red-200 mb-6">{{ error }}</div>

      <div class="space-y-6">
        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">PDF Файл <span class="text-rose-600">*</span></label>
          <div class="border-2 border-dashed border-slate-300 rounded-xl p-8 text-center bg-slate-50 hover:bg-rose-50 hover:border-rose-300 transition-colors">
            <input type="file" @change="handleFileChange" accept=".pdf" id="file-upload" class="hidden"/>
            <label for="file-upload" class="cursor-pointer flex flex-col items-center gap-3">
              <i class="fas fa-file-pdf text-4xl text-slate-400"></i>
              <span class="text-rose-700 font-semibold">{{ selectedFile ? selectedFile.name : 'Натисніть, щоб обрати PDF файл' }}</span>
            </label>
          </div>
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Назва роботи <span class="text-rose-600">*</span></label>
          <input v-model="title" type="text" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-rose-600 outline-none text-slate-900" placeholder="Введіть назву..." />
        </div>

        <div>
          <label class="block text-sm font-semibold text-slate-700 mb-2">Тип ліцензії</label>
          <select v-model="licenseType" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-rose-600 outline-none text-slate-900">
            <option value="OPEN_ACCESS">Відкритий доступ (Open Access)</option>
            <option value="INSTITUTIONAL">Локальна мережа (Інституційний)</option>
            <option value="RESTRICTED">Суворо обмежений доступ</option>
          </select>
        </div>

        <button @click="uploadFile" :disabled="uploading || !selectedFile || !title" class="w-full bg-slate-900 text-white rounded-xl py-4 font-semibold hover:bg-slate-800 transition-colors shadow-md disabled:bg-slate-400 mt-4 flex justify-center items-center">
          <i v-if="uploading" class="fas fa-spinner fa-spin mr-2"></i>
          {{ uploading ? 'ШІ аналізує файл...' : 'Завантажити в бібліотеку' }}
        </button>
      </div>
    </div>
  </div>
</template>