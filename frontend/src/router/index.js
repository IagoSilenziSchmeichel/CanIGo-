import { createRouter, createWebHistory } from 'vue-router'
import Home from '../App.vue'
import ItemsPage from '../pages/ItemsPage.vue'
import RemindersPage from '../pages/RemindersPage.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component: Home },
        { path: '/items', component: ItemsPage },
        { path: '/reminders', component: RemindersPage }
    ]
})

export default router