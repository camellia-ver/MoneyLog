document.getElementById("signupForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const email = document.getElementById("email").value;
    const userName = document.getElementById("userName").value;
    const password = document.getElementById("password").value;

    try {
            await apiRequest("/api/users/signup", {
            method: "POST",
            body: JSON.stringify({ email, userName, password }),
        });

        alert("회원가입이 완료되었습니다. 로그인해주세요.")
        window.location.href = "login.html";
    } catch (error) {
        const errorMessage = document.getElementById("errorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});