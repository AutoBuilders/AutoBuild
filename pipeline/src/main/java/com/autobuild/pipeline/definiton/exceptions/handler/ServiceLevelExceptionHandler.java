package com.autobuild.pipeline.definiton.exceptions.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Global handler for any/all exceptions coming from Pipeline.
 * 
 * @author Suvabrata Chowdhury
 */

@Slf4j
@RestControllerAdvice
public class ServiceLevelExceptionHandler {

    @ExceptionHandler(DuplicateEntryException.class)
    public ErrorResponse handleDuplicateEntryException(DuplicateEntryException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage())
                .property("errorCategory", "Client Error")
                .build();
    }

    @ExceptionHandler(InvalidIdException.class)
    public ErrorResponse handleInvalidIdException(InvalidIdException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, exception.getMessage())
                .property("errorCategory", "Client Error")
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        log.error(exception.getMessage(), exception);

        List<String> errorMessages = exception.getConstraintViolations()
                .stream().map(
                        constraintViolation -> constraintViolation
                                .getPropertyPath()
                                + " " +
                                constraintViolation.getMessage())
                .toList();

        return ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, errorMessages.toString())
                .property("errorCategory", "Client Error").build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage(), exception);

        List<String> errorMessages = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(
                        fieldError -> fieldError.getField()
                                + ": "
                                + fieldError.getDefaultMessage())
                .toList();

        return ErrorResponse.builder(exception, HttpStatus.BAD_REQUEST, errorMessages.toString())
                .property("errorCategory", "Client Error").build();
    }

    @ExceptionHandler(IOException.class)
    public ErrorResponse handleIOException(IOException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse.builder(exception, HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()).build();
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ErrorResponse handleInvalidFormatException(InvalidFormatException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse
                .builder(exception, HttpStatus.BAD_REQUEST, Arrays.asList(exception.getMessage().split(":")).get(1))
                .build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ErrorResponse illegalStateException(IllegalStateException exception) {
        log.error(exception.getMessage(), exception);

        return ErrorResponse
                .builder(exception, HttpStatus.BAD_REQUEST, exception.getMessage())
                .build();
    }
}
