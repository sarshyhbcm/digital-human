import request from '../../api'

export function getAttractions(params) {
  return request({
    url: '/attractions',
    method: 'get',
    params
  })
}

export function getAttractionDetail(id) {
  return request({
    url: `/attractions/${id}`,
    method: 'get'
  })
}

export function getBanners() {
  return request({
    url: '/banners',
    method: 'get'
  })
}
