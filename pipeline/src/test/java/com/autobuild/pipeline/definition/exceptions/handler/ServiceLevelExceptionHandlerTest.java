package com.autobuild.pipeline.definition.exceptions.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.autobuild.pipeline.definiton.exceptions.DuplicateEntryException;
import com.autobuild.pipeline.definiton.exceptions.InvalidIdException;
import com.autobuild.pipeline.definiton.exceptions.handler.ServiceLevelExceptionHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;

public class ServiceLevelExceptionHandlerTest {
    private ServiceLevelExceptionHandler globalHandler = new ServiceLevelExceptionHandler();

    @Test
    void testHandleDuplicateEntryException() {
        DuplicateEntryException exception = new DuplicateEntryException("Dummy Exception");

        ErrorResponse errorResponse = globalHandler.handleDuplicateEntryException(exception);

        assertEquals(HttpStatus.CONFLICT, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }

    @Test
    void testHandleInvalidIdException() {
        InvalidIdException exception = new InvalidIdException("Dummy Exception");

        ErrorResponse errorResponse = globalHandler.handleInvalidIdException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }

    @Test
    public void testHandleConstraintViolationException() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);

        ErrorResponse errorResponse = globalHandler.handleConstraintViolationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }

    @Test
    public void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult result = mock(BindingResult.class);

        doReturn(result).when(exception).getBindingResult();

        doReturn(List.of(new FieldError("Dummy","dummy","dummy"))).when(result).getFieldErrors();

        ErrorResponse errorResponse = globalHandler.handleMethodArgumentNotValidException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }

    @Test
    public void testHandleIOException() {
        IOException exception = new IOException("Dummy Exception");

        ErrorResponse errorResponse = globalHandler.handleIOException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }

    @Test
    public void testHandleInvalidFormatException() {
        InvalidFormatException exception = mock(InvalidFormatException.class);
        doReturn("bad format: value").when(exception).getMessage();

        ErrorResponse errorResponse = globalHandler.handleInvalidFormatException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }

    @Test
    public void testHandleIllegalStateException() {
        IllegalStateException exception = new IllegalStateException("illegal state");

        ErrorResponse errorResponse = globalHandler.illegalStateException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }
}
