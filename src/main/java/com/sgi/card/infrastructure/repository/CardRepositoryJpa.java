package com.sgi.card.infrastructure.repository;

import com.sgi.card.domain.model.Card;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Reactive repository for the Card entity.
 * Extends ReactiveMongoRepository to perform CRUD operations on MongoDB.
 */
public interface CardRepositoryJpa extends ReactiveMongoRepository<Card, String> {

    Flux<Card> findAllByIdOrTypeOrClientId(String id, String type, String clientId);
}
