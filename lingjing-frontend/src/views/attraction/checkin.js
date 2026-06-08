import request from '../../api'

export function checkIn(data) {
  return request({
    url: '/checkins',
    method: 'post',
    data
  })
}

export function getCheckInStatus(attractionId) {
  return request({
    url: `/checkins/status/${attractionId}`,
    method: 'get'
  })
}

export function getMyCheckIns() {
  return request({
    url: '/checkins/my',
    method: 'get'
  })
}

export function getCheckInCount() {
  return request({
    url: '/checkins/count',
    method: 'get'
  })
}
