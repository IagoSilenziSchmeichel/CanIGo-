import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import App from './App.vue'

// --- NEU: Router Mocking ---
// Das verhindert den Fehler: TypeError: Cannot read properties of undefined (reading 'path')
vi.mock('vue-router', () => ({
    useRoute: () => ({
        path: '/', // Simuliert, dass wir auf der Startseite sind
    }),
    useRouter: () => ({
        push: vi.fn(),
    }),
}))

// --- Mocking fetch ---
global.fetch = vi.fn()

function createFetchResponse(data, ok = true) {
    return {
        ok: ok,
        json: () => new Promise((resolve) => resolve(data)),
        text: () => new Promise((resolve) => resolve("Fehler im Backend"))
    }
}

describe('App.vue Integration Tests', () => {

    // Hilfsfunktion zum Mounten, damit wir nicht überall die Stubs schreiben müssen
    const mountApp = () => {
        return mount(App, {
            global: {
                stubs: ['RouterLink', 'RouterView'] // NEU: Verhindert Warnungen im Log
            }
        })
    }

    beforeEach(() => {
        vi.resetAllMocks()

        fetch.mockImplementation((url) => {
            if (url.includes('/notifications')) {
                return Promise.resolve(createFetchResponse([]))
            }
            if (url.includes('/gegenstaende')) {
                return Promise.resolve(createFetchResponse([]))
            }
            return Promise.resolve(createFetchResponse({}))
        })
    })

    it('zeigt den Titel "Can I Go?" an', () => {
        const wrapper = mountApp() // Nutzt die Hilfsfunktion
        expect(wrapper.text()).toContain('Can I Go?')
    })

    it('hat Eingabefelder für Name und Ort', () => {
        const wrapper = mountApp()
        expect(wrapper.find('input[placeholder="z.B. Hammer"]').exists()).toBe(true)
        expect(wrapper.find('input[placeholder="z.B. Werkbank"]').exists()).toBe(true)
    })

    it('zeigt Fehler, wenn man ohne Eingabe klickt', async () => {
        const wrapper = mountApp()
        const button = wrapper.find('#add button.btn-primary')
        await button.trigger('click')
        expect(wrapper.text()).toContain('Bitte Name und Ort ausfüllen')
    })

    it('sendet Daten an das Backend beim Speichern', async () => {
        const wrapper = mountApp()
        await wrapper.find('input[placeholder="z.B. Hammer"]').setValue('Plasma-Cutter')
        await wrapper.find('input[placeholder="z.B. Werkbank"]').setValue('Werkstatt')

        fetch.mockImplementationOnce((url, options) => {
            if (options && options.method === 'POST') {
                return Promise.resolve(createFetchResponse({ id: 100, name: 'Plasma-Cutter' }))
            }
            return Promise.resolve(createFetchResponse([]))
        })

        await wrapper.find('#add button.btn-primary').trigger('click')

        const calls = fetch.mock.calls
        const postCall = calls.find(call => call[1] && call[1].method === 'POST')

        expect(postCall).toBeDefined()
        expect(postCall[0]).toContain('/gegenstaende')
        expect(postCall[1].body).toContain('Plasma-Cutter')
    })

    it('zeigt geladene Gegenstände als Karte an', async () => {
        const mockItems = [
            { id: 1, name: 'Cyber-Deck', ort: 'Rucksack', wichtigkeit: 'UNVERZICHTBAR', kategorie: 'TECH' }
        ]

        fetch.mockImplementation((url) => {
            if (url.includes('/gegenstaende') && !url.includes('method')) {
                return Promise.resolve(createFetchResponse(mockItems))
            }
            return Promise.resolve(createFetchResponse([]))
        })

        const wrapper = mountApp()

        await new Promise(resolve => setTimeout(resolve, 10))
        await wrapper.vm.$nextTick()

        expect(wrapper.text()).toContain('Cyber-Deck')
        expect(wrapper.text()).toContain('Rucksack')
        expect(wrapper.text()).toContain('UNVERZICHTBAR')
    })

    it('zeigt den Bereich für Erinnerungen an', async () => {
        const mockNotifs = [
            { id: 1, message: 'TÜV fällig', createdAt: '2025-01-01T12:00:00' }
        ]

        fetch.mockImplementation((url) => {
            if (url.includes('/notifications')) {
                return Promise.resolve(createFetchResponse(mockNotifs))
            }
            return Promise.resolve(createFetchResponse([]))
        })

        const wrapper = mountApp()

        await new Promise(resolve => setTimeout(resolve, 10))
        await wrapper.vm.$nextTick()

        expect(wrapper.find('#notifications').exists()).toBe(true)
        expect(wrapper.text()).toContain('TÜV fällig')
    })
})