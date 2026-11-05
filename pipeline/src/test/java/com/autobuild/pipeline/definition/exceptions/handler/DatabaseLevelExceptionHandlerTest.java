package com.autobuild.pipeline.definition.exceptions.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;

import com.autobuild.pipeline.definiton.exceptions.handler.DatabaseLevelExceptionHandler;

import jakarta.persistence.EntityNotFoundException;

public class DatabaseLevelExceptionHandlerTest {
    private DatabaseLevelExceptionHandler globalHandler = new DatabaseLevelExceptionHandler();

    @Test
    public void testHandleEntityNotFoundException() {
        EntityNotFoundException exception = new EntityNotFoundException("Dummy Exception");

        ErrorResponse errorResponse = globalHandler.handleEntityNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }

    @Test
    public void testHandleConstraintViolationException() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);
        
        ErrorResponse errorResponse = globalHandler.handleConstraintViolationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        assertNotNull(errorResponse.getBody().getDetail());
    }
}
