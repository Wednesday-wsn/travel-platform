import request from './request'

// 上传图片，返回图片 URL
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
