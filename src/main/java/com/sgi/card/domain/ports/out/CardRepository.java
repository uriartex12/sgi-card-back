package com.sgi.card.domain.ports.out;

import com.sgi.card.domain.model.Card;
import com.sgi.card.infrastructure.dto.CardResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CardRepository {

    Mono<CardResponse> save(Card card);

    Mono<Card> findById(String cardId);

    Flux<CardResponse> findAll();

    Mono<Void> delete(Card card);

}
