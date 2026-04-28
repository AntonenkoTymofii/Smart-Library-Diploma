<script setup>
import { ref, onMounted } from 'vue'

// Стан додатку
const books = ref([])
const loading = ref(false)
const error = ref(null)

// Пагінація та фільтрація (зв'язуємо з бекендом!)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const searchQuery = ref('')
const isFirstPage = ref(true)
const isLastPage = ref(true)

const selectedFile = ref(null)
const uploading = ref(false)
const uploadSuccess = ref(false)
const fileInput = ref(null)

const handleFileChange = (event) => {
  selectedFile.value = event.target.files[0]
  uploadSuccess.value = false
}

const uploadFile = async () => {
  if (!selectedFile.value) return

  uploading.value = true
  const formData = new FormData()

  // 1. Передаємо сам файл
  formData.append('file', selectedFile.value)

  // 2. Передаємо назву (беремо назву файлу і відрізаємо ".pdf")
  const fileNameWithoutExt = selectedFile.value.name.replace(/\.[^/.]+$/, "")
  formData.append('title', fileNameWithoutExt)

  // 3. Передаємо тип ліцензії (тут напиши ту, яка є в твоєму Enum, наприклад 'OPEN_ACCESS' або 'PUBLIC_DOMAIN')
  formData.append('licenseType', 'OPEN_ACCESS')

  try {
    // ВАЖЛИВО: Виправили URL на правильний
    const response = await fetch('http://localhost:8080/api/v1/library/upload', {
      method: 'POST',
      body: formData,
    })

    if (!response.ok) {
      // Тепер, якщо буде помилка, ми побачимо її статус!
      throw new Error(`Помилка сервера: ${response.status}`)
    }

    uploadSuccess.value = true
    selectedFile.value = null
    if (fileInput.value) fileInput.value.value = ''

    await fetchBooks()

  } catch (err) {
    alert('Помилка при завантаженні: ' + err.message)
  } finally {
    uploading.value = false
  }
}

// Головна функція завантаження
const fetchBooks = async () => {
  loading.value = true
  error.value = null
  try {
    // Збираємо URL з параметрами
    const url = new URL('http://localhost:8080/api/v1/library/assets')
    url.searchParams.append('page', currentPage.value)
    url.searchParams.append('size', 2) // Ставимо 2 книги на сторінку, щоб одразу перевірити пагінацію!

    // Якщо ввели текст у пошук - додаємо параметр filter
    if (searchQuery.value.trim() !== '') {
      url.searchParams.append('filter', searchQuery.value.trim())
    }

    const response = await fetch(url)
    if (!response.ok) throw new Error(`Помилка сервера: ${response.status}`)

    const data = await response.json()

    // Оновлюємо дані на екрані
    books.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
    isFirstPage.value = data.first
    isLastPage.value = data.last

  } catch (err) {
    console.error(err)
    error.value = err.message
  } finally {
    loading.value = false
  }
}

// Викликається автоматично при старті
onMounted(() => fetchBooks())

// Дії кнопок
const nextPage = () => {
  if (!isLastPage.value) {
    currentPage.value++
    fetchBooks()
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    fetchBooks()
  }
}

const searchBooks = () => {
  currentPage.value = 0
  fetchBooks()
}

const downloadPdf = async (assetId, title) => {
  try {
    // Звертаємось до нашого бекенду за файлом
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${assetId}/download`);

    if (!response.ok) {
      throw new Error('Помилка при завантаженні файлу');
    }

    // Отримуємо файл як Blob (сирі байти)
    const blob = await response.blob();

    // Створюємо тимчасове посилання на цей файл у пам'яті браузера
    const downloadUrl = window.URL.createObjectURL(blob);

    // Створюємо невидимий тег <a>, щоб "клікнути" по ньому і почати скачування
    const link = document.createElement('a');
    link.href = downloadUrl;

    // Формуємо гарну назву файлу
    link.setAttribute('download', `${title}.pdf`);

    // Додаємо на сторінку, клікаємо і видаляємо
    document.body.appendChild(link);
    link.click();
    link.remove();

    // Очищаємо пам'ять
    window.URL.revokeObjectURL(downloadUrl);

  } catch (err) {
    console.error("Помилка завантаження:", err);
    alert("Не вдалося завантажити файл. Перевірте консоль.");
  }
}
</script>

<template>
  <div class="app-container">
    <header class="header">
      <h1>📚 Smart Library</h1>
      <p>Інтелектуальна система зберігання та пошуку академічних робіт</p>
    </header>

    <main class="main-content">
      <div class="search-bar">
        <input
            v-model="searchQuery"
            @keyup.enter="searchBooks"
            type="text"
            placeholder="Пошук за автором або назвою..."
            class="search-input"
        />
        <button @click="searchBooks" class="btn btn-primary">Знайти</button>
      </div>

      <div class="upload-section">
        <div class="card-upload">
          <h3>📤 Завантажити нову працю</h3>
          <div class="upload-controls">
            <input type="file" @change="handleFileChange" accept=".pdf" ref="fileInput" class="file-input-hidden" id="file-upload"/>
            <label for="file-upload" class="btn btn-outline">
              {{ selectedFile ? selectedFile.name : 'Обрати PDF файл' }}
            </label>
            <button
                @click="uploadFile"
                :disabled="!selectedFile || uploading"
                class="btn btn-primary"
            >
              {{ uploading ? '⏳ Завантаження...' : 'Завантажити в бібліотеку' }}
            </button>
          </div>
          <p v-if="uploadSuccess" style="color: green; margin-top: 10px;">✨ Файл успішно оброблено ШІ та додано!</p>
        </div>
      </div>

      <div v-if="loading" class="alert alert-info">⏳ Шукаємо документи в базі...</div>
      <div v-else-if="error" class="alert alert-error">❌ Помилка: {{ error }}</div>
      <div v-else-if="books.length === 0" class="alert alert-warning">📭 За вашим запитом нічого не знайдено.</div>

      <div v-else class="book-grid">
        <div v-for="book in books" :key="book.id" class="book-card">
          <div class="card-header">
            <span class="badge">PDF</span>
            <h2 class="book-title">{{ book.title }}</h2>
          </div>

          <div class="card-body">
            <p class="author">👨‍💻 <strong>Автор:</strong> {{ book.authors.join(', ') }}</p>
            <p class="summary">{{ book.summary }}</p>
          </div>

          <div class="card-footer">
            <div class="meta-info">
              <span v-if="book.publicationYear" class="year-badge">
                📅 {{ book.publicationYear }} рік
              </span>
              <span v-else class="year-badge unknown">
                📅 Рік не вказано
              </span>
            </div>
            <button
                @click="downloadPdf(book.id, book.title)"
                class="btn btn-download">
              📥 Скачати PDF
            </button>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button
            @click="prevPage"
            :disabled="currentPage === 0"
            class="btn btn-pagination"
        >
          ⬅ Попередня
        </button>

        <span class="page-info">
          Сторінка {{ currentPage + 1 }} з {{ totalPages }} <br>
          <small>(Всього документів: {{ totalElements }})</small>
        </span>

        <button
            @click="nextPage"
            :disabled="isLastPage"
            class="btn btn-pagination"
        >
          Наступна ➡
        </button>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Тут магія CSS, яка робить додаток красивим без зайвих бібліотек */
.app-container {
  font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background-color: #f4f7f6;
  min-height: 100vh;
  color: #333;
}

.header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 2rem 1rem;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.header h1 { margin: 0 0 0.5rem 0; font-size: 2.5rem; }
.header p { margin: 0; opacity: 0.8; }

.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

/* Пошук */
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 2rem;
}

.upload-section {
  margin-bottom: 2rem;
}
.card-upload {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  border: 2px dashed #2a5298;
  text-align: center;
}
.upload-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 1rem;
}
.file-input-hidden {
  display: none;
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.05);
  transition: border-color 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #2a5298;
}

/* Кнопки */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
}

.btn-primary {
  background-color: #2a5298;
  color: white;
}

.btn-primary:hover { background-color: #1e3c72; }

.btn-outline {
  background-color: transparent;
  color: #2a5298;
  border: 1px solid #2a5298;
}

.btn-pagination {
  background-color: #fff;
  border: 1px solid #ddd;
  color: #333;
}

.btn-pagination:disabled {
  background-color: #f4f4f4;
  color: #aaa;
  cursor: not-allowed;
}

.btn-pagination:not(:disabled):hover {
  background-color: #e9ecef;
}

/* Картки книг */
.book-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.book-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  overflow: hidden;
  transition: transform 0.2s;
}

.book-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}

.card-header {
  padding: 1.5rem 1.5rem 0.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.badge {
  background-color: #e8f0fe;
  color: #1a73e8;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
  margin-bottom: 8px;
  display: inline-block;
}

.book-title {
  margin: 0;
  font-size: 1.4rem;
  color: #2c3e50;
}

.card-body {
  padding: 1rem 1.5rem;
}

.author {
  margin-top: 0;
  color: #555;
}

.summary {
  color: #666;
  line-height: 1.6;
  font-size: 0.95rem;
}

.card-footer {
  padding: 1rem 1.5rem;
  background-color: #f8f9fa;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.year-badge {
  background-color: #e3f2fd;
  color: #1976d2;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
}

.year-badge.unknown {
  background-color: #f5f5f5;
  color: #9e9e9e;
}

.btn-download {
  background-color: #2a5298;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  transition: background 0.2s;
}

.btn-download:hover {
  background-color: #1e3c72;
}

/* Пагінація */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.page-info {
  text-align: center;
  font-weight: 500;
}

/* Сповіщення */
.alert {
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
}

.alert-info { background-color: #e8f4fd; color: #0277bd; }
.alert-error { background-color: #fde8e8; color: #c62828; }
.alert-warning { background-color: #fff8e1; color: #f57f17; }
</style>