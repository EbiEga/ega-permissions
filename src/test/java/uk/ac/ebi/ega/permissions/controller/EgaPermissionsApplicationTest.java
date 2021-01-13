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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import uk.ac.ebi.ega.permissions.service.JWTService;
import uk.ac.ebi.ega.permissions.service.JWTServiceImpl;
import uk.ac.ebi.ega.permissions.service.PermissionsService;
import uk.ac.ebi.ega.permissions.service.PermissionsServiceImpl;

import static org.mockito.Mockito.mock;

//@SpringBootApplication(exclude = {
//        DataSourceAutoConfiguration.class,
//        DataSourceTransactionManagerAutoConfiguration.class,
//        HibernateJpaAutoConfiguration.class
//})
public class EgaPermissionsApplicationTest {
    public static void main(final String[] args) {
        SpringApplication.run(EgaPermissionsApplicationTest.class, args);
    }

    @Bean
    public JWTService initJWTService() {
        return mock(JWTServiceImpl.class);
    }

    @Bean
    public PermissionsService initPermissionsService() {
        return mock(PermissionsServiceImpl.class);
    }

    @Bean
    public RequestHandler initRequestHandler() {
        return mock(RequestHandler.class);
    }
}
