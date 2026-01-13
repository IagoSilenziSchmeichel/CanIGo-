import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import App from './App.vue'

// 1. Mock für vue-router
vi.mock('vue-router', () => ({
    RouterLink: { name: 'RouterLink', render: () => null },
    RouterView: { name: 'RouterView', render: () => null },
    useRoute: () => ({
        path: '/',
    }),
    useRouter: () => ({
        push: vi.fn(),
    }),
}))

// 2. Umgebungsvariable setzen
import.meta.env.VITE_APP_BACKEND_BASE_URL = 'http://localhost:8080'

// 3. Globalen Fetch-Mock erstellen
global.fetch = vi.fn()

// Hilfsfunktion für Fetch-Antworten
function createFetchResponse(data, ok = true) {
    return {
        ok: ok,
        json: () => Promise.resolve(data),
        text: () => Promise.resolve("Fehler im Backend")
    }
}

describe('App.vue Integration Tests', () => {

    // Hilfsfunktion zum Mounten mit Stubs
    const mountApp = () => {
        return mount(App, {
            global: {
                stubs: {
                    RouterLink: { template: '<a><slot /></a>' },
                    RouterView: { template: '<div />' }
                }
            }
        })
    }

    beforeEach(() => {
        vi.resetAllMocks()

        // NEU: localStorage simulieren, damit die App denkt, wir sind eingeloggt
        // Dies verhindert die Meldung "Bitte zuerst einloggen"
        const mockStorage = {
            getItem: vi.fn((key) => (key === 'token' ? 'fake-jwt-token' : null)),
            setItem: vi.fn(),
            removeItem: vi.fn(),
            clear: vi.fn(),
        }
        vi.stubGlobal('localStorage', mockStorage)

        // Standard-Mock Verhalten für alle Tests
        fetch.mockImplementation((url) => {
            if (url.includes('/notifications')) return Promise.resolve(createFetchResponse([]))
            if (url.includes('/gegenstaende')) return Promise.resolve(createFetchResponse([]))
            return Promise.resolve(createFetchResponse({}))
        })
    })

    it('zeigt den Titel "Can I Go?" an', () => {
        const wrapper = mountApp()
        expect(wrapper.text()).toContain('Can I Go?')
    })

    it('hat Eingabefelder für Name und Ort', () => {
        const wrapper = mountApp()
        expect(wrapper.find('input[placeholder="z.B. Hammer"]').exists()).toBe(true)
        expect(wrapper.find('input[placeholder="z.B. Werkbank"]').exists()).toBe(true)
    })

    it('zeigt Fehler, wenn man ohne Eingabe klickt', async () => {
        const wrapper = mountApp()
        // Warten, bis refreshAuth() den Token aus dem Mock-Storage gelesen hat
        await wrapper.vm.$nextTick()

        const button = wrapper.find('#add button.btn-primary')
        await button.trigger('click')

        // Jetzt sollte die richtige Fehlermeldung erscheinen
        expect(wrapper.text()).toContain('Bitte Name und Ort ausfüllen')
    })

    it('sendet Daten an das Backend beim Speichern', async () => {
        const wrapper = mountApp()
        await wrapper.vm.$nextTick()

        await wrapper.find('input[placeholder="z.B. Hammer"]').setValue('Plasma-Cutter')
        await wrapper.find('input[placeholder="z.B. Werkbank"]').setValue('Werkstatt')

        fetch.mockImplementationOnce((url, options) => {
            if (options && options.method === 'POST') {
                return Promise.resolve(createFetchResponse({ id: 100, name: 'Plasma-Cutter' }))
            }
            return Promise.resolve(createFetchResponse([]))
        })

        await wrapper.find('#add button.btn-primary').trigger('click')

        const postCall = fetch.mock.calls.find(call => call[1] && call[1].method === 'POST')
        expect(postCall).toBeDefined()
        expect(postCall[1].body).toContain('Plasma-Cutter')
    })

    it('zeigt geladene Gegenstände in der Statistik an', async () => {
        const mockItems = [
            { id: 1, name: 'Cyber-Deck', ort: 'Rucksack', wichtigkeit: 'WICHTIG', kategorie: 'TECH' }
        ]

        fetch.mockImplementation((url) => {
            if (url.includes('/gegenstaende') && !url.includes('method')) {
                return Promise.resolve(createFetchResponse(mockItems))
            }
            return Promise.resolve(createFetchResponse([]))
        })

        const wrapper = mountApp()
        await wrapper.vm.$nextTick()

        // Warten auf async Daten-Verarbeitung
        await new Promise(resolve => setTimeout(resolve, 50))
        await wrapper.vm.$nextTick()

        const text = wrapper.text()
        // Da mockItems 1 Element enthält, muss die Statistik "1 Items" anzeigen
        expect(text).toContain('1 Items')
        expect(text).toContain('Wichtig:1')
    })
})