package com.sgi.card.infrastructure.mapper;

import com.sgi.card.infrastructure.dto.BalanceResponse;
import com.sgi.card.infrastructure.subscriber.events.BalanceEventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CardEventMapper {

    CardEventMapper INSTANCE = Mappers.getMapper(CardEventMapper.class);

    BalanceEventResponse map (BalanceResponse balanceResponse);
}
