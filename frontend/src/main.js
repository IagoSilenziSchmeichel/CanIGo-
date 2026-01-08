import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import { OktaAuth } from '@okta/okta-auth-js'
import OktaVue from '@okta/okta-vue'

const oktaAuth = new OktaAuth({
    issuer: import.meta.env.VITE_OKTA_ISSUER,
    clientId: import.meta.env.VITE_OKTA_CLIENT_ID,
    redirectUri: `${window.location.origin}/login/callback`,
    scopes: ['openid', 'profile', 'email'],
    pkce: true
})

createApp(App)
    .use(OktaVue, { oktaAuth })
    .use(router)
    .mount('#app')