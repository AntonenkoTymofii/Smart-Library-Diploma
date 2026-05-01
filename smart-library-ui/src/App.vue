<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { jwtDecode } from 'jwt-decode'

const router = useRouter()
const route = useRoute()
const isAuthenticated = ref(!!localStorage.getItem('jwt'))
const userRole = ref('GUEST')
const userName = ref('Гість')

const updateUserData = () => {
  const token = localStorage.getItem('jwt')
  isAuthenticated.value = !!token
  if (token) {
    try {
      const decoded = jwtDecode(token)
      userRole.value = decoded.role || 'READER'
      userName.value = decoded.sub || 'Користувач'
    } catch (e) {
      console.error("Помилка розшифровки токена", e)
    }
  } else {
    userRole.value = 'GUEST'
    userName.value = 'Гість'
  }
}

watch(() => route.path, updateUserData, { immediate: true })

const logout = () => {
  localStorage.removeItem('jwt')
  updateUserData()
  router.push('/login')
}

const canUpload = computed(() => ['ADMIN', 'MODERATOR'].includes(userRole.value))
</script>

<template>
  <div class="flex h-screen bg-slate-50 overflow-hidden font-sans text-gray-800">
    <aside v-if="route.path !== '/login'" class="w-64 bg-white/95 backdrop-blur-md border-r border-gray-100 flex flex-col justify-between hidden md:flex z-20 shadow-sm">
      <div>
        <div class="p-6 flex items-center space-x-3 cursor-pointer" @click="router.push('/')">
          <div class="w-10 h-10 bg-rose-700 rounded-xl shadow-lg shadow-rose-200 flex items-center justify-center text-white font-bold text-xl">
            <i class="fas fa-layer-group"></i>
          </div>
          <h1 class="text-xl font-bold tracking-tight text-slate-900">Smart<span class="text-rose-700">Library</span></h1>
        </div>

        <nav class="mt-4 px-4 space-y-2">
          <router-link to="/" class="w-full flex items-center space-x-3 px-4 py-3 rounded-xl font-medium transition-colors" :class="route.path === '/' ? 'bg-rose-50 text-rose-700' : 'text-gray-500 hover:bg-slate-50 hover:text-slate-800'">
            <i class="fas fa-home w-5"></i><span>Головна</span>
          </router-link>

          <router-link to="/catalog" class="w-full flex items-center space-x-3 px-4 py-3 rounded-xl font-medium transition-colors" :class="route.path.includes('/catalog') || route.path.includes('/asset') ? 'bg-rose-50 text-rose-700' : 'text-gray-500 hover:bg-slate-50 hover:text-slate-800'">
            <i class="fas fa-search w-5"></i><span>Каталог та Пошук</span>
          </router-link>

          <router-link v-if="isAuthenticated" to="/profile" class="w-full flex items-center space-x-3 px-4 py-3 rounded-xl font-medium transition-colors" :class="route.path === '/profile' ? 'bg-rose-50 text-rose-700' : 'text-gray-500 hover:bg-slate-50 hover:text-slate-800'">
            <i class="fas fa-user-circle w-5"></i><span>Мій кабінет</span>
          </router-link>
        </nav>
      </div>

      <div class="p-4">
        <button v-if="canUpload" @click="router.push('/upload')" class="w-full mb-4 flex items-center justify-center space-x-2 px-4 py-3 bg-slate-900 text-white rounded-xl font-medium hover:bg-slate-800 transition-colors shadow-md">
          <i class="fas fa-plus"></i><span>Завантажити PDF</span>
        </button>

        <div v-if="isAuthenticated" class="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-gray-100">
          <div class="flex items-center space-x-3 min-w-0">
            <div class="w-10 h-10 rounded-full bg-slate-200 text-slate-800 flex items-center justify-center font-bold">
              {{ userName.charAt(0).toUpperCase() }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold truncate text-slate-900" :title="userName">{{ userName.split('@')[0] }}</p>
              <p class="text-xs text-rose-700 font-medium">{{ userRole }}</p>
            </div>
          </div>
          <button @click="logout" class="text-gray-400 hover:text-rose-600 ml-2" title="Вийти">
            <i class="fas fa-sign-out-alt"></i>
          </button>
        </div>

        <button v-else @click="router.push('/login')" class="w-full py-3 bg-rose-700 text-white rounded-xl font-medium hover:bg-rose-800 transition-colors shadow-md">
          Увійти в систему
        </button>
      </div>
    </aside>

    <main class="flex-1 flex flex-col h-full relative overflow-y-auto hide-scrollbar">
      <router-view />
    </main>
  </div>
</template>

<style>
.hide-scrollbar::-webkit-scrollbar { display: none; }
.hide-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>