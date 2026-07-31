const BASE_URL = "http://localhost:8080"

async function apiRequest(path, options = {}) {
    const token = localStorage.getItem("token");

    const headers = {
        "Content-Type": "application/json",
        ...(token ? {Authorization: `Bearer ${token}`} : {}),
        ...options.headers,
    };
}