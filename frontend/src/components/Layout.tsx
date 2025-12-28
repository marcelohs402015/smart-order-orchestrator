import { ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { cn } from '../utils';

interface LayoutProps {
  children: ReactNode;
}

const navigation = [
  { name: 'Dashboard', path: '/' },
  { name: 'Pedidos', path: '/orders' },
  { name: 'Criar Pedido', path: '/orders/create' },
];

export const Layout = ({ children }: LayoutProps) => {
  const location = useLocation();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center">
              <Link to="/" className="flex items-center">
                <h1 className="text-xl font-bold text-gray-900">
                  Smart Order Orchestrator
                </h1>
              </Link>
            </div>
            <nav className="flex space-x-4">
              {navigation.map((item) => {
                const isActive = location.pathname === item.path;
                return (
                  <Link
                    key={item.path}
                    to={item.path}
                    className={cn(
                      'px-3 py-2 rounded-md text-sm font-medium transition-colors',
                      isActive
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                    )}
                  >
                    {item.name}
                  </Link>
                );
              })}
            </nav>
          </div>
        </div>
      </header>

      <main>{children}</main>

      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <p className="text-center text-sm text-gray-500">
            Smart Order Orchestrator - Sistema de Orquestração de Pedidos Resiliente
          </p>
        </div>
      </footer>
    </div>
  );
};
