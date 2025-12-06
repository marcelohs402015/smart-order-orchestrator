import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'

/**
 * Ponto de entrada da aplicação React.
 * 
 * <h3>Estrutura:</h3>
 * <ul>
 *   <li><strong>React 18+:</strong> Usa createRoot (nova API) para melhor performance</li>
 *   <li><strong>StrictMode:</strong> Habilita verificações extras em desenvolvimento</li>
 *   <li><strong>CSS Global:</strong> Importa estilos globais (TailwindCSS)</li>
 * </ul>
 */
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)

