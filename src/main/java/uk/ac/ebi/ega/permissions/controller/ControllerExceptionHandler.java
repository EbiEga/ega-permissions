package uk.ac.ebi.ega.permissions.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice(annotations = {Controller.class, Configuration.class})
public class ControllerExceptionHandler {

    //Overrides the default 500 Error response for requests not meeting the API Definition
    @ExceptionHandler(value = {ConstraintViolationException.class})
    ResponseEntity<Object> constraintViolationException(ConstraintViolationException ex) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("timestamp", String.valueOf(new Date()));
        responseBody.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        responseBody.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(responseBody);
    }


    @ExceptionHandler(value = {ValidationException.class})
    ResponseEntity<Object> validationExceptions(ValidationException ex) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("timestamp", String.valueOf(new Date()));
        responseBody.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        responseBody.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(responseBody);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    ResponseEntity<Object> customSecurityException(AccessDeniedException ex) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("timestamp", String.valueOf(new Date()));
        responseBody.put("status", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        responseBody.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(responseBody);
    }
}
