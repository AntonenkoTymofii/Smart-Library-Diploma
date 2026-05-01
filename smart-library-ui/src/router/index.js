import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import LibraryView from '../views/LibraryView.vue'
import AssetDetailView from '../views/AssetDetailView.vue'
import UploadView from '../views/UploadView.vue'
import ProfileView from '../views/ProfileView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/login', name: 'login', component: LoginView, meta: { requiresAuth: false } },
        { path: '/', name: 'dashboard', component: DashboardView, meta: { requiresAuth: false } },
        { path: '/catalog', name: 'catalog', component: LibraryView, meta: { requiresAuth: false } },
        { path: '/profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },
        { path: '/asset/:id', name: 'asset-detail', component: AssetDetailView, props: true },
        { path: '/upload', name: 'upload', component: UploadView, meta: { requiresAuth: true } }
    ]
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('jwt')
    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.name === 'login' && token) {
        next('/')
    } else {
        next()
    }
})

export default router