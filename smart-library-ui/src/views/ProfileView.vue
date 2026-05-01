<script setup>
import { ref, computed } from 'vue'
import { jwtDecode } from 'jwt-decode'

const userRole = ref('READER')
const userEmail = ref('')
const token = localStorage.getItem('jwt')

if (token) {
  try {
    const decoded = jwtDecode(token)
    userRole.value = decoded.role || 'READER'
    userEmail.value = decoded.sub || 'user@kpi.ua'
  } catch (e) {}
}

const permissions = computed(() => {
  return [
    { name: 'Перегляд каталогу', granted: true },
    { name: 'Читання відкритих книг', granted: true },
    { name: 'Завантаження PDF (Upload)', granted: ['ADMIN', 'MODERATOR'].includes(userRole.value) },
    { name: 'Редагування метаданих', granted: ['ADMIN', 'MODERATOR'].includes(userRole.value) },
    { name: 'Видалення матеріалів', granted: userRole.value === 'ADMIN' },
  ]
})
</script>

<template>
  <div class="p-6 md:p-10 max-w-4xl mx-auto w-full">
    <h2 class="text-3xl font-bold text-slate-900 mb-8">Особистий кабінет</h2>

    <div class="bg-white rounded-2xl p-8 shadow-sm border border-slate-200 mb-8 flex items-center space-x-6">
      <div class="w-24 h-24 rounded-2xl bg-rose-50 text-rose-700 flex items-center justify-center text-4xl font-bold shadow-sm border border-rose-100">
        {{ userEmail.charAt(0).toUpperCase() }}
      </div>
      <div>
        <h3 class="text-2xl font-bold text-slate-900">{{ userEmail.split('@')[0] }}</h3>
        <p class="text-slate-500 mb-2">{{ userEmail }}</p>
        <div class="flex space-x-2">
          <span class="bg-rose-100 text-rose-800 border border-rose-200 text-xs font-bold px-3 py-1 rounded-md uppercase tracking-wide">
            <i class="fas fa-shield-alt mr-1"></i> {{ userRole }}
          </span>
          <span class="bg-slate-100 text-slate-600 border border-slate-200 text-xs font-bold px-3 py-1 rounded-md uppercase tracking-wide">IP: Авторизовано</span>
        </div>
      </div>
    </div>

    <h3 class="text-xl font-bold text-slate-900 mb-4">Ваші права доступу (RBAC)</h3>
    <div class="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden mb-8">
      <table class="w-full text-left border-collapse">
        <thead>
        <tr class="bg-slate-50 text-slate-600 text-sm uppercase tracking-wider border-b border-slate-200">
          <th class="p-4 font-semibold">Дія / Ресурс</th>
          <th class="p-4 font-semibold">Доступ</th>
        </tr>
        </thead>
        <tbody class="text-sm">
        <tr v-for="(perm, index) in permissions" :key="index" class="border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors">
          <td class="p-4 text-slate-800 font-medium">{{ perm.name }}</td>
          <td class="p-4">
            <i v-if="perm.granted" class="fas fa-check-circle text-emerald-600 text-lg"></i>
            <i v-else class="fas fa-times-circle text-rose-600 text-lg"></i>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>