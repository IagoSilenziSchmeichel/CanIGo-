const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export function setToken(token) {
    localStorage.setItem("token", token);
}

export function clearToken() {
    localStorage.removeItem("token");
}

export function getToken() {
    return localStorage.getItem("token");
}

export async function apiFetch(path, options = {}) {
    const token = getToken();

    const headers = {
        ...(options.headers || {}),
    };

    // JSON automatisch setzen, wenn body vorhanden und noch kein Content-Type gesetzt
    if (options.body && !headers["Content-Type"]) {
        headers["Content-Type"] = "application/json";
    }

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const res = await fetch(`${BASE_URL}${path}`, {
        ...options,
        headers,
    });

    // Antwort lesen (kann leer sein)
    const text = await res.text();
    const data = text ? (() => { try { return JSON.parse(text); } catch { return text; } })() : null;

    if (!res.ok) {
        const msg = typeof data === "string" ? data : (data?.message || `HTTP ${res.status}`);
        throw new Error(msg);
    }

    return data;
}

// Auth helpers
export async function login(email, password) {
    return apiFetch("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
    });
}

export async function register(name, email, password) {
    return apiFetch("/auth/register", {
        method: "POST",
        body: JSON.stringify({ name, email, password }),
    });
}

export async function me() {
    return apiFetch("/api/me");
}

export async function getGegenstaende() {
    return apiFetch("/gegenstaende");
}