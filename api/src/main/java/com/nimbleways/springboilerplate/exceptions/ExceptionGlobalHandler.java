package com.nimbleways.springboilerplate.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionGlobalHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Object> handleOrderNotFoundException(OrderNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(OrderShouldHaveItemsException.class)
    public ResponseEntity<Object> handleOrderShouldHaveItemsException(OrderShouldHaveItemsException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }
}
