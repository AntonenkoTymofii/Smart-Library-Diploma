<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'

const route = useRoute()
const router = useRouter()
const book = ref(null)
const loading = ref(true)
const error = ref(null)

// Для режиму редагування
const isEditing = ref(false)
const editData = ref({ title: '', summary: '', publicationYear: null })

// Перевірка ролі (тільки ADMIN/MODERATOR може редагувати)
const token = localStorage.getItem('jwt')
const userRole = ref('GUEST')
if (token) {
  try { userRole.value = jwtDecode(token).role } catch (e) {}
}
const canEdit = computed(() => userRole.value === 'ADMIN' || userRole.value === 'MODERATOR')

const fetchBookDetails = async () => {
  loading.value = true
  try {
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${route.params.id}`, {
      headers: token ? { 'Authorization': `Bearer ${token}` } : {}
    })
    if (!response.ok) throw new Error('Книгу не знайдено')
    book.value = await response.json()
    // Заповнюємо дані для форми редагування
    editData.value = { ...book.value }
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

const saveChanges = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/v1/library/assets/${book.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(editData.value)
    })
    if (!response.ok) throw new Error('Помилка при збереженні')
    book.value = await response.json()
    isEditing.value = false
    alert('Дані успішно оновлено!')
  } catch (err) {
    alert(err.message)
  }
}

onMounted(fetchBookDetails)
</script>

<template>
  <div class="detail-container">
    <button @click="router.back()" class="btn-back">⬅ Назад до каталогу</button>

    <div v-if="loading" class="loader">⏳ Завантаження інформації...</div>
    <div v-else-if="error" class="error-box">❌ {{ error }}</div>

    <div v-else-if="book" class="book-detail-card">
      <div class="detail-header">
        <div v-if="!isEditing">
          <h1>{{ book.title }}</h1>
          <p class="author-tag">👨‍💻 {{ book.authors.join(', ') }}</p>
        </div>
        <div v-else class="edit-fields">
          <input v-model="editData.title" class="edit-input title-edit" placeholder="Назва книги" />
          <input v-model="editData.publicationYear" type="number" class="edit-input" placeholder="Рік видання" />
        </div>

        <button v-if="canEdit && !isEditing" @click="isEditing = true" class="btn btn-edit">📝 Редагувати</button>
      </div>

      <div class="detail-body">
        <h3>📄 Анотація від ШІ</h3>
        <p v-if="!isEditing" class="summary-text">{{ book.summary }}</p>
        <textarea v-else v-model="editData.summary" rows="8" class="edit-input textarea-edit"></textarea>

        <div class="meta-grid">
          <div class="meta-item">
            <strong>📅 Рік:</strong> {{ book.publicationYear || 'Не вказано' }}
          </div>
          <div class="meta-item">
            <strong>⚖️ Ліцензія:</strong> {{ book.licenseType }}
          </div>
        </div>
      </div>

      <div class="detail-footer">
        <div v-if="isEditing" class="edit-actions">
          <button @click="saveChanges" class="btn btn-save">✅ Зберегти</button>
          <button @click="isEditing = false" class="btn btn-cancel">Скасувати</button>
        </div>
        <a v-else :href="'http://localhost:8080/api/v1/library/assets/' + book.id + '/download'"
           class="btn btn-download" target="_blank">📥 Завантажити PDF</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-container { max-width: 800px; margin: 2rem auto; padding: 0 1rem; }
.btn-back { background: none; border: none; color: #2a5298; cursor: pointer; font-weight: 600; margin-bottom: 1rem; }
.book-detail-card { background: white; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); overflow: hidden; }
.detail-header { padding: 2rem; background: #f8f9fa; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: flex-start; }
.detail-header h1 { margin: 0; color: #2c3e50; font-size: 2rem; }
.author-tag { color: #666; font-size: 1.1rem; margin-top: 0.5rem; }
.detail-body { padding: 2rem; }
.summary-text { line-height: 1.8; color: #444; font-size: 1.05rem; white-space: pre-wrap; }
.meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 2rem; padding-top: 2rem; border-top: 1px solid #eee; }
.detail-footer { padding: 1.5rem 2rem; background: #f8f9fa; text-align: right; }
.btn { padding: 12px 25px; border-radius: 8px; font-weight: 600; cursor: pointer; border: none; }
.btn-edit { background: #e3f2fd; color: #1976d2; }
.btn-save { background: #2e7d32; color: white; }
.btn-cancel { background: #f5f5f5; color: #666; margin-left: 10px; }
.btn-download { background: #2a5298; color: white; text-decoration: none; display: inline-block; }
.edit-input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; margin-bottom: 10px; font-family: inherit; }
.title-edit { font-size: 1.5rem; font-weight: bold; }
.textarea-edit { resize: vertical; }
</style>