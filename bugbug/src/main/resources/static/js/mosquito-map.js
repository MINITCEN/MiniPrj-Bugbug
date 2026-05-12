(function () {
    const config = window.mosquitoMapConfig || {};
    const currentApiUrl = config.currentApiUrl || '/api/v1/mosquito/current';
    const detailApiBase = config.detailApiBase || '/api/v1/mosquito/detail';
    const trendApiBase = config.trendApiBase || '/api/v1/mosquito/trend';
    const geoJsonUrl = config.geoJsonUrl || '/map/seoul-districts.geojson';

    const state = {
        items: [],
        geoJson: null,
        selectedRegionId: null,
        map: null,
        districtLayer: null,
        layerByRegionId: new Map(),
        labelLayer: null
    };

    const dom = {
        regionList: document.getElementById('region-list'),
        searchInput: document.getElementById('search-input'),
        mapRoot: document.getElementById('map-root'),
        mapLoading: document.getElementById('map-loading'),
        lastUpdated: document.getElementById('last-updated'),
        refreshButton: document.getElementById('refresh-button'),
        detailRegion: document.getElementById('detail-region'),
        detailScore: document.getElementById('detail-score'),
        detailStatus: document.getElementById('detail-status'),
        detailRing: document.getElementById('detail-ring'),
        detailEmoji: document.getElementById('detail-emoji'),
        detailDate: document.getElementById('detail-date'),
        weatherDate: document.getElementById('weather-date'),
        detailTemp: document.getElementById('detail-temp'),
        detailHumidity: document.getElementById('detail-humidity'),
        detailRain: document.getElementById('detail-rain'),
        detailRainType: document.getElementById('detail-rain-type'),
        detailSky: document.getElementById('detail-sky'),
        detailWind: document.getElementById('detail-wind'),
        trendChart: document.getElementById('trend-chart')
    };

    document.addEventListener('DOMContentLoaded', init);
    dom.refreshButton.addEventListener('click', init);
    dom.searchInput.addEventListener('input', () => renderRegionList(filterItems()));

    async function init() {
        setLoading(true);
        try {
            if (!window.L) {
                throw new Error('Leaflet is not available.');
            }

            state.geoJson = await fetchJson(geoJsonUrl);

            try {
                const items = await fetchJson(currentApiUrl);
                state.items = Array.isArray(items) ? items : [];
            } catch (error) {
                console.error('current api failed', error);
                state.items = (state.geoJson?.features || []).map(feature => ({
                    regionId: null,
                    location: feature?.properties?.SIG_KOR_NM,
                    index: null
                }));
            }

            renderMap();
            renderRegionList(filterItems());
            updateLastUpdated();

            const selectableItems = state.items.filter(item => item.regionId != null);
            if (selectableItems.length > 0) {
                const preferred = selectableItems.find(item => item.regionId === state.selectedRegionId) || selectableItems[0];
                await selectRegion(preferred.regionId);
            } else {
                renderDetailFallback();
                renderTrendChart([]);
            }
        } catch (error) {
            console.error(error);
            renderEmptyMap('지도를 불러오지 못했습니다.');
        } finally {
            setLoading(false);
        }
    }

    function filterItems() {
        const keyword = dom.searchInput.value.trim();
        if (!keyword) {
            return [];
        }
        return state.items.filter(item => item.location.includes(keyword));
    }

    function renderRegionList(items) {
        dom.regionList.innerHTML = '';
        const hasKeyword = dom.searchInput.value.trim().length > 0;
        dom.regionList.classList.toggle('hidden', !hasKeyword);

        if (!hasKeyword) {
            return;
        }

        if (!items.length) {
            const empty = document.createElement('div');
            empty.className = 'empty';
            empty.textContent = '검색 결과가 없습니다.';
            dom.regionList.appendChild(empty);
            return;
        }

        items.forEach(item => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = item.regionId === state.selectedRegionId ? 'active' : '';
            button.innerHTML = `<span>${item.location}</span><strong>${formatIndex(item.index)}</strong>`;
            if (item.regionId != null) {
                button.addEventListener('click', () => selectRegion(item.regionId));
            } else {
                button.disabled = true;
            }
            dom.regionList.appendChild(button);
        });
    }

    function renderMap() {
        const features = state.geoJson?.features || [];
        if (!features.length) {
            renderEmptyMap('지도 자산을 찾지 못했습니다.');
            return;
        }

        const itemByName = new Map(state.items.map(item => [item.location, item]));
        destroyMap();

        state.map = L.map(dom.mapRoot, {
            attributionControl: false,
            zoomControl: true,
            dragging: true,
            scrollWheelZoom: true,
            doubleClickZoom: false,
            boxZoom: false,
            keyboard: true
        });

        const layer = L.geoJSON(state.geoJson, {
            style: feature => buildDistrictStyle(itemByName.get(feature?.properties?.SIG_KOR_NM)),
            onEachFeature: (feature, districtLayer) => {
                const name = feature?.properties?.SIG_KOR_NM;
                const item = itemByName.get(name);

                if (item?.regionId != null) {
                    state.layerByRegionId.set(item.regionId, districtLayer);
                    districtLayer.on({
                        click: () => selectRegion(item.regionId),
                        mouseover: () => {
                            if (item.regionId !== state.selectedRegionId) {
                                districtLayer.setStyle(getDistrictStyle(item.index, false, true));
                            }
                        },
                        mouseout: () => {
                            const isSelected = item.regionId === state.selectedRegionId;
                            districtLayer.setStyle(getDistrictStyle(item.index, isSelected, false));
                        }
                    });
                }
            }
        }).addTo(state.map);

        state.districtLayer = layer;
        state.labelLayer = L.layerGroup().addTo(state.map);
        renderDistrictLabels(itemByName);
        state.map.fitBounds(layer.getBounds(), { padding: [20, 20] });
        syncSelectionState();
    }

    function destroyMap() {
        state.layerByRegionId.clear();
        state.districtLayer = null;
        state.labelLayer = null;
        if (state.map) {
            state.map.remove();
            state.map = null;
        }
    }

    function renderDistrictLabels(itemByName) {
        if (!state.labelLayer) {
            return;
        }

        state.labelLayer.clearLayers();

        state.geoJson.features.forEach(feature => {
            const name = feature?.properties?.SIG_KOR_NM;
            const item = itemByName.get(name);
            if (!item) {
                return;
            }

            const center = getFeatureCenter(feature);
            if (!center) {
                return;
            }

            const marker = L.marker(center, {
                interactive: false,
                icon: L.divIcon({
                    className: 'district-label',
                    html: `<span class="district-label-text">${name} ${formatIndex(item.index)}</span>`,
                    iconSize: null
                })
            });

            marker.addTo(state.labelLayer);
        });
    }

    function getFeatureCenter(feature) {
        const coordinates = feature?.geometry?.coordinates;
        const type = feature?.geometry?.type;

        if (!coordinates || !type) {
            return null;
        }

        if (type === 'Polygon') {
            return getRingCenter(coordinates[0]);
        }

        if (type === 'MultiPolygon') {
            const ring = coordinates
                .map(polygon => polygon[0])
                .filter(Boolean)
                .sort((a, b) => b.length - a.length)[0];
            return getRingCenter(ring);
        }

        return null;
    }

    function getRingCenter(ring) {
        if (!Array.isArray(ring) || !ring.length) {
            return null;
        }

        let minLng = Infinity;
        let maxLng = -Infinity;
        let minLat = Infinity;
        let maxLat = -Infinity;

        ring.forEach(point => {
            const lng = point[0];
            const lat = point[1];
            minLng = Math.min(minLng, lng);
            maxLng = Math.max(maxLng, lng);
            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
        });

        return [(minLat + maxLat) / 2, (minLng + maxLng) / 2];
    }

    function buildDistrictStyle(item) {
        return getDistrictStyle(item?.index, item?.regionId === state.selectedRegionId, false);
    }

    function getDistrictStyle(index, selected, hovered) {
        return {
            fillColor: getColorByIndex(index),
            fillOpacity: hovered ? 0.92 : 0.84,
            color: selected ? '#0b5f36' : 'rgba(255, 255, 255, 0.96)',
            weight: selected ? 4 : hovered ? 3 : 2,
            opacity: 1
        };
    }

    async function selectRegion(regionId) {
        state.selectedRegionId = regionId;
        syncSelectionState();
        renderRegionList(filterItems());

        const selectedLayer = state.layerByRegionId.get(regionId);
        if (selectedLayer && state.map) {
            state.map.fitBounds(selectedLayer.getBounds(), {
                padding: [40, 40],
                maxZoom: 12
            });
        }

        try {
            const [detail, trend] = await Promise.all([
                fetchJson(`${detailApiBase}/${regionId}`),
                fetchJson(`${trendApiBase}/${regionId}`)
            ]);
            renderDetail(detail);
            renderTrendChart(Array.isArray(trend) ? trend : []);
        } catch (error) {
            console.error(error);
            const selected = state.items.find(item => item.regionId === regionId);
            renderDetailFallback(selected?.location);
            renderTrendChart([]);
        }
    }

    function syncSelectionState() {
        state.items.forEach(item => {
            if (item.regionId == null) {
                return;
            }
            const layer = state.layerByRegionId.get(item.regionId);
            if (!layer) {
                return;
            }
            const selected = item.regionId === state.selectedRegionId;
            layer.setStyle(getDistrictStyle(item.index, selected, false));
            if (selected) {
                layer.bringToFront();
            }
        });
    }

    function renderDetail(detail) {
        dom.detailRegion.textContent = detail?.regionName || '지역 선택';
        dom.detailScore.textContent = detail?.mosquitoIndex != null ? formatIndex(detail.mosquitoIndex) : '--';
        dom.detailStatus.textContent = detail?.mosquitoStatus || '데이터 없음';
        const detailColor = getColorByIndex(detail?.mosquitoIndex);
        dom.detailStatus.style.background = detailColor;
        dom.detailEmoji.textContent = '🦟';
        dom.detailEmoji.style.color = detailColor;
        dom.detailRing.style.background = buildProgressRing(detail?.mosquitoIndex, detailColor);
        dom.detailDate.textContent = detail?.mosquitoIndexDate || '-';
        dom.weatherDate.textContent = detail?.weatherBaseDate
            ? `${detail.weatherBaseDate} ${detail.weatherBaseTime || ''}`.trim()
            : '-';
        dom.detailTemp.textContent = detail?.temperature != null ? `${detail.temperature}°C` : '--';
        dom.detailHumidity.textContent = detail?.humidity != null ? `${detail.humidity}%` : '--';
        dom.detailRain.textContent = detail?.precipitation || '-';
        dom.detailRainType.textContent = detail?.precipitationType || '-';
        dom.detailSky.textContent = detail?.skyStatus || '-';
        dom.detailWind.textContent = detail?.windSpeed != null ? `${detail.windSpeed}m/s` : '-';
    }

    function renderDetailFallback(regionName) {
        renderDetail({
            regionName: regionName || '지역 선택',
            mosquitoStatus: '데이터 없음'
        });
    }

    function renderTrendChart(items) {
        const svg = dom.trendChart;
        svg.innerHTML = '';

        const width = 320;
        const height = 200;
        const padding = { top: 20, right: 18, bottom: 28, left: 24 };

        const axisX = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        axisX.setAttribute('class', 'chart-axis');
        axisX.setAttribute('x1', String(padding.left));
        axisX.setAttribute('y1', String(height - padding.bottom));
        axisX.setAttribute('x2', String(width - padding.right));
        axisX.setAttribute('y2', String(height - padding.bottom));
        svg.appendChild(axisX);

        if (!items.length) {
            const empty = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            empty.setAttribute('x', '50%');
            empty.setAttribute('y', '50%');
            empty.setAttribute('text-anchor', 'middle');
            empty.setAttribute('class', 'chart-label');
            empty.textContent = '추이 데이터가 없습니다.';
            svg.appendChild(empty);
            return;
        }

        const values = items.map(item => Number(item.index) || 0);
        const min = Math.min(...values);
        const max = Math.max(...values);
        const range = Math.max(max - min, 1);
        const usableWidth = width - padding.left - padding.right;
        const usableHeight = height - padding.top - padding.bottom;

        const points = items.map((item, index) => {
            const x = padding.left + (usableWidth / Math.max(items.length - 1, 1)) * index;
            const y = padding.top + (1 - ((Number(item.index) || 0) - min) / range) * usableHeight;
            return { x, y, label: item.date?.slice(5) || '', value: Number(item.index) || 0 };
        });

        const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        path.setAttribute('class', 'chart-line');
        path.setAttribute('d', points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' '));
        svg.appendChild(path);

        points.forEach(point => {
            const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            circle.setAttribute('class', 'chart-point');
            circle.setAttribute('cx', String(point.x));
            circle.setAttribute('cy', String(point.y));
            circle.setAttribute('r', '4');
            svg.appendChild(circle);

            const value = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            value.setAttribute('class', 'chart-value');
            value.setAttribute('x', String(point.x));
            value.setAttribute('y', String(point.y - 10));
            value.setAttribute('text-anchor', 'middle');
            value.textContent = String(Math.round(point.value));
            svg.appendChild(value);

            const label = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            label.setAttribute('class', 'chart-label');
            label.setAttribute('x', String(point.x));
            label.setAttribute('y', String(height - 8));
            label.setAttribute('text-anchor', 'middle');
            label.textContent = point.label;
            svg.appendChild(label);
        });
    }

    function renderEmptyMap(message) {
        if (state.map) {
            destroyMap();
        }
        dom.mapRoot.innerHTML = '';
        dom.mapLoading.textContent = message;
        dom.mapLoading.style.display = 'grid';
    }

    function setLoading(loading) {
        dom.mapLoading.style.display = loading ? 'grid' : 'none';
        if (loading) {
            dom.mapLoading.textContent = '모기 지도를 불러오는 중';
        }
    }

    function updateLastUpdated() {
        const now = new Date();
        dom.lastUpdated.textContent = `최종 업데이트 ${now.toLocaleString('ko-KR')}`;
    }

    function formatIndex(value) {
        if (value == null || Number.isNaN(Number(value))) {
            return '--';
        }
        return String(Math.round(Number(value)));
    }

    function getColorByIndex(index) {
        const value = Number(index);
        if (!Number.isFinite(value)) {
            return '#cad4ce';
        }
        if (value >= 75) return '#ff685d';
        if (value >= 50) return '#ffad4c';
        if (value >= 25) return '#ffd86b';
        return '#9de0a5';
    }

    function buildProgressRing(index, color) {
        const value = Number(index);
        if (!Number.isFinite(value)) {
            return 'conic-gradient(#cad4ce 0deg, #eef3ef 0deg 360deg)';
        }

        const clamped = Math.max(0, Math.min(100, value));
        const degrees = (clamped / 100) * 360;
        return `conic-gradient(${color} 0deg ${degrees}deg, #eef3ef ${degrees}deg 360deg)`;
    }

    async function fetchJson(url) {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Request failed: ${response.status}`);
        }
        return response.json();
    }
}());
