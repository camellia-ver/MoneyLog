let currentExpenses = [];
let currentCategories = [];
let categoryChart = null;

async function initDashboard() {
    if (!isLoggedIn()) {
        window.location.href = "login";
        return;
    }

    const today = new Date();
    const startDate = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split("T")[0];
    const endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split("T")[0];

    document.getElementById("filterStartDate").value = startDate;
    document.getElementById("filterEndDate").value = endDate;

    await renderMonthlySummary();
    await loadDashboardData(startDate, endDate, "");
}

async function loadDashboardData(startDate, endDate, categoryId) {
    try {
        const expenseUrl = categoryId
            ? `/api/expenses?categoryId=${categoryId}&startDate=${startDate}&endDate=${endDate}`
            : `/api/expenses?startDate=${startDate}&endDate=${endDate}`;

        const [summary, expenses, categories] = await Promise.all([
            apiRequest(`/api/expenses/summary?startDate=${startDate}&endDate=${endDate}`),
            apiRequest(expenseUrl),
            apiRequest("/api/categories"),
        ]);
        currentExpenses = expenses;
        currentCategories = categories;

        // 필터의 카테고리 select 채우기 (매번 최신 목록 유지)
        const filterCategorySelect = document.getElementById("filterCategoryId");
        const selectedValue = filterCategorySelect.value;
        filterCategorySelect.innerHTML = `<option value="">전체</option>`;
        categories.forEach((category) => {
            const option = document.createElement("option");
            option.value = category.id;
            option.textContent = category.name;
            filterCategorySelect.appendChild(option);
        });
        filterCategorySelect.value = selectedValue;

        // 카테고리별 지출
        const categoryList = document.getElementById("categoryList");
        categoryList.innerHTML = "";
        if (summary.categorySummaryList.length === 0) {
            categoryList.innerHTML = `<li class="text-secondary">조회된 카테고리가 없습니다.</li>`;
        } else {
            summary.categorySummaryList.forEach((categorySummary) => {
                const li = document.createElement("li");
                li.className = "d-flex justify-content-between mb-2";
                li.innerHTML = `
                    <span>${categorySummary.categoryName}</span>
                    <span>₩${categorySummary.totalAmount.toLocaleString()}</span>
                `;
                categoryList.appendChild(li);
            });
        }
        renderCategoryChart(summary.categorySummaryList);

        // 지출 목록
        const expenseList = document.getElementById("expenseList");
        expenseList.innerHTML = "";
        if (expenses.length === 0) {
            expenseList.innerHTML = `<li class="text-secondary">조회된 지출이 없습니다.</li>`;
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

        // 카테고리 관리
        const categoryManageList = document.getElementById("categoryManageList");
        categoryManageList.innerHTML = "";
        categories.forEach((category) => {
            const li = document.createElement("li");
            li.className = "d-flex justify-content-between mb-2";
            li.innerHTML = `
                <span>${category.name}</span>
                <span>
                    <button class="btn btn-sm btn-outline-secondary me-2" data-category-id="${category.id}" data-action="edit">수정</button>
                    <button class="btn btn-sm btn-outline-danger" data-category-id="${category.id}" data-action="delete">삭제</button>
                </span>
            `;
            categoryManageList.appendChild(li);
        });
    } catch (error) {
        alert("데이터를 불러오는 중 오류가 발생했습니다: " + error.message);
    }
}

initDashboard();

// 필터 폼 제출
document.getElementById("filterForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const startDate = document.getElementById("filterStartDate").value;
    const endDate = document.getElementById("filterEndDate").value;
    const categoryId = document.getElementById("filterCategoryId").value;

    await loadDashboardData(startDate, endDate, categoryId);
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

        const startDate = document.getElementById("filterStartDate").value;
        const endDate = document.getElementById("filterEndDate").value;
        const categoryId = document.getElementById("filterCategoryId").value;
        await loadDashboardData(startDate, endDate, categoryId);
    } catch (error) {
        const errorMessage = document.getElementById("categoryErrorMessage");
        errorMessage.classList.remove("d-none");
        errorMessage.textContent = error.message;
    }
});

document.getElementById("expenseModal").addEventListener("shown.bs.modal", async function () {
    const select = document.getElementById("expenseCategoryId");
    select.innerHTML = "";
    currentCategories.forEach((category) => {
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

        const startDate = document.getElementById("filterStartDate").value;
        const endDate = document.getElementById("filterEndDate").value;
        const categoryId2 = document.getElementById("filterCategoryId").value;
        await loadDashboardData(startDate, endDate, categoryId2);
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
            const startDate = document.getElementById("filterStartDate").value;
            const endDate = document.getElementById("filterEndDate").value;
            const categoryId = document.getElementById("filterCategoryId").value;
            await loadDashboardData(startDate, endDate, categoryId);
        } catch (error) {
            alert("삭제 중 오류가 발생했습니다: " + error.message);
        }
        return;
    }

    if (action === "edit") {
        const expenseId = event.target.dataset.expenseId;
        const expense = currentExpenses.find(e => e.id === Number(expenseId));

        const select = document.getElementById("expenseCategoryId");
        select.innerHTML = "";
        currentCategories.forEach((category) => {
            const option = document.createElement("option");
            option.value = category.id;
            option.textContent = category.name;
            select.appendChild(option);
        });

        const matchedCategory = currentCategories.find(c => c.name === expense.categoryName);
        select.value = matchedCategory.id;
        document.getElementById("expenseAmount").value = expense.amount;
        document.getElementById("expenseContent").value = expense.content;
        document.getElementById("expenseMemo").value = expense.memo || "";

        document.getElementById("expenseForm").dataset.editingId = expense.id;

        document.querySelector("#expenseModal .modal-title").textContent = "지출 수정";
        document.querySelector("#expenseForm button[type=submit]").textContent = "수정하기";

        const modal = new bootstrap.Modal(document.getElementById("expenseModal"));
        modal.show();
        return;
    }
});

document.getElementById("categoryManageList").addEventListener("click", async function (event) {
    const action = event.target.dataset.action;

    if (action === "delete") {
        const categoryId = event.target.dataset.categoryId;

        if (!confirm("정말 삭제하시겠습니까?")) {
            return;
        }

        try {
            await apiRequest(`/api/categories/${categoryId}`, { method: "DELETE" });
            const startDate = document.getElementById("filterStartDate").value;
            const endDate = document.getElementById("filterEndDate").value;
            const filterCategoryId = document.getElementById("filterCategoryId").value;
            await loadDashboardData(startDate, endDate, filterCategoryId);
        } catch (error) {
            alert("삭제 중 오류가 발생했습니다: " + error.message);
        }
        return;
    }

    if (action === "edit") {
        const categoryId = event.target.dataset.categoryId;
        const category = currentCategories.find(c => c.id === Number(categoryId));

        document.getElementById("categoryName").value = category.name;
        document.getElementById("categoryForm").dataset.editingId = category.id;

        document.querySelector("#categoryModal .modal-title").textContent = "카테고리 수정";
        document.querySelector("#categoryForm button[type=submit]").textContent = "수정하기";

        const modal = new bootstrap.Modal(document.getElementById("categoryModal"));
        modal.show();
        return;
    }
});

function renderCategoryChart(categorySummaryList) {
    const ctx = document.getElementById("categoryChart");

    // 기존 차트가 있다면 먼저 파괴 (겹쳐 그려지는 것 방지)
    if (categoryChart) {
        categoryChart.destroy();
    }

    const labels = categorySummaryList.map(c => c.categoryName);
    const data = categorySummaryList.map(c => c.totalAmount);

    categoryChart = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: ["#FF8FB8", "#72D6C9", "#FFD166", "#7CC8FF", "#A6E3A1"],
            }],
        },
    });
}

function getPreviousMonthRange(year, month) {
    // month는 0부터 시작(JS Date 관례) - 1월이 0
    const prevMonthDate = new Date(year, month - 1, 1);
    const prevYear = prevMonthDate.getFullYear();
    const prevMonth = prevMonthDate.getMonth();

    const start = new Date(prevYear, prevMonth, 1).toISOString().split("T")[0];
    const end = new Date(prevYear, prevMonth + 1, 0).toISOString().split("T")[0];

    return { start, end };
}

async function renderMonthlySummary() {
    const today = new Date();
    const currentStart = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().split("T")[0];
    const currentEnd = new Date(today.getFullYear(), today.getMonth() + 1, 0).toISOString().split("T")[0];

    const { start: prevStart, end: prevEnd } = getPreviousMonthRange(today.getFullYear(), today.getMonth());

    const [currentSummary, prevSummary] = await Promise.all([
        apiRequest(`/api/expenses/summary?startDate=${currentStart}&endDate=${currentEnd}`),
        apiRequest(`/api/expenses/summary?startDate=${prevStart}&endDate=${prevEnd}`),
    ]);

    const currentTotal = currentSummary.totalAmount;
    const prevTotal = prevSummary.totalAmount;

    document.getElementById("totalAmount").textContent = `₩${currentTotal.toLocaleString()}`;

    const changeRateElement = document.getElementById("changeRate");

    if (prevTotal === 0) {
        changeRateElement.textContent = "";
        return;
    }

    const changeRate = ((currentTotal - prevTotal) / prevTotal * 100).toFixed(1);
    const isIncrease = Number(changeRate) > 0;
    const isDecrease = Number(changeRate) < 0;

    const sign = isIncrease ? "+" : ""; // 음수는 toFixed 결과 자체에 "-"가 이미 포함됨
    const color = isIncrease
        ? "var(--color-expense)"
        : isDecrease
            ? "var(--color-success)"
            : "var(--color-text-secondary)";

    changeRateElement.textContent = `${sign}${changeRate}%`;
    changeRateElement.style.color = color;
}