package org.orioz.memberportfolio.exceptions;

public class MemberNotInPendingStatusException extends RuntimeException{
    public MemberNotInPendingStatusException(String message) {
        super(message);
    }
}
