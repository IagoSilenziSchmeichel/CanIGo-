import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import ItemsView from '../views/ItemsView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import LoginView from '../views/LoginView.vue'

const routes = [
    { path: '/', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/items', name: 'items', component: ItemsView },
    { path: '/notifications', name: 'notifications', component: NotificationsView }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router