/// <reference types="vite/client" />

/**
 * Definições de tipos para variáveis de ambiente do Vite.
 * 
 * <h3>Variáveis de Ambiente:</h3>
 * <p>Variáveis que começam com VITE_ são expostas ao código do cliente.
 * Use estas para configurações públicas (URLs de API, etc.).</p>
 * 
 * <h3>Segurança:</h3>
 * <p>NÃO coloque secrets aqui (tokens, chaves API). Secrets devem ficar
 * apenas no backend ou em variáveis de ambiente do servidor.</p>
 */
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_ENV: 'development' | 'staging' | 'production'
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

