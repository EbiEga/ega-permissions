/*
 * Copyright 2021-2021 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.ega.permissions.controller;

import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;


class ControllerExceptionHandlerTest {

    private ControllerExceptionHandler controllerExceptionHandler;
    private ValidationException validationException;
    private ConstraintViolationException constraintViolationException;


    @BeforeEach
    void setup() {
        this.controllerExceptionHandler = new ControllerExceptionHandler();
        this.validationException = new ValidationException("ValidationException");
        this.constraintViolationException = new ConstraintViolationException("ConstraintViolationException", Sets.newHashSet());
    }

    @Test
    void constraintViolationException() {
        ResponseEntity<Object> responseEntity = controllerExceptionHandler.constraintViolationException(constraintViolationException);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody().toString()).contains("ConstraintViolationException");
    }

    @Test
    void validationExceptions() {
        ResponseEntity<Object> responseEntity = controllerExceptionHandler.validationExceptions(validationException);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody().toString()).contains("ValidationException");
    }

}