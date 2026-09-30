package com.example.fakeflex.idempotency;

import com.example.fakeflex.dto.FakeFlexPOResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FakeFlexIdempotencyStore {

    private final Map<String, FakeFlexPOResponse> responses =
            new ConcurrentHashMap<>();

    public FakeFlexPOResponse get(String idempotencyKey) {
        return responses.get(idempotencyKey);
    }

    public FakeFlexPOResponse putIfAbsent(
            String idempotencyKey,
            FakeFlexPOResponse response) {
        return responses.putIfAbsent(
                idempotencyKey,
                response
        );
    }

    public Optional<FakeFlexPOResponse> findByRequestId(
            String requestId) {
        return responses.values()
                .stream()
                .filter(response -> requestId.equals(response.requestId()))
                .findFirst();
    }
}
