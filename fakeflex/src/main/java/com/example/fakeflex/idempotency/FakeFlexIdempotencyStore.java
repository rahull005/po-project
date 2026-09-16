package com.example.fakeflex.idempotency;

import com.example.fakeflex.dto.FakeFlexPOResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FakeFlexIdempotencyStore {

    private final Map<String, FakeFlexPOResponse> responses = new ConcurrentHashMap<>();

    public FakeFlexPOResponse get(String key) {
        return responses.get(key);
    }

    public void put(
            String key,
            FakeFlexPOResponse response) {

        responses.put(key, response);
    }
}
