package com.lotorojo.plugandchat.core.exception;

public class NonExistingRoomException extends RuntimeException {
    public NonExistingRoomException(String msg) {
        super(msg);
    }
}
