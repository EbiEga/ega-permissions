package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;

import java.util.List;
import java.util.Optional;

public interface PermissionsService {

    boolean accountExist(String accountId);

    List<Visa> getVisas(String accountId);

    Optional<PassportVisaObject> savePassportVisaObject(String accountId, PassportVisaObject passportVisaObject);

    int deletePassportVisaObject(String accountId, String value);
}
