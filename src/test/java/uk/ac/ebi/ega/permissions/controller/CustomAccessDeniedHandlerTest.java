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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler customAccessDeniedHandler;

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AccessDeniedException exception = new AccessDeniedException("AccessDeniedException");

    SecurityContext securityContext = mock(SecurityContext.class);

    @BeforeEach
    void setup() {
        this.customAccessDeniedHandler = new CustomAccessDeniedHandler();
    }

    @Test
    @DisplayName("CustomAccessDeniedHandler - Handle Exception")
    void handleException() throws ServletException, IOException {

        try (MockedStatic<SecurityContextHolder> contextHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        }

        when(securityContext.getAuthentication()).thenReturn(null);

        this.customAccessDeniedHandler.handle(request, response, exception);
        verify(response, times(1)).sendError(SC_UNAUTHORIZED, "AccessDeniedException");
    }
}