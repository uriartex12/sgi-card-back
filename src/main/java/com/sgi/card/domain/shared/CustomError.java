package com.sgi.card.domain.shared;

import com.sgi.card.infrastructure.exception.ApiError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum representing custom errors for the Card-service application.
 * Each constant includes an error code, message, and HTTP status for specific errors.
 */
@Getter
@AllArgsConstructor
public enum CustomError {

    E_OPERATION_FAILED(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "CARD-000", "Operation failed")),
    E_CARD_NOT_FOUND(new ApiError(HttpStatus.NOT_FOUND, "CARD-001", "Card not found")),
    E_INSUFFICIENT_BALANCE(new ApiError(HttpStatus.PAYMENT_REQUIRED, "CARD-004", "Insufficient balance")),
    E_MALFORMED_CARD_DATA(new ApiError(HttpStatus.BAD_REQUEST, "CARD-002", "Malformed card data")),
    E_ACCOUNT_ALREADY_ASSOCIATED(new ApiError(HttpStatus.CONFLICT, "CARD-003", "Account already associated with this card")),
    E_DUPLICATE_CARD_NUMBER(new ApiError(HttpStatus.CONFLICT, "CARD-005", "Card with this number already exists"));

    private final ApiError error;
}
