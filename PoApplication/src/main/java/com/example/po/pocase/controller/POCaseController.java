package com.example.po.pocase.controller;

import com.example.po.pocase.dto.CreatePORequest;
import com.example.po.pocase.dto.CreatePOResponse;
import com.example.po.pocase.service.POCaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pay-orders")
public class POCaseController {

    private final POCaseService poCaseService;

    public POCaseController(POCaseService poCaseService){
        this.poCaseService = poCaseService;
    }

    @PostMapping("/create")
    public CreatePOResponse create(
            @RequestHeader("X-USER-ID") String userId,
            @Valid @RequestBody CreatePORequest request){
        return poCaseService.create(request,userId);
    }
}
