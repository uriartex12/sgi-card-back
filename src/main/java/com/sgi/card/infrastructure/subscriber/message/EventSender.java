package com.sgi.card.infrastructure.subscriber.message;

import com.sgi.card.infrastructure.mapper.ObjectMappers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Component
@Slf4j
public class EventSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventSender(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @SneakyThrows
    public CompletableFuture<SendResult<String, Object>> sendEvent(Object event) {
            String topic = event.getClass().getSimpleName();
            String value = ObjectMappers.OBJECT_MAPPER.writeValueAsString(event);
            log.info("Publishing to Kafka topic {}: {}", topic, event);
            return kafkaTemplate.send(new ProducerRecord<>(topic, value));
    }
}