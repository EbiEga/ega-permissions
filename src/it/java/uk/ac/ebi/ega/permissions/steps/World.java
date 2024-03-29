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
package uk.ac.ebi.ega.permissions.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.repository.AccessGroupRepository;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.repository.AccountRepository;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.repository.ApiKeyRepository;
import uk.ac.ebi.ega.ga4gh.jwt.passport.persistence.repository.PassportClaimRepository;
import uk.ac.ebi.ega.permissions.dto.TokenParams;
import uk.ac.ebi.ega.permissions.helpers.DatasetHelper;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class World {
    String bearerAccessToken;
    ResponseEntity response;

    @Autowired
    ApiKeyRepository apiKeyRepository;

    @Autowired
    PassportClaimRepository passportClaimRepository;

    @Autowired
    AccessGroupRepository userGroupRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TokenParams tokenParams;

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Autowired
    DatasetHelper datasetHelper;

    public void cleanApiKeys() {
        this.apiKeyRepository.deleteAll();
    }

    public void cleanPermissions() {
        this.accountRepository.deleteAll();
        this.userGroupRepository.deleteAll();
        this.passportClaimRepository.deleteAll();
        this.datasetHelper.removeAll();
    }

    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(this.bearerAccessToken);
        return headers;
    }
}
