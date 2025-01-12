package com.sgi.card.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

import static com.sgi.card.infrastructure.mapper.ObjectMappers.OBJECT_MAPPER;

@Configuration
@Slf4j
public class KafkaJsonMessageConvertorConfiguration {

    @Bean
    public RecordMessageConverter jsonMessageConverter() {
        log.info("Creating JSON message converter");
        return new StringJsonMessageConverter(OBJECT_MAPPER);
    }
}
