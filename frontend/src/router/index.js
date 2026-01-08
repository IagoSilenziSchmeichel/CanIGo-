import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import ItemsView from '../views/ItemsView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import LoginView from '../views/LoginView.vue'
import LoginCallback from '../views/LoginCallback.vue'

import { oktaAuth } from '../okta'

const routes = [
    { path: '/', name: 'home', component: HomeView },

    { path: '/login', name: 'login', component: LoginView },
    { path: '/login/callback', name: 'login-callback', component: LoginCallback },

    {
        path: '/items',
        name: 'items',
        component: ItemsView,
        meta: { requiresAuth: true }
    },
    {
        path: '/notifications',
        name: 'notifications',
        component: NotificationsView,
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// eigener Guard (weil embedded Widget)
router.beforeEach(async (to) => {
    if (!to.meta.requiresAuth) return true

    const isAuth = await oktaAuth.isAuthenticated()
    if (isAuth) return true

    return { name: 'login', query: { from: to.fullPath } }
})

export default router