const BASE_URL = "http://localhost:8080"

async function apiRequest(path, options = {}) {
    const token = localStorage.getItem("token");

    const headers = {
        "Content-Type": "application/json",
        ...(token ? {Authorization: `Bearer ${token}`} : {}),
        ...options.headers,
    };

    const response = await fetch(BASE_URL + path,{
        ...options,
        headers,
    });

    if (!response.ok){
        const errorBody = await response.json();
        throw new Error(errorBody.message);
    }

    const text = await response.text()
    return text ? JSON.parse(text) : null;
}