package com.example.exceptions;

public class InvalidCreditAssessmentException extends RuntimeException {

    public InvalidCreditAssessmentException(String message) {
        super(message);
    }
}
