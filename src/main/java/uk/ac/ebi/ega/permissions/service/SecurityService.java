package uk.ac.ebi.ega.permissions.service;

import java.util.Optional;

public interface SecurityService {

    Optional<String> getCurrentUser();
}
