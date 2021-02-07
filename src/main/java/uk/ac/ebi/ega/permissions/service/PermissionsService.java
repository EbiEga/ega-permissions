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
package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.AccountAccess;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;
import uk.ac.ebi.ega.permissions.persistence.entities.AccountElixirId;
import uk.ac.ebi.ega.permissions.persistence.entities.Account;

import java.util.List;
import java.util.Optional;

public interface PermissionsService {

    boolean accountExist(String accountId);

    Optional<AccountElixirId> getAccountIdForElixirId(String elixirId);

    Optional<Account> getAccountByEmail(String email);

    List<Visa> getVisas(String accountId);

    PassportVisaObject savePassportVisaObject(String accountId, PassportVisaObject passportVisaObject) throws ServiceException, SystemException;

    void deletePassportVisaObject(String accountId, List<String> values);

    List<AccountAccess> getGrantedAccountsForDataset(String datasetId);
}
