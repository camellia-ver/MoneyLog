async function initDashboard() {
    if(!isLoggedIn()){
        window.location.href = "login.html";
        return;
    }
}

initDashboard()