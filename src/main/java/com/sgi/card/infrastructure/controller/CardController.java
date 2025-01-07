package com.sgi.card.infrastructure.controller;

import com.sgi.card.domain.ports.in.CardService;
import com.sgi.card.infrastructure.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CardController implements V1Api {

    private final CardService cardService;

    @Override
    public Mono<ResponseEntity<CardResponse>> associateDebitCardToAccount(String cardId, Mono<AssociateRequest> associateRequest, ServerWebExchange exchange) {
        return cardService.associateDebitCardToAccount(cardId, associateRequest)
                .map(cardResponse -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(cardResponse));
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> createCard(Mono<CardRequest> cardRequest, ServerWebExchange exchange) {
        return cardService.createCard(cardRequest)
               .map(cardResponse -> ResponseEntity.status(HttpStatus.CREATED)
                       .body(cardResponse));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCard(String cardId, ServerWebExchange exchange) {
        return cardService.deleteCard(cardId)
                .map(cardResponse -> ResponseEntity.ok().body(cardResponse));
    }

    @Override
    public Mono<ResponseEntity<Flux<CardResponse>>> getAllCards(ServerWebExchange exchange) {
        return Mono.fromSupplier(() -> ResponseEntity.ok().body(cardService.getAllCards()));
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> getCardById(String cardId, ServerWebExchange exchange) {
        return cardService.getCardById(cardId)
                .map(cardResponse -> ResponseEntity.ok().body(cardResponse));
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getPrimaryAccountBalance(String cardId, ServerWebExchange exchange) {
        return cardService.getPrimaryAccountBalance(cardId)
                .map(cardResponse -> ResponseEntity.ok().body(cardResponse));
    }

    @Override
    public Mono<ResponseEntity<TransactionResponse>> processPaymentOrWithdrawal(String cardId, Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return cardService.processPaymentOrWithdrawal(cardId, paymentRequest)
                .map(cardResponse -> ResponseEntity.ok().body(cardResponse));
    }

    @Override
    public Mono<ResponseEntity<CardResponse>> updateCard(String cardId, Mono<CardRequest> cardRequest, ServerWebExchange exchange) {
        return cardService.updateCard(cardId, cardRequest)
                .map(cardResponse -> ResponseEntity.ok().body(cardResponse));
    }
}
