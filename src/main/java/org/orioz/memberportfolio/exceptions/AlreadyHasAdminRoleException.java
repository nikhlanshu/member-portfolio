package org.orioz.memberportfolio.exceptions;

public class AlreadyHasAdminRoleException extends RuntimeException {
    public AlreadyHasAdminRoleException(String message) {
        super(message);
    }
}
