package com.marcelo.orchestrator.presentation.controller;

import com.marcelo.orchestrator.presentation.dto.OrderResponse;
import com.marcelo.orchestrator.presentation.dto.PaymentStatusResponse;
import com.marcelo.orchestrator.presentation.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "API for payment status operations")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{paymentId}/status")
    @Operation(
        summary = "Check payment status",
        description = "Checks the current status of a payment in the external gateway (AbacatePay) using the paymentId (bill_xxx). " +
                      "Automatically updates the order in the database if the status has changed."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status returned successfully")
    })
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(
        @Parameter(description = "Payment ID in AbacatePay (e.g. bill_xxx)", required = true)
        @PathVariable String paymentId
    ) {
        log.info("Checking payment status via API for paymentId={}", paymentId);
        PaymentStatusResponse response = paymentService.checkPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/orders/{orderId}/refresh-status")
    @Operation(
        summary = "Refresh payment status for order",
        description = "Refreshes the payment status of an order by querying the external gateway and updating the order accordingly."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order payment status refreshed successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> refreshPaymentStatusForOrder(
        @Parameter(description = "Order ID", required = true)
        @PathVariable UUID orderId
    ) {
        log.info("Refreshing payment status for order {}", orderId);
        OrderResponse response = paymentService.refreshPaymentStatusForOrder(orderId);
        return ResponseEntity.ok(response);
    }
}


