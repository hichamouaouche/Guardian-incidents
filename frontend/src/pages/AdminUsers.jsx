import { useEffect, useState } from 'react'
import { getUsers, enableUser, disableUser } from '../services/userService'
import { errorMessage } from '../services/api'
import { getUser } from '../services/authService'
import { formatDate } from '../components/Badge'

export default function AdminUsers() {
  const me = getUser()
  const [users, setUsers] = useState([])
  const [msg, setMsg] = useState({})

  const load = () => getUsers().then(setUsers).catch((e) => setMsg({ err: errorMessage(e) }))
  useEffect(() => { load() }, [])

  const toggle = async (u) => {
    setMsg({})
    try {
      if (u.enabled) await disableUser(u.id)
      else await enableUser(u.id)
      setMsg({ ok: `Compte de ${u.fullName} ${u.enabled ? 'désactivé' : 'activé'}` })
      load()
    } catch (e) {
      setMsg({ err: errorMessage(e) })
    }
  }

  return (
    <div>
      <h1>Gestion des utilisateurs</h1>
      {msg.ok && <div className="alert alert-success">{msg.ok}</div>}
      {msg.err && <div className="alert alert-error">{msg.err}</div>}
      <div className="card">
        <table className="table">
          <thead>
            <tr><th>#</th><th>Nom</th><th>Email</th><th>Rôle</th><th>Etat</th><th>Créé le</th><th>Action</th></tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.fullName}</td>
                <td>{u.email}</td>
                <td><span className={`role-chip role-${u.role}`}>{u.role}</span></td>
                <td>{u.enabled ? <span className="badge st-RESOLVED">Actif</span> : <span className="badge pr-CRITICAL">Désactivé</span>}</td>
                <td>{formatDate(u.createdAt)}</td>
                <td>
                  {u.email !== me.email && (
                    <button className={`btn btn-sm ${u.enabled ? 'btn-danger' : 'btn-success'}`} onClick={() => toggle(u)}>
                      {u.enabled ? 'Désactivér' : 'Activer'}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
