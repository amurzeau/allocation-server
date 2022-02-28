package org.amurzeau.allocation.rest;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum ErrorType {
    EXISTS("item already exists"),
    NOT_EXISTS("item doesn't exists"),
    MISSING_REQUIRED_FIELD("a required field is missing"),
    INVALID_FIELD("a field has an invalid value"),
    CANT_DELETE_REFERENCED("can't delete item as it is currently referenced"),
    ;

    public final String description;

    private ErrorType(String description) {
        this.description = description;
    }
}
