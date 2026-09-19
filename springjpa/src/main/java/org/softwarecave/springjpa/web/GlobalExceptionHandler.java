package org.softwarecave.springjpa.web;

import org.softwarecave.springjpa.asset.service.NoSuchAssetClassException;
import org.softwarecave.springjpa.asset.service.NoSuchAssetException;
import org.softwarecave.springjpa.common.InvalidSortSpecException;
import org.softwarecave.springjpa.service.DataValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler
    public ResponseEntity<Object> handleDataValidationException(DataValidationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.BAD_REQUEST, null);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNoSuchAssetException(NoSuchAssetException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleNoSuchAssetClassException(NoSuchAssetClassException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleInvalidSortSpecException(InvalidSortSpecException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.BAD_REQUEST, null);
    }
}
