package com.lotorojo.plugandchat.core.exception;

public class NonExistingTenantException extends RuntimeException {
    public NonExistingTenantException(String msg) {
        super(msg);
    }
}
