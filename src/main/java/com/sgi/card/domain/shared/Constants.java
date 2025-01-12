package com.sgi.card.domain.shared;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Random;

/**
 * Utility class for defining constants and helper methods used throughout the application.
 * This class includes static constants and a method to generate unique card numbers.
 */
public class Constants {

    public static final String EXTERNAL_REQUEST_SUCCESS_FORMAT = "Request to {} succeeded: {}";
    public static final String EXTERNAL_REQUEST_ERROR_FORMAT = "Error during request to {}";
    public static final String KAFKA_MESSAGE = "Mensaje enviado: {}";
    public static final String ERROR_KAFKA_MESSAGE = "Error al enviar mensaje: ";
    public static final String TOPIC_ORCHESTRATOR = "Processing topic: [{}], Payload: [{}] - Result:{}";

    public static String generateCardNumber() {
        return String.format("%04d00%012d", new Random().nextInt(10000), new Random().nextLong(1000000000000L));
    }

    public static String urlParamsComponentBuilder(String domain, String url, Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(domain.concat(url));
        params.forEach(builder::queryParam);
        return builder.toUriString();
    }
}
