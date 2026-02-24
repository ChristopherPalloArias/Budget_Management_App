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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import com.microservice.report.exception.ServiceIntegrationException;

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
    public List<TransactionData> fetchTransactions(String period, String token) {
        String url = transactionServiceUrl + "/transactions?period=" + period + "&size=1000";

        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
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
        } catch (HttpStatusCodeException e) {
            throw new ServiceIntegrationException(
                    "Error from transaction service: HTTP " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new ServiceIntegrationException("Error connecting to transaction service: " + e.getMessage(), e);
        }
    }



    private record PaginatedTransactionResponse(List<TransactionData> content) {}
}
