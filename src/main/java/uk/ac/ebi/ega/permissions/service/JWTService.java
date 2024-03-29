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

import com.nimbusds.jwt.SignedJWT;
import uk.ac.ebi.ega.ga4gh.jwt.passport.exception.JWTException;
import uk.ac.ebi.ega.permissions.model.Visa;

public interface JWTService {

    SignedJWT createJWT(Visa jwtData);

    void signJWT(SignedJWT signedJWT) throws JWTException;

    boolean isValidSignature(SignedJWT signedJWT);

}
