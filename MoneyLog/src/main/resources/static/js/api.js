const BASE_URL =
    window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
        ? "http://localhost:8080"
        : "";

async function apiRequest(path, options = {}) {
  const token = localStorage.getItem("token");

  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(BASE_URL + path, {
    ...options,
    headers,
  });

  const isAuthEndpoint = path.startsWith("/api/users/login") || path.startsWith("/api/users/signup");

  if (response.status === 401 && !isAuthEndpoint) {
    clearToken();

    const publicPages = ["/", "/login", "/signup"];
    if (!publicPages.includes(window.location.pathname)) {
      alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
      window.location.href = "login";
    }

    throw new Error("인증이 만료되었습니다.");
  }

  if (!response.ok) {
    const text = await response.text();
    const errorBody = text ? JSON.parse(text) : { message: "알 수 없는 오류가 발생했습니다." };
    throw new Error(errorBody.message);
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}