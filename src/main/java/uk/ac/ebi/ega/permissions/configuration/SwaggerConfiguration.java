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
package uk.ac.ebi.ega.permissions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static io.swagger.models.auth.In.HEADER;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.config.Elements.JWT;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@EnableSwagger2
@Configuration
public class SwaggerConfiguration {
    // @formatter:off
    @Bean
    public Docket buildDocket() {
        return new Docket(SWAGGER_2)
            .securitySchemes(singletonList(apiKey()))
            .securityContexts(singletonList(
                SecurityContext.builder()
                    .securityReferences(
                        singletonList(SecurityReference.builder()
                            .reference(JWT)
                            .scopes(new AuthorizationScope[0])
                            .build()))
                    .build()))
            .select()
            .apis(basePackage("uk.ac.ebi.ega.permissions.controller"))
            .build()
            .groupName("Permissions-API-Swagger");
    }

    private ApiKey apiKey() {
        return new ApiKey(
                JWT,
                AUTHORIZATION,
                HEADER.name()
        );
    }
    // @formatter:on
}
