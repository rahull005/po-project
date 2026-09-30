package com.example.po.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID);
        if(correlationId == null || correlationId.isBlank()){
            correlationId = UUID.randomUUID()
                    .toString();
        }

        MDC.put(CORRELATION_ID, correlationId);

        response.setHeader(CORRELATION_ID,correlationId);

        try{
            filterChain.doFilter(request,response);
        }finally {
            MDC.remove(CORRELATION_ID);
        }
    }
}

/*

                    WITHOUT MDC
                    ───────────────────────────────

                    Request A → PO-1001
                    Request B → PO-1002

                    Logs:

                    INFO Received request
                    INFO Received request
                    INFO Calling FakeFlex
                    ERROR FakeFlex failed
                    INFO Saving PO
                    INFO PO completed

                    Question:
                    Which PO failed?
                    ❌ Difficult to know



                    WITH MDC
                    ────────────────────────────────

                    Request A → PO-1001 → ABC123
                    Request B → PO-1002 → XYZ789

                    Logs:

                    INFO  [ABC123] Received request
                    INFO  [XYZ789] Received request
                    INFO  [ABC123] Calling FakeFlex
                    ERROR [XYZ789] FakeFlex failed
                    INFO  [ABC123] Saving PO
                    INFO  [ABC123] PO completed

                    Question:
                    Which request failed?
                    ✅ XYZ789

 */