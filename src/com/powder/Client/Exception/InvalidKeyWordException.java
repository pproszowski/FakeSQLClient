package com.powder.Client.Exception;

public class InvalidKeyWordException extends Throwable {
    @Override
    public String getMessage() {
        return "Invalid key word!";
    }
}
