import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getDashboard } from '../services/incidentService'
import { errorMessage } from '../services/api'
import { useAuth } from '../context/AuthContext'
import IncidentTable from '../components/IncidentTable'

function StatCard({ label, value, tone = 'blue' }) {
  return (
    <div className={`stat-card tone-${tone}`}>
      <div className="stat-value">{value ?? 0}</div>
      <div className="stat-label">{label}</div>
    </div>
  )
}

export default function Dashboard() {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getDashboard().then(setData).catch((e) => setError(errorMessage(e)))
  }, [])

  if (error) return <div className="alert alert-error">{error}</div>
  if (!data) return <p>Chargement...</p>

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Tableau de bord</h1>
          <p className="muted">Bienvenue, {user.fullName}</p>
        </div>
        {data.role === 'EMPLOYEE' && <Link className="btn btn-primary" to="/incidents/new">+ Déclarer un incident</Link>}
      </div>

      {data.role === 'EMPLOYEE' && (
        <>
          <div className="stats">
            <StatCard label="Total de mes incidents" value={data.total} />
            <StatCard label="Ouverts (OPEN)" value={data.open} tone="amber" />
            <StatCard label="En cours (IN_PROGRESS)" value={data.inProgress} tone="violet" />
            <StatCard label="Résolus ou clôturés" value={data.resolvedOrClosed} tone="green" />
          </div>
          <h2>Mes derniers incidents</h2>
          <div className="card"><IncidentTable incidents={data.latest} /></div>
        </>
      )}

      {data.role === 'TECHNICIAN' && (
        <>
          <div className="stats">
            <StatCard label="Incidents affectés" value={data.assigned} />
            <StatCard label="Incidents critiques" value={data.critical} tone="red" />
            <StatCard label="En cours" value={data.inProgress} tone="violet" />
            <StatCard label="Résolus" value={data.resolved} tone="green" />
          </div>
          <h2>Derniers incidents affectés</h2>
          <div className="card"><IncidentTable incidents={data.latest} showCreator showAssignee={false} /></div>
        </>
      )}

      {data.role === 'ADMIN' && (
        <>
          <h2>Utilisateurs</h2>
          <div className="stats">
            <StatCard label="Utilisateurs" value={data.totalUsers} />
            <StatCard label="Employés" value={data.employees} tone="cyan" />
            <StatCard label="Techniciens" value={data.technicians} tone="violet" />
          </div>
          <h2>Incidents</h2>
          <div className="stats">
            <StatCard label="Total incidents" value={data.totalIncidents} />
            <StatCard label="Ouverts (OPEN)" value={data.open} tone="amber" />
            <StatCard label="En cours (IN_PROGRESS)" value={data.inProgress} tone="violet" />
            <StatCard label="Clôturés (CLOSED)" value={data.closed} tone="green" />
          </div>
          <div className="card">
            <h3>Répartition par statut</h3>
            <div className="bars">
              {Object.entries(data.byStatus).map(([k, v]) => (
                <div className="bar-row" key={k}>
                  <span className="bar-label">{k}</span>
                  <div className="bar-track">
                    <div className={`bar-fill st-${k}`} style={{ width: `${data.totalIncidents ? (v / data.totalIncidents) * 100 : 0}%` }} />
                  </div>
                  <span className="bar-value">{v}</span>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  )
}
