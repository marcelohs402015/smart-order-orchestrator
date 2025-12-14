package com.marcelo.orchestrator.presentation.controller;

import com.marcelo.orchestrator.application.saga.OrderSagaCommand;
import com.marcelo.orchestrator.application.saga.OrderSagaOrchestrator;
import com.marcelo.orchestrator.application.saga.OrderSagaResult;
import com.marcelo.orchestrator.domain.port.OrderRepositoryPort;
import com.marcelo.orchestrator.presentation.dto.CreateOrderRequest;
import com.marcelo.orchestrator.presentation.dto.CreateOrderResponse;
import com.marcelo.orchestrator.presentation.dto.OrderResponse;
import com.marcelo.orchestrator.presentation.mapper.OrderPresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para operações de pedidos.
 * 
 * <p>Expõe endpoints HTTP para criação e consulta de pedidos.
 * Utiliza Saga Pattern para orquestrar o fluxo completo de criação.</p>
 * 
 * <h3>Versionamento:</h3>
 * <p>API versionada com prefixo `/api/v1` para permitir evolução
 * sem quebrar clientes existentes.</p>
 * 
 * <h3>Validação:</h3>
 * <p>Utiliza Bean Validation (@Valid) para validar dados de entrada
 * antes de processar.</p>
 * 
 * <h3>Documentação:</h3>
 * <p>Swagger/OpenAPI configurado para documentação automática da API.</p>
 * 
 * @author Marcelo
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API para gerenciamento de pedidos")
public class OrderController {
    
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final OrderRepositoryPort orderRepository;
    private final OrderPresentationMapper orderMapper;
    
    /**
     * Cria um novo pedido executando a saga completa.
     * 
     * <p>Orquestra os 3 passos da saga:
     * 1. Criar pedido
     * 2. Processar pagamento
     * 3. Analisar risco</p>
     * 
     * <p>Retorna resultado com pedido criado e ID da execução da saga
     * para rastreamento e observabilidade.</p>
     */
    @PostMapping
    @Operation(
        summary = "Criar novo pedido",
        description = "Cria um novo pedido executando a saga completa (criar pedido → processar pagamento → analisar risco)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.customerId());
        
        // Gerar idempotencyKey se não fornecido
        // Padrão: Idempotência - garante que requisições duplicadas não criem pedidos duplicados
        // IMPORTANTE: Se não fornecido, gera hash determinístico dos dados da requisição
        // para garantir idempotência mesmo sem chave explícita do cliente
        String idempotencyKey = request.idempotencyKey() != null && !request.idempotencyKey().isBlank()
            ? request.idempotencyKey()
            : generateIdempotencyKey(request); // Hash determinístico dos dados da requisição
        
        // Converter DTO para Command
        // Padrão: Dependency Injection - OrderPresentationMapper é injetado via construtor (SOLID)
        OrderSagaCommand command = OrderSagaCommand.builder()
            .idempotencyKey(idempotencyKey)
            .customerId(request.customerId())
            .customerName(request.customerName())
            .customerEmail(request.customerEmail())
            .items(orderMapper.toDomainList(request.items()))
            .paymentMethod(request.paymentMethod())
            .currency(request.currency() != null ? request.currency() : "BRL")
            .build();
        
        // Executar saga
        OrderSagaResult result = sagaOrchestrator.execute(command);
        
        // Converter resultado para response
        // Padrão: Idempotência - tratar caso de saga em progresso
        if (result.isSuccess()) {
            CreateOrderResponse response = CreateOrderResponse.success(
                OrderResponse.from(result.getOrder()),
                result.getSagaExecutionId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (result.isInProgress()) {
            // Saga já está em progresso (idempotência)
            CreateOrderResponse response = CreateOrderResponse.inProgress(
                result.getSagaExecutionId(),
                "Order creation is already in progress"
            );
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } else {
            CreateOrderResponse response = CreateOrderResponse.failed(
                result.getSagaExecutionId(),
                result.getErrorMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Busca um pedido pelo ID.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar pedido por ID",
        description = "Retorna um pedido específico pelo seu ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID do pedido", required = true)
            @PathVariable UUID id) {
        log.info("Finding order by ID: {}", id);
        
        return orderRepository.findById(id)
            .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca um pedido pelo número do pedido.
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(
        summary = "Buscar pedido por número",
        description = "Retorna um pedido específico pelo seu número (ex: ORD-1234567890)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponse> getOrderByNumber(
            @Parameter(description = "Número do pedido", required = true)
            @PathVariable String orderNumber) {
        log.info("Finding order by number: {}", orderNumber);
        
        return orderRepository.findByOrderNumber(orderNumber)
            .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lista todos os pedidos ou filtra por status.
     * 
     * <p>Se o parâmetro {@code status} for fornecido, retorna apenas pedidos
     * com aquele status. Caso contrário, retorna todos os pedidos.</p>
     */
    @GetMapping
    @Operation(
        summary = "Listar pedidos",
        description = "Retorna uma lista de pedidos. Pode ser filtrado por status usando o parâmetro query 'status' (PENDING, PAID, PAYMENT_FAILED, CANCELED)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada com sucesso")
    })
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @Parameter(description = "Status para filtrar pedidos (opcional). Valores: PENDING, PAID, PAYMENT_FAILED, CANCELED")
            @RequestParam(required = false) com.marcelo.orchestrator.domain.model.OrderStatus status) {
        
        List<OrderResponse> orders;
        
        if (status != null) {
            log.info("Finding orders by status: {}", status);
            orders = orderRepository.findByStatus(status).stream()
                .map(OrderResponse::from)
                .toList(); // Java 16+ - mais conciso que Collectors.toList()
        } else {
            log.info("Finding all orders");
            orders = orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList(); // Java 16+ - mais conciso que Collectors.toList()
        }
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Gera idempotencyKey determinística baseada nos dados da requisição.
     * 
     * <p>Padrão: Idempotência - Gera hash SHA-256 dos dados da requisição para garantir
     * que requisições idênticas (mesmos dados) tenham a mesma chave, prevenindo duplicação.</p>
     * 
     * <p>Se o cliente não fornecer idempotencyKey, esta função garante que requisições
     * com os mesmos dados produzam a mesma chave, mantendo idempotência.</p>
     * 
     * @param request Dados da requisição
     * @return Hash SHA-256 dos dados da requisição como idempotencyKey
     */
    private String generateIdempotencyKey(CreateOrderRequest request) {
        try {
            // Criar string única com todos os dados relevantes da requisição
            StringBuilder data = new StringBuilder();
            data.append(request.customerId());
            data.append(request.customerEmail());
            data.append(request.paymentMethod());
            if (request.currency() != null) {
                data.append(request.currency());
            }
            
            // Adicionar dados dos itens (produto, quantidade, preço)
            if (request.items() != null) {
                request.items().forEach(item -> {
                    data.append(item.productId());
                    data.append(item.productName());
                    data.append(item.quantity());
                    data.append(item.unitPrice());
                });
            }
            
            // Gerar hash SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));
            
            // Converter para hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback: se SHA-256 não disponível, usar UUID (não ideal, mas funcional)
            log.warn("SHA-256 not available, using UUID as fallback for idempotency key");
            return UUID.randomUUID().toString();
        }
    }
}

