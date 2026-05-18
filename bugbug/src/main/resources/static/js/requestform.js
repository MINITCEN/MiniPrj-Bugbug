
/* =========================================================
1. DOM 요소 참조
========================================================= */
const requestForm = document.querySelector(".form-card");

const contentEditor = document.getElementById("contentEditor");
const contentHidden = document.getElementById("contentHidden");

const imageInput = document.getElementById("imageFiles");
const videoInput = document.getElementById("videoFile");

const previewContainer = document.getElementById("previewContainer");
const previewConfirmArea = document.getElementById("previewConfirmArea");
const insertMediaBtn = document.getElementById("insertMediaBtn");

const locationInput = document.getElementById("location");
const selectedLocation = document.getElementById("selectedLocation");


/* =========================================================
2. 카카오 지도 관련 로직
========================================================= */
let map = null;
let marker = null;
let geocoder = null;

function initMap() {
    const mapContainer = document.getElementById("map");

    if (!mapContainer) {
        return;
    }

    const mapOption = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 4
    };

        map = new kakao.maps.Map(mapContainer, mapOption);
        geocoder = new kakao.maps.services.Geocoder();
}

    if (typeof kakao !== "undefined" && kakao.maps) {
    kakao.maps.load(function () {
        initMap();
    });
} else {
    console.error("카카오 지도 SDK가 로딩되지 않았습니다. JavaScript 키와 도메인 설정을 확인하세요.");
}

    function searchAddress() {
    const address = locationInput.value.trim();

    if (address === "") {
    alert("위치를 입력해주세요.");
    locationInput.focus();
    return;
}

    if (!geocoder) {
    alert("지도 API가 아직 초기화되지 않았습니다. JavaScript 키와 localhost 도메인 등록을 확인해주세요.");
    return;
}

    geocoder.addressSearch(address, function (result, status) {
    if (status !== kakao.maps.services.Status.OK) {
    alert("주소를 찾을 수 없습니다. 예: 서울 강남구 역삼동 형식으로 입력해보세요.");
    return;
}

    const latitude = result[0].y;
    const longitude = result[0].x;
    const coords = new kakao.maps.LatLng(latitude, longitude);

    map.setCenter(coords);

    if (marker) {
    marker.setMap(null);
}

    marker = new kakao.maps.Marker({
    map: map,
    position: coords
});

    selectedLocation.textContent =
    "선택 위치: " + address + " / 위도: " + latitude + ", 경도: " + longitude;
});
}


    /* =========================================================
    3. contenteditable 커서 위치 저장 로직
    ========================================================= */
    let savedRange = null;

    function saveCursorPosition() {
    const selection = window.getSelection();

    if (!selection || selection.rangeCount === 0) {
    return;
}

    const range = selection.getRangeAt(0);

    if (contentEditor.contains(range.commonAncestorContainer)) {
    savedRange = range.cloneRange();
}
}

    function restoreCursorPosition() {
    const selection = window.getSelection();

    if (!selection) {
    return;
}

    selection.removeAllRanges();

    if (savedRange) {
    selection.addRange(savedRange);
    return;
}

    const range = document.createRange();
    range.selectNodeContents(contentEditor);
    range.collapse(false);

    selection.addRange(range);
    savedRange = range.cloneRange();
}

    if (contentEditor) {
    contentEditor.addEventListener("keyup", saveCursorPosition);
    contentEditor.addEventListener("mouseup", saveCursorPosition);
    contentEditor.addEventListener("focus", saveCursorPosition);
}


    /* =========================================================
    4. 파일 선택 이벤트
    ========================================================= */
    if (imageInput) {
    imageInput.addEventListener("change", function () {
        refreshImagePreview();
    });
}

    if (videoInput) {
    videoInput.addEventListener("change", function () {
        refreshVideoPreview();
    });
}


    /* =========================================================
    5. 이미지 프리뷰 로직
    ========================================================= */
    function refreshImagePreview() {
    removePreviewByType("image");

    const files = Array.from(imageInput.files);

    if (files.length === 0) {
    toggleInsertButton();
    return;
}

    files.forEach(function (file, index) {
    if (!file.type.startsWith("image/")) {
    alert("이미지 파일만 첨부할 수 있습니다.");
    return;
}

    const reader = new FileReader();

    reader.onload = function (event) {
    const previewBox = createPreviewBox("image");

    const img = document.createElement("img");
    img.src = event.target.result;
    img.className = "preview-img";

    const removeBtn = createRemoveButton(function () {
    removeImageFile(index);
});

    previewBox.appendChild(img);
    previewBox.appendChild(removeBtn);

    previewContainer.appendChild(previewBox);
    toggleInsertButton();
};

    reader.readAsDataURL(file);
});
}

    function removeImageFile(removeIndex) {
    const dataTransfer = new DataTransfer();
    const files = Array.from(imageInput.files);

    files.forEach(function (file, index) {
    if (index !== removeIndex) {
    dataTransfer.items.add(file);
}
});

    imageInput.files = dataTransfer.files;
    refreshImagePreview();
}


    /* =========================================================
    6. 동영상 프리뷰 로직
    ========================================================= */
    function refreshVideoPreview() {
    removePreviewByType("video");

    const file = videoInput.files[0];

    if (!file) {
    toggleInsertButton();
    return;
}

    if (!file.type.startsWith("video/")) {
    alert("동영상 파일만 첨부할 수 있습니다.");
    videoInput.value = "";
    toggleInsertButton();
    return;
}

    const videoUrl = URL.createObjectURL(file);
    const previewBox = createPreviewBox("video");

    previewBox.dataset.objectUrl = videoUrl;

    const video = document.createElement("video");
    video.src = videoUrl;
    video.className = "preview-video";
    video.controls = true;

    const removeBtn = createRemoveButton(function () {
    videoInput.value = "";
    URL.revokeObjectURL(videoUrl);
    previewBox.remove();
    toggleInsertButton();
});

    previewBox.appendChild(video);
    previewBox.appendChild(removeBtn);

    previewContainer.appendChild(previewBox);
    toggleInsertButton();
}


    /* =========================================================
    7. 프리뷰 공통 생성/삭제 로직
    ========================================================= */
    function createPreviewBox(type) {
    const previewBox = document.createElement("div");
    previewBox.className = "preview-box";
    previewBox.dataset.type = type;

    return previewBox;
}

    function createRemoveButton(clickHandler) {
    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.className = "remove-file-btn";
    removeBtn.textContent = "×";
    removeBtn.addEventListener("click", clickHandler);

    return removeBtn;
}

    function removePreviewByType(type) {
    const previews = previewContainer.querySelectorAll(`[data-type="${type}"]`);

    previews.forEach(function (preview) {
    if (preview.dataset.objectUrl) {
    URL.revokeObjectURL(preview.dataset.objectUrl);
}

    preview.remove();
});

    toggleInsertButton();
}

    function clearAllPreviews() {
    const previews = previewContainer.querySelectorAll(".preview-box");

    previews.forEach(function (preview) {
    preview.remove();
});

    toggleInsertButton();
}

    function toggleInsertButton() {
    const hasPreview = previewContainer.querySelectorAll(".preview-box").length > 0;

    previewConfirmArea.style.display = hasPreview ? "block" : "none";
}


    /* =========================================================
    8. 프리뷰 미디어를 본문 커서 위치에 삽입
    ========================================================= */
    if (insertMediaBtn) {
    insertMediaBtn.addEventListener("click", function () {
        insertPreviewMediaToEditor();
    });
}

    function insertPreviewMediaToEditor() {
    const previewItems = previewContainer.querySelectorAll(".preview-box");

    if (previewItems.length === 0) {
    alert("삽입할 이미지나 동영상이 없습니다.");
    return;
}

    contentEditor.focus();
    restoreCursorPosition();

    const fragment = document.createDocumentFragment();

    previewItems.forEach(function (previewBox) {
    const type = previewBox.dataset.type;

    if (type === "image") {
    const previewImg = previewBox.querySelector("img");

    if (previewImg) {
    const img = createContentImage(previewImg.src);
    fragment.appendChild(img);
    fragment.appendChild(document.createElement("br"));
}
}

    if (type === "video") {
    const previewVideo = previewBox.querySelector("video");

    if (previewVideo) {
    const video = createContentVideo(previewVideo.src);
    fragment.appendChild(video);
    fragment.appendChild(document.createElement("br"));
}
}
});

    insertNodeAtCursor(fragment);
    clearAllPreviews();
    saveCursorPosition();
}

    function createContentImage(src) {
    const img = document.createElement("img");
    img.src = src;
    img.className = "content-image";
    img.style.maxWidth = "100%";
    img.style.display = "block";
    img.style.margin = "12px 0";

    return img;
}

    function createContentVideo(src) {
    const video = document.createElement("video");
    video.src = src;
    video.controls = true;
    video.className = "content-video";
    video.style.maxWidth = "100%";
    video.style.display = "block";
    video.style.margin = "12px 0";

    return video;
}

    function insertNodeAtCursor(node) {
    const selection = window.getSelection();

    if (!selection || selection.rangeCount === 0) {
    return;
}

    const range = selection.getRangeAt(0);

    range.deleteContents();
    range.insertNode(node);

    range.collapse(false);

    selection.removeAllRanges();
    selection.addRange(range);

    savedRange = range.cloneRange();
}

    /* =========================================================
    9. 폼 제출 전 본문 내용 hidden input에 저장
    ========================================================= */
    if (requestForm && contentEditor && contentHidden) {
    requestForm.addEventListener("submit", function (event) {
        const contentText = contentEditor.innerText.trim();
        const hasMedia = contentEditor.querySelectorAll("img, video").length > 0;

        if (contentText.length === 0 && !hasMedia) {
            event.preventDefault();
            alert("상세 내용을 입력해주세요.");
            contentEditor.focus();
            return;
        }
        contentHidden.value = contentEditor.innerHTML;
    });
}