async function initDashboard() {
    if (!isLoggedIn()) {
        window.location.href = "login.html";
        return;
    }

    await loadDashboardData();
}

async function loadDashboardData() {
    const today = new Date();
    const startDate = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split("T")[0];
    const endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split("T")[0];

    try {
        const [summary, expenses] = await Promise.all([
            apiRequest(`/api/expenses/summary?startDate=${startDate}&endDate=${endDate}`),
            apiRequest("/api/expenses"),
        ]);

        document.getElementById("totalAmount").textContent =
            `₩${summary.totalAmount.toLocaleString()}`;

        const categoryList = document.getElementById("categoryList");
        categoryList.innerHTML = "";
        if (summary.categorySummaryList.length === 0) {
            categoryList.innerHTML = `<li class="text-secondary">이번 달 지출 카테고리가 없습니다.</li>`;
        } else {
            summary.categorySummaryList.forEach((category) => {
                const li = document.createElement("li");
                li.className = "d-flex justify-content-between mb-2";
                li.innerHTML = `
                    <span>${category.categoryName}</span>
                    <span>₩${category.totalAmount.toLocaleString()}</span>
                `;
                categoryList.appendChild(li);
            });
        }

        const expenseList = document.getElementById("expenseList");
        expenseList.innerHTML = "";
        if (expenses.length === 0) {
            expenseList.innerHTML = `<li class="text-secondary">등록된 지출이 없습니다.</li>`;
        } else {
            expenses.forEach((expense) => {
                const li = document.createElement("li");
                li.className = "d-flex justify-content-between mb-2";
                li.innerHTML = `
                    <span>${expense.content} <small class="text-secondary">(${expense.categoryName})</small></span>
                    <span>₩${expense.amount.toLocaleString()}</span>
                `;
                expenseList.appendChild(li);
            });
        }
    } catch (error) {
        alert("데이터를 불러오는 중 오류가 발생했습니다: " + error.message);
    }
}

initDashboard();

document.getElementById("categoryForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("categoryName").value;

    try {
        await apiRequest("/api/categories", {
            method: "POST",
            body: JSON.stringify({ name }),
        });

        const modalElement = document.getElementById("categoryModal");
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();

        event.target.reset();
        await loadDashboardData();
    } catch (error) {
        const errorMessage = document.getElementById("categoryErrorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});