package com.sgi.card.application.service;

import com.sgi.card.domain.model.Card;
import com.sgi.card.domain.ports.in.CardService;
import com.sgi.card.domain.ports.out.CardRepository;
import com.sgi.card.domain.ports.out.FeignExternalService;
import com.sgi.card.domain.shared.CustomError;
import com.sgi.card.infrastructure.dto.*;
import com.sgi.card.infrastructure.exception.CustomException;
import com.sgi.card.infrastructure.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

import static com.sgi.card.domain.shared.Constants.urlComponentBuilder;
import static com.sgi.card.domain.shared.Constants.urlParamsComponentBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    @Value("${feign.client.config.account-service.url}")
    private String accountServiceUrl;

    @Value("${feign.client.config.transaction-service.url}")
    private String transactionServiceUrl;

    private final CardRepository cardRepository;

    private final FeignExternalService webClient;

    @Override
    public Mono<CardResponse> createCard(Mono<CardRequest> card) {
        return card.flatMap( cardRequest ->
               CardMapper.INSTANCE.map(Mono.just(cardRequest)))
               .flatMap(cardRepository::save);
    }

    @Override
    public Mono<Void> deleteCard(String cardId) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CARD_NOT_FOUND)))
                .flatMap(cardRepository::delete);
    }

    @Override
    public Flux<CardResponse> getAllCards() {
        return cardRepository.findAll();
    }

    @Override
    public Mono<CardResponse> getCardById(String cardId) {
        return cardRepository.findById(cardId)
                .map(CardMapper.INSTANCE::toCardResponse);
    }

    @Override
    public Mono<CardResponse> updateCard(String cardId, Mono<CardRequest> cardRequestMono) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CARD_NOT_FOUND)))
                .flatMap(card ->
                        cardRequestMono.map(updatedCard -> {
                            Card updated = CardMapper.INSTANCE.toCard(updatedCard);
                            updated.setId(card.getId());
                            return updated;
                        })
                )
                .flatMap(cardRepository::save);
    }

    @Override
    public Mono<CardResponse> associateDebitCardToAccount(String cardId, Mono<AssociateRequest> associateRequest) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CARD_NOT_FOUND)))
                .flatMap(debitCard -> associateRequest.flatMap(associate -> {
                    List<String> associatedAccountIds = Optional.ofNullable(debitCard.getAssociatedAccountIds())
                            .orElse(Collections.emptyList());
                    if (associatedAccountIds.contains(associate.getAccountId())) {
                        return Mono.error(new CustomException(CustomError.E_ACCOUNT_ALREADY_ASSOCIATED));
                    }
                    associatedAccountIds = Optional.ofNullable(debitCard.getAssociatedAccountIds())
                            .orElseGet(() -> {
                                List<String> newAccountIds = new ArrayList<>();
                                debitCard.setAssociatedAccountIds(newAccountIds);
                                return newAccountIds;
                            });
                    associatedAccountIds.add(associate.getAccountId());
                    return cardRepository.save(debitCard);
                }));
    }

    @Override
    public Mono<BalanceResponse> getPrimaryAccountBalance(String cardId) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CARD_NOT_FOUND)))
                .flatMap(card ->
                        Mono.from(webClient.get(accountServiceUrl.concat("/v1/accounts/{accountId}/balance"),
                                card.getMainAccountId(),
                                AccountResponse.class,
                                false))
                                .map(CardMapper.INSTANCE::toBalance)
                );
    }

    @Override
    public Flux<TransactionResponse> getLastTransactions(String cardId, Integer page, Integer size) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CARD_NOT_FOUND)))
                .flatMapMany(card ->
                        Flux.from(webClient.get(urlParamsComponentBuilder(transactionServiceUrl, "/v1/transactions",
                                        Map.of("cardId", cardId,
                                                "page", page,
                                                "size", size)),
                                null,
                                TransactionResponse.class,
                                true))
                );
    }

    @Override
    public Mono<TransactionResponse> processPaymentOrWithdrawal(String cardId, Mono<PaymentRequest> paymentRequest) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CARD_NOT_FOUND)))
                .flatMap(card -> paymentRequest.flatMap( payment ->
                        verifyFunds(card, payment.getAmount())
                                .flatMap(accountId ->
                                    modifyBalanceInAccountService(accountId, payment.getAmount(), "reduce")
                                            .flatMap(balanceResponse ->
                                                    registerTransaction(cardId, accountId, payment,
                                                            balanceResponse.getCardBalance())
                                            )
                                )
                        )
                );
    }

    private Mono<String> verifyFunds(Card card, BigDecimal amount) {
        return Flux.fromIterable(card.getAssociatedAccountIds())
                .concatMap(accountId ->
                        Mono.from(webClient.get(transactionServiceUrl.concat("/v1/transactions/{productId}/card"),
                                        accountId,
                                        BalanceResponse.class ,
                                        false))
                                .filter(response -> response.getCardBalance().compareTo(amount) >= 0)
                                .map(BalanceResponse::getCardId)
                )
                .next()
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_INSUFFICIENT_BALANCE)));
    }

    private Mono<TransactionResponse> registerTransaction(String cardId, String accountId, PaymentRequest request, BigDecimal balance) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setCardId(cardId);
        transactionRequest.setProductId(accountId);
        transactionRequest.setType(request.getType().name());
        transactionRequest.setAmount(request.getAmount());
        transactionRequest.setBalance(balance);
        return webClient.post(transactionServiceUrl.concat("/v1/transactions"),
                transactionRequest,
                TransactionResponse.class);
    }

    private Mono<BalanceResponse> modifyBalanceInAccountService(String accountId, BigDecimal amount, String action) {
        return webClient.post(urlComponentBuilder(accountServiceUrl,"/accounts/{accountId}/balance/{action}",
                        Map.of("accountId",accountId, "action", action)),
                        Map.of("amount", amount), BalanceResponse.class);
    }
}
