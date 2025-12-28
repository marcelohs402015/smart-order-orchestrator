import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useOrderStore } from '../store/orderStore';
import { CreateOrderRequest, ApiError } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import { Alert } from '../components/ui/Alert';
import { ValidationErrorsDisplay } from '../components/ValidationErrorsDisplay';
import { generateId, isValidUUID } from '../utils';
import { logger } from '../utils/logger';
import { useErrorHandler } from '../hooks/useErrorHandler';

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
  const { handleError, handleSuccess } = useErrorHandler();
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
      idempotencyKey: generateId(),
    },
  });

  useEffect(() => {
    if (validationErrors) {
      Object.entries(validationErrors).forEach(([field, message]) => {
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
      const request: CreateOrderRequest = {
        customerId: data.customerId.trim(),
        customerName: data.customerName.trim(),
        customerEmail: data.customerEmail.trim(),
        items: data.items
          .filter((item) => item.productName?.trim() && item.quantity > 0 && item.unitPrice > 0)
          .map((item) => ({
            productId: (item.productId || generateId()).trim(),
            productName: item.productName.trim(),
            quantity: Number(item.quantity),
            unitPrice: Number(item.unitPrice),
          })),
        paymentMethod: data.paymentMethod.trim(),
        ...(data.currency && data.currency.trim() ? { currency: data.currency.trim() } : {}),
        ...(data.idempotencyKey && data.idempotencyKey.trim() 
          ? { idempotencyKey: data.idempotencyKey.trim() } 
          : {}),
      };

      if (request.items.length === 0) {
        setError('items', {
          type: 'manual',
          message: 'Adicione pelo menos um item válido',
        });
        return;
      }

      logger.log('Enviando requisição de criação de pedido', {
        url: '/api/v1/orders',
        method: 'POST',
        customerId: request.customerId,
        itemsCount: request.items.length,
      });

      const response = await createOrder(request);

      if (response.success && response.order) {
        setShowSuccess(true);
        handleSuccess('Pedido criado com sucesso!');
        reset();
        setTimeout(() => {
          navigate('/orders');
        }, 2000);
      } else if (response.sagaExecutionId && !response.success) {
        if (response.errorMessage) {
          logger.warn('Saga falhou', {
            sagaExecutionId: response.sagaExecutionId,
            errorMessage: response.errorMessage,
          });
        } else {
          setIsProcessing(true);
        }
      }
    } catch (err) {
      const apiError = err as ApiError;
      handleError(apiError, {
        showNotification: true,
        notificationTitle: 'Erro ao criar pedido',
      });
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
            <ValidationErrorsDisplay
              errors={(validationErrors || error.details || {}) as Record<string, string>}
            />
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
