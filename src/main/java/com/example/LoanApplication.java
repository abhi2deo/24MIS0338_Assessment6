package com.example;

import com.example.exceptions.InvalidLoanApplicationException;

public class LoanApplication {

    private String applicationId;
    private Customer customer;
    private double requestedLoanAmount;
    private int requestedTenureMonths;

    public LoanApplication(String applicationId, Customer customer,
                            double requestedLoanAmount, int requestedTenureMonths) {

        if (applicationId == null || applicationId.trim().isEmpty()) {
            throw new InvalidLoanApplicationException("Application ID cannot be null or empty");
        }
        if (customer == null) {
            throw new InvalidLoanApplicationException("Customer cannot be null");
        }
        if (requestedLoanAmount <= 0) {
            throw new InvalidLoanApplicationException("Requested loan amount must be greater than zero");
        }
        if (requestedTenureMonths <= 0) {
            throw new InvalidLoanApplicationException("Requested tenure must be greater than zero months");
        }

        this.applicationId = applicationId;
        this.customer = customer;
        this.requestedLoanAmount = requestedLoanAmount;
        this.requestedTenureMonths = requestedTenureMonths;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getRequestedLoanAmount() {
        return requestedLoanAmount;
    }

    public int getRequestedTenureMonths() {
        return requestedTenureMonths;
    }

    /**
     * Simplified monthly installment for the requested loan (no interest applied).
     */
    public double calculateMonthlyInstallment() {
        return requestedLoanAmount / requestedTenureMonths;
    }
}
