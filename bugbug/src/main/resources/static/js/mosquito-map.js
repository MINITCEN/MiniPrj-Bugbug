(function () {
    const config = window.mosquitoMapConfig || {};
    const currentApiUrl = config.currentApiUrl || '/api/v1/mosquito/current';
    const detailApiBase = config.detailApiBase || '/api/v1/mosquito/detail';
    const trendApiBase = config.trendApiBase || '/api/v1/mosquito/trend';
    const geoJsonUrl = config.geoJsonUrl || '/map/seoul-districts.geojson';
    const svgNs = 'http://www.w3.org/2000/svg';
    const svgViewBox = { width: 760, height: 700, padding: 20 };
    const mapScaleMultiplier = 1.12;
    const labelOffsets = {
        '도봉구': { x: 3, y: -6 },
        '은평구': { x: -6, y: 2 },
        '동대문구': { x: 4, y: 4 },
        '동작구': { x: 0, y: 7 },
        '금천구': { x: 0, y: 9 },
        '구로구': { x: -5, y: 2 },
        '종로구': { x: -3, y: -1 },
        '강북구': { x: 2, y: -4 },
        '중랑구': { x: 6, y: 1 },
        '강남구': { x: 5, y: 5 },
        '강서구': { x: -8, y: 3 },
        '중구': { x: 2, y: 2 },
        '강동구': { x: 6, y: 0 },
        '광진구': { x: 5, y: 5 },
        '마포구': { x: -4, y: 2 },
        '관악구': { x: -2, y: 8 },
        '서초구': { x: 0, y: 9 },
        '성북구': { x: 2, y: 0 },
        '노원구': { x: 6, y: -5 },
        '송파구': { x: 7, y: 7 },
        '성동구': { x: 6, y: 3 },
        '서대문구': { x: -7, y: -2 },
        '양천구': { x: -6, y: 2 },
        '영등포구': { x: -1, y: 4 },
        '용산구': { x: -1, y: 7 }
    };
    const labelBox = { width: 50, height: 42 };

    const state = {
        items: [],
        geoJson: null,
        selectedRegionId: null,
        featureElementByRegionName: new Map(),
        labelElementByRegionName: new Map(),
        selectedOutlinePath: null
    };

    const dom = {
        regionList: document.getElementById('region-list'),
        searchInput: document.getElementById('search-input'),
        mapSvg: document.getElementById('map-svg'),
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
            state.geoJson = await fetchJson(geoJsonUrl);

            try {
                const items = await fetchJson(currentApiUrl);
                state.items = Array.isArray(items) ? items : [];
            } catch (error) {
                console.error('current api failed', error);
                state.items = (state.geoJson?.features || []).map(feature => ({
                    regionId: null,
                    location: feature?.properties?.SIG_KOR_NM,
                    index: null,
                    status: null,
                    message: '현재 모기지수 데이터를 불러오지 못했습니다.'
                }));
            }
            renderMap();
            renderRegionList(filterItems());
            updateLastUpdated();

            const selectableItems = state.items.filter(item => item.regionId != null);
            if (selectableItems.length > 0) {
                const preferred = selectableItems.find(item => state.selectedRegionId === item.regionId) || selectableItems[0];
                await selectRegion(preferred.regionId);
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
                button.style.cursor = 'default';
            }
            dom.regionList.appendChild(button);
        });
    }

    function renderMap() {
        dom.mapSvg.innerHTML = '';
        state.featureElementByRegionName.clear();
        state.labelElementByRegionName.clear();

        const features = state.geoJson?.features || [];
        if (!features.length) {
            renderEmptyMap('지도 자산을 찾지 못했습니다.');
            return;
        }

        const projected = projectFeatures(features);
        const polygonLayer = document.createElementNS(svgNs, 'g');
        const labelLayer = document.createElementNS(svgNs, 'g');
        const outlineLayer = document.createElementNS(svgNs, 'g');

        const itemByName = new Map(state.items.map(item => [item.location, item]));

        projected.forEach(entry => {
            const item = itemByName.get(entry.name);
            const fill = getColorByIndex(item?.index);
            const regionId = item?.regionId ?? null;

            const path = document.createElementNS(svgNs, 'path');
            path.setAttribute('d', entry.path);
            path.setAttribute('class', 'district-shape');
            path.setAttribute('fill', fill);
            path.dataset.regionName = entry.name;
            if (regionId !== null) {
                path.dataset.regionId = String(regionId);
                path.addEventListener('click', () => selectRegion(regionId));
            }
            polygonLayer.appendChild(path);
            state.featureElementByRegionName.set(entry.name, path);

            if (item) {
                const label = document.createElementNS(svgNs, 'g');
                label.setAttribute('class', 'district-label');
                const labelPosition = applyLabelOffset(entry.name, entry.centroid, entry.outerRing);
                label.setAttribute('transform', `translate(${labelPosition.x}, ${labelPosition.y})`);
                if (regionId !== null) {
                    label.dataset.regionId = String(regionId);
                    label.addEventListener('click', () => selectRegion(regionId));
                }

                const rect = document.createElementNS(svgNs, 'rect');
                rect.setAttribute('x', String(-labelBox.width / 2));
                rect.setAttribute('y', String(-labelBox.height / 2));
                rect.setAttribute('width', String(labelBox.width));
                rect.setAttribute('height', String(labelBox.height));
                rect.setAttribute('rx', '19');
                rect.setAttribute('ry', '19');

                const nameText = document.createElementNS(svgNs, 'text');
                nameText.setAttribute('class', 'label-name');
                nameText.setAttribute('y', '-8');
                nameText.textContent = entry.name;

                const valueText = document.createElementNS(svgNs, 'text');
                valueText.setAttribute('class', 'label-index');
                valueText.setAttribute('y', '10');
                valueText.textContent = formatIndex(item.index);

                label.appendChild(rect);
                label.appendChild(nameText);
                label.appendChild(valueText);
                labelLayer.appendChild(label);
                state.labelElementByRegionName.set(entry.name, label);
            }
        });

        dom.mapSvg.appendChild(polygonLayer);
        dom.mapSvg.appendChild(outlineLayer);
        dom.mapSvg.appendChild(labelLayer);
        state.selectedOutlinePath = outlineLayer;
        syncSelectionState();
    }

    function projectFeatures(features) {
        const points = [];

        features.forEach(feature => {
            forEachCoordinate(feature.geometry, ([lon, lat]) => {
                points.push({ lon, lat });
            });
        });

        const minLon = Math.min(...points.map(point => point.lon));
        const maxLon = Math.max(...points.map(point => point.lon));
        const minLat = Math.min(...points.map(point => point.lat));
        const maxLat = Math.max(...points.map(point => point.lat));

        const lonRange = maxLon - minLon || 1;
        const latRange = maxLat - minLat || 1;
        const innerWidth = svgViewBox.width - svgViewBox.padding * 2;
        const innerHeight = svgViewBox.height - svgViewBox.padding * 2;

        const scale = Math.min(innerWidth / lonRange, innerHeight / latRange) * mapScaleMultiplier;
        const xOffset = (svgViewBox.width - lonRange * scale) / 2;
        const yOffset = (svgViewBox.height - latRange * scale) / 2;

        return features.map(feature => {
            const rings = geometryToRings(feature.geometry).map(ring =>
                ring.map(([lon, lat]) => ({
                    x: xOffset + (lon - minLon) * scale,
                    y: svgViewBox.height - (yOffset + (lat - minLat) * scale)
                }))
            );

            return {
                name: feature.properties.SIG_KOR_NM,
                path: ringsToPath(rings),
                centroid: centroidFromRings(rings),
                outerRing: rings[0] || []
            };
        });
    }

    function geometryToRings(geometry) {
        if (!geometry) {
            return [];
        }
        if (geometry.type === 'Polygon') {
            return geometry.coordinates || [];
        }
        if (geometry.type === 'MultiPolygon') {
            return (geometry.coordinates || []).flat();
        }
        return [];
    }

    function ringsToPath(rings) {
        return rings.map(ring => ring.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x.toFixed(2)} ${point.y.toFixed(2)}`).join(' ') + ' Z').join(' ');
    }

    function centroidFromRings(rings) {
        const outer = rings[0] || [];
        if (!outer.length) {
            return { x: svgViewBox.width / 2, y: svgViewBox.height / 2 };
        }

        let area = 0;
        let cx = 0;
        let cy = 0;

        for (let i = 0; i < outer.length; i++) {
            const current = outer[i];
            const next = outer[(i + 1) % outer.length];
            const cross = current.x * next.y - next.x * current.y;
            area += cross;
            cx += (current.x + next.x) * cross;
            cy += (current.y + next.y) * cross;
        }

        if (Math.abs(area) < 1e-6) {
            const sum = outer.reduce((acc, point) => {
                acc.x += point.x;
                acc.y += point.y;
                return acc;
            }, { x: 0, y: 0 });
            return {
                x: sum.x / outer.length,
                y: sum.y / outer.length
            };
        }

        return {
            x: cx / (3 * area),
            y: cy / (3 * area)
        };
    }

    function applyLabelOffset(name, centroid, ring) {
        const offset = labelOffsets[name] || { x: 0, y: 0 };
        const desired = {
            x: centroid.x + offset.x,
            y: centroid.y + offset.y
        };

        if (!ring.length) {
            return desired;
        }

        const anchor = { x: centroid.x, y: centroid.y };
        for (let step = 0; step <= 10; step++) {
            const ratio = step / 10;
            const candidate = {
                x: desired.x + (anchor.x - desired.x) * ratio,
                y: desired.y + (anchor.y - desired.y) * ratio
            };
            if (isLabelInsidePolygon(candidate, ring)) {
                return candidate;
            }
        }

        return anchor;
    }

    function isLabelInsidePolygon(center, ring) {
        const halfWidth = labelBox.width / 2;
        const halfHeight = labelBox.height / 2;
        const corners = [
            { x: center.x - halfWidth, y: center.y - halfHeight },
            { x: center.x + halfWidth, y: center.y - halfHeight },
            { x: center.x + halfWidth, y: center.y + halfHeight },
            { x: center.x - halfWidth, y: center.y + halfHeight }
        ];

        return corners.every(point => pointInPolygon(point, ring));
    }

    function pointInPolygon(point, ring) {
        let inside = false;

        for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
            const xi = ring[i].x;
            const yi = ring[i].y;
            const xj = ring[j].x;
            const yj = ring[j].y;

            const intersects = ((yi > point.y) !== (yj > point.y))
                && (point.x < ((xj - xi) * (point.y - yi)) / ((yj - yi) || 1e-9) + xi);

            if (intersects) {
                inside = !inside;
            }
        }

        return inside;
    }

    function forEachCoordinate(geometry, visitor) {
        if (!geometry) {
            return;
        }
        const rings = geometryToRings(geometry);
        rings.forEach(ring => ring.forEach(visitor));
    }

    async function selectRegion(regionId) {
        state.selectedRegionId = regionId;
        syncSelectionState();
        renderRegionList(filterItems());

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
        const selected = state.items.find(item => item.regionId === state.selectedRegionId);
        const selectedName = selected?.location;

        state.featureElementByRegionName.forEach((element, name) => {
            element.classList.toggle('active', name === selectedName);
        });

        state.labelElementByRegionName.forEach((element, name) => {
            element.classList.toggle('active', name === selectedName);
        });

        if (state.selectedOutlinePath) {
            state.selectedOutlinePath.innerHTML = '';
            if (selectedName) {
                const source = state.featureElementByRegionName.get(selectedName);
                if (source) {
                    const outline = document.createElementNS(svgNs, 'path');
                    outline.setAttribute('d', source.getAttribute('d'));
                    outline.setAttribute('class', 'district-outline');
                    state.selectedOutlinePath.appendChild(outline);
                }
            }
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

        const axisX = document.createElementNS(svgNs, 'line');
        axisX.setAttribute('class', 'chart-axis');
        axisX.setAttribute('x1', String(padding.left));
        axisX.setAttribute('y1', String(height - padding.bottom));
        axisX.setAttribute('x2', String(width - padding.right));
        axisX.setAttribute('y2', String(height - padding.bottom));
        svg.appendChild(axisX);

        if (!items.length) {
            const empty = document.createElementNS(svgNs, 'text');
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

        const path = document.createElementNS(svgNs, 'path');
        path.setAttribute('class', 'chart-line');
        path.setAttribute('d', points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' '));
        svg.appendChild(path);

        points.forEach(point => {
            const circle = document.createElementNS(svgNs, 'circle');
            circle.setAttribute('class', 'chart-point');
            circle.setAttribute('cx', String(point.x));
            circle.setAttribute('cy', String(point.y));
            circle.setAttribute('r', '4');
            svg.appendChild(circle);

            const value = document.createElementNS(svgNs, 'text');
            value.setAttribute('class', 'chart-value');
            value.setAttribute('x', String(point.x));
            value.setAttribute('y', String(point.y - 10));
            value.setAttribute('text-anchor', 'middle');
            value.textContent = String(Math.round(point.value));
            svg.appendChild(value);

            const label = document.createElementNS(svgNs, 'text');
            label.setAttribute('class', 'chart-label');
            label.setAttribute('x', String(point.x));
            label.setAttribute('y', String(height - 8));
            label.setAttribute('text-anchor', 'middle');
            label.textContent = point.label;
            svg.appendChild(label);
        });
    }

    function renderEmptyMap(message) {
        dom.mapSvg.innerHTML = '';
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
