package com.sgi.card.domain.ports.in;

import com.sgi.card.infrastructure.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing bank cards.
 * Defines the operations for creating, updating, deleting, and querying cards.
 */
public interface CardService {
    Mono<CardResponse> createCard(Mono<CardRequest> card);
    Mono<Void> deleteCard(String cardId);
    Flux<CardResponse> getAllCards(String clientId, String type, String cardId);
    Mono<CardResponse> getCardById(String cardId);
    Mono<CardResponse> updateCard(String cardId, Mono<CardRequest> card);
    Mono<CardResponse> associateDebitCardToAccount(String debitCardId, Mono<AssociateRequest> associateRequest);
    Mono<BalanceResponse> getPrimaryAccountBalance(String cardId);
    Flux<TransactionResponse> getLastTransactions(String cardId, Integer page, Integer size);
    Mono<Void> processPaymentOrWithdrawal(String cardId, Mono<PaymentRequest> request);
}
