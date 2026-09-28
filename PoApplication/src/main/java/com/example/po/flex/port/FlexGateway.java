package com.example.po.flex.port;

import com.example.po.flex.domain.FlexCreatePORequest;
import com.example.po.flex.domain.FlexCreatePOResponse;

public interface FlexGateway {
    FlexCreatePOResponse createPayOrder(
            FlexCreatePORequest request
    );

    FlexCreatePOResponse getPayOrderStatus(
            String requestId
    );
}
