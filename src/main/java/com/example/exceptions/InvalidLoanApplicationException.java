package com.example.exceptions;

public class InvalidLoanApplicationException extends RuntimeException {

    public InvalidLoanApplicationException(String message) {
        super(message);
    }
}
