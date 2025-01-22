package com.sgi.card.application.service.impl;

import com.sgi.card.application.service.CardEventService;
import com.sgi.card.domain.ports.in.CardService;
import com.sgi.card.infrastructure.mapper.CardEventMapper;
import com.sgi.card.infrastructure.subscriber.message.EventSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.sgi.card.domain.shared.Constants.ERROR_KAFKA_MESSAGE;
import static com.sgi.card.domain.shared.Constants.KAFKA_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardEventServiceImpl implements CardEventService {

    private final CardService cardService;

    private final EventSender kafkaTemplate;

    @Override
    public void getBalanceEvent(String cardId) {
        cardService.getPrimaryAccountBalance(cardId)
                .map(CardEventMapper.INSTANCE::map)
                .flatMap(event -> Mono.fromFuture(() -> kafkaTemplate.sendEvent(event)))
                .doOnSuccess(result -> log.info(KAFKA_MESSAGE, result))
                .doOnError(error -> log.error(ERROR_KAFKA_MESSAGE))
                .subscribe();
    }


}
