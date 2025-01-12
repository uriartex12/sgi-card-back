package com.sgi.card.infrastructure.mapper;

import com.sgi.card.domain.model.Card;
import com.sgi.card.infrastructure.dto.BalanceResponse;
import com.sgi.card.infrastructure.dto.PaymentRequest;
import com.sgi.card.infrastructure.subscriber.events.OrchestratorEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExternalOrchestratorDataMapper {

    ExternalOrchestratorDataMapper INSTANCE = Mappers.getMapper(ExternalOrchestratorDataMapper.class);

    @Mapping(target = "type", source = "payment.type")
    @Mapping(target = "clientId", source = "card.clientId")
    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "balance", source = "balance.accountBalance")
    OrchestratorEvent toOrchestratorEvent(Card card, BalanceResponse balance, PaymentRequest payment);
}