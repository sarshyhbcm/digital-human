<template>
  <div ref="mapContainer" class="map-container" />
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

const props = defineProps({
  attractions: { type: Array, required: true }
})

const emit = defineEmits(['check-in'])

const mapContainer = ref(null)
let map = null
let markers = []
let infoWindow = null

const key = import.meta.env.VITE_AMAP_KEY
const secret = import.meta.env.VITE_AMAP_SECRET

function markerColor(area) {
  return area === '灵山胜境' ? '#409eff' : '#67c23a'
}

function createMarkerContent(attraction, index) {
  const color = markerColor(attraction.area)
  const checked = attraction.checked
  const bg = checked ? '#67c23a' : color
  const border = checked ? '#67c23a' : color

  return `<div style="
    position:relative;width:36px;height:36px;border-radius:50%;
    background:${bg};border:3px solid ${border};
    display:flex;align-items:center;justify-content:center;
    color:#fff;font-size:14px;font-weight:bold;
    box-shadow:0 2px 8px rgba(0,0,0,0.3);
    cursor:pointer;
  ">${checked ? '✓' : index + 1}
    <div style="position:absolute;bottom:-18px;left:50%;transform:translateX(-50%);
      white-space:nowrap;font-size:11px;color:#303133;font-weight:500;
      text-shadow:0 0 4px #fff,0 0 4px #fff;">
      ${attraction.name.length > 5 ? attraction.name.slice(0, 5) + '..' : attraction.name}
    </div>
  </div>`
}

function createInfoContent(attraction) {
  const tags = attraction.tags || ''
  return `<div style="min-width:170px;">
    <div style="font-size:15px;font-weight:600;color:#303133;margin-bottom:4px;">
      ${attraction.name}
    </div>
    <div style="font-size:12px;color:#909399;margin-bottom:8px;">
      ${attraction.area}${tags ? ' · ' + tags : ''}
    </div>
    ${attraction.checked
      ? '<div style="font-size:13px;color:#67c23a;font-weight:600;">✅ 已打卡</div>'
      : '<button data-checkin-btn style="padding:6px 14px;background:#409eff;color:#fff;border:none;border-radius:6px;font-size:13px;cursor:pointer;">📍 GPS打卡</button>'
    }
  </div>`
}

function renderMarkers() {
  if (!map || !props.attractions.length) return

  markers.forEach(m => map.remove(m))
  markers = []

  props.attractions.forEach((a, i) => {
    const pos = [Number(a.longitude), Number(a.latitude)]

    const marker = new AMap.Marker({
      position: pos,
      content: createMarkerContent(a, i),
      offset: new AMap.Pixel(-18, -18),
      zIndex: a.checked ? 120 : 100
    })

    marker.on('click', () => {
      openInfoWindow(a, marker, pos)
    })

    map.add(marker)
    markers.push(marker)
  })

  if (markers.length > 0) {
    map.setFitView(markers, false, [80, 80, 80, 80])
  }
}

function openInfoWindow(attraction, marker, position) {
  if (infoWindow) infoWindow.close()

  infoWindow = new AMap.InfoWindow({
    content: createInfoContent(attraction),
    offset: new AMap.Pixel(-15, -36)
  })

  infoWindow.open(map, position)

  setTimeout(() => {
    const btn = document.querySelector('[data-checkin-btn]')
    if (btn) {
      btn.onclick = () => emit('check-in', attraction)
    }
  }, 100)
}

function destroyMap() {
  if (infoWindow) infoWindow.close()
  markers.forEach(m => map?.remove(m))
  markers = []
  if (map) {
    map.destroy()
    map = null
  }
}

watch(() => props.attractions, () => {
  if (map) renderMarkers()
}, { deep: true })

onMounted(async () => {
  if (!key || !mapContainer.value) return

  try {
    window._AMapSecurityConfig = {
      securityJsCode: secret
    }
    const AMap = await AMapLoader.load({
      key,
      version: '2.0',
      plugins: ['AMap.PlaceSearch']
    })

    map = new AMap.Map(mapContainer.value, {
      zoom: 13,
      center: [120.095, 31.428],
      mapStyle: 'amap://styles/light',
      showIndoorMap: false,
      features: ['bg', 'road', 'building', 'point']
    })

    // Wait for map to render before adding markers
    setTimeout(() => renderMarkers(), 200)
  } catch (e) {
    console.error('高德地图加载失败:', e)
  }
})

onUnmounted(() => {
  destroyMap()
})
</script>

<style scoped>
.map-container {
  width: 100%;
  height: 620px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
</style>
