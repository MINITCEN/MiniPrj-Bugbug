(function () {
    const userRole = document.body.dataset.userRole || "USER";

    const pageContent = (data) => Array.isArray(data) ? data : (data.content || []);
    const shortDate = (value) => value ? String(value).replace("T", " ").slice(0, 16) : "";

    function clearList(list) {
        list.replaceChildren();
    }

    function emptyItem(text) {
        const li = document.createElement("li");
        li.className = "empty";
        li.textContent = text;
        return li;
    }

    function activityItem(title, href, status, meta) {
        const li = document.createElement("li");
        const left = document.createElement(href ? "a" : "span");
        if (href) {
            left.href = href;
        }
        left.textContent = title;

        const right = document.createElement("span");
        right.className = status ? "status" : "meta";
        right.textContent = status || meta || "";

        li.append(left, right);
        return li;
    }

    function dashToEmpty(value) {
        return value === "-" ? "" : value;
    }

    function emptyAddressToBlank(value) {
        return value === "등록된 주소가 없습니다." ? "" : value;
    }

    function showProfileError(message) {
        const error = document.getElementById("profile-form-error");
        error.textContent = message;
        error.hidden = false;
    }

    function updateProfileView(data) {
        document.getElementById("profile-nickname").textContent = data.nickname;
        document.getElementById("profile-initial").textContent = data.nickname.substring(0, 1);
        document.getElementById("profile-phone").textContent = data.phoneNumber || "-";
        document.getElementById("profile-address").textContent = data.address || "등록된 주소가 없습니다.";
        document.getElementById("profile-email").textContent = data.email;
    }

    function openProfileModal() {
        document.getElementById("profile-input-nickname").value = document.getElementById("profile-nickname").textContent.trim();
        document.getElementById("profile-input-phone").value = dashToEmpty(document.getElementById("profile-phone").textContent.trim());
        document.getElementById("profile-input-address").value = emptyAddressToBlank(document.getElementById("profile-address").textContent.trim());
        document.getElementById("profile-form-error").hidden = true;
        document.getElementById("profile-modal").hidden = false;
        document.getElementById("profile-input-nickname").focus();
    }

    function closeProfileModal() {
        document.getElementById("profile-modal").hidden = true;
    }

    function openHunterGradeModal() {
        document.getElementById("hunter-grade-modal").hidden = false;
    }

    function closeHunterGradeModal() {
        document.getElementById("hunter-grade-modal").hidden = true;
    }

    function submitProfileForm(event) {
        event.preventDefault();

        const payload = {
            nickname: document.getElementById("profile-input-nickname").value.trim(),
            phoneNumber: document.getElementById("profile-input-phone").value.trim(),
            address: document.getElementById("profile-input-address").value.trim()
        };

        fetch("/api/mypage/info", {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        })
            .then(async (res) => {
                if (!res.ok) {
                    const message = await res.text();
                    throw new Error(message || "개인정보 변경에 실패했습니다.");
                }
                return res.json();
            })
            .then((data) => {
                updateProfileView(data);
                closeProfileModal();
                alert("개인정보가 변경되었습니다.");
            })
            .catch((error) => showProfileError(error.message));
    }

    function loadUserDashboard() {
        fetch("/api/mypage/requests?size=3")
            .then((res) => res.json())
            .then((data) => {
                const list = document.getElementById("my-requests-list");
                clearList(list);
                const items = pageContent(data);
                if (!items.length) {
                    list.append(emptyItem("최근 등록한 의뢰가 없습니다."));
                    return;
                }
                items.forEach((req) => {
                    list.append(activityItem(req.title, `/requestView/detail/${req.requestId}`, req.status, shortDate(req.createdAt)));
                });
            });

        fetch("/api/mypage/bookmarks/hunters")
            .then((res) => res.json())
            .then((data) => {
                const list = document.getElementById("my-bookmarks-list");
                clearList(list);
                const items = pageContent(data);
                if (!items.length) {
                    list.append(emptyItem("찜한 헌터가 없습니다."));
                    return;
                }
                items.slice(0, 3).forEach((hunter) => {
                    list.append(activityItem(`${hunter.hunterName} 헌터`, "/hunter", hunter.grade, `${hunter.responseCount || 0}회 완료`));
                });
            });
    }

    function renderReviewBars(ratingCounts) {
        const container = document.getElementById("hunter-review-bars");
        if (!container) {
            return;
        }

        container.replaceChildren();

        if (typeof ratingCounts === "string") {
            const message = document.createElement("p");
            message.className = "empty";
            message.textContent = ratingCounts;
            container.append(message);
            return;
        }

        const counts = ratingCounts || {};
        const scores = [5, 4, 3, 2, 1];
        const total = scores.reduce((sum, score) => sum + Number(counts[score] || 0), 0);

        if (!total) {
            const empty = document.createElement("p");
            empty.className = "empty";
            empty.textContent = "아직 받은 리뷰가 없습니다.";
            container.append(empty);
            return;
        }

        scores.forEach((score) => {
            const count = Number(counts[score] || 0);
            const percent = Math.round((count / total) * 100);

            const label = document.createElement("span");
            label.textContent = `${score}점`;

            const bar = document.createElement("div");
            bar.className = "bar";
            bar.setAttribute("aria-label", `${score}점 리뷰 ${count}개`);

            const fill = document.createElement("span");
            fill.style.width = `${percent}%`;
            bar.append(fill);

            const countText = document.createElement("span");
            countText.textContent = count;

            container.append(label, bar, countText);
        });
    }

    function updateHunterGradeView(data) {
        const count = Number(data.completionCount || 0);
        const grade = data.grade || "루키";
        const gradeMarks = {
            "루키": "R",
            "브론즈": "B",
            "실버": "S",
            "골드": "G",
            "레전드": "L"
        };

        document.getElementById("hunter-grade").textContent = grade;
        document.getElementById("hunter-grade-mark").textContent = gradeMarks[grade] || "-";
        document.getElementById("hunter-completion-cnt").textContent = `총 ${count}건 활동`;
        document.getElementById("hunter-rating").textContent = Number(data.averageRating || 0).toFixed(1);
        document.getElementById("hunter-res-count").textContent = `${count}건`;
    }

    function loadHunterDashboard() {
        fetch("/api/mypage/hunter/profile")
            .then((res) => res.json())
            .then((data) => updateHunterGradeView(data));

        fetch("/api/mypage/hunter/review-summary")
            .then(async (res) => {
                if (!res.ok) {
                    const message = await res.text();
                    throw new Error(message || "리뷰 요약을 불러오지 못했습니다.");
                }
                return res.json();
            })
            .then((data) => renderReviewBars(data))
            .catch((error) => renderReviewBars(error.message || "리뷰 요약을 불러오지 못했습니다."));

        fetch("/api/mypage/hunter/tasks?size=3")
            .then((res) => res.json())
            .then((data) => {
                const list = document.getElementById("hunter-tasks-list");
                clearList(list);
                const items = pageContent(data);
                if (!items.length) {
                    list.append(emptyItem("수행 중인 의뢰가 없습니다."));
                    return;
                }
                items.forEach((task) => {
                    list.append(activityItem(task.title, `/requestView/detail/${task.requestId}`, task.status, task.approxLocation));
                });
            });

        fetch("/api/mypage/hunter/bookmarks/requests?size=3")
            .then((res) => res.json())
            .then((data) => {
                const list = document.getElementById("hunter-saved-requests-list");
                clearList(list);
                const items = pageContent(data);
                if (!items.length) {
                    list.append(emptyItem("찜한 의뢰가 없습니다."));
                    return;
                }
                items.forEach((saved) => {
                    list.append(activityItem(saved.title, `/requestView/detail/${saved.requestId}`, null, saved.approxLocation));
                });
            });
    }

    function applyHunter() {
        if (!confirm("헌터 등록 신청을 진행할까요?")) {
            return;
        }

        fetch("/api/mypage/hunter/apply", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ pledgeAgreed: true })
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("apply failed");
                }
                alert("헌터 신청이 접수되었습니다.");
            })
            .catch(() => alert("헌터 신청 처리 중 문제가 발생했습니다."));
    }

    function resignHunter() {
        if (!confirm("헌터 자격을 해제하고 일반 회원으로 전환할까요?")) {
            return;
        }

        fetch("/api/mypage/hunter/resign", { method: "POST" })
            .then((res) => {
                if (!res.ok) {
                    throw new Error("resign failed");
                }
                alert("헌터 자격이 해제되었습니다.");
                window.location.reload();
            })
            .catch(() => alert("헌터 자격 해제 중 문제가 발생했습니다."));
    }

    function logout() {
        fetch("/api/auth/logout", { method: "POST" })
            .finally(() => {
                window.location.href = "/";
            });
    }

    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("profile-form").addEventListener("submit", submitProfileForm);
        document.getElementById("hunter-grade-modal").addEventListener("click", (event) => {
            if (event.target.id === "hunter-grade-modal") {
                closeHunterGradeModal();
            }
        });

        if (userRole === "HUNTER") {
            loadHunterDashboard();
            return;
        }
        loadUserDashboard();
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeHunterGradeModal();
        }
    });

    window.openProfileModal = openProfileModal;
    window.closeProfileModal = closeProfileModal;
    window.openHunterGradeModal = openHunterGradeModal;
    window.closeHunterGradeModal = closeHunterGradeModal;
    window.applyHunter = applyHunter;
    window.resignHunter = resignHunter;
    window.logout = logout;
}());
