<script setup>
import { ref, onMounted } from 'vue'

const books = ref([])
const loading = ref(true)
const error = ref(null)

const fetchBooks = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/library/assets')

    if (!response.ok) {
      throw new Error(`Помилка сервера: ${response.status}`)
    }

    const data = await response.json()

    books.value = data.content

  } catch (err) {
    console.error(err)
    error.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBooks()
})
</script>

<template>
  <main style="font-family: sans-serif; padding: 20px; max-width: 800px; margin: 0 auto;">
    <h1>Моя Smart Бібліотека</h1>

    <div v-if="loading" style="color: blue;">
      Підключаємось до бази даних...
    </div>

    <div v-else-if="error" style="color: red; padding: 20px; border: 1px solid red; border-radius: 8px;">
      <b>Помилка:</b> {{ error }} <br><br>
      <i>Перевір, чи запущений Spring Boot і чи налаштований CORS!</i>
    </div>

    <div v-else>
      <p style="color: green;">Успішно завантажено книг: {{ books.length }}</p>

      <div
          v-for="book in books"
          :key="book.id"
          style="border: 1px solid #ccc; padding: 15px; margin-bottom: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);"
      >
        <h2 style="margin-top: 0; color: #333;">{{ book.title }}</h2>
        <p><strong>Автори:</strong> {{ book.authors.join(', ') }}</p>
        <p style="font-size: 0.95em; color: #555; line-height: 1.5;">
          {{ book.summary }}
        </p>
        <p style="font-size: 0.8em; color: gray;">ID: {{ book.id }}</p>
      </div>

    </div>
  </main>
</template>