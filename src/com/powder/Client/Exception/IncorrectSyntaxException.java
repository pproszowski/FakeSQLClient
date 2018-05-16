package com.powder.Client.Exception;

public class IncorrectSyntaxException extends Throwable {
    @Override
    public String getMessage() {
        return "Incorrect syntax!";
    }
}
