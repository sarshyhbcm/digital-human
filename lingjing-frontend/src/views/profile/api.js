import request from '../../api'

export function getMyAchievements() {
  return request({
    url: '/achievements/my',
    method: 'get'
  })
}

export function getAchievements() {
  return request({
    url: '/achievements',
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
