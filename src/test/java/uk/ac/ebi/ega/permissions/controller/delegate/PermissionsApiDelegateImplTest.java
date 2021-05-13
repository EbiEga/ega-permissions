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

package uk.ac.ebi.ega.permissions.controller.delegate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.ac.ebi.ega.permissions.controller.RequestHandler;
import uk.ac.ebi.ega.permissions.model.Format;
import uk.ac.ebi.ega.permissions.model.PermissionsResponses;
import uk.ac.ebi.ega.permissions.model.Visas;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionsApiDelegateImplTest {

    private PermissionsApiDelegateImpl permissionsApiDelegate;
    private RequestHandler requestHandler = mock(RequestHandler.class);

    @BeforeEach
    void setup() {
        this.permissionsApiDelegate = new PermissionsApiDelegateImpl(requestHandler);
    }

    @Test
    void readPermissions() {
        ResponseEntity<Visas> requestHandlerResponse = ResponseEntity.ok(new Visas());
        when(requestHandler.getPermissionsForUser("accountId", Format.PLAIN)).thenReturn(requestHandlerResponse);
        ResponseEntity<Visas> response = this.permissionsApiDelegate.readPermissions("accountId", null, Format.PLAIN);
        assertThat(response).isEqualTo(requestHandlerResponse);
    }

    @Test
    void readPermissions_noAccountId() {
        assertThatThrownBy(() -> this.permissionsApiDelegate.readPermissions(null, null, Format.PLAIN))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void createPermissions() {
        ResponseEntity<PermissionsResponses> requestHandlerResponse = ResponseEntity.ok(new PermissionsResponses());
        when(requestHandler.createPermissions(eq("accountId"), anyList(), eq(Format.PLAIN))).thenReturn(requestHandlerResponse);
        ResponseEntity<PermissionsResponses> response = this.permissionsApiDelegate.createPermissions(new ArrayList<>(), null, "accountId", Format.PLAIN);
        assertThat(response).isEqualTo(requestHandlerResponse);
    }

    @Test
    void deletePermissions() {
        ResponseEntity<Void> requestHandlerResponse =  ResponseEntity.status(HttpStatus.OK).build();
        when(requestHandler.getAccountIdForElixirId(any())).thenReturn("accountId");
        when(requestHandler.deletePermissions(eq("accountId"), anyList())).thenReturn(requestHandlerResponse);
        ResponseEntity<Void> response = this.permissionsApiDelegate.deletePermissions(new ArrayList<>(), "accountId", null);
        assertThat(response).isEqualTo(requestHandlerResponse);
    }
}