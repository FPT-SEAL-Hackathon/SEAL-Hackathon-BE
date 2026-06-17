package com.fpt.swp.sealhackathonbe.core.exception.ExceptionPrivate;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String message) {
        super(message);
    }
}