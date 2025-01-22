package com.sgi.card.infrastructure.subscriber.events;

public record BalanceEvent(String cardId) {
    public static final String TOPIC = "BalanceEvent";
}
