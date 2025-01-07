package com.sgi.card.infrastructure.mapper;

import com.sgi.card.domain.model.Card;
import com.sgi.card.infrastructure.dto.AccountResponse;
import com.sgi.card.infrastructure.dto.BalanceResponse;
import com.sgi.card.infrastructure.dto.CardRequest;
import com.sgi.card.infrastructure.dto.CardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper
public interface CardMapper {

    CardMapper INSTANCE = Mappers.getMapper(CardMapper.class);

    @Mapping(target = "type", source = "type")
    CardResponse toCardResponse(Card card);

    @Mapping(target = "id", ignore = true)
    Card toCard(CardRequest  cardRequest);

    Card created(CardRequest card);

    BalanceResponse toBalance(AccountResponse accountResponse);

    default Mono<Card> map(Mono<CardRequest> cardRequestMono) {
        return cardRequestMono.map(this::created);
    }

    default OffsetDateTime map(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toInstant() : null;
    }
}
