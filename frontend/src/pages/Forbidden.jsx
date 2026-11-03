import { Link } from 'react-router-dom'

export default function Forbidden() {
  return (
    <div className="center-page">
      <h1 className="big-code">403</h1>
      <p>Accès interdit : votre rôle ne permet pas d'accéder à cette page.</p>
      <Link className="btn btn-primary" to="/dashboard">Retour au tableau de bord</Link>
    </div>
  )
}
