package com.lotorojo.plugandchat.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(DuplicateNameException.class)
    public ProblemDetail handleTenantDuplicateName(DuplicateNameException ex){

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Duplicate Tenant's name");
        problemDetail.setType(URI.create("path-to-documentation"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
}

}
