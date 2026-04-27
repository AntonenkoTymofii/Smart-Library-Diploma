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
  if (!isFirstPage.value) {
    currentPage.value--
    fetchBooks()
  }
}

const searchBooks = () => {
  currentPage.value = 0 // При новому пошуку завжди повертаємось на 1 сторінку
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
            <span class="id-hint">ID: {{ book.id.substring(0, 8) }}...</span>
            <button
                @click="downloadPdf(book.id, book.title)"
                class="btn btn-outline"
            >
              📥 Завантажити PDF
            </button>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button
            @click="prevPage"
            :disabled="isFirstPage"
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
  background-color: #fcfcfc;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.id-hint {
  font-size: 0.8rem;
  color: #aaa;
  font-family: monospace;
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