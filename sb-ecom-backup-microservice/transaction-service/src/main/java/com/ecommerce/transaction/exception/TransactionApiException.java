package com.ecommerce.transaction.exception;

import org.springframework.http.HttpStatus;

public class TransactionApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private HttpStatus status;
    private String message;
    
    public TransactionApiException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public TransactionApiException(String message, HttpStatus status, String message1) {
        super(message);
        this.status = status;
        this.message = message1;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
} 