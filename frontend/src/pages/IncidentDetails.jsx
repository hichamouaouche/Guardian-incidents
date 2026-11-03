import { useCallback, useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getIncident, updateStatus, assignIncident, addComment } from '../services/incidentService'
import { getTechnicians } from '../services/userService'
import { errorMessage } from '../services/api'
import { getUser } from '../services/authService'
import { StatusBadge, PriorityBadge, formatDate } from '../components/Badge'

const NEXT_STATUSES = {
  TECHNICIAN: { OPEN: ['IN_PROGRESS'], ASSIGNED: ['IN_PROGRESS'], IN_PROGRESS: ['RESOLVED'], RESOLVED: ['IN_PROGRESS'] },
  ADMIN: {
    OPEN: ['IN_PROGRESS', 'CLOSED'], ASSIGNED: ['IN_PROGRESS', 'CLOSED'],
    IN_PROGRESS: ['RESOLVED', 'CLOSED'], RESOLVED: ['IN_PROGRESS', 'CLOSED'],
  },
}

export default function IncidentDetails() {
  const { id } = useParams()
  const user = getUser()
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [actionMsg, setActionMsg] = useState({})
  const [technicians, setTechnicians] = useState([])
  const [techId, setTechId] = useState('')
  const [note, setNote] = useState('')

  const load = useCallback(() => {
    getIncident(id).then(setData).catch((e) => setError(errorMessage(e)))
  }, [id])

  useEffect(() => {
    load()
    if (user.role === 'ADMIN') getTechnicians().then(setTechnicians)
  }, [load, user.role])

  const run = async (fn, success) => {
    setActionMsg({})
    try {
      await fn()
      setActionMsg({ ok: success })
      load()
    } catch (e) {
      setActionMsg({ err: errorMessage(e) })
    }
  }

  if (error) {
    return (
      <div>
        <div className="alert alert-error big">{error}</div>
        <Link to="/dashboard">Retour au tableau de bord</Link>
      </div>
    )
  }
  if (!data) return <p>Chargement...</p>

  const inc = data.incident
  const canAct = (user.role === 'TECHNICIAN' || user.role === 'ADMIN') && inc.status !== 'CLOSED'
  const nextStatuses = NEXT_STATUSES[user.role]?.[inc.status] || []

  return (
    <div>
      <div className="page-head">
        <div>
          <p className="muted small">Incident #{inc.id}</p>
          <h1>{inc.title}</h1>
        </div>
        <div className="badges"><PriorityBadge value={inc.priority} /> <StatusBadge value={inc.status} /></div>
      </div>

      {actionMsg.ok && <div className="alert alert-success">{actionMsg.ok}</div>}
      {actionMsg.err && <div className="alert alert-error">{actionMsg.err}</div>}

      <div className="grid-2">
        <div className="card">
          <h3>Détails</h3>
          <dl className="info">
            <dt>Catégorie</dt><dd>{inc.category}</dd>
            <dt>Déclaré par</dt><dd>{inc.createdBy.fullName}</dd>
            <dt>Technicien</dt><dd>{inc.assignedTo?.fullName || <em className="muted">Non affecté</em>}</dd>
            <dt>Créé le</dt><dd>{formatDate(inc.createdAt)}</dd>
            <dt>Mis à jour</dt><dd>{formatDate(inc.updatedAt)}</dd>
          </dl>
          <h3>Description</h3>
          <p className="description">{inc.description}</p>
        </div>

        <div className="card">
          <h3>Historique</h3>
          <ul className="timeline">
            {data.history.map((h) => (
              <li key={h.id}>
                <span className="tl-date">{formatDate(h.createdAt)}</span>
                <span className="tl-text">{h.description}</span>
                <span className="tl-by">par {h.performedBy}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      {canAct && (
        <div className="card actions">
          <h3>Traitement</h3>
          <div className="action-row">
            {nextStatuses.map((s) => (
              <button key={s} className="btn btn-primary"
                onClick={() => run(() => updateStatus(inc.id, s), `Statut modifié : ${inc.status} -> ${s}`)}>
                Passer à {s}
              </button>
            ))}
            {user.role === 'ADMIN' && (
              <div className="assign">
                <select value={techId} onChange={(e) => setTechId(e.target.value)}>
                  <option value="">-- Technicien --</option>
                  {technicians.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
                </select>
                <button className="btn btn-secondary" disabled={!techId}
                  onClick={() => run(() => assignIncident(inc.id, techId), 'Incident affecté')}>
                  Affecter
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      <div className="card">
        <h3>Notes de traitement ({data.comments.length})</h3>
        {data.comments.length === 0 && <p className="muted">Aucune note pour le moment.</p>}
        <ul className="comments">
          {data.comments.map((c) => (
            <li key={c.id}>
              <div className="comment-head"><b>{c.author}</b> <span className="muted small">{formatDate(c.createdAt)}</span></div>
              <div>{c.content}</div>
            </li>
          ))}
        </ul>
        {canAct && (
          <form className="comment-form" onSubmit={(e) => {
            e.preventDefault()
            run(() => addComment(inc.id, note), 'Note ajoutée').then(() => setNote(''))
          }}>
            <input value={note} onChange={(e) => setNote(e.target.value)} placeholder="Ex : Le poste a été isolé du réseau." />
            <button className="btn btn-primary" disabled={!note.trim()}>Ajouter la note</button>
          </form>
        )}
      </div>
    </div>
  )
}
