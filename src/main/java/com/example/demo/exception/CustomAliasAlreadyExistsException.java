package com.example.demo.exception;

public class CustomAliasAlreadyExistsException extends RuntimeException {
    public CustomAliasAlreadyExistsException(String alias) {
        super("Custom alias '" + alias + "' is already taken");
    }
}
