let currentExpense = [];
let currentCategories = [];

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
        const [summary, expenses, categories] = await Promise.all([
            apiRequest(`/api/expenses/summary?startDate=${startDate}&endDate=${endDate}`),
            apiRequest("/api/expenses"),
            apiRequest("/api/categories"),
        ]);
        currentExpenses = expenses; 
        currentCategories = categories;

        // 이번 달 총 소비
        document.getElementById("totalAmount").textContent =
            `₩${summary.totalAmount.toLocaleString()}`;

        // 카테고리별 지출
        const categoryList = document.getElementById("categoryList");
        categoryList.innerHTML = "";
        if (summary.categorySummaryList.length === 0) {
            categoryList.innerHTML = `<li class="text-secondary">이번 달 지출 카테고리가 없습니다.</li>`;
        } else {
            summary.categorySummaryList.forEach((categorySummary) => {
                const li = document.createElement("li");
                li.className = "d-flex justify-content-between mb-2";
                li.innerHTML = `
                    <span>${categorySummary.categoryName}</span>
                    <span>
                        ₩${categorySummary.totalAmount.toLocaleString()}
                    </span>
                `;
                categoryList.appendChild(li);
            });
        }

        // 최근 지출 목록
        const expenseList = document.getElementById("expenseList");
        expenseList.innerHTML = "";
        if (expenses.length === 0) {
            expenseList.innerHTML = `<li class="text-secondary">등록된 지출이 없습니다.</li>`;
        } else {
            expenses.forEach((expense) => {
                const li = document.createElement("li");
                li.className = "mb-2";

                const createdDate = new Date(expense.createdAt).toLocaleDateString();

                li.innerHTML = `
                    <div class="d-flex justify-content-between" style="cursor: pointer;"
                         data-bs-toggle="collapse" data-bs-target="#detail-${expense.id}">
                        <span>${expense.content} <small class="text-secondary">(${expense.categoryName})</small></span>
                        <span>₩${expense.amount.toLocaleString()}</span>
                    </div>
                    <div class="collapse mt-2 ps-2" id="detail-${expense.id}">
                        <p class="text-secondary mb-1">메모: ${expense.memo || "없음"}</p>
                        <p class="text-secondary mb-2">등록일: ${createdDate}</p>
                        <button class="btn btn-sm btn-outline-secondary me-2" data-expense-id="${expense.id}" data-action="edit">
                            수정
                        </button>
                        <button class="btn btn-sm btn-outline-danger" data-expense-id="${expense.id}" data-action="delete">
                            삭제
                        </button>
                    </div>
                `;
                expenseList.appendChild(li);
            });
        }

        const categoryManageList = document.getElementById("categoryManageList");
        categoryManageList.innerHTML = "";
        categories.forEach((category) => {
            const li = document.createElement("li");
            li.className = "d-flex justify-content-between mb-2";
            li.innerHTML = `
                <span>${category.name}</span>
                <button class="btn btn-sm btn-outline-secondary me-2" data-category-id="${category.id}" data-action="edit">수정</button>
                <button class="btn btn-sm btn-outline-danger" data-category-id="${category.id}" data-action="delete">삭제</button>
            `;
            categoryManageList.appendChild(li);
        });
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

document.getElementById("expenseModal").addEventListener("shown.bs.modal", async function () {
    const categories = await apiRequest("/api/categories");

    const select = document.getElementById("expenseCategoryId");
    select.innerHTML = "";

    categories.forEach((category) => {
        const option = document.createElement("option");
        option.value = category.id;
        option.textContent = category.name;
        select.appendChild(option);
    });
});

document.getElementById("expenseForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const categoryId = document.getElementById("expenseCategoryId").value;
    const amount = Number(document.getElementById("expenseAmount").value);
    const content = document.getElementById("expenseContent").value;
    const memo = document.getElementById("expenseMemo").value;

    const editingId = event.target.dataset.editingId;

    try {
        if (editingId) {
            await apiRequest(`/api/expenses/${editingId}`, {
                method: "PUT",
                body: JSON.stringify({ categoryId, amount, content, memo }),
            });
        } else {
            await apiRequest("/api/expenses", {
                method: "POST",
                body: JSON.stringify({ categoryId, amount, content, memo }),
            });
        }

        const modalElement = document.getElementById("expenseModal");
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();

        event.target.reset();
        delete event.target.dataset.editingId;
        document.querySelector("#expenseModal .modal-title").textContent = "지출 추가"; 
        document.querySelector("#expenseForm button[type=submit]").textContent = "추가하기"; 
        
        await loadDashboardData();
    } catch (error) {
        const errorMessage = document.getElementById("expenseErrorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});

document.getElementById("expenseList").addEventListener("click", async function (event) {
    const action = event.target.dataset.action;
    
    if (action === "delete") {
        const expenseId = event.target.dataset.expenseId;

        if (!confirm("정말 삭제하시겠습니까?")) {
            return;
        }

        try {
            await apiRequest(`/api/expenses/${expenseId}`, { method: "DELETE" });
            await loadDashboardData();
        } catch (error) {
            alert("삭제 중 오류가 발생했습니다: " + error.message);
        }
        return; 
    }

    if (action === "edit") {
        const expenseId = event.target.dataset.expenseId;
        const expense = currentExpenses.find(e => e.id === Number(expenseId));

        // 1. 카테고리 select 채우기 (기존 categories 배열 재사용)
        const select = document.getElementById("expenseCategoryId");
        select.innerHTML = "";
        currentCategories.forEach((category) => {
            const option = document.createElement("option");
            option.value = category.id;
            option.textContent = category.name;
            select.appendChild(option);
        });

        // 2. 필드 채우기
        const matchedCategory = currentCategories.find(c => c.name === expense.categoryName);
        select.value = matchedCategory.id;
        document.getElementById("expenseAmount").value = expense.amount;
        document.getElementById("expenseContent").value = expense.content;
        document.getElementById("expenseMemo").value = expense.memo || "";

        // 3. 수정 중인 id 기록
        document.getElementById("expenseForm").dataset.editingId = expense.id;

        // 4. 텍스트 변경
        document.querySelector("#expenseModal .modal-title").textContent = "지출 수정";
        document.querySelector("#expenseForm button[type=submit]").textContent = "수정하기";

        // 5. 모달 열기
        const modal = new bootstrap.Modal(document.getElementById("expenseModal"));
        modal.show();
        return;
    }
});

document.getElementById("categoryList").addEventListener("click", async function (event) {
    if (event.target.dataset.action !== "delete") {
        return;
    }

    const categoryId = event.target.dataset.categoryId;

    if (!confirm("정말 삭제하시겠습니까?")) {
        return;
    }

    try {
        await apiRequest(`/api/categories/${categoryId}`, { method: "DELETE" });
        await loadDashboardData();
    } catch (error) {
        alert("삭제 중 오류가 발생했습니다: " + error.message);
    }
});

document.getElementById("categoryForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("categoryName").value;
    const editingId = event.target.dataset.editingId;

    try {
        if (editingId) {
            await apiRequest(`/api/categories/${editingId}`, {
                method: "PUT",
                body: JSON.stringify({ name }),
            });
        } else {
            await apiRequest("/api/categories", {
                method: "POST",
                body: JSON.stringify({ name }),
            });
        }

        const modalElement = document.getElementById("categoryModal");
        const modal = bootstrap.Modal.getInstance(modalElement);
        modal.hide();

        event.target.reset();
        delete event.target.dataset.editingId;
        document.querySelector("#categoryModal .modal-title").textContent = "카테고리 추가";
        document.querySelector("#categoryForm button[type=submit]").textContent = "추가하기";

        await loadDashboardData();
    } catch (error) {
        const errorMessage = document.getElementById("categoryErrorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});