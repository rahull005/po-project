package com.example.fakeflex.controller;

import com.example.fakeflex.dto.FakeFlexPORequest;
import com.example.fakeflex.dto.FakeFlexPOResponse;
import com.example.fakeflex.dto.FakeFlexScenario;
import com.example.fakeflex.service.FakeFlexService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fake-flex/api/v1")
public class FakeFlexController {

    private final FakeFlexService service;

    public FakeFlexController(FakeFlexService service) {
        this.service = service;
    }

    @PostMapping("/pay-orders")
    public FakeFlexPOResponse createPayOrder(

            @RequestHeader(
                    value = "X-Flex-Scenario",
                    defaultValue = "SUCCESS"
            )
            FakeFlexScenario scenario,

            @Valid
            @RequestBody
            FakeFlexPORequest request) {

        return service.createPayOrder(
                request,
                scenario
        );
    }


    @GetMapping("/pay-orders/{requestId}")
    public FakeFlexPOResponse getPayOrder(@PathVariable String requestId){
        return service.getPayOrder(requestId);
    }
}