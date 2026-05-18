(function () {
    const config = window.mosquitoMapConfig || {};
    const geoJsonUrl = config.geoJsonUrl || '/map/seoul-districts.geojson';
    const summaryApiBase = config.summaryApiBase || '/api/v1/mosquito/summary';

    const state = {
        items: Array.isArray(config.regions) ? config.regions : [],
        geoJson: null,
        selectedRegionId: config.selectedRegionId ?? null,
        map: null,
        districtLayer: null,
        selectedOutlineLayer: null
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
    if (dom.refreshButton) {
        dom.refreshButton.addEventListener('click', init);
    }
    dom.searchInput.addEventListener('input', () => renderRegionList(filterItems()));

    async function init() {
        setLoading(true);

        try {
            state.geoJson = await fetchJson(geoJsonUrl);

            renderMap();
            renderRegionList(filterItems());
            updateLastUpdated();

            const selectableItems = state.items.filter(item => item.regionId != null);
            if (selectableItems.length > 0) {
                const preferred = selectableItems.find(item => item.regionId === state.selectedRegionId) || selectableItems[0];
                state.selectedRegionId = preferred.regionId;
                drawSelectedOutline();
                renderRegionList(filterItems());
                renderDetail(config.initialDetail);
                renderTrendChart(Array.isArray(config.initialTrend) ? config.initialTrend : []);
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

    function renderMap() {
        const features = state.geoJson?.features || [];
        if (!features.length) {
            renderEmptyMap('지도 자산을 찾지 못했습니다.');
            return;
        }

        destroyMap();

        state.map = L.map(dom.mapRoot, {
            attributionControl: false,
            zoomControl: true,
            scrollWheelZoom: true,
            doubleClickZoom: false
        });

        const itemByName = new Map(state.items.map(item => [item.location, item]));

        state.districtLayer = L.geoJSON(state.geoJson, {
            style: feature => {
                const item = itemByName.get(feature?.properties?.SIG_KOR_NM);
                return buildDistrictStyle(item?.index);
            },
            onEachFeature: (feature, layer) => {
                const item = itemByName.get(feature?.properties?.SIG_KOR_NM);
                const labelText = item
                    ? `${item.location} ${formatIndex(item.index)}`
                    : (feature?.properties?.SIG_KOR_NM || '');

                layer.bindTooltip(
                    `<span class="district-label-text">${labelText}</span>`,
                    {
                        permanent: true,
                        direction: 'center',
                        className: 'district-label',
                        opacity: 1
                    }
                );

                if (item?.regionId != null) {
                    layer.on('click', () => selectRegion(item.regionId));
                }
            }
        }).addTo(state.map);

        state.map.fitBounds(state.districtLayer.getBounds(), { padding: [20, 20] });
        drawSelectedOutline();
    }

    function destroyMap() {
        if (state.map) {
            state.map.remove();
            state.map = null;
        }
        state.districtLayer = null;
        state.selectedOutlineLayer = null;
    }

    function buildDistrictStyle(index) {
        return {
            fillColor: getColorByIndex(index),
            fillOpacity: 0.84,
            color: 'rgba(255, 255, 255, 0.96)',
            weight: 2,
            opacity: 1
        };
    }

    function buildSelectedOutlineStyle() {
        return {
            fill: false,
            color: '#0b5f36',
            weight: 4,
            opacity: 1
        };
    }

    function drawSelectedOutline() {
        if (!state.map) {
            return;
        }

        if (state.selectedOutlineLayer) {
            state.map.removeLayer(state.selectedOutlineLayer);
            state.selectedOutlineLayer = null;
        }

        const feature = findFeatureByRegionId(state.selectedRegionId);
        if (!feature) {
            return;
        }

        state.selectedOutlineLayer = L.geoJSON(feature, {
            style: buildSelectedOutlineStyle
        }).addTo(state.map);
    }

    function findFeatureByRegionId(regionId) {
        if (regionId == null) {
            return null;
        }

        const selectedItem = state.items.find(item => item.regionId === regionId);
        if (!selectedItem) {
            return null;
        }

        return (state.geoJson?.features || []).find(
            feature => feature?.properties?.SIG_KOR_NM === selectedItem.location
        ) || null;
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

    async function selectRegion(regionId) {
        state.selectedRegionId = regionId;
        drawSelectedOutline();
        renderRegionList(filterItems());

        const feature = findFeatureByRegionId(regionId);
        if (feature && state.map) {
            state.map.fitBounds(L.geoJSON(feature).getBounds(), {
                padding: [40, 40],
                maxZoom: 12
            });
        }

        try {
            const summary = await fetchJson(`${summaryApiBase}/${regionId}`);
            renderDetail(summary?.detail);
            renderTrendChart(Array.isArray(summary?.trend) ? summary.trend : []);
        } catch (error) {
            console.error(error);
            const selected = state.items.find(item => item.regionId === regionId);
            renderDetailFallback(selected?.location);
            renderTrendChart([]);
        }
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
        destroyMap();
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
        if (!dom.lastUpdated) {
            return;
        }
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
