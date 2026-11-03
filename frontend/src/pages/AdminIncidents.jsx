import { useEffect, useState } from 'react'
import { getAllIncidents, assignIncident, STATUSES } from '../services/incidentService'
import { getTechnicians } from '../services/userService'
import { errorMessage } from '../services/api'
import IncidentTable from '../components/IncidentTable'

export default function AdminIncidents() {
  const [incidents, setIncidents] = useState(null)
  const [technicians, setTechnicians] = useState([])
  const [filter, setFilter] = useState('')
  const [msg, setMsg] = useState({})

  const load = (status) => getAllIncidents(status).then(setIncidents).catch((e) => setMsg({ err: errorMessage(e) }))

  useEffect(() => { load(filter) }, [filter])
  useEffect(() => { getTechnicians().then(setTechnicians) }, [])

  const assign = async (incident, techId) => {
    if (!techId) return
    setMsg({})
    try {
      const res = await assignIncident(incident.id, techId)
      setMsg({ ok: `Incident #${incident.id} affecté à ${res.assignedTo.fullName}` })
      load(filter)
    } catch (e) {
      setMsg({ err: errorMessage(e) })
    }
  }

  return (
    <div>
      <div className="page-head">
        <h1>Tous les incidents</h1>
        <select value={filter} onChange={(e) => setFilter(e.target.value)} className="filter">
          <option value="">Tous les statuts</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>
      {msg.ok && <div className="alert alert-success">{msg.ok}</div>}
      {msg.err && <div className="alert alert-error">{msg.err}</div>}
      <div className="card">
        {incidents ? (
          <IncidentTable incidents={incidents} showCreator
            extra={(i) => i.status === 'CLOSED' ? null : (
              <select className="select-sm" defaultValue="" onChange={(e) => assign(i, e.target.value)}>
                <option value="">Affecter à...</option>
                {technicians.map((t) => <option key={t.id} value={t.id}>{t.fullName}</option>)}
              </select>
            )}
          />
        ) : <p>Chargement...</p>}
      </div>
    </div>
  )
}
