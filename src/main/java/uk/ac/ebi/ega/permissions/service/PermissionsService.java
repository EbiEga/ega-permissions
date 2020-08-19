package uk.ac.ebi.ega.permissions.service;

import uk.ac.ebi.ega.permissions.exception.ServiceException;
import uk.ac.ebi.ega.permissions.exception.SystemException;
import uk.ac.ebi.ega.permissions.model.PassportVisaObject;
import uk.ac.ebi.ega.permissions.model.Visa;

import java.util.List;

public interface PermissionsService {

    boolean accountExist(String accountId);

    List<Visa> getVisas(String accountId);

    PassportVisaObject savePassportVisaObject(String accountId, PassportVisaObject passportVisaObject) throws ServiceException, SystemException;

    int deletePassportVisaObject(String accountId, String value);
}
