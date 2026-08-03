document.getElementById("loginForm").addEventListener("submit", async function (event) {
  event.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  try {
    const result = await apiRequest("/api/users/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });

    saveToken(result.token);
    window.location.href = "dashboard.html";
  } catch (error) {
    const errorMessage = document.getElementById("errorMessage");
    errorMessage.classList.remove("d-none");
    errorMessage.textContent = error.message;
  }
});