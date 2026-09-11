package com.example.fakeflex.controller;

import com.example.fakeflex.dto.FakeFlexPORequest;
import com.example.fakeflex.dto.FakeFlexPOResponse;
import com.example.fakeflex.service.FakeFlexService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fake-flex/api/v1")
public class FakeFlexController {

    private final FakeFlexService service;

    public FakeFlexController(FakeFlexService service) {
        this.service = service;
    }

    @PostMapping("/pay-orders")
    public FakeFlexPOResponse createPayOrder(
            @Valid @RequestBody FakeFlexPORequest request) {

        return service.createPayOrder(request);
    }
}