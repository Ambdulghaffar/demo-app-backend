package com.elhaffar.exoformbackend.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Integer id) {
        super(resource + " introuvable avec l'id : " + id);
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " introuvable : " + identifier);
    }
}
