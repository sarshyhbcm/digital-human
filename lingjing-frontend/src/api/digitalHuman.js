import request from '../api'

export function getDigitalHumanConfig() {
  return request({
    url: '/digital-human/config',
    method: 'get'
  })
}

export function updateDigitalHumanConfig(data) {
  return request({
    url: '/digital-human/config',
    method: 'put',
    data
  })
}
