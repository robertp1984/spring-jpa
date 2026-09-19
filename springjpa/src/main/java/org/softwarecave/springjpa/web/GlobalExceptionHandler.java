package org.softwarecave.springjpa.web;

import org.softwarecave.springjpa.service.DataValidationException;
import org.softwarecave.springjpa.service.NoDataException;
import org.softwarecave.springjpa.service.RequestValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({DataValidationException.class})
    public ResponseEntity<Object> handleBadRequest(DataValidationException ex) {
        return problemResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({RequestValidationException.class})
    public ResponseEntity<Object> handleBadRequest(RequestValidationException ex) {
        return problemResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({NoDataException.class})
    public ResponseEntity<Object> handleNotFound(NoDataException ex) {
        return problemResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<Object> problemResponse(HttpStatus status, String message) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        return createResponseEntity(pd, null, status, null);
    }
}
