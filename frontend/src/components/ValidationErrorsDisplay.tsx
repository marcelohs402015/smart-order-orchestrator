interface ValidationErrorsDisplayProps {
  errors: Record<string, string>;
  className?: string;
}

const fieldNameMap: Record<string, string> = {
  customerId: 'ID do Cliente',
  customerName: 'Nome do Cliente',
  customerEmail: 'Email do Cliente',
  items: 'Itens do Pedido',
  paymentMethod: 'Método de Pagamento',
  currency: 'Moeda',
  idempotencyKey: 'Chave de Idempotência',
};

const itemFieldMap: Record<string, string> = {
  productId: 'ID do Produto',
  productName: 'Nome do Produto',
  quantity: 'Quantidade',
  unitPrice: 'Preço Unitário',
};

const formatFieldName = (field: string): string => {
  if (field.startsWith('items[')) {
    const match = field.match(/items\[(\d+)\]\.(.+)/);
    if (match) {
      const index = parseInt(match[1]) + 1;
      const itemField = match[2];
      return `Item ${index} - ${itemFieldMap[itemField] || itemField}`;
    }
  }
  return fieldNameMap[field] || field;
};

export const ValidationErrorsDisplay = ({
  errors,
  className,
}: ValidationErrorsDisplayProps) => {
  if (!errors || Object.keys(errors).length === 0) {
    return null;
  }

  return (
    <div className={`mt-3 p-3 bg-red-50 border border-red-200 rounded text-sm ${className || ''}`}>
      <p className="font-medium mb-2">Erros de validação do backend:</p>
      <ul className="list-disc list-inside space-y-1">
        {Object.entries(errors).map(([field, message]) => (
          <li key={field} className="text-red-800">
            <strong>{formatFieldName(field)}:</strong> {String(message)}
          </li>
        ))}
      </ul>
    </div>
  );
};

