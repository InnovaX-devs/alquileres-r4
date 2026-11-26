import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Home from './pages/Home'
import PropiedadDetalle from './pages/PropiedadDetalle'
import Login from './pages/Login'
import Register from './pages/Register'
import Perfil from './pages/Perfil'
import PublicarPropiedad from './pages/PublicarPropiedad'
import MisPropiedades from './pages/MisPropiedades'
import EditarPropiedad from './pages/EditarPropiedad'
import Favoritos from './pages/Favoritos'
import MisVisitas from './pages/MisVisitas'
import VisitasRecibidas from './pages/VisitasRecibidas'
import CrearAlerta from './pages/CrearAlerta'
import MisAlertas from './pages/MisAlertas'
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
          <Route path="/mis-propiedades" element={<MisPropiedades />} />
          <Route path="/propiedades/:id/editar" element={<EditarPropiedad />} />
          <Route path="/favoritos" element={<Favoritos />} />
          <Route path="/visitas" element={<MisVisitas />} />
          <Route path="/visitas/recibidas" element={<VisitasRecibidas />} />
          <Route path="/alertas" element={<MisAlertas />} />
          <Route path="/alertas/nueva" element={<CrearAlerta />} />
        </Route>

        {/* 404 */}
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}