package fr.ambuconnect.common.exceptions;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class BadRequestException extends WebApplicationException {

    public BadRequestException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
} 