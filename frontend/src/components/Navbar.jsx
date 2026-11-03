import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const LINKS = {
  EMPLOYEE: [
    { to: '/dashboard', label: 'Tableau de bord' },
    { to: '/incidents/my', label: 'Mes incidents' },
    { to: '/incidents/new', label: 'Déclarer un incident' },
  ],
  TECHNICIAN: [
    { to: '/dashboard', label: 'Tableau de bord' },
    { to: '/incidents/assigned', label: 'Incidents affectés' },
  ],
  ADMIN: [
    { to: '/dashboard', label: 'Tableau de bord' },
    { to: '/admin/incidents', label: 'Tous les incidents' },
    { to: '/admin/users', label: 'Utilisateurs' },
  ],
}

const ROLE_LABEL = { EMPLOYEE: 'Employé', TECHNICIAN: 'Technicien', ADMIN: 'Administrateur' }

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  if (!user) return null

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <div className="brand">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor"><path d="M12 2 4 5v6c0 5 3.4 9.7 8 11 4.6-1.3 8-6 8-11V5l-8-3z"/></svg>
        SecIncidents
      </div>
      <nav>
        {(LINKS[user.role] || []).map((l) => (
          <NavLink key={l.to} to={l.to} end className={({ isActive }) => (isActive ? 'active' : '')}>
            {l.label}
          </NavLink>
        ))}
      </nav>
      <div className="user-box">
        <NavLink to="/profile" className="user-name">{user.fullName}</NavLink>
        <span className={`role-chip role-${user.role}`}>{ROLE_LABEL[user.role]}</span>
        <button className="btn btn-light btn-sm" onClick={handleLogout}>Déconnexion</button>
      </div>
    </header>
  )
}
