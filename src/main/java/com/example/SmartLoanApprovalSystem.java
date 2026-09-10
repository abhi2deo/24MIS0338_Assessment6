package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SmartLoanApprovalSystem {

    public static final int MIN_AGE = 21;
    public static final double MIN_MONTHLY_INCOME = 20000.0;
    public static final double LOAN_TO_INCOME_MULTIPLIER = 20.0;
    public static final int MIN_CREDIT_SCORE = 650;
    public static final int HIGH_CREDIT_SCORE = 750;
    public static final double LOW_DTI_THRESHOLD = 0.35;
    public static final double MAX_ACCEPTABLE_DTI = 0.50;

    public static double calculateMaxPermissibleLoanAmount(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        return customer.getMonthlyIncome() * LOAN_TO_INCOME_MULTIPLIER;
    }

    public static double calculateDti(LoanApplication application) {
        if (application == null) {
            throw new IllegalArgumentException("Loan application cannot be null");
        }
        Customer customer = application.getCustomer();
        double newInstallment = application.calculateMonthlyInstallment();
        double totalMonthlyDebt = customer.getExistingMonthlyDebtPayments() + newInstallment;

        if (customer.getMonthlyIncome() == 0) {
            return Double.MAX_VALUE;
        }
        return totalMonthlyDebt / customer.getMonthlyIncome();
    }

    public static LoanAssessmentResult evaluate(LoanApplication application, CreditAssessment creditAssessment) {

        if (application == null) {
            throw new IllegalArgumentException("Loan application cannot be null");
        }
        if (creditAssessment == null) {
            throw new IllegalArgumentException("Credit assessment cannot be null");
        }

        Customer customer = application.getCustomer();
        List<String> reasons = new ArrayList<>();

        double maxPermissibleLoanAmount = calculateMaxPermissibleLoanAmount(customer);
        double dti = calculateDti(application);
        int creditScore = creditAssessment.getCreditScore();

        boolean criticalFailure = false;

        if (customer.getAge() < MIN_AGE) {
            reasons.add("Customer is under the minimum age requirement of " + MIN_AGE);
            criticalFailure = true;
        }

        if (customer.getGovernmentId() == null || customer.getGovernmentId().trim().isEmpty()) {
            reasons.add("Customer does not have a valid government-issued identification number");
            criticalFailure = true;
        }

        if (customer.getMonthlyIncome() < MIN_MONTHLY_INCOME) {
            reasons.add("Monthly income is below the minimum threshold of " + MIN_MONTHLY_INCOME);
            criticalFailure = true;
        }

        if (application.getRequestedLoanAmount() > maxPermissibleLoanAmount) {
            reasons.add(String.format(
                    "Requested loan amount (%.2f) exceeds the maximum permissible amount (%.2f) for this income",
                    application.getRequestedLoanAmount(), maxPermissibleLoanAmount));
            criticalFailure = true;
        }

        if (creditScore < MIN_CREDIT_SCORE) {
            reasons.add("Credit score (" + creditScore + ") is below the minimum requirement of " + MIN_CREDIT_SCORE);
            criticalFailure = true;
        }

        if (dti > MAX_ACCEPTABLE_DTI) {
            reasons.add(String.format(
                    "Debt-to-income ratio (%.2f) exceeds the maximum acceptable ratio of %.2f", dti, MAX_ACCEPTABLE_DTI));
            criticalFailure = true;
        }

        String riskClassification;

        if (criticalFailure) {
            riskClassification = "Rejected - High Risk";
        } else if (creditScore >= HIGH_CREDIT_SCORE && dti <= LOW_DTI_THRESHOLD) {
            riskClassification = "Low Risk";
        } else {
            riskClassification = "Medium Risk";
        }

        return new LoanAssessmentResult(riskClassification, reasons, maxPermissibleLoanAmount, dti);
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter number of loan applications: ");
            int n = Integer.parseInt(scanner.nextLine().trim());

            for (int i = 1; i <= n; i++) {

                System.out.println("\nEnter customer details for application " + i);

                System.out.print("Customer ID: ");
                String customerId = scanner.nextLine();

                System.out.print("Name: ");
                String name = scanner.nextLine();

                System.out.print("Age: ");
                int age = Integer.parseInt(scanner.nextLine().trim());

                System.out.print("Government ID: ");
                String governmentId = scanner.nextLine();

                System.out.print("Monthly Income: ");
                double monthlyIncome = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Existing Monthly Debt Payments: ");
                double existingDebt = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Application ID: ");
                String applicationId = scanner.nextLine();

                System.out.print("Requested Loan Amount: ");
                double loanAmount = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Requested Tenure (months): ");
                int tenure = Integer.parseInt(scanner.nextLine().trim());

                System.out.print("Credit Assessment ID: ");
                String assessmentId = scanner.nextLine();

                System.out.print("Credit Score (300-900): ");
                int creditScore = Integer.parseInt(scanner.nextLine().trim());

                try {
                    Customer customer = new Customer(customerId, name, age, governmentId, monthlyIncome, existingDebt);
                    LoanApplication application = new LoanApplication(applicationId, customer, loanAmount, tenure);
                    CreditAssessment creditAssessment = new CreditAssessment(assessmentId, creditScore);

                    LoanAssessmentResult result = evaluate(application, creditAssessment);

                    System.out.println("\n----- Result for " + customer.getName() + " -----");
                    System.out.println(result);

                } catch (RuntimeException e) {
                    System.out.println("Invalid input for application " + i + ": " + e.getMessage());
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
