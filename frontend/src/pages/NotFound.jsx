import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="center-page">
      <h1 className="big-code">404</h1>
      <p>Page introuvable.</p>
      <Link className="btn btn-primary" to="/dashboard">Retour au tableau de bord</Link>
    </div>
  )
}
