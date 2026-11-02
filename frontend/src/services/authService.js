import api from './api'

/**
 * Stockage du JWT dans le navigateur (localStorage).
 * Cles : token, refreshToken, user (role, fullName, email)
 */
const TOKEN_KEY = 'token'
const REFRESH_KEY = 'refreshToken'
const USER_KEY = 'user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_KEY)
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  return raw ? JSON.parse(raw) : null
}

export function saveSession(data) {
  localStorage.setItem(TOKEN_KEY, data.token)
  if (data.refreshToken) localStorage.setItem(REFRESH_KEY, data.refreshToken)
  const current = getUser() || {}
  localStorage.setItem(USER_KEY, JSON.stringify({
    role: data.role ?? current.role,
    fullName: data.fullName ?? current.fullName,
    email: data.email ?? current.email,
  }))
}

export function updateStoredUser(patch) {
  localStorage.setItem(USER_KEY, JSON.stringify({ ...(getUser() || {}), ...patch }))
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USER_KEY)
}

/** Decode le payload du JWT (sans verifier la signature : c'est le role du backend). */
export function decodeToken(token = getToken()) {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(decodeURIComponent(escape(atob(payload))))
  } catch {
    return null
  }
}

export function isAuthenticated() {
  return !!getToken() && !!getUser()
}

export async function login(email, password) {
  const { data } = await api.post('/auth/login', { email, password })
  saveSession(data)
  return data
}

export async function register(fullName, email, password) {
  const { data } = await api.post('/auth/register', { fullName, email, password })
  return data
}

export async function logout() {
  const refreshToken = getRefreshToken()
  try {
    if (refreshToken) await api.post('/auth/logout', { refreshToken })
  } finally {
    clearSession()
  }
}
