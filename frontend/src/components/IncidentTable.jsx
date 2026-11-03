import { Link } from 'react-router-dom'
import { StatusBadge, PriorityBadge, formatDate } from './Badge'

export default function IncidentTable({ incidents, showCreator = false, showAssignee = true, extra }) {
  if (!incidents.length) return <p className="empty">Aucun incident.</p>
  return (
    <table className="table">
      <thead>
        <tr>
          <th>#</th>
          <th>Titre</th>
          <th>Catégorie</th>
          <th>Priorité</th>
          <th>Statut</th>
          {showCreator && <th>Déclaré par</th>}
          {showAssignee && <th>Technicien</th>}
          <th>Créé le</th>
          {extra && <th>Actions</th>}
        </tr>
      </thead>
      <tbody>
        {incidents.map((i) => (
          <tr key={i.id}>
            <td>{i.id}</td>
            <td><Link to={`/incidents/${i.id}`}>{i.title}</Link></td>
            <td>{i.category}</td>
            <td><PriorityBadge value={i.priority} /></td>
            <td><StatusBadge value={i.status} /></td>
            {showCreator && <td>{i.createdBy?.fullName}</td>}
            {showAssignee && <td>{i.assignedTo?.fullName || <em className="muted">Non affecté</em>}</td>}
            <td>{formatDate(i.createdAt)}</td>
            {extra && <td>{extra(i)}</td>}
          </tr>
        ))}
      </tbody>
    </table>
  )
}
