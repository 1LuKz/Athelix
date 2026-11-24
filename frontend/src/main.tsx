import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './app/App'
import '@fontsource/material-symbols-rounded/400.css'
import './styles/tokens.css'
import './styles/global.css'
import './styles/compact.css'
import './styles/experience.css'
import './styles/dashboard.css'
import './styles/fixed-layout.css'
import './styles/auth.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
