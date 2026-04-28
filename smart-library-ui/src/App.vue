<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isAuthenticated = ref(!!localStorage.getItem('jwt'))

watch(() => route.path, () => {
  isAuthenticated.value = !!localStorage.getItem('jwt')
})

const logout = () => {
  localStorage.removeItem('jwt')
  isAuthenticated.value = false
  router.push('/login')
}
</script>

<template>
  <div class="app-container">
    <header class="header">
      <div class="nav-content">
        <div class="logo" @click="router.push('/')" style="cursor: pointer;">
          <h1>📚 Smart Library</h1>
          <p>Інтелектуальна система</p>
        </div>

        <div class="nav-buttons">
          <button v-if="isAuthenticated" @click="logout" class="btn btn-outline-white">
            Вийти
          </button>
          <button v-else-if="route.path !== '/login'" @click="router.push('/login')" class="btn btn-login">
            Увійти
          </button>
        </div>
      </div>
    </header>

    <router-view />
  </div>
</template>

<style>
.btn-login {
  background-color: #ffffff;
  color: #2a5298;
  border: none;
}

.btn-login:hover {
  background-color: #e3f2fd;
}

body {
  margin: 0;
  padding: 0;
  background-color: #f4f7f6;
  font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.app-container {
  min-height: 100vh;
  color: #333;
}

.header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 1.5rem 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 900px;
  margin: 0 auto;
}

.logo h1 { margin: 0 0 0.2rem 0; font-size: 2rem; }
.logo p { margin: 0; opacity: 0.8; font-size: 0.9rem;}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
}

.btn-outline-white {
  background-color: transparent;
  color: white;
  border: 1px solid white;
}
.btn-outline-white:hover {
  background-color: rgba(255,255,255,0.1);
}
</style>