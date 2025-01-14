package com.sgi.card.infrastructure.repository.impl;

import com.sgi.card.domain.model.Card;
import com.sgi.card.domain.ports.out.CardRepository;
import com.sgi.card.infrastructure.dto.CardResponse;
import com.sgi.card.infrastructure.mapper.CardMapper;
import com.sgi.card.infrastructure.repository.CardRepositoryJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Implementation of the {@link CardRepository} interface.
 * Provides operations for managing bank accounts using a JPA-based repository.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CardRepositoryImpl implements CardRepository {

    private final CardRepositoryJpa repositoryJpa;

    @Override
    public Mono<CardResponse> save(Card card) {
        return repositoryJpa.save(card)
                .map(CardMapper.INSTANCE::toCardResponse);
    }

    @Override
    public Mono<Card> findById(String cardId) {
        return repositoryJpa.findById(cardId);
    }

    @Override
    public Flux<CardResponse> findAll(String clientId, String type, String cardId) {
        boolean allNull = Stream.of(clientId, type, cardId).allMatch(Objects::isNull);
        Flux<Card> resultFlux = allNull
                ? repositoryJpa.findAll() : repositoryJpa.findAllByIdOrTypeOrClientId(cardId, type, clientId);
        return resultFlux.map(CardMapper.INSTANCE::toCardResponse);
    }

    @Override
    public Mono<Void> delete(Card card) {
        return repositoryJpa.delete(card);
    }
}
