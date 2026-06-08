import request from '../../api'

export function getKbDocuments() {
  return request({
    url: '/kb/documents',
    method: 'get'
  })
}

export function importKbDocument(filePath) {
  return request({
    url: '/kb/import',
    method: 'post',
    data: { filePath }
  })
}

export function deleteKbDocument(id) {
  return request({
    url: `/kb/documents/${id}`,
    method: 'delete'
  })
}

export function searchKb(query) {
  return request({
    url: '/kb/search',
    method: 'get',
    params: { q: query, limit: 5 }
  })
}
