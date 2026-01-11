package com.marcelo.orchestrator.presentation.controller;

import com.marcelo.orchestrator.domain.model.OrderStatus;
import com.marcelo.orchestrator.presentation.dto.CreateOrderRequest;
import com.marcelo.orchestrator.presentation.dto.CreateOrderResponse;
import com.marcelo.orchestrator.presentation.dto.OrderResponse;
import com.marcelo.orchestrator.presentation.service.OrderService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "API para gerenciamento de pedidos")
public class OrderController {
    
    private final OrderService orderService;
    
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
        
        CreateOrderResponse response = orderService.createOrder(request);
        
        HttpStatus httpStatus = response.success() ? HttpStatus.CREATED 
            : response.inProgress() ? HttpStatus.ACCEPTED 
            : HttpStatus.BAD_REQUEST;
        
        return ResponseEntity.status(httpStatus).body(response);
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
        
        OrderResponse order = orderService.getOrderById(id);
        return order != null 
            ? ResponseEntity.ok(order)
            : ResponseEntity.notFound().build();
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
        
        OrderResponse order = orderService.getOrderByNumber(orderNumber);
        return order != null 
            ? ResponseEntity.ok(order)
            : ResponseEntity.notFound().build();
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
            @RequestParam(required = false) OrderStatus status) {
        
        List<OrderResponse> orders = orderService.getAllOrders(status);
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
        OrderResponse response = orderService.analyzeRiskManually(id);
        return ResponseEntity.ok(response);
    }
}