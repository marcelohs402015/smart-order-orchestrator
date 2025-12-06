/**
 * Componente principal da aplicação.
 * 
 * <h3>Arquitetura Frontend:</h3>
 * <ul>
 *   <li><strong>React Router:</strong> Roteamento client-side (SPA)</li>
 *   <li><strong>Zustand:</strong> State management global (leve e performático)</li>
 *   <li><strong>Axios:</strong> Cliente HTTP para comunicação com API REST</li>
 *   <li><strong>React Hook Form + Zod:</strong> Validação de formulários</li>
 * </ul>
 * 
 * <h3>Estrutura de Pastas:</h3>
 * <pre>
 * src/
 *   ├── components/     # Componentes reutilizáveis
 *   ├── pages/          # Páginas/rotas da aplicação
 *   ├── hooks/           # Custom hooks
 *   ├── store/           # Zustand stores (state management)
 *   ├── services/        # Serviços de API (Axios)
 *   ├── types/           # TypeScript types/interfaces
 *   ├── utils/           # Funções utilitárias
 *   └── lib/             # Configurações de bibliotecas
 * </pre>
 */
function App() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="container mx-auto px-4 py-8">
        <header className="mb-8">
          <h1 className="text-4xl font-bold text-gray-900">
            Smart Order Orchestrator
          </h1>
          <p className="mt-2 text-lg text-gray-600">
            Sistema de orquestração de pedidos resiliente com IA
          </p>
        </header>
        
        <main className="bg-white rounded-lg shadow p-6">
          <div className="text-center py-12">
            <h2 className="text-2xl font-semibold text-gray-800 mb-4">
              Frontend Configurado com Sucesso! 🚀
            </h2>
            <p className="text-gray-600 mb-6">
              React + Vite + TypeScript + TailwindCSS
            </p>
            <div className="flex flex-wrap justify-center gap-4 text-sm text-gray-500">
              <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full">
                React 18
              </span>
              <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full">
                Vite
              </span>
              <span className="px-3 py-1 bg-purple-100 text-purple-800 rounded-full">
                TypeScript
              </span>
              <span className="px-3 py-1 bg-cyan-100 text-cyan-800 rounded-full">
                TailwindCSS
              </span>
              <span className="px-3 py-1 bg-orange-100 text-orange-800 rounded-full">
                Zustand
              </span>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}

export default App

