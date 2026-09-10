package com.example;

import com.example.exceptions.InvalidCustomerDataException;

public class Customer {

    private String customerId;
    private String name;
    private int age;
    private String governmentId;
    private double monthlyIncome;
    private double existingMonthlyDebtPayments;

    public Customer(String customerId, String name, int age, String governmentId,
                     double monthlyIncome, double existingMonthlyDebtPayments) {

        if (customerId == null || customerId.trim().isEmpty()) {
            throw new InvalidCustomerDataException("Customer ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidCustomerDataException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new InvalidCustomerDataException("Age cannot be negative");
        }
        if (governmentId == null || governmentId.trim().isEmpty()) {
            throw new InvalidCustomerDataException("Government-issued ID cannot be null or empty");
        }
        if (monthlyIncome < 0) {
            throw new InvalidCustomerDataException("Monthly income cannot be negative");
        }
        if (existingMonthlyDebtPayments < 0) {
            throw new InvalidCustomerDataException("Existing monthly debt payments cannot be negative");
        }

        this.customerId = customerId;
        this.name = name;
        this.age = age;
        this.governmentId = governmentId;
        this.monthlyIncome = monthlyIncome;
        this.existingMonthlyDebtPayments = existingMonthlyDebtPayments;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGovernmentId() {
        return governmentId;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getExistingMonthlyDebtPayments() {
        return existingMonthlyDebtPayments;
    }
}
