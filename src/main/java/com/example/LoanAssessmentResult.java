package com.example;

import java.util.List;

public class LoanAssessmentResult {

    private String riskClassification;
    private List<String> reasons;
    private double maxPermissibleLoanAmount;
    private double calculatedDti;

    public LoanAssessmentResult(String riskClassification, List<String> reasons,
                                 double maxPermissibleLoanAmount, double calculatedDti) {
        this.riskClassification = riskClassification;
        this.reasons = reasons;
        this.maxPermissibleLoanAmount = maxPermissibleLoanAmount;
        this.calculatedDti = calculatedDti;
    }

    public String getRiskClassification() {
        return riskClassification;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public double getMaxPermissibleLoanAmount() {
        return maxPermissibleLoanAmount;
    }

    public double getCalculatedDti() {
        return calculatedDti;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Risk Classification: ").append(riskClassification);
        sb.append(String.format("%nMax Permissible Loan Amount: %.2f", maxPermissibleLoanAmount));
        sb.append(String.format("%nCalculated DTI: %.2f", calculatedDti));
        if (!reasons.isEmpty()) {
            sb.append("\nReasons:");
            for (String reason : reasons) {
                sb.append("\n - ").append(reason);
            }
        }
        return sb.toString();
    }
}
