import { useEffect, useState } from 'react'
import { getProfile, updateProfile, changePassword } from '../services/userService'
import { errorMessage, fieldErrors } from '../services/api'
import { saveSession, updateStoredUser } from '../services/authService'
import { useAuth } from '../context/AuthContext'
import { formatDate } from '../components/Badge'

export default function Profile() {
  const { refreshUser } = useAuth()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({ fullName: '', email: '' })
  const [pwd, setPwd] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' })
  const [msg, setMsg] = useState({})
  const [errs, setErrs] = useState({})

  useEffect(() => {
    getProfile().then((p) => { setProfile(p); setForm({ fullName: p.fullName, email: p.email }) })
  }, [])

  const submitProfile = async (e) => {
    e.preventDefault()
    setMsg({}); setErrs({})
    try {
      const res = await updateProfile(form)
      // l'email (subject du JWT) a pu changer : on enregistre le nouveau token
      saveSession({ token: res.token })
      updateStoredUser({ fullName: res.user.fullName, email: res.user.email })
      refreshUser()
      setProfile(res.user)
      setMsg({ profile: 'Profil mis à jour' })
    } catch (err) {
      setMsg({ profileError: errorMessage(err) }); setErrs(fieldErrors(err))
    }
  }

  const submitPassword = async (e) => {
    e.preventDefault()
    setMsg({}); setErrs({})
    if (pwd.newPassword !== pwd.confirmPassword) {
      setMsg({ pwdError: 'La confirmation ne correspond pas au nouveau mot de passe' })
      return
    }
    try {
      const res = await changePassword(pwd)
      setMsg({ pwd: res.message })
      setPwd({ oldPassword: '', newPassword: '', confirmPassword: '' })
    } catch (err) {
      setMsg({ pwdError: errorMessage(err) }); setErrs(fieldErrors(err))
    }
  }

  if (!profile) return <p>Chargement...</p>

  return (
    <div>
      <h1>Mon profil</h1>
      <div className="grid-2">
        <div className="card">
          <h3>Informations</h3>
          <dl className="info">
            <dt>Nom</dt><dd>{profile.fullName}</dd>
            <dt>Email</dt><dd>{profile.email}</dd>
            <dt>Rôle</dt><dd><span className={`role-chip role-${profile.role}`}>{profile.role}</span></dd>
            <dt>Compte</dt><dd>{profile.enabled ? 'Actif' : 'Désactivé'}</dd>
            <dt>Créé le</dt><dd>{formatDate(profile.createdAt)}</dd>
          </dl>
          <form onSubmit={submitProfile} noValidate>
            <h3>Modifier mon profil</h3>
            {msg.profile && <div className="alert alert-success">{msg.profile}</div>}
            {msg.profileError && <div className="alert alert-error">{msg.profileError}</div>}
            <label>Nom complet
              <input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
              {errs.fullName && <span className="field-error">{errs.fullName}</span>}
            </label>
            <label>Email
              <input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
              {errs.email && <span className="field-error">{errs.email}</span>}
            </label>
            <button className="btn btn-primary">Enregistrer</button>
          </form>
        </div>
        <div className="card">
          <form onSubmit={submitPassword} noValidate>
            <h3>Changer le mot de passe</h3>
            {msg.pwd && <div className="alert alert-success">{msg.pwd}</div>}
            {msg.pwdError && <div className="alert alert-error">{msg.pwdError}</div>}
            <label>Ancien mot de passe
              <input type="password" value={pwd.oldPassword} onChange={(e) => setPwd({ ...pwd, oldPassword: e.target.value })} />
            </label>
            <label>Nouveau mot de passe
              <input type="password" value={pwd.newPassword} onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })} />
              {errs.newPassword && <span className="field-error">{errs.newPassword}</span>}
            </label>
            <label>Confirmation du nouveau mot de passe
              <input type="password" value={pwd.confirmPassword} onChange={(e) => setPwd({ ...pwd, confirmPassword: e.target.value })} />
            </label>
            <button className="btn btn-primary">Changer le mot de passe</button>
          </form>
        </div>
      </div>
    </div>
  )
}
