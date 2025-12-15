import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
/**
 * Configuração do Vite para o projeto Smart Order Orchestrator Frontend.
 * 
 * <h3>Por que Vite?</h3>
 * <ul>
 *   <li><strong>Performance:</strong> Dev server extremamente rápido usando ESM nativo</li>
 *   <li><strong>HMR (Hot Module Replacement):</strong> Atualizações instantâneas durante desenvolvimento</li>
 *   <li><strong>Build otimizado:</strong> Usa Rollup para produção, gerando bundles otimizados</li>
 *   <li><strong>TypeScript nativo:</strong> Suporte completo sem configuração adicional</li>
 * </ul>
 * 
 * <h3>Configurações:</h3>
 * <ul>
 *   <li><strong>React Plugin:</strong> Habilita suporte completo ao React</li>
 *   <li><strong>Path Aliases:</strong> Permite imports absolutos (ex: @/components)</li>
 *   <li><strong>Port 5173:</strong> Porta padrão do Vite (pode ser alterada via .env)</li>
 * </ul>
 */
export default defineConfig({
  plugins: [react()],
  
  // Resolução de paths - permite imports absolutos
  // Exemplo: import Button from '@/components/Button' ao invés de '../../../components/Button'
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  
  // Configuração do servidor de desenvolvimento
  server: {
    port: 5173,
    proxy: {
      // Proxy para API do backend Spring Boot
      // Redireciona /api/* para http://localhost:8081/api/*
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        // Não remove /api do path, mantém completo: /api/v1/orders -> http://localhost:8080/api/v1/orders
      },
    },
  },
  
  // Configuração de build para produção
  build: {
    outDir: 'dist',
    sourcemap: true, // Gera source maps para debugging em produção
    rollupOptions: {
      output: {
        // Code splitting: separa vendor (node_modules) do código da aplicação
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          store: ['zustand'],
          forms: ['react-hook-form', 'zod'],
        },
      },
    },
  },
})

