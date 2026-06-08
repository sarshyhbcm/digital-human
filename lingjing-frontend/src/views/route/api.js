import request from '../../api'

export function getTourRoutes() {
  return request({
    url: '/routes',
    method: 'get'
  })
}

export function getTourRouteDetail(id) {
  return request({
    url: `/routes/${id}`,
    method: 'get'
  })
}

/**
 * 根据偏好推荐路线
 * @param {string} preference - 偏好：历史文化/自然风光/亲子家庭
 * @returns {Promise<Array<{route: object, matchScore: number, matchedTags: string[]}>>}
 */
export function getRecommendedRoutes(preference) {
  return request({
    url: '/routes/recommend',
    method: 'get',
    params: { preference }
  })
}

/**
 * 自定义路线优化
 * @param {number[]} attractionIds - 选择的景点ID列表
 * @returns {Promise<{attractions: object[], segments: object[], totalDistanceMeters: number, estimatedMinutes: number}>}
 */
export function optimizeCustomRoute(attractionIds) {
  return request({
    url: '/routes/custom/optimize',
    method: 'post',
    data: { attractionIds }
  })
}

/** 保存路线 */
export function saveUserRoute(data) {
  return request({
    url: '/user-routes',
    method: 'post',
    data
  })
}

/** 更新路线 */
export function updateUserRoute(id, data) {
  return request({
    url: `/user-routes/${id}`,
    method: 'put',
    data
  })
}

/** 将推荐路线保存到我的路线 */
export function saveTourRouteToMyRoutes(routeId) {
  return request({
    url: `/user-routes/from-tour/${routeId}`,
    method: 'post'
  })
}

/** 获取我的路线列表 */
export function getUserRoutes() {
  return request({
    url: '/user-routes',
    method: 'get'
  })
}

/** 获取路线详情（含景点+路段） */
export function getUserRouteDetail(id) {
  return request({
    url: `/user-routes/${id}`,
    method: 'get'
  })
}

/** 删除路线 */
export function deleteUserRoute(id) {
  return request({
    url: `/user-routes/${id}`,
    method: 'delete'
  })
}
