package com.sgi.card.infrastructure.feign;

import com.sgi.card.domain.ports.out.FeignExternalService;
import com.sgi.card.domain.shared.CustomError;
import com.sgi.card.infrastructure.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.sgi.card.domain.shared.Constants.EXTERNAL_REQUEST_ERROR_FORMAT;
import static com.sgi.card.domain.shared.Constants.EXTERNAL_REQUEST_SUCCESS_FORMAT;

/**
 * Implementation of the Feign external service to make HTTP requests in a reactive manner with Circuit Breaker support.
 */
@Slf4j
@Service
public class FeignExternalServiceImpl implements FeignExternalService {

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public FeignExternalServiceImpl(WebClient.Builder webClientBuilder,
                                    ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = webClientBuilder.build();
        this.circuitBreaker = circuitBreakerFactory.create("card-service");
    }

    @Override
    public <T, R> Mono<R> post(String url, T requestBody, Class<R> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .doOnNext(response -> logSuccess(url, response))
                .doOnError(ex -> logError(url, ex))
                .onErrorResume(ex -> Mono.error(new CustomException(CustomError.E_OPERATION_FAILED)))
                .transformDeferred(circuitBreaker::run);
    }


    @Override
    public <R> Publisher<R> get(String url, String pathVariable, Class<R> responseType, boolean isFlux) {
        var responseSpec = webClient.get()
                .uri(url, pathVariable)
                .retrieve();
        if (isFlux) {
            return responseSpec.bodyToFlux(responseType)
                    .doOnNext(response -> logSuccess(url, response))
                    .doOnError(ex -> logError(url, ex))
                    .onErrorResume(ex
                            -> Flux.error(new CustomException(CustomError.E_OPERATION_FAILED)))
                    .transformDeferred(circuitBreaker::run);
        } else {
            return responseSpec.bodyToMono(responseType)
                    .doOnNext(response -> logSuccess(url, response))
                    .doOnError(ex -> logError(url, ex))
                    .onErrorResume(ex
                            -> Mono.error(new CustomException(CustomError.E_OPERATION_FAILED)))
                    .transformDeferred(circuitBreaker::run);
        }
    }

    private <R> void logSuccess(String url, R response) {
        log.info(EXTERNAL_REQUEST_SUCCESS_FORMAT, url, response);
    }

    private void logError(String url, Throwable ex) {
        log.error(EXTERNAL_REQUEST_ERROR_FORMAT, url, ex);
    }
}
