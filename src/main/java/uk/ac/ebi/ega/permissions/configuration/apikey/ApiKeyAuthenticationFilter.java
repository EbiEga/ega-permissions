/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ega.permissions.configuration.apikey;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.ega.permissions.service.ApiKeyService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApiKeyAuthenticationFilter implements Filter {

    static final private String AUTH_METHOD = "api-key";

    final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(final ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String apiKey = null;
        try {
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                apiKey = getApiKey((HttpServletRequest) request);
                if (apiKey != null) {
                    String username = apiKeyService.getUserFromToken(apiKey).orElseThrow(() -> new AccessDeniedException("API_KEY Token is invalid"));
                    SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthenticationToken(username, apiKey, AuthorityUtils.NO_AUTHORITIES));
                }
            }
            chain.doFilter(request, response);
        } catch (Exception ex) {
            errorResponse("Error processing API_KEY: " + apiKey, response);
        }
    }

    private String getApiKey(HttpServletRequest httpRequest) {
        String apiKey = null;

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null) {
            authHeader = authHeader.trim();
            if (authHeader.toLowerCase().startsWith(AUTH_METHOD + " ")) {
                apiKey = authHeader.substring(AUTH_METHOD.length()).trim();
            }
        }

        return apiKey;
    }

    private void errorResponse(String message, ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(401);
        httpResponse.getWriter().write(message);
    }
}