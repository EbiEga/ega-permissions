/*
 *
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.permissions.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        Map<String, String> responseBody = getResponseBody(HttpStatus.BAD_REQUEST, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(responseBody);
    }


    @ExceptionHandler(value = {ValidationException.class})
    ResponseEntity<Object> validationExceptions(ValidationException ex) {
        Map<String, String> responseBody = getResponseBody(HttpStatus.NOT_FOUND, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(responseBody);
    }

    private Map<String, String> getResponseBody(HttpStatus status, Exception ex) {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("timestamp", String.valueOf(new Date()));
        responseBody.put("status", String.valueOf(status.value()));
        responseBody.put("message", ex.getMessage());
        return responseBody;
    }
}
