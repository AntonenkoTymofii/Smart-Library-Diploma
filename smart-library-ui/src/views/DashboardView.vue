<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const stats = ref({ total: 0, aiProcessed: 0, protected: 0 })
const loading = ref(true)

const fetchStats = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/library/assets/stats');
    if (response.ok) {
      stats.value = await response.json();
    }
  } catch (error) {
    console.error("Помилка завантаження статистики", error);
  } finally {
    loading.value = false;
  }
}

onMounted(fetchStats)
</script>

<template>
  <div class="p-6 md:p-10 max-w-6xl mx-auto w-full">
    <div class="flex justify-between items-end mb-8">
      <div>
        <h2 class="text-3xl font-bold text-slate-900">Вітаємо у Smart Library! 👋</h2>
        <p class="text-slate-500 mt-1">Інтелектуальний репозитарій з автоматичною генерацією метаданих.</p>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
      <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex items-center space-x-4">
        <div class="w-14 h-14 bg-slate-100 text-slate-800 rounded-2xl flex items-center justify-center text-2xl">
          <i class="fas fa-book"></i>
        </div>
        <div>
          <p class="text-slate-500 text-sm font-medium">Всього документів</p>
          <p class="text-2xl font-bold text-slate-900">
            <i v-if="loading" class="fas fa-spinner fa-spin text-sm text-slate-400"></i>
            <span v-else>{{ stats.total }}</span>
          </p>
        </div>
      </div>
      <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex items-center space-x-4">
        <div class="w-14 h-14 bg-rose-50 text-rose-700 rounded-2xl flex items-center justify-center text-2xl">
          <i class="fas fa-brain"></i>
        </div>
        <div>
          <p class="text-slate-500 text-sm font-medium">Оброблено ШІ (LLM)</p>
          <p class="text-2xl font-bold text-slate-900">
            <i v-if="loading" class="fas fa-spinner fa-spin text-sm text-slate-400"></i>
            <span v-else>{{ stats.aiProcessed }}</span>
          </p>
        </div>
      </div>
      <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 flex items-center space-x-4">
        <div class="w-14 h-14 bg-emerald-50 text-emerald-700 rounded-2xl flex items-center justify-center text-2xl">
          <i class="fas fa-shield-alt"></i>
        </div>
        <div>
          <p class="text-slate-500 text-sm font-medium">Захищено (DRM)</p>
          <p class="text-2xl font-bold text-slate-900">
            <i v-if="loading" class="fas fa-spinner fa-spin text-sm text-slate-400"></i>
            <span v-else>{{ stats.protected }}</span>
          </p>
        </div>
      </div>
    </div>

    <div class="bg-gradient-to-r from-slate-900 to-rose-900 rounded-3xl p-8 text-white shadow-lg relative overflow-hidden">
      <div class="absolute right-0 top-0 opacity-10 transform translate-x-1/4 -translate-y-1/4">
        <i class="fas fa-robot text-9xl"></i>
      </div>
      <div class="relative z-10 max-w-xl">
        <span class="bg-white/20 text-white text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wide backdrop-blur-sm mb-4 inline-block">Оновлення системи</span>
        <h3 class="text-2xl font-bold mb-2">Новий AI Library Chat вже доступний!</h3>
        <p class="text-rose-100 mb-6">Тепер ви можете ставити питання безпосередньо до змісту наукових статей. Штучний інтелект знайде відповідь у тексті.</p>
        <button @click="router.push('/catalog')" class="bg-white text-rose-800 px-6 py-2.5 rounded-xl font-semibold hover:bg-rose-50 transition-colors shadow-sm">
          Перейти до каталогу
        </button>
      </div>
    </div>
  </div>
</template>