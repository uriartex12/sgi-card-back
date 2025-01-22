package com.sgi.card.infrastructure.subscriber.listener;

import com.sgi.card.application.service.CardEventService;
import com.sgi.card.domain.ports.in.CardService;
import com.sgi.card.infrastructure.annotations.KafkaController;
import com.sgi.card.infrastructure.dto.BalanceResponse;
import com.sgi.card.infrastructure.subscriber.events.BalanceEvent;
import com.sgi.card.infrastructure.subscriber.events.OrchestratorEventResponse;
import com.sgi.card.infrastructure.subscriber.message.EventSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import reactor.core.publisher.Mono;

import static com.sgi.card.domain.shared.Constants.TOPIC_ORCHESTRATOR;

@Slf4j
@AllArgsConstructor
@KafkaController
public class TopicListenerCard {

    private final CardEventService cardEventService;

    private final EventSender kafkaTemplate;

    @KafkaListener(
            groupId = "${app.name}",
            topics = OrchestratorEventResponse.TOPIC
    )
    private void orchestratorResult(OrchestratorEventResponse orchestratorEventResponse) {
        log.info(TOPIC_ORCHESTRATOR, OrchestratorEventResponse.TOPIC,
                orchestratorEventResponse,
                orchestratorEventResponse.getStatus());
    }

    @KafkaListener(
            groupId = "${app.name}",
            topics = BalanceEvent.TOPIC
    )
    private void balanceHandle(BalanceEvent balanceEvent) {
        cardEventService.getBalanceEvent(balanceEvent.cardId());
    }
}
