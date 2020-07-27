package uk.ac.ebi.ega.permissions.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.PermissionsResponse;
import uk.ac.ebi.ega.permissions.model.Visa;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PermissionsApiDelegateImpl implements PermissionsApiDelegate {

    @Override
    public ResponseEntity<List<PermissionsResponse>> createPermissions(String accountId, List<PassportVisaObject> passportVisaObject) {
        findUserAccount(accountId);

        //TODO: Process requests and determine valid and invalid ones. Default validations (required attributes) are already fulfilled in this stage.
        List<PermissionsResponse> permissionsResponses = new ArrayList<>(passportVisaObject.size());

        for (PassportVisaObject visaObject : passportVisaObject) {
            PermissionsResponse permissionsResponse = new PermissionsResponse();
            permissionsResponse.setGa4ghVisaV1(visaObject);

            //TODO: Replace in the future with some error processing the permissions requests
            if (visaObject.getValue().equalsIgnoreCase("error")) {
                permissionsResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            } else {
                permissionsResponse.setStatus(HttpStatus.OK.value());
            }
            permissionsResponses.add(permissionsResponse);
        }
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(permissionsResponses);
    }

    @Override
    public ResponseEntity<Void> deletePermissions(String accountId, String value) {
        findUserAccount(accountId);

        //TODO: Determine what to do if the value (object to apply the permission) is not found. Should we return an error or just OK
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<List<Visa>> readPermissions(String accountId) {
        findUserAccount(accountId);

        Visa visa = new Visa();
        visa.setSub("EGAW00000015388");
        visa.setIss("https://ega.ebi.ac.uk:8053/ega-openid-connect-server/");
        visa.setExp(1592824514);
        visa.setIat(1592820914);
        visa.setJti("f030c620-993b-49af-a830-4b9af4f379f8");

        visa.setGa4ghVisaV1(createPassportVisaObject());

        return ResponseEntity.ok(Arrays.asList(visa));

    }

    //TODO: This method would return an UserAccount object in the future if required by the caller methods
    private void findUserAccount(String userAccountId) {
        //TODO: If the user is not found, a validation exception is thrown. View ControllerExceptionHandler
        if (userAccountId.equalsIgnoreCase("invalid")) {
            throw new ValidationException("User account invalid or not found");
        }
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
