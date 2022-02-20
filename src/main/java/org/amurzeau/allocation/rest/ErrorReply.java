package org.amurzeau.allocation.rest;

public class ErrorReply {
    public ErrorType error;
    public String description;

    public static ErrorReply create(ErrorType error, String format, Object... remainingArgs) {
        ErrorReply result = new ErrorReply();
        result.error = error;
        result.description = String.format(format, remainingArgs);

        return result;
    }
}
