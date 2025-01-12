package com.sgi.card.infrastructure.subscriber.listener;

import com.sgi.card.infrastructure.annotations.KafkaController;
import com.sgi.card.infrastructure.subscriber.events.OrchestratorEventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import static com.sgi.card.domain.shared.Constants.TOPIC_ORCHESTRATOR;

@KafkaController
@Slf4j
public class TopicListenerCard {

    @KafkaListener(
            groupId = "${app.name}",
            topics = OrchestratorEventResponse.TOPIC
    )
    private void orchestratorResult(OrchestratorEventResponse orchestratorEventResponse) {
        log.info(TOPIC_ORCHESTRATOR, OrchestratorEventResponse.TOPIC,
                orchestratorEventResponse,
                orchestratorEventResponse.getStatus());
    }
}
