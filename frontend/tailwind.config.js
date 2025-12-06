/** @type {import('tailwindcss').Config} */
/**
 * Configuração do TailwindCSS para o projeto.
 * 
 * <h3>Por que TailwindCSS?</h3>
 * <ul>
 *   <li><strong>Utility-First:</strong> Estilização rápida sem escrever CSS customizado</li>
 *   <li><strong>Consistência:</strong> Design system baseado em tokens (cores, espaçamentos)</li>
 *   <li><strong>Performance:</strong> PurgeCSS remove classes não utilizadas no build</li>
 *   <li><strong>Produtividade:</strong> Desenvolvimento mais rápido sem alternar entre arquivos</li>
 * </ul>
 * 
 * <h3>Configurações:</h3>
 * <ul>
 *   <li><strong>Content:</strong> Define onde o Tailwind deve procurar classes (purge automático)</li>
 *   <li><strong>Theme:</strong> Customização de cores, fontes, breakpoints (opcional)</li>
 *   <li><strong>Plugins:</strong> Extensões como forms, typography (opcional)</li>
 * </ul>
 */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      // Cores customizadas do projeto (opcional)
      colors: {
        primary: {
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#0369a1',
          800: '#075985',
          900: '#0c4a6e',
        },
      },
    },
  },
  plugins: [],
}

