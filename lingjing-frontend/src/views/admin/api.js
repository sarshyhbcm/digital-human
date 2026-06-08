import request from '../../api/index'

export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}

export function getTrends() {
  return request.get('/admin/dashboard/trends')
}

export function getPopularAttractions() {
  return request.get('/admin/dashboard/popular-attractions')
}

export function getSentiment() {
  return request.get('/admin/dashboard/sentiment')
}

export function getTopQuestions() {
  return request.get('/admin/dashboard/top-questions')
}

export function getUsers(params) {
  return request.get('/admin/users', { params })
}

export function updateUserStatus(id, data) {
  return request.put(`/admin/users/${id}/status`, data)
}

export function updateUserRole(id, data) {
  return request.put(`/admin/users/${id}/role`, data)
}

export function getConversations(params) {
  return request.get('/admin/conversations', { params })
}

export function getConversationMessages(id) {
  return request.get(`/admin/conversations/${id}/messages`)
}

export function getAchievements() {
  return request.get('/admin/achievements')
}

export function createAchievement(data) {
  return request.post('/admin/achievements', data)
}

export function updateAchievement(id, data) {
  return request.put(`/admin/achievements/${id}`, data)
}

export function deleteAchievement(id) {
  return request.delete(`/admin/achievements/${id}`)
}

export function getQrCodes() {
  return request.get('/admin/qrcodes')
}

export function generateQrCode(id) {
  return request.post(`/admin/qrcodes/${id}/generate`)
}

// 数字人形象管理
export function getDigitalHumanConfig() {
  return request.get('/digital-human/config')
}

export function updateDigitalHumanConfig(data) {
  return request.put('/digital-human/config', data)
}

// 感受度报告
export function getInteractionTypes() {
  return request.get('/admin/dashboard/interaction-types')
}

// 热搜管理
export function getHotSearches() {
  return request.get('/admin/hot-searches')
}
export function createHotSearch(data) {
  return request.post('/admin/hot-searches', data)
}
export function updateHotSearch(id, data) {
  return request.put(`/admin/hot-searches/${id}`, data)
}
export function deleteHotSearch(id) {
  return request.delete(`/admin/hot-searches/${id}`)
}

// ======== 景点管理 ========
export function getAdminAttractions(params) {
  return request.get('/admin/attractions', { params })
}

export function getAdminAttractionDetail(id) {
  return request.get(`/admin/attractions/${id}`)
}

export function createAttraction(data) {
  return request.post('/admin/attractions', data)
}

export function updateAttraction(id, data) {
  return request.put(`/admin/attractions/${id}`, data)
}

export function deleteAttraction(id) {
  return request.delete(`/admin/attractions/${id}`)
}

export function uploadAttractionCover(id, formData) {
  return request({
    url: `/admin/attractions/${id}/cover`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

export function uploadAttractionImages(id, formData) {
  return request({
    url: `/admin/attractions/${id}/images`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function removeAttractionImage(id, data) {
  return request({
    url: `/admin/attractions/${id}/images`,
    method: 'delete',
    data
  })
}

// ======== 轮播图管理 ========
export function getBanners() {
  return request.get('/admin/banners')
}

export function createBanner(data) {
  return request.post('/admin/banners', data)
}

export function updateBanner(id, data) {
  return request.put(`/admin/banners/${id}`, data)
}

export function deleteBanner(id) {
  return request.delete(`/admin/banners/${id}`)
}

export function uploadBannerImage(formData) {
  return request({
    url: '/admin/banners/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}
