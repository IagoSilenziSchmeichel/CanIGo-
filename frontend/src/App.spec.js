import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import App from './App.vue'

// --- Mocking fetch ---
// Wir erstellen einen Mock, der so tut, als wäre er das Backend
global.fetch = vi.fn()

// Hilfsfunktion, um einfach API-Antworten zu simulieren
function createFetchResponse(data, ok = true) {
    return {
        ok: ok,
        json: () => new Promise((resolve) => resolve(data)),
        text: () => new Promise((resolve) => resolve("Fehler im Backend"))
    }
}

describe('App.vue Integration Tests', () => {

    beforeEach(() => {
        vi.resetAllMocks()

        // Intelligenter Mock: Unterscheidet zwischen Gegenständen und Notifications
        fetch.mockImplementation((url) => {
            if (url.includes('/notifications')) {
                return Promise.resolve(createFetchResponse([])) // Standard: Keine Notifs
            }
            if (url.includes('/gegenstaende')) {
                return Promise.resolve(createFetchResponse([])) // Standard: Keine Items
            }
            return Promise.resolve(createFetchResponse({}))
        })
    })

    // TEST 1: Rendert die Seite korrekt?
    it('zeigt den Titel "Can I Go?" an', () => {
        const wrapper = mount(App)
        expect(wrapper.text()).toContain('Can I Go?')
    })

    // TEST 2: Sind die Eingabefelder da?
    it('hat Eingabefelder für Name und Ort', () => {
        const wrapper = mount(App)
        // Wir suchen nach den spezifischen Placeholdern
        expect(wrapper.find('input[placeholder="z.B. Hammer"]').exists()).toBe(true)
        expect(wrapper.find('input[placeholder="z.B. Werkbank"]').exists()).toBe(true)
    })

    // TEST 3: Funktioniert die Validierung?
    it('zeigt Fehler, wenn man ohne Eingabe klickt', async () => {
        const wrapper = mount(App)

        // Suche den Hinzufügen-Button im #add Bereich
        const button = wrapper.find('#add button.btn-primary')
        await button.trigger('click')

        // Erwarte Fehlermeldung
        expect(wrapper.text()).toContain('Bitte Name und Ort ausfüllen')
    })

    // TEST 4: Wird ein POST-Request gesendet?
    it('sendet Daten an das Backend beim Speichern', async () => {
        const wrapper = mount(App)

        // Formular ausfüllen
        await wrapper.find('input[placeholder="z.B. Hammer"]').setValue('Plasma-Cutter')
        await wrapper.find('input[placeholder="z.B. Werkbank"]').setValue('Werkstatt')

        // Mocken, dass der POST-Request erfolgreich ist
        fetch.mockImplementationOnce((url, options) => {
            if (options && options.method === 'POST') {
                return Promise.resolve(createFetchResponse({ id: 100, name: 'Plasma-Cutter' }))
            }
            return Promise.resolve(createFetchResponse([]))
        })

        // Button klicken
        await wrapper.find('#add button.btn-primary').trigger('click')

        // Prüfen: Wurde fetch mit POST aufgerufen?
        // Wir suchen in allen Aufrufen nach einem mit method: 'POST'
        const calls = fetch.mock.calls
        const postCall = calls.find(call => call[1] && call[1].method === 'POST')

        expect(postCall).toBeDefined()
        expect(postCall[0]).toContain('/gegenstaende') // URL Check
        expect(postCall[1].body).toContain('Plasma-Cutter') // Body Check
    })

    // TEST 5: Werden Gegenstände in der Liste angezeigt?
    it('zeigt geladene Gegenstände als Karte an', async () => {
        const mockItems = [
            { id: 1, name: 'Cyber-Deck', ort: 'Rucksack', wichtigkeit: 'UNVERZICHTBAR', kategorie: 'TECH' }
        ]

        // Mock: Wenn Gegenstände geladen werden, gib unsere Testdaten zurück
        fetch.mockImplementation((url) => {
            if (url.includes('/gegenstaende') && !url.includes('method')) {
                return Promise.resolve(createFetchResponse(mockItems))
            }
            return Promise.resolve(createFetchResponse([]))
        })

        const wrapper = mount(App)

        // Kurz warten, bis Vue die Daten verarbeitet hat
        await new Promise(resolve => setTimeout(resolve, 10))
        await wrapper.vm.$nextTick()

        // Prüfen, ob die Daten im HTML stehen
        expect(wrapper.text()).toContain('Cyber-Deck')
        expect(wrapper.text()).toContain('Rucksack')
        expect(wrapper.text()).toContain('UNVERZICHTBAR')
    })

    // TEST 6: Werden Notifications angezeigt?
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

        const wrapper = mount(App)

        await new Promise(resolve => setTimeout(resolve, 10))
        await wrapper.vm.$nextTick()

        // Prüfen ob die Section da ist und der Text
        expect(wrapper.find('#notifications').exists()).toBe(true)
        expect(wrapper.text()).toContain('TÜV fällig')
    })
})