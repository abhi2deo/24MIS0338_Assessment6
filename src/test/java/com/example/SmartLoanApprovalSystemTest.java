package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.exceptions.InvalidCreditAssessmentException;
import com.example.exceptions.InvalidCustomerDataException;
import com.example.exceptions.InvalidLoanApplicationException;

import org.junit.Test;

public class SmartLoanApprovalSystemTest {

    // ---------- Helper factory for a clean baseline customer/application ----------

    private Customer baselineCustomer() {
        // income 50000, existing debt 5000
        return new Customer("CUST001", "Harsh", 30, "GOVID12345", 50000, 5000);
    }

    private LoanApplication baselineApplication(Customer customer, double loanAmount, int tenure) {
        return new LoanApplication("APP001", customer, loanAmount, tenure);
    }

    // ---------- Normal / Low Risk scenarios ----------

    @Test
    public void testLowRiskApproval() {
        Customer customer = baselineCustomer(); // income 50000, existing debt 5000
        // loan 60000 / 12 months = 5000 installment -> total debt 10000 -> DTI = 0.20 (<=0.35)
        LoanApplication application = baselineApplication(customer, 60000, 12);
        CreditAssessment creditAssessment = new CreditAssessment("CA001", 780); // >=750

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Low Risk", result.getRiskClassification());
        assertTrue(result.getReasons().isEmpty());
    }

    // ---------- Medium Risk scenarios ----------

    @Test
    public void testMediumRiskDueToModerateCreditScore() {
        Customer customer = baselineCustomer();
        LoanApplication application = baselineApplication(customer, 60000, 12); // DTI 0.20
        CreditAssessment creditAssessment = new CreditAssessment("CA002", 700); // between 650-750

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Medium Risk", result.getRiskClassification());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    public void testMediumRiskDueToModerateDti() {
        Customer customer = new Customer("CUST002", "Priya", 28, "GOVID222", 50000, 15000);
        // loan 60000/12 = 5000 installment, total debt 20000, DTI = 0.40 (between 0.35 and 0.50)
        LoanApplication application = baselineApplication(customer, 60000, 12);
        CreditAssessment creditAssessment = new CreditAssessment("CA003", 800); // high score

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Medium Risk", result.getRiskClassification());
    }

    // ---------- Boundary scenarios ----------

    @Test
    public void testAgeExactlyMinimumIsNotACriticalFailure() {
        Customer customer = new Customer("CUST003", "Vijay", 21, "GOVID333", 50000, 5000);
        LoanApplication application = baselineApplication(customer, 60000, 12);
        CreditAssessment creditAssessment = new CreditAssessment("CA004", 780);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Low Risk", result.getRiskClassification());
    }

    @Test
    public void testAgeJustBelowMinimumIsRejected() {
        Customer customer = new Customer("CUST004", "Rahul", 20, "GOVID444", 50000, 5000);
        LoanApplication application = baselineApplication(customer, 60000, 12);
        CreditAssessment creditAssessment = new CreditAssessment("CA005", 780);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Rejected - High Risk", result.getRiskClassification());
        assertTrue(result.getReasons().get(0).contains("minimum age"));
    }

    @Test
    public void testCreditScoreExactlyAtMinimumIsAccepted() {
        Customer customer = baselineCustomer();
        LoanApplication application = baselineApplication(customer, 60000, 12);
        CreditAssessment creditAssessment = new CreditAssessment("CA006", 650); // exactly minimum

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Medium Risk", result.getRiskClassification()); // meets min but not "high" score
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    public void testCreditScoreJustBelowMinimumIsRejected() {
        Customer customer = baselineCustomer();
        LoanApplication application = baselineApplication(customer, 60000, 12);
        CreditAssessment creditAssessment = new CreditAssessment("CA007", 649);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Rejected - High Risk", result.getRiskClassification());
        assertTrue(result.getReasons().get(0).contains("Credit score"));
    }

    @Test
    public void testCreditScoreExactlyAtHighThresholdIsLowRisk() {
        Customer customer = baselineCustomer();
        LoanApplication application = baselineApplication(customer, 60000, 12); // DTI 0.20
        CreditAssessment creditAssessment = new CreditAssessment("CA008", 750); // exactly high threshold

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Low Risk", result.getRiskClassification());
    }

    @Test
    public void testDtiExactlyAtLowThresholdIsLowRisk() {
        // income 100000, existing debt 0, loan installment must equal 35000 to hit DTI exactly 0.35
        Customer customer = new Customer("CUST005", "Meena", 26, "GOVID555", 100000, 0);
        LoanApplication application = baselineApplication(customer, 35000, 1); // installment = 35000
        CreditAssessment creditAssessment = new CreditAssessment("CA009", 780);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals(0.35, result.getCalculatedDti(), 0.0001);
        assertEquals("Low Risk", result.getRiskClassification());
    }

    @Test
    public void testDtiExactlyAtMaxAcceptableIsMediumNotRejected() {
        // income 100000, existing debt 0, installment = 50000 -> DTI exactly 0.50
        Customer customer = new Customer("CUST006", "Arun", 27, "GOVID666", 100000, 0);
        LoanApplication application = baselineApplication(customer, 50000, 1);
        CreditAssessment creditAssessment = new CreditAssessment("CA010", 780);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals(0.50, result.getCalculatedDti(), 0.0001);
        assertEquals("Medium Risk", result.getRiskClassification()); // not > max, so not rejected
    }

    @Test
    public void testDtiJustAboveMaxIsRejected() {
        // income 100000, existing debt 0, installment = 50001 -> DTI slightly above 0.50
        Customer customer = new Customer("CUST007", "Sara", 29, "GOVID777", 100000, 0);
        LoanApplication application = baselineApplication(customer, 50001, 1);
        CreditAssessment creditAssessment = new CreditAssessment("CA011", 780);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Rejected - High Risk", result.getRiskClassification());
        assertTrue(result.getReasons().get(0).contains("Debt-to-income"));
    }

    @Test
    public void testLoanAmountExactlyAtMaxPermissibleIsAccepted() {
        Customer customer = baselineCustomer(); // income 50000 -> max permissible = 1,000,000
        double maxPermissible = SmartLoanApprovalSystem.calculateMaxPermissibleLoanAmount(customer);
        LoanApplication application = baselineApplication(customer, maxPermissible, 240);
        CreditAssessment creditAssessment = new CreditAssessment("CA012", 780);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertTrue(result.getReasons().isEmpty());
        assertEquals(1000000.0, result.getMaxPermissibleLoanAmount(), 0.0001);
    }

    @Test
    public void testLoanAmountJustAboveMaxPermissibleIsRejected() {
        Customer customer = baselineCustomer();
        double maxPermissible = SmartLoanApprovalSystem.calculateMaxPermissibleLoanAmount(customer);
        LoanApplication application = baselineApplication(customer, maxPermissible + 1, 240);
        CreditAssessment creditAssessment = new CreditAssessment("CA013", 780);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Rejected - High Risk", result.getRiskClassification());
        assertTrue(result.getReasons().get(0).contains("exceeds the maximum permissible amount"));
    }

    // ---------- Multiple-failure scenario ----------

    @Test
    public void testMultipleCriticalFailuresAreAllReported() {
        // underage, low income, low credit score, and loan amount exceeding limit
        Customer customer = new Customer("CUST008", "Tom", 19, "GOVID888", 10000, 0);
        LoanApplication application = baselineApplication(customer, 500000, 12);
        CreditAssessment creditAssessment = new CreditAssessment("CA014", 500);

        LoanAssessmentResult result = SmartLoanApprovalSystem.evaluate(application, creditAssessment);

        assertEquals("Rejected - High Risk", result.getRiskClassification());
        // age, income, loan-amount-exceeds-limit, credit score should all be present (DTI may also fail)
        assertTrue(result.getReasons().size() >= 4);
    }

    // ---------- Existing loan obligations considered in DTI ----------

    @Test
    public void testExistingDebtIsFactoredIntoDti() {
        Customer customerWithNoDebt = new Customer("CUST009", "Neha", 30, "GOVID999", 50000, 0);
        Customer customerWithDebt = new Customer("CUST010", "Neha2", 30, "GOVID000", 50000, 10000);

        LoanApplication applicationNoDebt = baselineApplication(customerWithNoDebt, 60000, 12);
        LoanApplication applicationWithDebt = baselineApplication(customerWithDebt, 60000, 12);

        CreditAssessment creditAssessment = new CreditAssessment("CA015", 780);

        LoanAssessmentResult resultNoDebt = SmartLoanApprovalSystem.evaluate(applicationNoDebt, creditAssessment);
        LoanAssessmentResult resultWithDebt = SmartLoanApprovalSystem.evaluate(applicationWithDebt, creditAssessment);

        assertTrue(resultWithDebt.getCalculatedDti() > resultNoDebt.getCalculatedDti());
    }

    // ---------- Invalid input / custom exception scenarios ----------

    @Test
    public void testNegativeAgeThrowsInvalidCustomerDataException() {
        try {
            new Customer("CUST011", "Zara", -5, "GOVID111", 50000, 0);
            fail("Expected InvalidCustomerDataException for negative age");
        } catch (InvalidCustomerDataException e) {
            assertTrue(e.getMessage().contains("Age"));
        }
    }

    @Test
    public void testEmptyGovernmentIdThrowsInvalidCustomerDataException() {
        try {
            new Customer("CUST012", "Dev", 25, "", 50000, 0);
            fail("Expected InvalidCustomerDataException for empty government ID");
        } catch (InvalidCustomerDataException e) {
            assertTrue(e.getMessage().contains("Government"));
        }
    }

    @Test
    public void testNegativeIncomeThrowsInvalidCustomerDataException() {
        try {
            new Customer("CUST013", "Om", 25, "GOVID222", -1000, 0);
            fail("Expected InvalidCustomerDataException for negative income");
        } catch (InvalidCustomerDataException e) {
            assertTrue(e.getMessage().contains("income"));
        }
    }

    @Test
    public void testZeroLoanAmountThrowsInvalidLoanApplicationException() {
        Customer customer = baselineCustomer();
        try {
            new LoanApplication("APP002", customer, 0, 12);
            fail("Expected InvalidLoanApplicationException for zero loan amount");
        } catch (InvalidLoanApplicationException e) {
            assertTrue(e.getMessage().contains("loan amount"));
        }
    }

    @Test
    public void testZeroTenureThrowsInvalidLoanApplicationException() {
        Customer customer = baselineCustomer();
        try {
            new LoanApplication("APP003", customer, 50000, 0);
            fail("Expected InvalidLoanApplicationException for zero tenure");
        } catch (InvalidLoanApplicationException e) {
            assertTrue(e.getMessage().contains("tenure"));
        }
    }

    @Test
    public void testNullCustomerInApplicationThrowsException() {
        try {
            new LoanApplication("APP004", null, 50000, 12);
            fail("Expected InvalidLoanApplicationException for null customer");
        } catch (InvalidLoanApplicationException e) {
            assertTrue(e.getMessage().contains("Customer"));
        }
    }

    @Test
    public void testCreditScoreBelowRangeThrowsException() {
        try {
            new CreditAssessment("CA016", 250);
            fail("Expected InvalidCreditAssessmentException for out-of-range credit score");
        } catch (InvalidCreditAssessmentException e) {
            assertTrue(e.getMessage().contains("Credit score"));
        }
    }

    @Test
    public void testCreditScoreAboveRangeThrowsException() {
        try {
            new CreditAssessment("CA017", 950);
            fail("Expected InvalidCreditAssessmentException for out-of-range credit score");
        } catch (InvalidCreditAssessmentException e) {
            assertTrue(e.getMessage().contains("Credit score"));
        }
    }

    @Test
    public void testCreditScoreValidRangeBoundariesAreAccepted() {
        CreditAssessment lowBoundary = new CreditAssessment("CA018", CreditAssessment.MIN_VALID_SCORE);
        CreditAssessment highBoundary = new CreditAssessment("CA019", CreditAssessment.MAX_VALID_SCORE);

        assertEquals(300, lowBoundary.getCreditScore());
        assertEquals(900, highBoundary.getCreditScore());
    }

    @Test
    public void testNullApplicationInEvaluateThrowsException() {
        CreditAssessment creditAssessment = new CreditAssessment("CA020", 700);
        try {
            SmartLoanApprovalSystem.evaluate(null, creditAssessment);
            fail("Expected IllegalArgumentException for null application");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("application"));
        }
    }

    @Test
    public void testNullCreditAssessmentInEvaluateThrowsException() {
        Customer customer = baselineCustomer();
        LoanApplication application = baselineApplication(customer, 60000, 12);
        try {
            SmartLoanApprovalSystem.evaluate(application, null);
            fail("Expected IllegalArgumentException for null credit assessment");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("assessment"));
        }
    }
}
