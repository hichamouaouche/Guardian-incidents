import { createContext, useContext, useState, useCallback } from 'react'
import * as authService from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(authService.getUser())

  const login = useCallback(async (email, password) => {
    const data = await authService.login(email, password)
    setUser(authService.getUser())
    return data
  }, [])

  const logout = useCallback(async () => {
    await authService.logout()
    setUser(null)
  }, [])

  const refreshUser = useCallback(() => setUser(authService.getUser()), [])

  return (
    <AuthContext.Provider value={{ user, login, logout, refreshUser, isAuthenticated: !!user && authService.isAuthenticated() }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
