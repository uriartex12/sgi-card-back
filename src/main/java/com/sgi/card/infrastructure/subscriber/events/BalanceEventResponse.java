package com.sgi.card.infrastructure.subscriber.events;

import java.math.BigDecimal;

public record BalanceEventResponse (String cardId, String accountId, String clientId, BigDecimal accountBalance) {}