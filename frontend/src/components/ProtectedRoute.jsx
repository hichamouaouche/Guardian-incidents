import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { isAuthenticated } from '../services/authService'

/**
 * Empeche un utilisateur NON authentifie d'acceder aux pages protegees.
 * Rappel : c'est une protection d'ergonomie, la vraie securite est cote backend.
 */
export default function ProtectedRoute() {
  const location = useLocation()
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  return <Outlet />
}
