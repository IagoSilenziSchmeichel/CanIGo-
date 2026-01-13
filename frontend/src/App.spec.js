import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import App from './App.vue'

// 1. Mock für vue-router (Muss vor den Tests definiert sein)
vi.mock('vue-router', () => ({
    RouterLink: { name: 'RouterLink', render: () => null },
    RouterView: { name: 'RouterView', render: () => null },
    useRoute: () => ({ path: '/' }),
    useRouter: () => ({ push: vi.fn() }),
}))

// 2. Hilfsfunktion für Fetch-Antworten (Wichtig: Immer JSON als Array)
const createFetchResponse = (data) => ({
    ok: true,
    json: () => Promise.resolve(data),
    text: () => Promise.resolve(JSON.stringify(data))
})

describe('App.vue Integration Tests', () => {

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

        // 3. localStorage simulieren
        const mockStorage = {
            getItem: vi.fn((key) => (key === 'token' ? 'fake-token' : null)),
            setItem: vi.fn(),
            removeItem: vi.fn(),
            clear: vi.fn(),
        }
        vi.stubGlobal('localStorage', mockStorage)

        // 4. Globaler Fetch-Mock: Wir nutzen vi.stubGlobal für maximale Stabilität
        // Wir geben IMMER ein leeres Array zurück, außer der Test definiert es anders
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(createFetchResponse([])))
    })

    it('zeigt den Titel "Can I Go?" an', async () => {
        const wrapper = mountApp()
        await flushPromises()
        expect(wrapper.text()).toContain('Can I Go?')
    })

    it('hat Eingabefelder für Name und Ort', async () => {
        const wrapper = mountApp()
        await flushPromises()
        expect(wrapper.find('input[placeholder="z.B. Hammer"]').exists()).toBe(true)
        expect(wrapper.find('input[placeholder="z.B. Werkbank"]').exists()).toBe(true)
    })

    it('zeigt Fehler, wenn man ohne Eingabe klickt', async () => {
        const wrapper = mountApp()
        await flushPromises()

        const button = wrapper.find('#add button.btn-primary')
        await button.trigger('click')
        // Warten auf die Reaktivität von Vue
        await wrapper.vm.$nextTick()

        expect(wrapper.text()).toContain('Bitte Name und Ort ausfüllen')
    })

    it('sendet Daten an das Backend beim Speichern', async () => {
        const wrapper = mountApp()
        await flushPromises()

        await wrapper.find('input[placeholder="z.B. Hammer"]').setValue('Plasma-Cutter')
        await wrapper.find('input[placeholder="z.B. Werkbank"]').setValue('Werkstatt')

        // Spezieller Mock für diesen einen POST-Aufruf
        fetch.mockResolvedValueOnce(createFetchResponse({ id: 100, name: 'Plasma-Cutter' }))

        await wrapper.find('#add button.btn-primary').trigger('click')
        await flushPromises()

        const postCall = fetch.mock.calls.find(call => call[1]?.method === 'POST')
        expect(postCall).toBeDefined()
        expect(postCall[1].body).toContain('Plasma-Cutter')
    })

    it('zeigt geladene Gegenstände in der Statistik an', async () => {
        const mockItems = [
            { id: 1, name: 'Cyber-Deck', ort: 'Rucksack', wichtigkeit: 'WICHTIG', kategorie: 'TECH' }
        ]

        // Wir sagen dem Mock, dass er für diesen Test die Liste liefern soll
        fetch.mockResolvedValue(createFetchResponse(mockItems))

        const wrapper = mountApp()

        // Intensives Warten auf asynchrone Prozesse
        await flushPromises()
        await wrapper.vm.$nextTick()
        await wrapper.vm.$nextTick()

        const text = wrapper.text()

        // Wir prüfen, ob die Zahl "1" in der Statistik-Badge erscheint
        expect(text).toContain('1 Items')
        const statsCard = wrapper.find('.hero-card')
        expect(statsCard.text()).toContain('1')
    })
})