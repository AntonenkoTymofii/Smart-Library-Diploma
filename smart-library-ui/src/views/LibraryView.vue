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

const useSemanticSearch = ref(false)
const isSemanticSearchActive = ref(false)

const token = localStorage.getItem('jwt')
const isGuest = computed(() => !token)

const getHeaders = () => {
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

const fetchBooks = async () => {
  loading.value = true
  error.value = null
  try {
    if (searchQuery.value.trim() !== '' && useSemanticSearch.value) {
      isSemanticSearchActive.value = true;
      const url = new URL('http://localhost:8080/api/v1/library/assets/search')
      url.searchParams.append('q', searchQuery.value.trim())

      const response = await fetch(url, { headers: getHeaders() })
      if (!response.ok) throw new Error(`Помилка сервера: ${response.status}`)

      const data = await response.json()
      books.value = data
      totalPages.value = 1
      totalElements.value = data.length
    } else {
      isSemanticSearchActive.value = false;
      const url = new URL('http://localhost:8080/api/v1/library/assets')
      url.searchParams.append('page', currentPage.value)
      url.searchParams.append('size', 10)

      if (searchQuery.value.trim() !== '') {
        url.searchParams.append('filter', searchQuery.value.trim())
      }

      const response = await fetch(url, { headers: getHeaders() })
      if (!response.ok) throw new Error(`Помилка сервера: ${response.status}`)

      const data = await response.json()
      books.value = data.content
      totalPages.value = data.totalPages
      totalElements.value = data.totalElements
    }
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

const downloadPdf = async (assetId, title) => {
  try {
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${assetId}/download`, { headers: getHeaders() });
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
  <div class="w-full">
    <header class="bg-white border-b border-slate-200 sticky top-0 z-10 px-6 md:px-10 py-6 shadow-sm">
      <div class="max-w-5xl mx-auto">
        <h2 class="text-2xl font-bold text-slate-900 mb-4">Каталог та Пошук</h2>

        <!-- Блок пошуку з тумблером -->
        <div class="bg-slate-50 rounded-2xl border border-slate-200 p-2 shadow-sm focus-within:ring-2 focus-within:ring-rose-100 focus-within:border-rose-400 transition-all">
          <div class="relative flex items-center w-full h-12">
            <div class="grid place-items-center h-full w-14" :class="useSemanticSearch ? 'text-rose-700' : 'text-slate-400'">
              <i class="fas" :class="useSemanticSearch ? 'fa-brain' : 'fa-search'"></i>
            </div>
            <input v-model="searchQuery" @keyup.enter="searchBooks" class="peer h-full w-full outline-none text-base text-slate-700 pr-4 bg-transparent" type="text" placeholder="Введіть назву, автора або запитайте за змістом..." />
            <button @click="searchBooks" class="bg-slate-900 hover:bg-slate-800 text-white h-full px-8 rounded-xl font-medium transition-colors">
              Знайти
            </button>
          </div>

          <!-- Сам тумблер -->
          <div class="flex items-center px-4 pb-2 mt-2 border-t border-slate-200 pt-3">
            <label class="flex items-center cursor-pointer">
              <div class="relative">
                <input type="checkbox" v-model="useSemanticSearch" @change="searchBooks" class="sr-only">
                <div class="block w-10 h-6 rounded-full transition-colors" :class="useSemanticSearch ? 'bg-rose-700' : 'bg-slate-300'"></div>
                <div class="dot absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform" :class="useSemanticSearch ? 'transform translate-x-4' : ''"></div>
              </div>
              <span class="ml-3 text-sm font-medium transition-colors" :class="useSemanticSearch ? 'text-rose-700' : 'text-slate-500'">
                ШІ-пошук за змістом (векторний пошук pgvector)
              </span>
            </label>
          </div>
        </div>

      </div>
    </header>

    <div class="p-6 md:p-10 max-w-5xl mx-auto">
      <div v-if="loading" class="text-center py-10 text-slate-500"><i class="fas fa-spinner fa-spin text-2xl mb-2"></i><br>Завантаження...</div>
      <div v-else-if="error" class="bg-red-50 text-red-700 p-4 rounded-xl border border-red-100">{{ error }}</div>
      <div v-else-if="books.length === 0" class="text-center py-10 text-slate-500">Книг не знайдено.</div>

      <div v-else class="grid grid-cols-1 gap-6">
        <div v-for="book in books" :key="book.id" class="bg-white rounded-2xl p-6 shadow-sm border border-slate-200 hover:shadow-md transition-shadow relative overflow-hidden">
          <div v-if="book.licenseType === 'RESTRICTED'" class="absolute right-0 top-0 w-32 h-32 bg-amber-50 rounded-bl-full -z-10"></div>

          <div class="flex flex-col md:flex-row gap-6">
            <div class="w-full md:w-32 h-40 bg-slate-50 rounded-xl flex items-center justify-center text-slate-300 shrink-0 border border-slate-200">
              <i class="fas fa-file-pdf text-4xl"></i>
            </div>

            <div class="flex-1">
              <div class="flex justify-between items-start mb-2">
                <span v-if="book.licenseType === 'OPEN_ACCESS'" class="bg-emerald-50 text-emerald-700 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wide border border-emerald-100">Відкритий доступ</span>
                <span v-else class="bg-amber-50 text-amber-700 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wide border border-amber-100"><i class="fas fa-lock mr-1"></i> Захищено</span>
              </div>

              <router-link :to="'/asset/' + book.id" class="hover:underline decoration-rose-700 underline-offset-4">
                <h4 class="text-xl font-bold text-slate-900 mb-1">{{ book.title }}</h4>
              </router-link>

              <p class="text-sm text-slate-500 mb-3">
                <i class="fas fa-user mr-1 text-slate-400"></i> {{ book.authors ? book.authors.join(', ') : 'Автор невідомий' }}
                <span v-if="book.publicationYear" class="ml-2 pl-2 border-l border-slate-300"><i class="fas fa-calendar-alt mr-1 text-slate-400"></i> {{ book.publicationYear }}</span>
              </p>

              <div class="bg-rose-50/30 p-4 rounded-xl border border-rose-100/50 mb-4">
                <p class="text-sm text-slate-700 line-clamp-2">
                  <i class="fas fa-robot text-rose-700 mr-2" title="AI Згенеровано"></i>
                  <strong>AI Анотація:</strong> {{ book.summary || 'Анотація відсутня' }}
                </p>
              </div>

              <div class="flex space-x-3">
                <router-link :to="'/asset/' + book.id" class="bg-rose-50 text-rose-800 px-5 py-2 rounded-lg font-medium hover:bg-rose-100 transition-colors border border-rose-100">
                  <i class="fas fa-eye mr-2"></i>Деталі та Чат
                </router-link>
                <button v-if="!isGuest" @click="downloadPdf(book.id, book.title)" class="bg-slate-100 text-slate-700 px-5 py-2 rounded-lg font-medium hover:bg-slate-200 transition-colors">
                  <i class="fas fa-download mr-2"></i>Завантажити
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1 && !isSemanticSearchActive" class="flex justify-between items-center mt-10 p-4 bg-white border border-slate-200 rounded-xl shadow-sm">
        <button @click="prevPage" :disabled="currentPage === 0" class="px-4 py-2 bg-slate-50 rounded-lg text-slate-700 disabled:opacity-50 hover:bg-slate-100 font-medium transition-colors">⬅ Назад</button>
        <span class="text-slate-600 font-medium">Сторінка {{ currentPage + 1 }} з {{ totalPages }}</span>
        <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="px-4 py-2 bg-slate-50 rounded-lg text-slate-700 disabled:opacity-50 hover:bg-slate-100 font-medium transition-colors">Далі ➡</button>
      </div>
    </div>
  </div>
</template>