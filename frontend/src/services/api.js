import axios from 'axios'
import { clearSession, getRefreshToken, saveSession, getToken } from './authService'

/**
 * Instance Axios unique utilisee par toute l'application.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8085',
  headers: { 'Content-Type': 'application/json' },
})

/* ------------------------------------------------------------------
 * Interceptor de REQUETE : ajoute automatiquement
 *      Authorization: Bearer <JWT>
 * sur toutes les requetes vers l'API protegee.
 * ------------------------------------------------------------------ */
api.interceptors.request.use((config) => {
  const token = getToken()
  if (token && !config.url.startsWith('/auth/')) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/* ------------------------------------------------------------------
 * Interceptor de REPONSE : gestion du 401 Unauthorized
 *  1. si un refresh token existe -> POST /auth/refresh puis on rejoue la requete
 *  2. sinon (ou si le refresh echoue) :
 *       - suppression du JWT
 *       - redirection vers /login
 *       - message "session expiree"
 * ------------------------------------------------------------------ */
let refreshPromise = null

function expireSession() {
  clearSession()
  sessionStorage.setItem('authMessage', 'Votre session a expiré. Veuillez vous reconnecter.')
  window.location.href = '/login?expired=1'
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    const status = error.response?.status
    const isAuthCall = original?.url?.startsWith('/auth/')

    if (status === 401 && !isAuthCall && !original._retry) {
      original._retry = true
      const refreshToken = getRefreshToken()
      if (refreshToken) {
        try {
          // une seule requete de refresh meme si plusieurs appels echouent en parallele
          refreshPromise = refreshPromise || api.post('/auth/refresh', { refreshToken })
          const { data } = await refreshPromise
          saveSession(data)
          original.headers.Authorization = `Bearer ${data.token}`
          return api(original)
        } catch (e) {
          expireSession()
          return Promise.reject(e)
        } finally {
          refreshPromise = null
        }
      }
      expireSession()
    }
    return Promise.reject(error)
  }
)

/** Extrait un message lisible d'une erreur Axios (format ErrorResponse du backend). */
export function errorMessage(err, fallback = 'Une erreur est survenue') {
  return err?.response?.data?.message || fallback
}

/** Extrait les erreurs de validation champ par champ (@Valid). */
export function fieldErrors(err) {
  return err?.response?.data?.errors || {}
}

export default api
