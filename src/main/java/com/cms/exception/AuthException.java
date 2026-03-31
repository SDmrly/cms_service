package com.cms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public AuthException(String code, String message) {
        super(message);
        this.code = code;
        this.status = switch (code) {
            case "ACCOUNT_LOCKED" -> HttpStatus.LOCKED;
            case "ACCOUNT_DISABLED" -> HttpStatus.FORBIDDEN;
            case "INVALID_CREDENTIALS", "SESSION_EXPIRED", "TOKEN_EXPIRED", "INVALID_TOKEN" -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.UNAUTHORIZED;
        };
    }
}
