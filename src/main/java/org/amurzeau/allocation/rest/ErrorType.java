package org.amurzeau.allocation.rest;

public enum ErrorType {
    EXISTS("item already exists"),
    NOT_EXISTS("item doesn't exists"),
    MISSING_REQUIRED_FIELD("a required field is missing"),
    INVALID_FIELD("a field has an invalid value"),
    ;

    public final String description;

    private ErrorType(String description) {
        this.description = description;
    }
}
