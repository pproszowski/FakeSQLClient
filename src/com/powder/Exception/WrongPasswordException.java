package com.powder.Exception;

public class WrongPasswordException extends Throwable {
    @Override
    public String getMessage() {
        return "Bad password.";
    }
}
