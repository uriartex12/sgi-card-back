package com.sgi.card.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Represents a Card in the system.
 * Contains information such as the Card's personal details, contact information,
 * and unique identifiers.
 * The class is mapped to the 'Card' collection in MongoDB.
 */
@Setter
@Getter
@Builder
@Document(collection = "cards")
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndex(def = "{'id': 1, 'mainAccountId': 1}", name = "id_account_index", unique = true)
public class Card {
    @Id
    private String id;
    private String cardNumber;
    private Instant expirationDate;
    private String type;
    private String mainAccountId;
    private List<String> associatedAccountIds;
    private String clientId;
}