import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import LibraryView from '../views/LibraryView.vue'
import AssetDetailView from '../views/AssetDetailView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: LoginView,
            meta: { requiresAuth: false }
        },
        {
            path: '/',
            name: 'library',
            component: LibraryView,
            meta: { requiresAuth: false }
        },
        {
            path: '/asset/:id',
            name: 'asset-detail',
            component: AssetDetailView,
            props: true
        }
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