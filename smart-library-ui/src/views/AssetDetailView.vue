<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'

const route = useRoute()
const router = useRouter()
const book = ref(null)
const loading = ref(true)
const error = ref(null)

const isEditing = ref(false)
const isChatOpen = ref(false)
const authorInputString = ref('')
const editData = ref({ title: '', summary: '', publicationYear: null, licenseType: 'OPEN_ACCESS' })

const chatQuery = ref('')
const chatMessages = ref([
  { id: 1, role: 'bot', text: 'Привіт! Я AI-асистент. Про що б ви хотіли дізнатися з цієї книги? Задавайте питання, і я знайду відповідь у тексті.' }
])
const isAiTyping = ref(false)
const chatWindow = ref(null)

const token = localStorage.getItem('jwt')
const userRole = ref('GUEST')
if (token) {
  try { userRole.value = jwtDecode(token).role } catch (e) {}
}
const canEdit = computed(() => userRole.value === 'ADMIN' || userRole.value === 'MODERATOR')
const isAdmin = computed(() => userRole.value === 'ADMIN')

const getHeaders = () => {
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

const fetchBookDetails = async () => {
  loading.value = true
  try {
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${route.params.id}`, {
      headers: getHeaders()
    })
    if (!response.ok) throw new Error('Книгу не знайдено')
    book.value = await response.json()
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

const openEdit = () => {
  editData.value = { ...book.value }
  authorInputString.value = book.value.authors ? book.value.authors.join(', ') : ''
  isEditing.value = true
}

const saveChanges = async () => {
  try {
    editData.value.authors = authorInputString.value.split(',').map(a => a.trim()).filter(a => a)

    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${book.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...getHeaders()
      },
      body: JSON.stringify(editData.value)
    })
    if (!response.ok) throw new Error('Помилка при збереженні')
    book.value = await response.json()
    isEditing.value = false
    alert('Дані успішно оновлено (JSON -> MARC 21)!')
  } catch (err) {
    alert(err.message)
  }
}

const deleteBook = async () => {
  if (!confirm('Ви впевнені, що хочете назавжди видалити цю книгу?')) return;
  try {
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${book.value.id}`, {
      method: 'DELETE',
      headers: getHeaders()
    });
    if (!response.ok) throw new Error('Помилка при видаленні');
    alert('Книгу успішно видалено');
    router.push('/catalog');
  } catch (err) {
    alert(err.message);
  }
}

const sendMessage = async () => {
  if (!chatQuery.value.trim() || isAiTyping.value) return;

  const userQuestion = chatQuery.value.trim();
  chatQuery.value = '';

  chatMessages.value.push({ id: Date.now(), role: 'user', text: userQuestion });
  scrollToBottom();

  isAiTyping.value = true;

  try {
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${book.value.id}/chat?query=${encodeURIComponent(userQuestion)}`, {
      method: 'GET',
      headers: getHeaders()
    });

    if (!response.ok) throw new Error("Помилка сервера");

    const answer = await response.text();
    chatMessages.value.push({ id: Date.now(), role: 'bot', text: answer });

  } catch (err) {
    chatMessages.value.push({ id: Date.now(), role: 'bot', text: "Вибачте, сталася помилка з'єднання з ШІ." });
  } finally {
    isAiTyping.value = false;
    scrollToBottom();
  }
}

const scrollToBottom = async () => {
  await nextTick();
  if (chatWindow.value) chatWindow.value.scrollTop = chatWindow.value.scrollHeight;
}

onMounted(fetchBookDetails)
</script>

<template>
  <div class="p-6 md:p-10 max-w-4xl mx-auto w-full relative">
    <button @click="router.push('/catalog')" class="flex items-center text-slate-800 hover:text-rose-700 font-medium mb-6 transition-colors">
      <i class="fas fa-arrow-left mr-2"></i> Назад до каталогу
    </button>

    <div v-if="loading" class="text-center py-10"><i class="fas fa-spinner fa-spin text-2xl text-rose-700"></i></div>
    <div v-else-if="error" class="bg-red-50 text-red-700 p-4 rounded-xl">{{ error }}</div>

    <div v-else-if="book" class="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
      <div class="p-8 bg-slate-50 border-b border-slate-100 flex justify-between items-start">
        <div>
          <span v-if="book.licenseType === 'OPEN_ACCESS'" class="bg-emerald-100 text-emerald-800 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wide mb-3 inline-block">Відкритий доступ</span>
          <span v-else class="bg-amber-100 text-amber-800 text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wide mb-3 inline-block"><i class="fas fa-lock mr-1"></i> Захищено</span>
          <h1 class="text-3xl font-bold text-slate-900 mb-2">{{ book.title }}</h1>
          <p class="text-slate-500 text-lg"><i class="fas fa-user-edit mr-2"></i>{{ book.authors ? book.authors.join(', ') : 'Автор невідомий' }}</p>
        </div>
        <div class="flex space-x-2">
          <button v-if="canEdit" @click="openEdit" class="w-10 h-10 rounded-full bg-white border border-slate-200 text-slate-500 hover:text-rose-700 hover:border-rose-300 flex items-center justify-center transition-colors shadow-sm" title="Редагувати">
            <i class="fas fa-edit"></i>
          </button>
          <button v-if="isAdmin" @click="deleteBook" class="w-10 h-10 rounded-full bg-white border border-slate-200 text-slate-500 hover:text-red-700 hover:border-red-300 flex items-center justify-center transition-colors shadow-sm" title="Видалити">
            <i class="fas fa-trash-alt"></i>
          </button>
        </div>
      </div>

      <div class="p-8">
        <h3 class="text-lg font-bold text-slate-900 mb-3 flex items-center">
          <i class="fas fa-robot text-rose-700 mr-2"></i> AI Анотація (Згенеровано LLM)
        </h3>
        <p class="text-slate-600 leading-relaxed mb-8 whitespace-pre-wrap">{{ book.summary || 'Анотацію ще не згенеровано.' }}</p>

        <div class="grid grid-cols-2 gap-4 border-t border-slate-100 pt-6">
          <div>
            <p class="text-sm text-slate-400 font-medium">Рік видання</p>
            <p class="font-semibold text-slate-900">{{ book.publicationYear || 'Не вказано' }}</p>
          </div>
          <div>
            <p class="text-sm text-slate-400 font-medium">ID у базі (MARC)</p>
            <p class="font-semibold text-slate-900 text-sm font-mono truncate" :title="book.id">{{ book.id }}</p>
          </div>
        </div>
      </div>

      <div class="p-6 bg-slate-50 border-t border-slate-100 flex flex-wrap gap-4 justify-end">
        <button @click="isChatOpen = true" class="flex items-center bg-rose-100 text-rose-800 px-6 py-3 rounded-xl font-bold hover:bg-rose-200 transition-colors shadow-sm">
          <i class="fas fa-comment-sparkles mr-2"></i>Запитати у AI Library Chat
        </button>
        <a v-if="token" :href="'http://localhost:8080/api/v1/library/assets/' + book.id + '/download'" target="_blank" class="flex items-center bg-slate-900 text-white px-6 py-3 rounded-xl font-bold hover:bg-slate-800 transition-colors shadow-md">
          <i class="fas fa-download mr-2"></i>Завантажити PDF
        </a>
        <p v-else class="text-sm text-slate-500 py-3">Потрібна <router-link to="/login" class="text-rose-700 underline">авторизація</router-link> для скачування</p>
      </div>
    </div>

    <div v-if="isEditing" class="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg overflow-hidden">
        <div class="p-6 border-b border-slate-100 flex justify-between items-center">
          <h3 class="text-xl font-bold text-slate-900">Редагування метаданих</h3>
          <button @click="isEditing = false" class="text-slate-400 hover:text-slate-600"><i class="fas fa-times text-xl"></i></button>
        </div>
        <div class="p-6 space-y-4">
          <div class="bg-rose-50 text-rose-800 text-sm p-3 rounded-lg flex items-start">
            <i class="fas fa-info-circle mt-1 mr-2"></i>
            Дані були автоматично згенеровані ШІ. Ви можете виправити неточності.
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Назва книги</label>
            <input v-model="editData.title" type="text" class="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-rose-600 outline-none">
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Автор(и) (через кому) <span class="text-rose-600">*</span></label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400"><i class="fas fa-user-edit"></i></div>
              <input v-model="authorInputString" type="text" class="w-full bg-slate-50 border border-slate-200 rounded-lg pl-10 pr-4 py-2 focus:ring-2 focus:ring-rose-600 outline-none font-medium text-slate-900">
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Рік видання</label>
              <input v-model="editData.publicationYear" type="number" class="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-rose-600 outline-none">
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Ліцензія</label>
              <select v-model="editData.licenseType" class="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-rose-600 outline-none">
                <option value="OPEN_ACCESS">Відкритий доступ</option>
                <option value="INSTITUTIONAL">Локальна мережа</option>
                <option value="RESTRICTED">Обмежений доступ</option>
              </select>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Анотація</label>
            <textarea v-model="editData.summary" rows="4" class="w-full bg-slate-50 border border-slate-200 rounded-lg px-4 py-2 focus:ring-2 focus:ring-rose-600 outline-none"></textarea>
          </div>
        </div>
        <div class="p-6 bg-slate-50 border-t border-slate-100 flex justify-end space-x-3">
          <button @click="isEditing = false" class="px-5 py-2 rounded-lg font-medium text-slate-600 hover:bg-slate-200">Скасувати</button>
          <button @click="saveChanges" class="px-5 py-2 bg-rose-700 text-white rounded-lg font-medium hover:bg-rose-800 shadow-sm">Зберегти</button>
        </div>
      </div>
    </div>

    <div :class="isChatOpen ? 'translate-x-0 shadow-2xl' : 'translate-x-full'" class="fixed right-0 top-0 h-full w-full md:w-96 bg-white/95 backdrop-blur-md transform transition-transform duration-300 z-50 flex flex-col border-l border-slate-200">
      <div class="p-4 border-b border-slate-200 flex justify-between items-center bg-white">
        <div class="flex items-center space-x-2">
          <i class="fas fa-robot text-rose-700 text-xl"></i>
          <h3 class="font-bold text-slate-900">AI Library Chat</h3>
        </div>
        <button @click="isChatOpen = false" class="text-slate-400 hover:text-slate-600 bg-slate-100 hover:bg-slate-200 w-8 h-8 rounded-full flex items-center justify-center transition-colors">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <div ref="chatWindow" class="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/50 scroll-smooth">
        <div class="text-xs text-center text-slate-400 font-medium uppercase tracking-wider mb-4">Контекст: "{{ book?.title }}"</div>

        <div v-for="msg in chatMessages" :key="msg.id" :class="msg.role === 'bot' ? 'flex items-start space-x-3' : 'flex items-start space-x-3 flex-row-reverse space-x-reverse'">
          <div :class="msg.role === 'bot' ? 'bg-gradient-to-br from-slate-800 to-rose-800' : 'bg-slate-900'" class="w-8 h-8 rounded-full flex items-center justify-center text-white shrink-0 shadow-sm">
            <i :class="msg.role === 'bot' ? 'fas fa-robot' : 'fas fa-user'" class="text-xs"></i>
          </div>
          <div :class="msg.role === 'bot' ? 'bg-white rounded-tl-none text-slate-700 border-slate-100' : 'bg-slate-800 rounded-tr-none text-white border-transparent'" class="p-3 rounded-2xl shadow-sm text-sm border">
            {{ msg.text }}
          </div>
        </div>

        <div v-if="isAiTyping" class="flex items-start space-x-3">
          <div class="w-8 h-8 rounded-full bg-gradient-to-br from-slate-800 to-rose-800 flex items-center justify-center text-white shrink-0 shadow-sm">
            <i class="fas fa-robot text-xs"></i>
          </div>
          <div class="bg-white p-3 rounded-2xl rounded-tl-none shadow-sm text-sm text-slate-700 border border-slate-100 flex space-x-1 items-center h-10">
            <div class="w-2 h-2 bg-slate-400 rounded-full animate-bounce"></div>
            <div class="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
            <div class="w-2 h-2 bg-slate-400 rounded-full animate-bounce" style="animation-delay: 0.4s"></div>
          </div>
        </div>
      </div>

      <div class="p-4 bg-white border-t border-slate-200">
        <div class="relative flex items-center">
          <input v-model="chatQuery" @keyup.enter="sendMessage" :disabled="isAiTyping" type="text" class="w-full bg-slate-100 rounded-xl pl-4 pr-12 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-rose-300 focus:bg-white transition-all border border-transparent focus:border-rose-300 disabled:opacity-50" placeholder="Запитайте у книги...">
          <button @click="sendMessage" :disabled="isAiTyping" class="absolute right-2 w-8 h-8 bg-rose-700 text-white rounded-lg flex items-center justify-center hover:bg-rose-800 transition-colors shadow-sm disabled:bg-slate-400">
            <i class="fas fa-arrow-up text-xs"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>