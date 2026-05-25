package com.lotorojo.plugandchat.core.exception;

import io.jsonwebtoken.JwtException;
import org.apache.tomcat.websocket.AuthenticationException;
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

@ExceptionHandler(NonExistingTenantException.class)
    public ProblemDetail handleNonExistingTenantException(NonExistingTenantException ex){

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setType(URI.create("path-to-domunetation"));
    problemDetail.setTitle("Non Existing Tenant");
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
}

@ExceptionHandler(io.jsonwebtoken.JwtException.class)
public ProblemDetail handleJwtException(JwtException ex){
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Token Invalido o Expirado");
    problemDetail.setTitle("Authentication Error");
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
}

@ExceptionHandler(IllegalArgumentException.class)
public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex){
    String detail = ex.getMessage().contains("UUID") ? "Tenant ID invalido" : ex.getMessage();

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problemDetail.setTitle("Error de Formato");
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
}

@ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problemDetail.setTitle("Authentication Error");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
}

@ExceptionHandler(DuplicateAgentException.class)
    public ProblemDetail handleDuplicateAgentException(DuplicateAgentException ex){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Duplicate Agent");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
}

}

