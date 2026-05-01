<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const authData = ref({ email: '', password: '' })
const authError = ref(null)
const loading = ref(false)
const isLoginMode = ref(true)

const authenticate = async () => {
  authError.value = null
  loading.value = true

  const url = isLoginMode.value
      ? 'http://localhost:8080/api/v1/auth/authenticate'
      : 'http://localhost:8080/api/v1/auth/register'

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(authData.value)
    })

    if (!response.ok) throw new Error('Невірний email або пароль (або користувач вже існує)')

    const data = await response.json()

    localStorage.setItem('jwt', data.token)
    router.push('/')

  } catch (err) {
    authError.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>{{ isLoginMode ? 'Вхід у систему' : 'Реєстрація' }}</h2>

      <div v-if="authError" class="alert alert-error">{{ authError }}</div>

      <input v-model="authData.email" type="email" placeholder="Email" class="search-input" />
      <input v-model="authData.password" type="password" placeholder="Пароль" class="search-input" />

      <button @click="authenticate" class="btn btn-primary" :disabled="loading">
        {{ loading ? 'Зачекайте...' : (isLoginMode ? 'Увійти' : 'Зареєструватися') }}
      </button>

      <p class="toggle-mode">
        <a href="#" @click.prevent="isLoginMode = !isLoginMode">
          {{ isLoginMode ? 'Немає акаунту? Зареєструйтесь' : 'Вже є акаунт? Увійти' }}
        </a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
}
.auth-card {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.auth-card h2 { text-align: center; margin-top: 0; color: #2c3e50; }
.search-input {
  width: 100%;
  padding: 12px 15px;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
}
.btn { padding: 12px; border: none; border-radius: 8px; font-size: 1rem; cursor: pointer; font-weight: 600; }
.btn-primary { background-color: #2a5298; color: white; }
.btn-primary:disabled { background-color: #a0b4d4; cursor: not-allowed; }
.alert-error { background-color: #fde8e8; color: #c62828; padding: 10px; border-radius: 6px; text-align: center; }
.toggle-mode { text-align: center; margin: 0; }
.toggle-mode a { color: #2a5298; text-decoration: none; font-weight: 500; }
</style>