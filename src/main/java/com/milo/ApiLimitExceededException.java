package com.milo;

public class ApiLimitExceededException extends RuntimeException {
    public ApiLimitExceededException(String message) {
        super(message);
    }
}
