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
import uk.ac.ebi.ega.permissions.service.SecurityService;

import java.util.Optional;

@Configuration
public class SecurityUtilsConfig {

    @Bean
    public SecurityService securityService() {
        return new SecurityService() {
            @Override
            public Optional<String> getCurrentUser() {
                return Optional.of("test@ebi.ac.uk");
            }
        };
    }
}
