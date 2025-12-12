/**
 * Página para criar um novo pedido.
 * 
 * <h3>Funcionalidades:</h3>
 * <ul>
 *   <li>Formulário para criar pedido</li>
 *   <li>Adicionar/remover itens dinamicamente</li>
 *   <li>Validação de formulário</li>
 *   <li>Integração com store Zustand</li>
 * </ul>
 */

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useOrderStore } from '../store/orderStore';
import { CreateOrderRequest } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import { Alert } from '../components/ui/Alert';
import { generateId, isValidUUID } from '../utils';

// Schema de validação com Zod
const orderItemSchema = z.object({
  productId: z
    .string()
    .min(1, 'ID do produto é obrigatório')
    .refine((val) => isValidUUID(val), {
      message: 'ID do produto deve ser um UUID válido',
    }),
  productName: z.string().min(1, 'Nome do produto é obrigatório'),
  quantity: z.number().min(1, 'Quantidade deve ser pelo menos 1'),
  unitPrice: z.number().min(0.01, 'Preço deve ser maior que zero'),
});

const createOrderSchema = z.object({
  customerId: z
    .string()
    .min(1, 'ID do cliente é obrigatório')
    .refine((val) => isValidUUID(val), {
      message: 'ID do cliente deve ser um UUID válido',
    }),
  customerName: z.string().min(1, 'Nome do cliente é obrigatório'),
  customerEmail: z.string().email('Email inválido'),
  items: z.array(orderItemSchema).min(1, 'Adicione pelo menos um item'),
  paymentMethod: z.string().min(1, 'Método de pagamento é obrigatório'),
  currency: z.string().optional(),
  idempotencyKey: z
    .string()
    .optional()
    .refine((val) => !val || isValidUUID(val), {
      message: 'Chave de idempotência deve ser um UUID válido',
    }),
});

type CreateOrderFormData = z.infer<typeof createOrderSchema>;

export const CreateOrderPage = () => {
  const navigate = useNavigate();
  const { createOrder, loading, error, validationErrors, clearError, clearValidationErrors } = useOrderStore();
  const [showSuccess, setShowSuccess] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
    reset,
    setError,
  } = useForm<CreateOrderFormData>({
    resolver: zodResolver(createOrderSchema),
    defaultValues: {
      customerId: generateId(),
      customerName: '',
      customerEmail: '',
      items: [{ productId: generateId(), productName: '', quantity: 1, unitPrice: 0 }],
      paymentMethod: 'credit_card',
      currency: 'BRL',
      idempotencyKey: generateId(), // Gerar automaticamente
    },
  });

  // Sincronizar erros de validação do backend com react-hook-form
  useEffect(() => {
    if (validationErrors) {
      Object.entries(validationErrors).forEach(([field, message]) => {
        // Mapear campos do backend para campos do formulário
        const formField = field as keyof CreateOrderFormData;
        setError(formField, {
          type: 'server',
          message: message,
        });
      });
    }
  }, [validationErrors, setError]);

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const onSubmit = async (data: CreateOrderFormData) => {
    clearError();
    clearValidationErrors();
    setShowSuccess(false);
    setIsProcessing(false);

    try {
      // Garantir que todos os campos obrigatórios estão presentes e válidos
      // Filtrar campos null/undefined para evitar problemas de deserialização
      
      // Validar UUID do cliente
      if (!isValidUUID(data.customerId)) {
        setError('customerId', {
          type: 'manual',
          message: 'ID do cliente deve ser um UUID válido',
        });
        return;
      }
      
      // Validar UUIDs dos produtos
      const invalidProductIds = data.items
        .map((item, index) => ({ item, index }))
        .filter(({ item }) => item.productId && !isValidUUID(item.productId));
      
      if (invalidProductIds.length > 0) {
        invalidProductIds.forEach(({ index }) => {
          setError(`items.${index}.productId`, {
            type: 'manual',
            message: 'ID do produto deve ser um UUID válido',
          });
        });
        return;
      }
      
      const request: CreateOrderRequest = {
        customerId: data.customerId.trim(), // UUID como string
        customerName: data.customerName.trim(),
        customerEmail: data.customerEmail.trim(),
        items: data.items
          .filter((item) => item.productName?.trim() && item.quantity > 0 && item.unitPrice > 0)
          .map((item) => ({
            productId: (item.productId || generateId()).trim(), // UUID como string
            productName: item.productName.trim(),
            quantity: Number(item.quantity), // Garantir que é número inteiro
            unitPrice: Number(item.unitPrice), // BigDecimal no backend será deserializado de number
          })),
        paymentMethod: data.paymentMethod.trim(),
        ...(data.currency && data.currency.trim() ? { currency: data.currency.trim() } : {}),
        // idempotencyKey será enviado se fornecido, senão backend gera automaticamente
        ...(data.idempotencyKey && data.idempotencyKey.trim() 
          ? { idempotencyKey: data.idempotencyKey.trim() } 
          : {}),
      };

      // Validar que há pelo menos um item válido
      if (request.items.length === 0) {
        setError('items', {
          type: 'manual',
          message: 'Adicione pelo menos um item válido',
        });
        return;
      }

      // Log para debug (remover em produção)
      console.log('📤 Enviando requisição:', {
        url: '/api/v1/orders',
        method: 'POST',
        payload: JSON.stringify(request, null, 2),
        customerId: request.customerId,
        itemsCount: request.items.length,
        items: request.items.map(item => ({
          productId: item.productId,
          productName: item.productName,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          unitPriceType: typeof item.unitPrice,
        })),
      });

      const response = await createOrder(request);

      if (response.success && response.order) {
        // Sucesso completo (201) - Pedido criado e saga executada com sucesso
        setShowSuccess(true);
        reset();
        setTimeout(() => {
          navigate('/orders');
        }, 2000);
      } else if (response.sagaExecutionId && !response.success) {
        // Saga falhou ou está em progresso
        if (response.errorMessage) {
          // Saga falhou (ex: pagamento falhou, análise de risco falhou)
          // O erro já está no store, mas vamos melhorar a mensagem
          console.warn('⚠️ Saga falhou:', {
            sagaExecutionId: response.sagaExecutionId,
            errorMessage: response.errorMessage,
          });
          // Não redirecionar, mostrar erro na UI
        } else {
          // Saga em progresso (202) - não é erro, mas estado intermediário
          setIsProcessing(true);
          // Não redirecionar, aguardar confirmação do usuário
        }
      }
    } catch (err) {
      // Erro já está no store (validação ou outro erro)
      console.error('❌ Erro ao criar pedido:', err);
      
      // Log detalhado para debug
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as any;
        console.error('📋 Detalhes do erro do backend:', {
          status: axiosError.response?.status,
          statusText: axiosError.response?.statusText,
          error: axiosError.response?.data?.error,
          message: axiosError.response?.data?.message,
          details: axiosError.response?.data?.details,
          requestPayload: axiosError.config?.data ? JSON.parse(axiosError.config.data) : null,
        });
        
        // Se houver details, logar cada campo com erro
        if (axiosError.response?.data?.details) {
          console.error('🔍 Erros de validação por campo:');
          Object.entries(axiosError.response.data.details).forEach(([field, message]) => {
            console.error(`  - ${field}: ${message}`);
          });
        }
      }
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Criar Novo Pedido</h1>
        <p className="mt-2 text-gray-600">
          Preencha os dados abaixo para criar um novo pedido
        </p>
      </div>

      {error && error.status === 202 && (
        <Alert variant="info" onClose={clearError} className="mb-6">
          <div>
            <p className="font-semibold mb-1">Pedido em processamento</p>
            <p>{error.message}</p>
            {error.status && (
              <p className="text-sm mt-2 opacity-75">
                O pedido está sendo processado. Você pode acompanhar o status na lista de pedidos.
              </p>
            )}
          </div>
        </Alert>
      )}

      {error && error.status !== 202 && (
        <Alert variant="error" onClose={clearError} className="mb-6">
          <div>
            <p className="font-semibold mb-1">
              {error.isBusinessError ? 'Erro ao processar pedido' : 'Erro ao criar pedido'}
            </p>
            <p>{error.message}</p>
            {error.isBusinessError && (
              <div className="mt-3 p-3 bg-orange-50 border border-orange-200 rounded text-sm">
                <p className="font-medium mb-1">ℹ️ Erro de negócio (não é erro de validação)</p>
                <p className="text-gray-700 mb-2">
                  O pedido foi recebido, mas a saga falhou durante a execução. 
                  Possíveis causas: pagamento recusado, análise de risco falhou, ou outro erro no processamento.
                </p>
                <p className="text-gray-700">
                  💡 <strong>Dica:</strong> Pedidos com falha de pagamento podem ser visualizados na{' '}
                  <button
                    type="button"
                    onClick={() => navigate('/orders')}
                    className="text-blue-600 hover:text-blue-800 underline font-medium"
                  >
                    lista de pedidos
                  </button>
                  {' '}filtrados por status "Falha de Pagamento".
                </p>
              </div>
            )}
            {(validationErrors && Object.keys(validationErrors).length > 0) || (error.details && Object.keys(error.details).length > 0 && !error.isBusinessError) ? (
              <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded text-sm">
                <p className="font-medium mb-2">Erros de validação do backend:</p>
                <ul className="list-disc list-inside space-y-1">
                  {Object.entries(validationErrors || error.details || {}).map(([field, message]) => {
                    // Mapear nomes de campos do backend para nomes mais legíveis
                    const fieldNameMap: Record<string, string> = {
                      'customerId': 'ID do Cliente',
                      'customerName': 'Nome do Cliente',
                      'customerEmail': 'Email do Cliente',
                      'items': 'Itens do Pedido',
                      'paymentMethod': 'Método de Pagamento',
                      'currency': 'Moeda',
                      'idempotencyKey': 'Chave de Idempotência',
                    };
                    
                    // Se for um campo de item (ex: items[0].productId)
                    let displayField = field;
                    if (field.startsWith('items[')) {
                      const match = field.match(/items\[(\d+)\]\.(.+)/);
                      if (match) {
                        const index = parseInt(match[1]) + 1; // +1 para exibir como "Item 1" ao invés de "Item 0"
                        const itemField = match[2];
                        const itemFieldMap: Record<string, string> = {
                          'productId': 'ID do Produto',
                          'productName': 'Nome do Produto',
                          'quantity': 'Quantidade',
                          'unitPrice': 'Preço Unitário',
                        };
                        displayField = `Item ${index} - ${itemFieldMap[itemField] || itemField}`;
                      }
                    } else {
                      displayField = fieldNameMap[field] || field;
                    }
                    
                    return (
                      <li key={field} className="text-red-800">
                        <strong>{displayField}:</strong> {message}
                      </li>
                    );
                  })}
                </ul>
              </div>
            ) : null}
            {error.status === 400 && !validationErrors && !error.details && (
              <div className="mt-2 text-sm text-gray-600">
                <p>💡 Dica: Verifique o console do navegador (F12) para ver os detalhes completos do erro.</p>
              </div>
            )}
          </div>
        </Alert>
      )}

      {isProcessing && (
        <Alert variant="info" className="mb-6">
          <div>
            <p className="font-semibold mb-1">Pedido em processamento</p>
            <p>Seu pedido está sendo processado. Aguarde a conclusão da saga.</p>
            <Button
              type="button"
              variant="outline"
              size="sm"
              className="mt-3"
              onClick={() => navigate('/orders')}
            >
              Ver Pedidos
            </Button>
          </div>
        </Alert>
      )}

      {showSuccess && (
        <Alert variant="success" className="mb-6">
          Pedido criado com sucesso! Redirecionando...
        </Alert>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <Card className="mb-6">
          <h2 className="text-xl font-semibold mb-4">Dados do Cliente</h2>
          <div className="space-y-4">
            <Input
              label="Nome do Cliente"
              {...register('customerName')}
              error={errors.customerName?.message}
              required
            />
            <Input
              label="Email do Cliente"
              type="email"
              {...register('customerEmail')}
              error={errors.customerEmail?.message}
              required
            />
            <Input
              label="Método de Pagamento"
              {...register('paymentMethod')}
              error={errors.paymentMethod?.message}
              placeholder="credit_card, debit_card, pix, etc"
              required
            />
            <Input
              label="Chave de Idempotência (opcional)"
              {...register('idempotencyKey')}
              error={errors.idempotencyKey?.message}
              placeholder="Deixe em branco para gerar automaticamente"
              helperText="Usado para prevenir duplicação de pedidos. Se não fornecido, será gerado automaticamente."
            />
          </div>
        </Card>

        <Card className="mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold">Itens do Pedido</h2>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() =>
                append({
                  productId: generateId(),
                  productName: '',
                  quantity: 1,
                  unitPrice: 0,
                })
              }
            >
              + Adicionar Item
            </Button>
          </div>

          <div className="space-y-4">
            {fields.map((field, index) => (
              <Card key={field.id} padding="sm" className="bg-gray-50">
                <div className="flex items-start justify-between mb-4">
                  <h3 className="font-medium text-gray-700">Item {index + 1}</h3>
                  {fields.length > 1 && (
                    <Button
                      type="button"
                      variant="danger"
                      size="sm"
                      onClick={() => remove(index)}
                    >
                      Remover
                    </Button>
                  )}
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Input
                    label="Nome do Produto"
                    {...register(`items.${index}.productName`)}
                    error={errors.items?.[index]?.productName?.message}
                    required
                  />
                  <Input
                    label="Quantidade"
                    type="number"
                    {...register(`items.${index}.quantity`, { valueAsNumber: true })}
                    error={errors.items?.[index]?.quantity?.message}
                    required
                    min={1}
                  />
                  <Input
                    label="Preço Unitário"
                    type="number"
                    step="0.01"
                    {...register(`items.${index}.unitPrice`, { valueAsNumber: true })}
                    error={errors.items?.[index]?.unitPrice?.message}
                    required
                    min={0.01}
                  />
                </div>
              </Card>
            ))}
          </div>

          {errors.items && (
            <p className="mt-2 text-sm text-red-600">{errors.items.message}</p>
          )}
        </Card>

        <div className="flex gap-4">
          <Button
            type="submit"
            isLoading={loading === 'loading'}
            disabled={loading === 'loading'}
          >
            Criar Pedido
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate('/orders')}
          >
            Cancelar
          </Button>
        </div>
      </form>
    </div>
  );
};

