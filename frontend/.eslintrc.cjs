/**
 * Configuração do ESLint para TypeScript e React.
 * 
 * <h3>Por que ESLint?</h3>
 * <ul>
 *   <li><strong>Qualidade de Código:</strong> Detecta erros e padrões problemáticos</li>
 *   <li><strong>Consistência:</strong> Garante estilo de código uniforme no time</li>
 *   <li><strong>Best Practices:</strong> Aplica regras recomendadas do React e TypeScript</li>
 * </ul>
 */
module.exports = {
  root: true,
  env: { browser: true, es2020: true },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
  ],
  ignorePatterns: ['dist', '.eslintrc.cjs'],
  parser: '@typescript-eslint/parser',
  plugins: ['react-refresh'],
  rules: {
    'react-refresh/only-export-components': [
      'warn',
      { allowConstantExport: true },
    ],
    '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
  },
}

