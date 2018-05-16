package com.powder.Client.Exception;

public class UnexpectedResponseException extends Throwable {
    @Override
    public String getMessage() {
        return "Unexpected response from server has been received";
    }
}
