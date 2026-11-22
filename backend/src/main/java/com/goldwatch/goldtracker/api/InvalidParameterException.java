package com.goldwatch.goldtracker.api;

/** A request parameter that is well-formed but not acceptable (answered with HTTP 400). */
public class InvalidParameterException extends RuntimeException {

    private final String parameter;

    public InvalidParameterException(String parameter, String message) {
        super(message);
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
