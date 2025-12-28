package com.marcelo.orchestrator.presentation.controller;

import com.marcelo.orchestrator.application.saga.OrderSagaCommand;
import com.marcelo.orchestrator.application.saga.OrderSagaOrchestrator;
import com.marcelo.orchestrator.application.saga.OrderSagaResult;
import com.marcelo.orchestrator.application.usecase.AnalyzeRiskCommand;
import com.marcelo.orchestrator.application.usecase.AnalyzeRiskUseCase;
import com.marcelo.orchestrator.domain.model.Order;
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


@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API para gerenciamento de pedidos")
public class OrderController {
    
    private final OrderSagaOrchestrator sagaOrchestrator;
    private final OrderRepositoryPort orderRepository;
    private final OrderPresentationMapper orderMapper;
    private final AnalyzeRiskUseCase analyzeRiskUseCase;
    
    
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
        
        
        
        
        
        String idempotencyKey = request.idempotencyKey() != null && !request.idempotencyKey().isBlank()
            ? request.idempotencyKey()
            : generateIdempotencyKey(request); 
        
        
        
        OrderSagaCommand command = OrderSagaCommand.builder()
            .idempotencyKey(idempotencyKey)
            .customerId(request.customerId())
            .customerName(request.customerName())
            .customerEmail(request.customerEmail())
            .items(orderMapper.toDomainList(request.items()))
            .paymentMethod(request.paymentMethod())
            .currency(request.currency() != null ? request.currency() : "BRL")
            .build();
        
        
        OrderSagaResult result = sagaOrchestrator.execute(command);
        
        
        
        if (result.isSuccess()) {
            CreateOrderResponse response = CreateOrderResponse.success(
                OrderResponse.from(result.getOrder()),
                result.getSagaExecutionId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (result.isInProgress()) {
            
            CreateOrderResponse response = CreateOrderResponse.inProgress(
                result.getOrder() != null ? OrderResponse.from(result.getOrder()) : null,
                result.getSagaExecutionId(),
                "Order creation is already in progress"
            );
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } else {
            CreateOrderResponse response = CreateOrderResponse.failed(
                result.getOrder() != null ? OrderResponse.from(result.getOrder()) : null,
                result.getSagaExecutionId(),
                result.getErrorMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    
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
                .toList(); 
        } else {
            log.info("Finding all orders");
            orders = orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList(); 
        }
        
        return ResponseEntity.ok(orders);
    }
    
    
    @PostMapping("/{id}/analyze-risk")
    @Operation(
        summary = "Analisar risco de um pedido via IA",
        description = "Dispara manualmente a análise de risco usando o mecanismo de IA configurado (OpenAI), " +
                      "para um pedido que esteja PAID ou PAYMENT_PENDING."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Análise de risco executada (ou ignorada, se desabilitada)"),
        @ApiResponse(responseCode = "400", description = "Estado inválido para análise de risco"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<OrderResponse> analyzeRiskManually(
            @Parameter(description = "ID do pedido", required = true)
            @PathVariable UUID id
    ) {
        log.info("Manually triggering risk analysis for order {}", id);
        
        
        AnalyzeRiskCommand command = AnalyzeRiskCommand.builder()
            .orderId(id)
            .paymentMethod("PIX")
            .build();
        
        Order analyzedOrder = analyzeRiskUseCase.execute(command);
        return ResponseEntity.ok(OrderResponse.from(analyzedOrder));
    }
    
    
    private String generateIdempotencyKey(CreateOrderRequest request) {
        try {
            
            StringBuilder data = new StringBuilder();
            data.append(request.customerId());
            data.append(request.customerEmail());
            data.append(request.paymentMethod());
            if (request.currency() != null) {
                data.append(request.currency());
            }
            
            
            if (request.items() != null) {
                request.items().forEach(item -> {
                    data.append(item.productId());
                    data.append(item.productName());
                    data.append(item.quantity());
                    data.append(item.unitPrice());
                });
            }
            
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.toString().getBytes(StandardCharsets.UTF_8));
            
            
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
            
            log.warn("SHA-256 not available, using UUID as fallback for idempotency key");
            return UUID.randomUUID().toString();
        }
    }
}

