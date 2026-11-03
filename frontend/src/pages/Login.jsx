import { useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { errorMessage } from '../services/api'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // message pose par l'interceptor Axios lors d'un 401 (session expiree)
  const [info] = useState(() => {
    const msg = sessionStorage.getItem('authMessage')
    sessionStorage.removeItem('authMessage')
    if (msg || params.get('expired')) return msg || 'Votre session a expiré. Veuillez vous reconnecter.'
    if (params.get('registered')) return 'Compte créé avec succes. Vous pouvez vous connecter.'
    return ''
  })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true })
    } catch (err) {
      setError(errorMessage(err, 'Connexion impossible'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit} noValidate>
        <div className="auth-logo">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="currentColor"><path d="M12 2 4 5v6c0 5 3.4 9.7 8 11 4.6-1.3 8-6 8-11V5l-8-3z"/></svg>
        </div>
        <h1>Connexion</h1>
        <p className="muted center">Plateforme de gestion des incidents</p>
        {info && <div className="alert alert-warning">{info}</div>}
        {error && <div className="alert alert-error">{error}</div>}
        <label>Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nom@entreprise.ma" required />
        </label>
        <label>Mot de passe
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="********" required />
        </label>
        <button className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Connexion...' : 'Se connecter'}</button>
        <p className="center small">Pas de compte ? <Link to="/register">Créer un compte</Link></p>
      </form>
    </div>
  )
}
