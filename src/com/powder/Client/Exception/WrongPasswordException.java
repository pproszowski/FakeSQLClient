package com.powder.Client.Exception;

public class WrongPasswordException extends Throwable {
    @Override
    public String getMessage() {
        return "Bad password.";
    }
}
