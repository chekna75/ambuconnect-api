package fr.ambuconnect.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class NotFoundException extends WebApplicationException {

    public NotFoundException(String message) {
        super(message, Response.Status.NOT_FOUND);
    }
} 