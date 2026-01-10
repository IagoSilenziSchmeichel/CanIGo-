import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import ItemsPage from '../pages/ItemsPage.vue'
import RemindersPage from '../pages/RemindersPage.vue'
import LoginPage from '../pages/LoginPage.vue'
import RegisterPage from '../pages/RegisterPage.vue'
import CallbackPage from '../pages/CallbackPage.vue'

function isLoggedIn() {
    return !!(
        localStorage.getItem('token') ||
        localStorage.getItem('auth_token') ||
        localStorage.getItem('access_token')
    )
}

const routes = [
    { path: '/', name: 'home', component: HomeView, meta: { public: true } },

    { path: '/login', name: 'login', component: LoginPage, meta: { public: true } },
    { path: '/register', name: 'register', component: RegisterPage, meta: { public: true } },

    // du nutzt aktuell "/callback" (nicht "/auth/callback")
    { path: '/callback', name: 'callback', component: CallbackPage, meta: { public: true } },

    { path: '/items', name: 'items', component: ItemsPage, meta: { requiresAuth: true } },
    { path: '/notifications', name: 'notifications', component: RemindersPage, meta: { requiresAuth: true } },

    // optional: 404 fallback
    { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to) => {
    const loggedIn = isLoggedIn()

    // protected -> login
    if (to.meta.requiresAuth && !loggedIn) {
        return { path: '/login', query: { from: to.fullPath } }
    }

    // logged in -> weg von login/register
    if ((to.path === '/login' || to.path === '/register') && loggedIn) {
        return '/'
    }

    return true
})

export default router