package com.example;

import com.example.exceptions.InvalidCreditAssessmentException;

public class CreditAssessment {

    public static final int MIN_VALID_SCORE = 300;
    public static final int MAX_VALID_SCORE = 900;

    private String assessmentId;
    private int creditScore;

    public CreditAssessment(String assessmentId, int creditScore) {

        if (assessmentId == null || assessmentId.trim().isEmpty()) {
            throw new InvalidCreditAssessmentException("Assessment ID cannot be null or empty");
        }
        if (creditScore < MIN_VALID_SCORE || creditScore > MAX_VALID_SCORE) {
            throw new InvalidCreditAssessmentException(
                    "Credit score must be between " + MIN_VALID_SCORE + " and " + MAX_VALID_SCORE);
        }

        this.assessmentId = assessmentId;
        this.creditScore = creditScore;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public int getCreditScore() {
        return creditScore;
    }
}
