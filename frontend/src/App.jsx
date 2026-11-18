import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Home from './pages/Home'
import PropiedadDetalle from './pages/PropiedadDetalle'
import Login from './pages/Login'
import Register from './pages/Register'
import Perfil from './pages/Perfil'
import PublicarPropiedad from './pages/PublicarPropiedad'
import NotFound from './pages/NotFound'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        {/* Públicas */}
        <Route path="/" element={<Home />} />
        <Route path="/propiedades/:id" element={<PropiedadDetalle />} />
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Register />} />

        {/* Protegidas */}
        <Route element={<ProtectedRoute />}>
          <Route path="/perfil" element={<Perfil />} />
          <Route path="/publicar" element={<PublicarPropiedad />} />
        </Route>

        {/* 404 */}
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}