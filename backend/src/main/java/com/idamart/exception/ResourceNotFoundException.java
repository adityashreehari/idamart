package com.idamart.exception;

/** Thrown when something the client asked for does not exist -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
