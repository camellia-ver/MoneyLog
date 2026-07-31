function saveToken(token){
    localStorage.setItem("token", token);
}

function getToken(){
    return localStorage.getItem("token");
}

function clearToken(){
    localStorage.removeItem("toekn");
}

function isLoggedIn(){
    return getToken() !== null
}