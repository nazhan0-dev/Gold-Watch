package com.goldwatch.goldtracker.source;

/** Raised when a source is unreachable or its payload is missing, malformed or implausible. */
public class PriceSourceException extends RuntimeException {

    public PriceSourceException(String message) {
        super(message);
    }

    public PriceSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
