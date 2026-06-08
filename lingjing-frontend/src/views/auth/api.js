import request from '../../api'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function getCurrentUser() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

export function updateProfile(data) {
  return request({
    url: '/auth/profile',
    method: 'put',
    data
  })
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/auth/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getMyAchievements() {
  return request({
    url: '/achievements/my',
    method: 'get'
  })
}

export function equipAchievement(achievementId) {
  return request({
    url: `/achievements/equip/${achievementId}`,
    method: 'put'
  })
}

export function unequipAchievement() {
  return request({
    url: '/achievements/equip',
    method: 'delete'
  })
}
