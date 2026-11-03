import { Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import RoleRoute from './components/RoleRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Profile from './pages/Profile'
import MyIncidents from './pages/MyIncidents'
import NewIncident from './pages/NewIncident'
import IncidentDetails from './pages/IncidentDetails'
import AssignedIncidents from './pages/AssignedIncidents'
import AdminUsers from './pages/AdminUsers'
import AdminIncidents from './pages/AdminIncidents'
import Forbidden from './pages/Forbidden'
import NotFound from './pages/NotFound'

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          {/* ---------- Routes publiques ---------- */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* ---------- Routes protegees (utilisateur connecte) ---------- */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/forbidden" element={<Forbidden />} />

            <Route element={<RoleRoute roles={['EMPLOYEE']} />}>
              <Route path="/incidents/my" element={<MyIncidents />} />
              <Route path="/incidents/new" element={<NewIncident />} />
            </Route>

            <Route element={<RoleRoute roles={['TECHNICIAN']} />}>
              <Route path="/incidents/assigned" element={<AssignedIncidents />} />
            </Route>

            {/* le controle proprietaire / affectation est fait par le backend */}
            <Route path="/incidents/:id" element={<IncidentDetails />} />

            <Route element={<RoleRoute roles={['ADMIN']} />}>
              <Route path="/admin/users" element={<AdminUsers />} />
              <Route path="/admin/incidents" element={<AdminIncidents />} />
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
    </>
  )
}
