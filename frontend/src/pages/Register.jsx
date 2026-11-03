import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../services/authService'
import { errorMessage, fieldErrors } from '../services/api'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirm: '' })
  const [errors, setErrors] = useState({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setErrors({})
    if (form.password !== form.confirm) {
      setErrors({ confirm: 'Les mots de passe ne correspondent pas' })
      return
    }
    setLoading(true)
    try {
      // aucun role n'est envoye : le backend attribue EMPLOYEE
      await register(form.fullName, form.email, form.password)
      navigate('/login?registered=1')
    } catch (err) {
      setError(errorMessage(err))
      setErrors(fieldErrors(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit} noValidate>
        <h1>Créer un compte</h1>
        <p className="muted center">Le compte est créé avec le role <b>EMPLOYEE</b></p>
        {error && <div className="alert alert-error">{error}</div>}
        <label>Nom complet
          <input name="fullName" value={form.fullName} onChange={onChange} placeholder="Ahmed Benali" />
          {errors.fullName && <span className="field-error">{errors.fullName}</span>}
        </label>
        <label>Email
          <input name="email" type="email" value={form.email} onChange={onChange} placeholder="ahmed.benali@entreprise.ma" />
          {errors.email && <span className="field-error">{errors.email}</span>}
        </label>
        <label>Mot de passe (8 caractères minimum)
          <input name="password" type="password" value={form.password} onChange={onChange} />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </label>
        <label>Confirmation du mot de passe
          <input name="confirm" type="password" value={form.confirm} onChange={onChange} />
          {errors.confirm && <span className="field-error">{errors.confirm}</span>}
        </label>
        <button className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Création...' : "S'inscrire"}</button>
        <p className="center small">Déjà inscrit ? <Link to="/login">Se connecter</Link></p>
      </form>
    </div>
  )
}
