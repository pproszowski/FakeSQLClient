package com.powder.Client.Exception;

public class UnknownTypeException extends Throwable {
    @Override
    public String getMessage() {
        return "Error: unknown type of variable";
    }
}
