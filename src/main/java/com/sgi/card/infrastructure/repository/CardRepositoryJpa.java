package com.sgi.card.infrastructure.repository;

import com.sgi.card.domain.model.Card;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repositorio Reactivo para la entidad Card.
 * Extiende de ReactiveMongoRepository para realizar operaciones CRUD en MongoDB.
 */
public interface CardRepositoryJpa extends ReactiveMongoRepository<Card, String> {

    Flux<Card> findAllByIdOrTypeOrClientId(String id, String type, String clientId);
}
