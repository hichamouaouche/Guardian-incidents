import api from './api'

// ---- Profil (tout utilisateur authentifie)
export const getProfile = () => api.get('/api/profile').then((r) => r.data)
export const updateProfile = (data) => api.put('/api/profile', data).then((r) => r.data)
export const changePassword = (data) => api.put('/api/profile/password', data).then((r) => r.data)

// ---- Administration (ADMIN)
export const getUsers = () => api.get('/api/admin/users').then((r) => r.data)
export const getTechnicians = () => api.get('/api/admin/technicians').then((r) => r.data)
export const enableUser = (id) => api.put(`/api/admin/users/${id}/enable`).then((r) => r.data)
export const disableUser = (id) => api.put(`/api/admin/users/${id}/disable`).then((r) => r.data)
export const getStatistics = () => api.get('/api/admin/statistics').then((r) => r.data)
