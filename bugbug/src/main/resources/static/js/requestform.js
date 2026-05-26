/* =========================================================
1. DOM 요소 참조
========================================================= */
const requestForm = document.querySelector(".form-card");

const contentEditor = document.getElementById("contentEditor");
const contentHidden = document.getElementById("contentHidden");

const imageInput = document.getElementById("imageFiles");
const videoInput = document.getElementById("videoFile");
const videoUploadButton = document.getElementById("videoUploadButton");
const statusSelect = document.getElementById("statusSelect");

const previewContainer = document.getElementById("previewContainer");
const existingMediaGrid = document.querySelector(".existing-media-grid");
const mediaEmptyText = document.getElementById("mediaEmptyText");

const locationInput = document.getElementById("location");
const selectedLocation = document.getElementById("selectedLocation");


/* =========================================================
2. 카카오 지도 관련 로직
========================================================= */
let map = null;
let marker = null;
let geocoder = null;
let selectedImageFiles = [];
let selectedVideoFile = null;
let selectedVideoAttached = false;
let existingVideoUrl = getExistingVideoUrl();

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
3. 파일 선택 이벤트
========================================================= */
if (imageInput) {
    imageInput.addEventListener("change", function () {
        refreshImagePreview();
    });
}

if (videoInput) {
    videoInput.addEventListener("click", function (event) {
        if (hasExistingVideo()) {
            event.preventDefault();
            alert("기존 동영상을 삭제한 후 새 동영상을 등록해 주세요.");
            return;
        }

        if (hasSelectedVideo()) {
            event.preventDefault();
            alert("동영상은 1개만 첨부할 수 있습니다. 선택한 동영상을 삭제한 후 다시 등록해 주세요.");
        }
    });

    videoInput.addEventListener("change", function () {
        refreshVideoPreview();
    });
}

if (videoUploadButton) {
    videoUploadButton.addEventListener("click", function () {
        if (!videoInput) {
            return;
        }

        if (hasExistingVideo()) {
            alert("기존 동영상을 삭제한 후 새 동영상을 등록해 주세요.");
            return;
        }

        if (hasSelectedVideo()) {
            alert("동영상은 1개만 첨부할 수 있습니다. 선택한 동영상을 삭제한 후 다시 등록해 주세요.");
            return;
        }

        videoInput.click();
    });
}


/* =========================================================
4. 이미지 프리뷰 로직
========================================================= */
function refreshImagePreview() {
    const files = Array.from(imageInput.files);
    const validFiles = [];

    files.forEach(function (file) {
        if (!file.type.startsWith("image/")) {
            alert("이미지 파일만 첨부할 수 있습니다.");
            return;
        }

        validFiles.push(file);
    });

    selectedImageFiles = selectedImageFiles.concat(validFiles);
    syncImageInputFiles();
    renderImagePreviews();
}

function renderImagePreviews() {
    removePreviewByType("image");

    selectedImageFiles.forEach(function (file, index) {
        if (!file.type.startsWith("image/")) {
            alert("이미지 파일만 첨부할 수 있습니다.");
            removeImageFile(index);
            return;
        }

        const reader = new FileReader();

        reader.onload = function (event) {
            const previewBox = createPreviewBox("image");

            const img = document.createElement("img");
            img.src = event.target.result;
            img.className = "preview-img";
            img.alt = file.name;

            const removeBtn = createRemoveButton("선택 이미지 취소", function () {
                removeImageFile(index);
            });

            previewBox.appendChild(img);
            previewBox.appendChild(removeBtn);
            previewContainer.appendChild(previewBox);
            toggleEmptyText();
        };

        reader.readAsDataURL(file);
    });

    toggleEmptyText();
}

function removeImageFile(removeIndex) {
    selectedImageFiles = selectedImageFiles.filter(function (file, index) {
        return index !== removeIndex;
    });

    syncImageInputFiles();
    renderImagePreviews();
}

function syncImageInputFiles() {
    const dataTransfer = new DataTransfer();

    selectedImageFiles.forEach(function (file) {
        dataTransfer.items.add(file);
    });

    imageInput.files = dataTransfer.files;
}


/* =========================================================
5. 동영상 프리뷰 로직
========================================================= */
function refreshVideoPreview() {
    if (hasExistingVideo()) {
        videoInput.value = "";
        syncVideoInputFile();
        alert("기존 동영상을 삭제한 후 새 동영상을 등록해 주세요.");
        toggleEmptyText();
        return;
    }

    const file = videoInput.files[0];

    if (!file) {
        toggleEmptyText();
        return;
    }

    if (hasSelectedVideo()) {
        syncVideoInputFile();
        alert("동영상은 1개만 첨부할 수 있습니다. 선택한 동영상을 삭제한 후 다시 등록해 주세요.");
        toggleEmptyText();
        return;
    }

    if (!file.type.startsWith("video/")) {
        alert("동영상 파일만 첨부할 수 있습니다.");
        videoInput.value = "";
        selectedVideoFile = null;
        selectedVideoAttached = false;
        toggleEmptyText();
        return;
    }

    selectedVideoFile = file;
    syncVideoInputFile();

    const videoUrl = URL.createObjectURL(file);
    const previewBox = createPreviewBox("video");

    previewBox.dataset.objectUrl = videoUrl;

    const video = document.createElement("video");
    video.src = videoUrl;
    video.className = "preview-video";
    video.controls = true;

    const removeBtn = createRemoveButton("선택 동영상 취소", function () {
        videoInput.value = "";
        selectedVideoFile = null;
        URL.revokeObjectURL(videoUrl);
        previewBox.remove();
        selectedVideoAttached = false;
        toggleEmptyText();
    });

    previewBox.appendChild(video);
    previewBox.appendChild(removeBtn);
    previewContainer.appendChild(previewBox);
    selectedVideoAttached = true;
    toggleEmptyText();
}


/* =========================================================
6. 프리뷰 공통 생성/삭제 로직
========================================================= */
function createPreviewBox(type) {
    const previewBox = document.createElement("div");
    previewBox.className = "preview-box";
    previewBox.dataset.type = type;

    if (type === "video") {
        previewBox.classList.add("video-preview-box");
    }

    return previewBox;
}

function createRemoveButton(ariaLabel, clickHandler) {
    const removeBtn = document.createElement("button");
    removeBtn.type = "button";
    removeBtn.className = "remove-file-btn";
    removeBtn.textContent = "x";
    removeBtn.setAttribute("aria-label", ariaLabel);
    removeBtn.addEventListener("click", clickHandler);

    return removeBtn;
}

document.addEventListener("click", function (event) {
    const removeButton = event.target.closest(".existing-remove-btn");

    if (!removeButton || !requestForm) {
        return;
    }

    const mediaUrl = removeButton.dataset.url;
    const mediaType = removeButton.dataset.mediaType;

    if (!mediaUrl || !mediaType) {
        return;
    }

    appendDeleteMediaInput(mediaType, mediaUrl);

    if (mediaType === "video") {
        existingVideoUrl = null;
    }

    const previewBox = removeButton.closest(".preview-box");
    if (previewBox) {
        previewBox.remove();
    }

    toggleEmptyText();
});

function appendDeleteMediaInput(mediaType, mediaUrl) {
    const hiddenInput = document.createElement("input");
    hiddenInput.type = "hidden";
    hiddenInput.name = mediaType === "image" ? "imageUrls" : "videoUrl";
    hiddenInput.value = mediaUrl;
    requestForm.appendChild(hiddenInput);
}

function getExistingVideoUrl() {
    const existingVideoButton = document.querySelector('.existing-remove-btn[data-media-type="video"]');
    return existingVideoButton ? existingVideoButton.dataset.url : null;
}

function hasExistingVideo() {
    return Boolean(existingVideoUrl);
}

function hasSelectedVideo() {
    return Boolean(selectedVideoFile || selectedVideoAttached);
}

function syncVideoInputFile() {
    const dataTransfer = new DataTransfer();

    if (selectedVideoFile) {
        dataTransfer.items.add(selectedVideoFile);
    }

    videoInput.files = dataTransfer.files;
}

function removePreviewByType(type) {
    const previews = previewContainer.querySelectorAll(`[data-type="${type}"]`);

    previews.forEach(function (preview) {
        if (preview.dataset.objectUrl) {
            URL.revokeObjectURL(preview.dataset.objectUrl);
        }

        preview.remove();
    });

    toggleEmptyText();
}

function toggleEmptyText() {
    if (!mediaEmptyText || !previewContainer) {
        return;
    }

    const hasNewPreview = previewContainer.querySelectorAll(".preview-box").length > 0;
    const hasExistingPreview = existingMediaGrid && existingMediaGrid.querySelectorAll(".preview-box").length > 0;
    const hasPreview = hasNewPreview || hasExistingPreview;
    mediaEmptyText.style.display = hasPreview ? "none" : "block";
}

toggleEmptyText();


/* =========================================================
7. 진행 상태 색상 동기화
========================================================= */
if (statusSelect) {
    statusSelect.addEventListener("change", syncStatusColor);
    syncStatusColor();
}

function syncStatusColor() {
    statusSelect.classList.remove("is-waiting", "is-reserved", "is-done");

    if (statusSelect.value === "대기 중") {
        statusSelect.classList.add("is-waiting");
        return;
    }

    if (statusSelect.value === "예약 완료") {
        statusSelect.classList.add("is-reserved");
        return;
    }

    if (statusSelect.value === "완료") {
        statusSelect.classList.add("is-done");
    }
}


/* =========================================================
8. 폼 제출 전 본문 내용 hidden input에 저장
========================================================= */
if (requestForm && contentEditor && contentHidden) {
    requestForm.addEventListener("submit", function (event) {
        const contentText = contentEditor.innerText.trim();

        if (contentText.length === 0) {
            event.preventDefault();
            alert("상세 내용을 입력해주세요.");
            contentEditor.focus();
            return;
        }

        contentHidden.value = contentText;
    });
}
