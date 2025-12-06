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

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useOrderStore } from '../store/orderStore';
import { CreateOrderRequest, OrderItemRequest } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import { Alert } from '../components/ui/Alert';
import { generateId, isValidEmail } from '../utils';

// Schema de validação com Zod
const orderItemSchema = z.object({
  productId: z.string().min(1, 'ID do produto é obrigatório'),
  productName: z.string().min(1, 'Nome do produto é obrigatório'),
  quantity: z.number().min(1, 'Quantidade deve ser pelo menos 1'),
  unitPrice: z.number().min(0.01, 'Preço deve ser maior que zero'),
});

const createOrderSchema = z.object({
  customerId: z.string().min(1, 'ID do cliente é obrigatório'),
  customerName: z.string().min(1, 'Nome do cliente é obrigatório'),
  customerEmail: z.string().email('Email inválido'),
  items: z.array(orderItemSchema).min(1, 'Adicione pelo menos um item'),
  paymentMethod: z.string().min(1, 'Método de pagamento é obrigatório'),
  currency: z.string().optional(),
});

type CreateOrderFormData = z.infer<typeof createOrderSchema>;

export const CreateOrderPage = () => {
  const navigate = useNavigate();
  const { createOrder, loading, error, clearError } = useOrderStore();
  const [showSuccess, setShowSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
    reset,
  } = useForm<CreateOrderFormData>({
    resolver: zodResolver(createOrderSchema),
    defaultValues: {
      customerId: generateId(),
      customerName: '',
      customerEmail: '',
      items: [{ productId: generateId(), productName: '', quantity: 1, unitPrice: 0 }],
      paymentMethod: 'credit_card',
      currency: 'BRL',
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const onSubmit = async (data: CreateOrderFormData) => {
    clearError();
    setShowSuccess(false);

    try {
      const request: CreateOrderRequest = {
        ...data,
        items: data.items.map((item) => ({
          ...item,
          productId: item.productId || generateId(),
        })),
      };

      const response = await createOrder(request);

      if (response.success) {
        setShowSuccess(true);
        reset();
        setTimeout(() => {
          navigate('/orders');
        }, 2000);
      }
    } catch (err) {
      // Erro já está no store
      console.error('Erro ao criar pedido:', err);
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

      {error && (
        <Alert variant="error" onClose={clearError} className="mb-6">
          {error.message}
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

