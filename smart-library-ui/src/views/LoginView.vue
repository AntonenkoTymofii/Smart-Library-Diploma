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
  <div class="flex items-center justify-center min-h-[calc(100vh-80px)] w-full px-4">
    <div class="bg-white p-10 rounded-2xl shadow-lg border border-slate-200 w-full max-w-md flex flex-col gap-6">

      <div class="text-center mb-2">
        <div class="w-16 h-16 bg-rose-700 rounded-2xl shadow-lg shadow-rose-200 mx-auto flex items-center justify-center text-white font-bold text-3xl mb-4">
          <i class="fas fa-layer-group"></i>
        </div>
        <h2 class="text-2xl font-bold text-slate-900">{{ isLoginMode ? 'Вхід у систему' : 'Реєстрація' }}</h2>
      </div>

      <div v-if="authError" class="bg-red-50 text-red-700 p-4 rounded-xl border border-red-200 text-sm text-center">
        {{ authError }}
      </div>

      <div class="space-y-4">
        <input v-model="authData.email" type="email" placeholder="Електронна пошта" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-rose-600 outline-none text-slate-900 placeholder-slate-400" />
        <input v-model="authData.password" type="password" placeholder="Пароль" class="w-full bg-slate-50 border border-slate-300 rounded-xl px-4 py-3 focus:ring-2 focus:ring-rose-600 outline-none text-slate-900 placeholder-slate-400" />
      </div>

      <button @click="authenticate" :disabled="loading" class="w-full bg-slate-900 text-white rounded-xl py-3 font-semibold hover:bg-slate-800 transition-colors shadow-md disabled:bg-slate-400 mt-2">
        {{ loading ? 'Зачекайте...' : (isLoginMode ? 'Увійти' : 'Зареєструватися') }}
      </button>

      <p class="text-center text-sm text-slate-500 mt-4">
        <a href="#" @click.prevent="isLoginMode = !isLoginMode" class="text-rose-700 font-semibold hover:underline">
          {{ isLoginMode ? 'Немає акаунту? Зареєструйтесь' : 'Вже є акаунт? Увійти' }}
        </a>
      </p>
    </div>
  </div>
</template>