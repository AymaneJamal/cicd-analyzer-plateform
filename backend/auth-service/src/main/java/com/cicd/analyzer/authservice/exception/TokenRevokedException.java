package com.cicd.analyzer.authservice.exception;

public class TokenRevokedException extends RuntimeException {

    public TokenRevokedException() {
        super("Token has been revoked");
    }
}