package com.sgi.card.domain.ports.out;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface for making reactive POST and GET HTTP requests to external services.
 * Defines methods for sending and receiving data asynchronously using Mono and Flux.
 */
public interface FeignExternalService {
    <T, R> Mono<R> post(String url, T requestBody, Class<R> responseType);
    <R> Publisher<R> get(String url, String pathVariable, Class<R> responseType, boolean isFlux);
}
