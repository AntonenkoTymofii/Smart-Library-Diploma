<script setup>
import { ref, onMounted, computed } from 'vue'
import { jwtDecode } from 'jwt-decode'

const books = ref([])
const loading = ref(false)
const error = ref(null)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const searchQuery = ref('')

const selectedFile = ref(null)
const uploading = ref(false)
const uploadSuccess = ref(false)
const fileInput = ref(null)

const userRole = ref('GUEST')
const token = localStorage.getItem('jwt')
const isGuest = computed(() => !token)

if (token) {
  try {
    const decoded = jwtDecode(token)
    userRole.value = decoded.role || 'READER'
  } catch (e) {
    console.error("Помилка розшифровки токена", e)
  }
}

const canUpload = computed(() => {
  return userRole.value === 'ADMIN' || userRole.value === 'MODERATOR'
})

const getHeaders = () => {
  const currentToken = localStorage.getItem('jwt')
  return currentToken ? { 'Authorization': `Bearer ${currentToken}` } : {}
}

const fetchBooks = async () => {
  loading.value = true
  error.value = null
  try {
    const url = new URL('http://localhost:8080/api/v1/library/assets')
    url.searchParams.append('page', currentPage.value)
    url.searchParams.append('size', 5)
    if (searchQuery.value.trim() !== '') url.searchParams.append('filter', searchQuery.value.trim())

    const response = await fetch(url, { headers: getHeaders() })
    if (!response.ok) throw new Error(`Помилка сервера: ${response.status}`)

    const data = await response.json()
    books.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchBooks())

const searchBooks = () => { currentPage.value = 0; fetchBooks() }
const nextPage = () => { if (currentPage.value < totalPages.value - 1) { currentPage.value++; fetchBooks() } }
const prevPage = () => { if (currentPage.value > 0) { currentPage.value--; fetchBooks() } }

const handleFileChange = (e) => { selectedFile.value = e.target.files[0]; uploadSuccess.value = false }

const uploadFile = async () => {
  if (!selectedFile.value) return
  uploading.value = true
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('title', selectedFile.value.name.replace(/\.[^/.]+$/, ""))
  formData.append('licenseType', 'OPEN_ACCESS')

  try {
    const response = await fetch('http://localhost:8080/api/v1/library/upload', {
      method: 'POST',
      headers: getHeaders(),
      body: formData,
    })
    if (!response.ok) throw new Error(`Помилка завантаження: ${response.status}`)

    uploadSuccess.value = true
    selectedFile.value = null
    if (fileInput.value) fileInput.value.value = ''
    await fetchBooks()
  } catch (err) {
    alert(err.message)
  } finally {
    uploading.value = false
  }
}

const downloadPdf = async (assetId, title) => {
  try {
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${assetId}/download`, {
      headers: getHeaders()
    });
    if (!response.ok) throw new Error('Помилка при завантаженні');
    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', `${title}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(downloadUrl);
  } catch (err) {
    alert("Не вдалося завантажити файл.");
  }
}
</script>

<template>
  <div class="library-container">
    <div class="search-bar">
      <input v-model="searchQuery" @keyup.enter="searchBooks" type="text" placeholder="Пошук..." class="search-input"/>
      <button @click="searchBooks" class="btn btn-primary">Знайти</button>
    </div>

    <div v-if="canUpload" class="upload-section">
      <div class="card-upload">
        <h3>📤 Завантажити нову працю (Доступно для: {{ userRole }})</h3>
        <div class="upload-controls">
          <input type="file" @change="handleFileChange" accept=".pdf" ref="fileInput" class="file-input-hidden" id="file-upload"/>
          <label for="file-upload" class="btn btn-outline">
            {{ selectedFile ? selectedFile.name : 'Обрати PDF файл' }}
          </label>
          <button @click="uploadFile" :disabled="!selectedFile || uploading" class="btn btn-primary">
            {{ uploading ? '⏳ Завантаження...' : 'Завантажити' }}
          </button>
        </div>
        <p v-if="uploadSuccess" style="color: green;">✨ Успішно додано!</p>
      </div>
    </div>

    <div v-if="loading" class="alert alert-info">⏳ Завантаження...</div>
    <div v-else-if="error" class="alert alert-error">❌ {{ error }}</div>
    <div v-else class="book-grid">
      <div v-for="book in books" :key="book.id" class="book-card">
        <div class="card-header">
          <h2 class="book-title">{{ book.title }}</h2>
        </div>
        <div class="card-body">
          <p class="author">👨‍💻 <strong>Автор:</strong> {{ book.authors.join(', ') }}</p>
          <p class="summary">{{ book.summary }}</p>
        </div>
        <div class="card-footer">
          <span v-if="book.publicationYear" class="year-badge">📅 {{ book.publicationYear }} рік</span>
          <button
              v-if="!isGuest"
              @click="downloadPdf(book.id, book.title)"
              class="btn btn-download"
          >
            📥 Скачати
          </button>
          <p v-else style="font-size: 0.8rem; color: #999;">🔒 Увійдіть, щоб скачати</p>
        </div>
      </div>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button @click="prevPage" :disabled="currentPage === 0" class="btn btn-pagination">⬅ Назад</button>
      <span>Сторінка {{ currentPage + 1 }} з {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="btn btn-pagination">Далі ➡</button>
    </div>
  </div>
</template>

<style scoped>
.library-container { max-width: 900px; margin: 0 auto; padding: 2rem 1rem; }
.search-bar { display: flex; gap: 10px; margin-bottom: 2rem; }
.search-input { flex: 1; padding: 12px 20px; font-size: 1rem; border: 1px solid #ddd; border-radius: 8px; }
.btn { padding: 10px 20px; border: none; border-radius: 8px; font-size: 1rem; cursor: pointer; font-weight: 600; }
.btn-primary { background-color: #2a5298; color: white; }
.btn-outline { background-color: transparent; color: #2a5298; border: 1px solid #2a5298; }
.upload-section { margin-bottom: 2rem; }
.card-upload { background: white; padding: 1.5rem; border-radius: 12px; border: 2px dashed #2a5298; text-align: center; }
.upload-controls { display: flex; justify-content: center; align-items: center; gap: 15px; margin-top: 1rem; }
.file-input-hidden { display: none; }
.book-grid { display: flex; flex-direction: column; gap: 1.5rem; }
.book-card { background: white; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.card-header { padding: 1.5rem 1.5rem 0.5rem; border-bottom: 1px solid #f0f0f0; }
.book-title { margin: 0; font-size: 1.4rem; color: #2c3e50; }
.card-body { padding: 1rem 1.5rem; }
.summary { color: #666; line-height: 1.6; }
.card-footer { padding: 1rem 1.5rem; background-color: #f8f9fa; border-top: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.year-badge { background-color: #e3f2fd; color: #1976d2; padding: 6px 12px; border-radius: 20px; font-weight: 600; }
.btn-download { background-color: #2a5298; color: white; }
.pagination { display: flex; justify-content: space-between; align-items: center; margin-top: 2rem; padding: 1rem; background: white; border-radius: 8px; }
.btn-pagination { background-color: #fff; border: 1px solid #ddd; }
.btn-pagination:disabled { opacity: 0.5; cursor: not-allowed; }
</style>