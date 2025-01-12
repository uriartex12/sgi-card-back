package com.sgi.card.infrastructure.subscriber.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrchestratorEventResponse {

    private String cardId;
    private String accountId;
    private String clientId;
    private String type;
    private String status;
    private BigDecimal amount;
    private BigDecimal balance;

    public static final String TOPIC = "OrchestratorEventResponse";
}