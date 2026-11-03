const LABELS = {
  OPEN: 'Ouvert', ASSIGNED: 'Affecté', IN_PROGRESS: 'En cours', RESOLVED: 'Résolu', CLOSED: 'Clôturé',
  LOW: 'Basse', MEDIUM: 'Moyenne', HIGH: 'Haute', CRITICAL: 'Critique',
}

export function StatusBadge({ value }) {
  return <span className={`badge st-${value}`}>{LABELS[value] || value}</span>
}

export function PriorityBadge({ value }) {
  return <span className={`badge pr-${value}`}>{LABELS[value] || value}</span>
}

export const formatDate = (iso) =>
  iso ? new Date(iso).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' }) : '-'
