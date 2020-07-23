package uk.ac.ebi.ega.permissions.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ega.permissions.api.PermissionsApiDelegate;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;

import java.util.Arrays;
import java.util.List;

@Service
public class PermissionsService implements PermissionsApiDelegate {

    @Override
    public ResponseEntity<List<PermissionsResponse>> createPermissions(String accountId, List<PassportVisaObject> passportVisaObject) {
        PermissionsResponse permissionsResponse = new PermissionsResponse();
        permissionsResponse.setGa4ghVisaV1(createPassportVisaObject());
        permissionsResponse.setStatus(201);
        return ResponseEntity.ok(Arrays.asList(permissionsResponse));
    }

    @Override
    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<List<Visa>> readPermissions(String accountId) {

        Visa visa = new Visa();
        visa.setSub("EGAW00000015388");
        visa.setIss("https://ega.ebi.ac.uk:8053/ega-openid-connect-server/");
        visa.setExp(1592824514);
        visa.setIat(1592820914);
        visa.setJti("f030c620-993b-49af-a830-4b9af4f379f8");

        visa.setGa4ghVisaV1(createPassportVisaObject());

        return ResponseEntity.ok(Arrays.asList(visa));


    }

    private PassportVisaObject createPassportVisaObject() {
        PassportVisaObject passportVisaObject = new PassportVisaObject();
        passportVisaObject.setType("ControlledAccessGrants");
        passportVisaObject.setAsserted(1568814383);
        passportVisaObject.setValue("https://ega-archive.org/datasets/EGAD00001002069");
        passportVisaObject.setSource("https://ega-archive.org/dacs/EGAC00001000514");
        passportVisaObject.setBy("dac");
        return passportVisaObject;
    }
}
