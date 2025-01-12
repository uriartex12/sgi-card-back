package com.sgi.card.application.service;

import com.sgi.card.domain.model.Card;
import com.sgi.card.domain.ports.in.CardService;
import com.sgi.card.domain.ports.out.CardRepository;
import com.sgi.card.domain.ports.out.FeignExternalService;
import com.sgi.card.domain.shared.CustomError;
import com.sgi.card.infrastructure.dto.AccountResponse;
import com.sgi.card.infrastructure.dto.BalanceResponse;
import com.sgi.card.infrastructure.dto.CardResponse;
import com.sgi.card.infrastructure.dto.PaymentRequest;
import com.sgi.card.infrastructure.dto.TransactionResponse;
import com.sgi.card.infrastructure.dto.CardRequest;
import com.sgi.card.infrastructure.dto.AssociateRequest;
import com.sgi.card.infrastructure.exception.CustomException;
import com.sgi.card.infrastructure.mapper.CardMapper;
import com.sgi.card.infrastructure.mapper.ExternalOrchestratorDataMapper;
import com.sgi.card.infrastructure.subscriber.events.OrchestratorEvent;
import com.sgi.card.infrastructure.subscriber.message.EventSender;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static com.sgi.card.domain.shared.Constants.*;

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

    private final EventSender kafkaTemplate;

    @Override
    public Mono<CardResponse> createCard(Mono<CardRequest> card) {
        return card.flatMap( cardRequest -> {
            cardRequest.setCardNumber(generateCardNumber());
            cardRequest.setExpirationDate(OffsetDateTime.now().plusYears(5));
            return CardMapper.INSTANCE.map(Mono.just(cardRequest))
               .flatMap(cardRepository::save);
        });
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
    @SneakyThrows
    public Mono<Void> processPaymentOrWithdrawal(String cardId, Mono<PaymentRequest> paymentRequest) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_CARD_NOT_FOUND)))
                .flatMap(card ->
                        paymentRequest.flatMap(payment ->
                                verifyFunds(card.getAssociatedAccountIds(), payment.getAmount())
                                        .flatMap(balance -> {
                                            OrchestratorEvent event = ExternalOrchestratorDataMapper.INSTANCE
                                                    .toOrchestratorEvent(card, balance, payment);
                                            event.setBalance(balance.getAccountBalance().subtract(payment.getAmount()));
                                            return Mono.fromFuture(kafkaTemplate.sendEvent(event))
                                                    .doOnSuccess(result -> log.info(KAFKA_MESSAGE, result.getRecordMetadata()))
                                                    .doOnError(error -> log.error(ERROR_KAFKA_MESSAGE, error))
                                                    .then();
                                        })
                        )
                );
    }



    private Mono<BalanceResponse> verifyFunds(List<String> associatedAccountIds, BigDecimal amount) {
        return Flux.fromIterable(associatedAccountIds)
                .concatMap(accountId ->
                        Mono.from(webClient.get(accountServiceUrl.concat("/v1/accounts/{accountId}/balances"),
                                        accountId,
                                        BalanceResponse.class ,
                                        false))
                                .filter(response -> response.getAccountBalance().compareTo(amount) >= 0)
                )
                .next()
                .switchIfEmpty(Mono.error(new CustomException(CustomError.E_INSUFFICIENT_BALANCE)));
    }

}
