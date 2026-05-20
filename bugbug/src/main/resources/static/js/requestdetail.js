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

if (typeof kakao !== "undefined" && kakao.maps) {
    kakao.maps.load(function () {
        initDetailMap();
    });
} else {
    console.error("카카오 지도 SDK가 로딩되지 않았습니다.");
}