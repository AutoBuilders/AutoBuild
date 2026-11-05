package com.autobuild.pipeline.definiton.exceptions.handler;

import org.hibernate.exception.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global handler for handling database level exceptions.
 * @author Suvabrata Chowdhury
 */

@Slf4j
@RestControllerAdvice
public class DatabaseLevelExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder(exception, HttpStatus.NOT_FOUND, exception.getMessage())
                .property("errorCategory", "Client Error")
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        log.error(exception.getMessage(), exception);

        String errorMessage = exception.getConstraintName() 
                                + "," + exception.getMessage() 
                                + "," + exception.getErrorMessage();
        
        return ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, errorMessage)
                .property("errorCategory", "Client Error").build();
    }
}
