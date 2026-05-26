(function () {
    const state = {
        page: 0,
        size: 9,
        totalPages: 1,
        totalElements: 0,
        sort: "id,desc",
        keyword: ""
    };

    const list = document.getElementById("hunter-list");
    const stateBox = document.getElementById("hunter-state");
    const pageInfo = document.getElementById("hunter-page-info");
    const prevBtn = document.getElementById("hunter-prev-btn");
    const nextBtn = document.getElementById("hunter-next-btn");
    const totalCount = document.getElementById("hunter-total-count");
    const searchInput = document.getElementById("hunter-search-input");
    const sortSelect = document.getElementById("hunter-sort-select");

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#039;");
    }

    function showState(message) {
        stateBox.textContent = message;
        stateBox.classList.remove("is-hidden");
    }

    function hideState() {
        stateBox.classList.add("is-hidden");
    }

    function setPaging(data) {
        state.page = data.number || 0;
        state.totalPages = data.totalPages || 1;
        state.totalElements = data.totalElements || 0;
        pageInfo.textContent = `${state.page + 1} / ${state.totalPages}`;
        prevBtn.disabled = data.first || state.totalPages <= 1;
        nextBtn.disabled = data.last || state.totalPages <= 1;
        totalCount.textContent = state.totalElements.toLocaleString("ko-KR");
    }

    function filteredHunters(items) {
        const keyword = state.keyword.trim().toLowerCase();
        if (!keyword) {
            return items;
        }

        return items.filter((hunter) => {
            const name = String(hunter.name || "").toLowerCase();
            const grade = String(hunter.grade || "").toLowerCase();
            return name.includes(keyword) || grade.includes(keyword);
        });
    }

    function isBookmarked(hunter) {
        return Boolean(hunter.isBookmarked ?? hunter.bookmarked);
    }

    function renderHunters(items) {
        const hunters = filteredHunters(items);
        list.innerHTML = "";

        if (hunters.length === 0) {
            showState(state.keyword ? "검색 조건에 맞는 헌터가 없습니다." : "등록된 헌터가 없습니다.");
            return;
        }

        hideState();
        const fragment = document.createDocumentFragment();

        hunters.forEach((hunter) => {
            const bookmarked = isBookmarked(hunter);
            const card = document.createElement("article");
            card.className = "hunter-card";
            card.innerHTML = `
                <div class="hunter-card__head">
                    <div>
                        <h2 class="hunter-card__name">${escapeHtml(hunter.name || "이름 없는 헌터")}</h2>
                        <span class="hunter-card__grade">${escapeHtml(hunter.grade || "일반 헌터")}</span>
                    </div>
                    <button
                        class="hunter-bookmark ${bookmarked ? "is-active" : ""}"
                        type="button"
                        aria-label="${bookmarked ? "찜 취소" : "찜하기"}"
                        data-hunter-id="${escapeHtml(hunter.hunterId)}"
                    >${bookmarked ? "♥" : "♡"}</button>
                </div>
                <div class="hunter-card__stats">
                    <div class="hunter-stat">
                        <span>퇴치 완료</span>
                        <strong>${Number(hunter.completionCount || 0).toLocaleString("ko-KR")}회</strong>
                    </div>
                </div>
            `;
            fragment.appendChild(card);
        });

        list.appendChild(fragment);
    }

    function loadHunters(page) {
        const targetPage = Math.max(0, page);
        const params = new URLSearchParams({
            page: String(targetPage),
            size: String(state.size),
            sort: state.sort
        });

        showState("헌터 목록을 불러오는 중입니다.");

        fetch(`/api/hunters?${params.toString()}`)
            .then((response) => {
                if (!response.ok) {
                    throw new Error("헌터 목록을 불러오지 못했습니다.");
                }
                return response.json();
            })
            .then((data) => {
                setPaging(data);
                renderHunters(data.content || []);
            })
            .catch(() => {
                list.innerHTML = "";
                totalCount.textContent = "-";
                showState("헌터 목록을 불러오는 중 문제가 발생했습니다.");
                prevBtn.disabled = true;
                nextBtn.disabled = true;
            });
    }

    function toggleBookmark(button) {
        const hunterId = button.dataset.hunterId;
        if (!hunterId) {
            return;
        }

        button.disabled = true;
        fetch(`/api/hunters/${encodeURIComponent(hunterId)}/bookmarks`, { method: "POST" })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("찜 상태를 변경하지 못했습니다.");
                }

                const active = !button.classList.contains("is-active");
                button.classList.toggle("is-active", active);
                button.textContent = active ? "♥" : "♡";
                button.setAttribute("aria-label", active ? "찜 취소" : "찜하기");
            })
            .catch(() => {
                alert("찜 상태를 변경하지 못했습니다.");
            })
            .finally(() => {
                button.disabled = false;
            });
    }

    let searchTimer;

    prevBtn.addEventListener("click", () => loadHunters(state.page - 1));
    nextBtn.addEventListener("click", () => loadHunters(state.page + 1));

    sortSelect.addEventListener("change", () => {
        state.sort = sortSelect.value;
        loadHunters(0);
    });

    searchInput.addEventListener("input", () => {
        clearTimeout(searchTimer);
        searchTimer = window.setTimeout(() => {
            state.keyword = searchInput.value;
            loadHunters(0);
        }, 180);
    });

    list.addEventListener("click", (event) => {
        const button = event.target.closest(".hunter-bookmark");
        if (button) {
            toggleBookmark(button);
        }
    });

    document.addEventListener("DOMContentLoaded", () => loadHunters(0));
    window.addEventListener("pageshow", (event) => {
        if (event.persisted) {
            loadHunters(state.page);
        }
    });
})();
