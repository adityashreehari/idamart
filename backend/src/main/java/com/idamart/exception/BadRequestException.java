package com.idamart.exception;

/** Thrown when a request breaks a business rule (e.g. deleting a category that still has products) -> HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
