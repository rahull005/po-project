package com.example.fakeflex.controller;

import com.example.fakeflex.dto.FakeFlexPORequest;
import com.example.fakeflex.dto.FakeFlexPOResponse;
import com.example.fakeflex.dto.FakeFlexScenario;
import com.example.fakeflex.service.FakeFlexService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/fake-flex/api/v1")
public class FakeFlexController {

    private final FakeFlexService service;

    public FakeFlexController(FakeFlexService service) {
        this.service = service;
    }

    @PostMapping("/pay-orders")
    public FakeFlexPOResponse createPayOrder(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader(
                    value = "X-Flex-Scenario",
                    defaultValue = "SUCCESS"
            ) FakeFlexScenario scenario,
            @Valid @RequestBody FakeFlexPORequest request) {

        return service.createPayOrder(
                idempotencyKey,
                request,
                scenario
        );
    }

    @GetMapping("/pay-orders/{requestId}")
    public FakeFlexPOResponse getPayOrder(
            @PathVariable String requestId) {
        return service.getPayOrder(requestId);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateRequest(
            com.example.fakeflex.exception.FakeFlexDuplicateRequestException e) {
        return e.getMessage();
    }
}
