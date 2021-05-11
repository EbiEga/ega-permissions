package uk.ac.ebi.ega.permissions.exception;

public class SystemException extends RuntimeException {

    public SystemException(final String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
