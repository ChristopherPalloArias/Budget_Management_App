package com.microservice.report.infrastructure.client;

import com.microservice.report.service.TransactionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransactionClientImpl implements TransactionClient {

    private final RestTemplate restTemplate;

    @Value("${application.transaction-service.url:http://transaction:8081/api/v1}")
    private String transactionServiceUrl;

    @Override
    public List<TransactionData> fetchTransactions(String period) {
        String jwt = getJwtFromContext();
        String url = transactionServiceUrl + "/transactions?period=" + period + "&size=1000";

        HttpHeaders headers = new HttpHeaders();
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PaginatedTransactionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<PaginatedTransactionResponse>() {}
            );

            if (response.getBody() != null && response.getBody().content() != null) {
                return response.getBody().content();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con el microservicio de transacciones: " + e.getMessage(), e);
        }
    }

    private String getJwtFromContext() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    private record PaginatedTransactionResponse(List<TransactionData> content) {}
}
