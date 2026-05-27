let detailMap = null;
let detailMarker = null;
let detailGeocoder = null;

function initDetailMap() {
    const mapContainer = document.getElementById("detailMap");
    const locationInput = document.getElementById("detailLocation");

    if (!mapContainer || !locationInput) {
        return;
    }

    const locationText = locationInput.value;

    if (!locationText || locationText.trim() === "") {
        mapContainer.textContent = "위치 정보가 없습니다.";
        return;
    }

    detailMap = new kakao.maps.Map(mapContainer, {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 5
    });

    detailGeocoder = new kakao.maps.services.Geocoder();

    detailGeocoder.addressSearch(locationText, function (result, status) {
        if (status !== kakao.maps.services.Status.OK) {
            mapContainer.textContent = "지도를 불러올 수 없습니다.";
            return;
        }

        const latitude = result[0].y;
        const longitude = result[0].x;
        const coords = new kakao.maps.LatLng(latitude, longitude);

        detailMap.setCenter(coords);

        if (detailMarker) {
            detailMarker.setMap(null);
        }

        detailMarker = new kakao.maps.Marker({
            map: detailMap,
            position: coords
        });
    });
}

function getRequestId() {
    return document.getElementById("requestId")?.value;
}

function getLoginUserId() {
    const value = document.getElementById("loginUserId")?.value;
    return value ? Number(value) : null;
}

function isDeletedComment(comment) {
    return Boolean(comment.deleted ?? comment.isDeleted);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatCommentDate(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
}

function setCommentMessage(message, type = "info") {
    const messageBox = document.getElementById("commentMessage");
    if (!messageBox) {
        return;
    }

    if (!message) {
        messageBox.hidden = true;
        messageBox.textContent = "";
        messageBox.className = "comment-message";
        return;
    }

    messageBox.hidden = false;
    messageBox.textContent = message;
    messageBox.className = `comment-message ${type}`;
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    if (!response.ok) {
        let message = "댓글 처리 중 오류가 발생했습니다.";

        try {
            const text = await response.text();
            if (text) {
                message = text;
            }
        } catch (error) {
            console.error(error);
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

function countComments(comments) {
    return comments.reduce((total, comment) => total + 1 + countComments(comment.children || []), 0);
}

function updateCommentCount(comments) {
    const countElement = document.getElementById("commentCount");
    if (countElement) {
        countElement.textContent = String(countComments(comments));
    }
}

function createInlineForm(type, comment) {
    const form = document.createElement("form");
    form.className = "comment-inline-form";
    form.dataset.formType = type;

    const textarea = document.createElement("textarea");
    textarea.className = "comment-textarea";
    textarea.rows = 3;
    textarea.maxLength = 1000;
    textarea.required = true;
    textarea.placeholder = type === "reply" ? "답글을 입력하세요." : "댓글 내용을 수정하세요.";
    textarea.value = type === "edit" ? (comment.content || "") : "";

    const actions = document.createElement("div");
    actions.className = "comment-inline-actions";

    const submitButton = document.createElement("button");
    submitButton.type = "submit";
    submitButton.className = "btn-primary comment-inline-btn";
    submitButton.textContent = type === "reply" ? "답글 등록" : "수정 저장";

    const cancelButton = document.createElement("button");
    cancelButton.type = "button";
    cancelButton.className = "btn-outline comment-inline-btn";
    cancelButton.textContent = "취소";
    cancelButton.addEventListener("click", function () {
        form.remove();
    });

    actions.append(submitButton, cancelButton);
    form.append(textarea, actions);
    return form;
}

function closeOtherInlineForms(currentItem) {
    document.querySelectorAll(".comment-inline-form").forEach(function (form) {
        if (!currentItem || !currentItem.contains(form)) {
            form.remove();
        }
    });
}

function toggleInlineForm(commentItem, type, comment) {
    closeOtherInlineForms(commentItem);

    const existingForm = commentItem.querySelector(`.comment-inline-form[data-form-type="${type}"]`);
    if (existingForm) {
        existingForm.remove();
        return;
    }

    commentItem.querySelectorAll(".comment-inline-form").forEach(function (form) {
        form.remove();
    });

    const form = createInlineForm(type, comment);
    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const textarea = form.querySelector("textarea");
        const content = textarea.value.trim();

        if (!content) {
            alert("내용을 입력하세요.");
            textarea.focus();
            return;
        }

        try {
            if (type === "reply") {
                await createReply(comment.commentId, content);
            } else {
                await updateComment(comment.commentId, content);
            }
            form.remove();
        } catch (error) {
            alert(error.message);
        }
    });

    commentItem.appendChild(form);
    form.querySelector("textarea")?.focus();
}

function buildCommentItem(comment, level = 0) {
    const item = document.createElement("article");
    item.className = `comment-item${level > 0 ? " reply" : ""}`;
    item.dataset.commentId = String(comment.commentId);

    const deleted = isDeletedComment(comment);
    const loginUserId = getLoginUserId();
    const isOwner = loginUserId !== null && loginUserId === Number(comment.userId);

    const meta = document.createElement("div");
    meta.className = "comment-meta";
    meta.innerHTML = `
        <div class="comment-author-group">
            <strong class="comment-author">${escapeHtml(comment.userNickname || "알 수 없음")}</strong>
            <span class="comment-date">${escapeHtml(formatCommentDate(comment.createdAt))}</span>
        </div>
    `;

    const actions = document.createElement("div");
    actions.className = "comment-actions";

    if (!deleted) {
        const replyButton = document.createElement("button");
        replyButton.type = "button";
        replyButton.className = "comment-action-btn reply-action";
        replyButton.textContent = "답글";
        replyButton.addEventListener("click", function () {
            toggleInlineForm(item, "reply", comment);
        });
        actions.appendChild(replyButton);
    }

    if (!deleted && isOwner) {
        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.className = "comment-action-btn edit-action";
        editButton.textContent = "수정";
        editButton.addEventListener("click", function () {
            toggleInlineForm(item, "edit", comment);
        });

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "comment-action-btn delete-action";
        deleteButton.textContent = "삭제";
        deleteButton.addEventListener("click", function () {
            deleteComment(comment.commentId);
        });

        actions.append(editButton, deleteButton);
    }

    meta.appendChild(actions);

    const content = document.createElement("div");
    content.className = `comment-content${deleted ? " deleted" : ""}`;
    content.textContent = comment.content || "";

    item.append(meta, content);

    const children = comment.children || [];
    if (children.length > 0) {
        const childrenContainer = document.createElement("div");
        childrenContainer.className = "comment-children";
        children.forEach(function (child) {
            childrenContainer.appendChild(buildCommentItem(child, level + 1));
        });
        item.appendChild(childrenContainer);
    }

    return item;
}

function renderComments(comments) {
    updateCommentCount(comments);

    const list = document.getElementById("commentList");
    if (!list) {
        return;
    }

    list.innerHTML = "";

    if (!comments.length) {
        const empty = document.createElement("div");
        empty.className = "comment-empty";
        empty.textContent = "아직 등록된 댓글이 없습니다.";
        list.appendChild(empty);
        return;
    }

    comments.forEach(function (comment) {
        list.appendChild(buildCommentItem(comment));
    });
}

async function loadComments() {
    const requestId = getRequestId();
    if (!requestId) {
        return;
    }

    try {
        setCommentMessage("");
        const comments = await requestJson(`/api/requests/${requestId}/comments`, {
            method: "GET",
            headers: {}
        });
        renderComments(comments || []);
    } catch (error) {
        console.error(error);
        setCommentMessage(error.message, "error");
    }
}

async function createComment(content) {
    const requestId = getRequestId();
    await requestJson(`/api/requests/${requestId}/comments`, {
        method: "POST",
        body: JSON.stringify({ content })
    });
    await loadComments();
}

async function createReply(commentId, content) {
    const requestId = getRequestId();
    await requestJson(`/api/requests/${requestId}/comments/${commentId}/replies`, {
        method: "POST",
        body: JSON.stringify({ content })
    });
    await loadComments();
}

async function updateComment(commentId, content) {
    const requestId = getRequestId();
    await requestJson(`/api/requests/${requestId}/comments/${commentId}`, {
        method: "PATCH",
        body: JSON.stringify({ content })
    });
    await loadComments();
}

async function deleteComment(commentId) {
    const requestId = getRequestId();
    if (!window.confirm("댓글을 삭제하시겠습니까?")) {
        return;
    }

    await requestJson(`/api/requests/${requestId}/comments/${commentId}`, {
        method: "DELETE"
    });
    await loadComments();
}

function bindCommentForm() {
    const form = document.getElementById("commentForm");
    const textarea = document.getElementById("commentContent");

    if (!form || !textarea) {
        return;
    }

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const content = textarea.value.trim();
        if (!content) {
            textarea.focus();
            return;
        }

        try {
            await createComment(content);
            textarea.value = "";
        } catch (error) {
            alert(error.message);
        }
    });
}

function initComments() {
    bindCommentForm();
    loadComments();
}

if (typeof kakao !== "undefined" && kakao.maps) {
    kakao.maps.load(function () {
        initDetailMap();
    });
} else {
    console.error("카카오 지도 SDK가 로딩되지 않았습니다.");
}

document.addEventListener("DOMContentLoaded", function () {
    initComments();
});

function bindSavedRequestButton() {
    const button = document.querySelector(".like-btn");

    if (!button) {
        return;
    }

    button.addEventListener("click", async function () {
        const requestId = button.dataset.requestId;

        try {
            const response = await fetch(`/api/hunters/requests/${requestId}/bookmarks`, {
                method: "POST"
            });

            if (!response.ok) {
                alert("찜 처리 중 오류가 발생했습니다.");
                return;
            }

            const result = await response.json();
            button.classList.toggle("is-active", result.bookmarked);
            button.textContent = result.bookmarked ? "♥ 찜 취소" : "♡ 찜하기";
        } catch (error) {
            console.error(error);
            alert("찜 처리 중 오류가 발생했습니다.");
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    initComments();
    bindSavedRequestButton();
});
