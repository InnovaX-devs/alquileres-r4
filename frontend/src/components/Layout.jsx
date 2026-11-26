import { Outlet } from 'react-router-dom'
import Navbar from './Navbar'
import Footer from './Footer'
import SesionExpirada from './SesionExpirada'

export default function Layout() {
  return (
    // overflow-x-clip: nada provoca scroll horizontal en celulares
    <div className="flex min-h-screen flex-col overflow-x-clip">
      <SesionExpirada />
      <Navbar />
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-8">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}