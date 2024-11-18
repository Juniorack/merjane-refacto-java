package com.nimbleways.springboilerplate.exceptions;

public class OrderShouldHaveItemsException extends RuntimeException {
    public OrderShouldHaveItemsException(String message) {
        super(message);
    }
}
