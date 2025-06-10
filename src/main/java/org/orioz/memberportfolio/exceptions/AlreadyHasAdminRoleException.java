package org.orioz.memberportfolio.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyHasAdminRoleException extends RuntimeException {
    public AlreadyHasAdminRoleException(String message) {
        super(message);
    }
}
