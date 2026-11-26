const apiUrl = import.meta.env.VITE_API_URL

if (!apiUrl) {
  throw new Error('Falta VITE_API_URL en el archivo .env')
}

export const API_URL = apiUrl

// Clave de CARTO para el mapa base Voyager (opcional). Sin clave se usa OpenStreetMap estándar.
// Se pide gratis en https://carto.com/basemaps/apikey
export const MAPA_KEY = import.meta.env.VITE_MAPA_KEY?.trim() || null
