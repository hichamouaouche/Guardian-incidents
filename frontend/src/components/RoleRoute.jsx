import { Navigate, Outlet } from 'react-router-dom'
import { getUser } from '../services/authService'

/**
 * Controle l'acces a une page selon le role.
 * Exemple : <RoleRoute roles={['ADMIN']} /> pour /admin/users
 */
export default function RoleRoute({ roles }) {
  const user = getUser()
  if (!user) return <Navigate to="/login" replace />
  if (!roles.includes(user.role)) return <Navigate to="/forbidden" replace />
  return <Outlet />
}
