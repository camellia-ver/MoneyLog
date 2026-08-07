if (!isLoggedIn()) {
    window.location.href = "login";
}

document.getElementById("usernameForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const userName = document.getElementById("newUserName").value;

    try {
        await apiRequest("/api/users/me/username", {
            method: "PUT",
            body: JSON.stringify({ userName }),
        });

        alert("사용자명이 변경되었습니다.");
        event.target.reset();
    } catch (error) {
        const errorMessage = document.getElementById("usernameErrorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});

document.getElementById("passwordForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;

    try {
        await apiRequest("/api/users/me/password", {
            method: "PUT",
            body: JSON.stringify({ currentPassword, newPassword }),
        });

        alert("비밀번호가 변경되었습니다.");
        event.target.reset();
    } catch (error) {
        const errorMessage = document.getElementById("passwordErrorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});

document.getElementById("deleteAccountForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const password = document.getElementById("deletePassword").value;

    try {
        await apiRequest("/api/users/me", {
            method: "DELETE",
            body: JSON.stringify({ password }),
        });

        clearToken();
        window.location.href = "/";
    } catch (error) {
        const errorMessage = document.getElementById("deleteErrorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});