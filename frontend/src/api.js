// src/api.js

const RAW_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";
export const BASE_URL = RAW_BASE_URL.replace(/\/$/, ""); // ✅ trailing slash entfernen

// -------- Token Storage --------
export function setToken(token) {
    if (!token) return;
    localStorage.setItem("token", token);
}

export function clearToken() {
    localStorage.removeItem("token");
    localStorage.removeItem("user"); // optional: falls du user speicherst
}

export function getToken() {
    return localStorage.getItem("token");
}

// -------- Helpers --------
function buildUrl(path) {
    // erlaubt: "/gegenstaende" oder "gegenstaende"
    const p = String(path || "");
    if (!p) return BASE_URL;
    return `${BASE_URL}${p.startsWith("/") ? "" : "/"}${p}`;
}

async function readBodySafe(res) {
    const text = await res.text().catch(() => "");
    if (!text) return null;

    try {
        return JSON.parse(text);
    } catch {
        return text; // z.B. Plaintext Fehlermeldung
    }
}

function extractErrorMessage(data, res) {
    if (typeof data === "string" && data.trim()) return data;
    if (data?.message) return data.message;
    if (data?.error) return data.error;
    if (data?.errors && Array.isArray(data.errors)) return data.errors.join(", ");
    return `HTTP ${res.status}`;
}

// -------- Core API --------
export async function apiFetch(path, options = {}) {
    const token = getToken();

    // Headers zusammenbauen
    const headers = {
        ...(options.headers || {}),
    };

    // JSON automatisch setzen, wenn body vorhanden und Content-Type fehlt
    if (options.body != null && !headers["Content-Type"]) {
        headers["Content-Type"] = "application/json";
    }

    // Authorization automatisch setzen, falls Token vorhanden
    if (token && !headers.Authorization && !headers.authorization) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const res = await fetch(buildUrl(path), {
        ...options,
        headers,
    });

    const data = await readBodySafe(res);

    if (!res.ok) {
        // 401 -> Token evtl. ungültig/abgelaufen, optional löschen
        if (res.status === 401) {
            // clearToken(); // ✅ aktivieren, wenn du bei 401 automatisch ausloggen willst
        }
        throw new Error(extractErrorMessage(data, res));
    }

    return data;
}

// -------- Auth helpers --------
export async function login(email, password) {
    const data = await apiFetch("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
    });

    // ✅ wenn Backend token liefert -> speichern
    if (data?.token) setToken(data.token);

    // optional: user info speichern
    if (data?.userId || data?.email || data?.name) {
        localStorage.setItem(
            "user",
            JSON.stringify({
                userId: data.userId ?? null,
                email: data.email ?? null,
                name: data.name ?? null,
            })
        );
    }

    return data;
}

export async function register(name, email, password) {
    const data = await apiFetch("/auth/register", {
        method: "POST",
        body: JSON.stringify({ name, email, password }),
    });

    // ✅ nach Registrierung direkt token speichern (falls Response token enthält)
    if (data?.token) setToken(data.token);

    // optional: user info speichern
    if (data?.userId || data?.email || data?.name) {
        localStorage.setItem(
            "user",
            JSON.stringify({
                userId: data.userId ?? null,
                email: data.email ?? null,
                name: data.name ?? null,
            })
        );
    }

    return data;
}

export async function logout() {
    clearToken();
}

// -------- Gegenstände API --------
export async function getGegenstaende() {
    return apiFetch("/gegenstaende", { method: "GET" });
}

export async function createGegenstand(payload) {
    return apiFetch("/gegenstaende", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

export async function updateGegenstand(id, payload) {
    return apiFetch(`/gegenstaende/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
    });
}

export async function deleteGegenstand(id) {
    return apiFetch(`/gegenstaende/${id}`, {
        method: "DELETE",
    });
}

// -------- optional: current user --------
export async function me() {
    return apiFetch("/api/me", { method: "GET" });
}