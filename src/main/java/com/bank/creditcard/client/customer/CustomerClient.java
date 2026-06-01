package com.bank.creditcard.client.customer;

import com.bank.creditcard.client.customer.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerClient {

    private final WebClient webClient;

    @Value("${customer-service.base-url}")
    private String customerServiceBaseUrl;

    public Mono<CustomerResponse> findCustomerById(String customerId) {
        return webClient
                .get()
                .uri(
                        customerServiceBaseUrl +
                                "/api/v1/customers/{id}",
                        customerId
                )
                .retrieve()
                .bodyToMono(CustomerResponse.class);
    }
}
