import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createIncident, CATEGORIES, PRIORITIES } from '../services/incidentService'
import { errorMessage, fieldErrors } from '../services/api'

export default function NewIncident() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', description: '', category: 'MALWARE', priority: '' })
  const [errors, setErrors] = useState({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setErrors({}); setLoading(true)
    try {
      const payload = { ...form, priority: form.priority || null }
      const created = await createIncident(payload)
      navigate(`/incidents/${created.id}`)
    } catch (err) {
      setError(errorMessage(err)); setErrors(fieldErrors(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>Déclarer un incident</h1>
      <form className="card form-card" onSubmit={handleSubmit} noValidate>
        {error && <div className="alert alert-error">{error}</div>}
        <label>Titre *
          <input name="title" value={form.title} onChange={onChange} placeholder="Ex : Poste infecté par un malware" />
          {errors.title && <span className="field-error">{errors.title}</span>}
        </label>
        <label>Description *
          <textarea name="description" rows="5" value={form.description} onChange={onChange}
            placeholder="Décrivez le problème, le poste concerné, depuis quand..." />
          {errors.description && <span className="field-error">{errors.description}</span>}
        </label>
        <div className="grid-2">
          <label>Catégorie *
            <select name="category" value={form.category} onChange={onChange}>
              {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
            {errors.category && <span className="field-error">{errors.category}</span>}
          </label>
          <label>Priorité *
            <select name="priority" value={form.priority} onChange={onChange}>
              <option value="">-- Choisir --</option>
              {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
            {errors.priority && <span className="field-error">{errors.priority}</span>}
          </label>
        </div>
        <button className="btn btn-primary" disabled={loading}>{loading ? 'Envoi...' : 'Déclarer'}</button>
      </form>
    </div>
  )
}
