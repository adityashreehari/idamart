package com.idamart.exception;

/** Thrown when creating something that already exists (e.g. same category name) -> HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
