import { createRouter, createWebHistory } from 'vue-router'
import { LoginCallback, navigationGuard } from '@okta/okta-vue'

import HomeView from '../views/HomeView.vue'
import ItemsView from '../views/ItemsView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import LoginView from '../views/LoginView.vue'

const routes = [
    { path: '/', name: 'home', component: HomeView },

    { path: '/login', name: 'login', component: LoginView },

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
    },

    { path: '/login/callback', component: LoginCallback }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach(navigationGuard)

export default router