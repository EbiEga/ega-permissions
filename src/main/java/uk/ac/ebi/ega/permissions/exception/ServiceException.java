package uk.ac.ebi.ega.permissions.exception;

import org.springframework.http.HttpStatus;

public class ServiceException extends Exception {

    private HttpStatus httpStatus;

    public ServiceException(String message, Throwable cause, HttpStatus httpStatus) {
        this(message, cause);
        this.httpStatus = httpStatus;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
