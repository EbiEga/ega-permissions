package uk.ac.ebi.ega.permissions.exception;

public class JWTException extends SystemException {

    public JWTException(final String message) {
        super(message);
    }

    public JWTException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
