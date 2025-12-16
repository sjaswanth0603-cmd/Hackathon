

public abstract class BillingService implements Service {

    /**
     * Template method pattern - final method that uses hook methods
     */
    public final double calculateCharges(Patient patient) {
        validatePatient(patient);

        double dailyRate = getDailyRate(patient.getWard());
        long daysStayed = patient.getStayLengthInDays();

        BillingPolicy policy = selectPolicy(patient);

        return policy.computeTotal(dailyRate, daysStayed);
    }

    /**
     * Hook method - validate patient state
     */
    protected void validatePatient(Patient patient) {
        if (patient.getAdmitDate() == null) {
            throw new InvalidPatientStateException(
                    "Cannot calculate billing: Patient has no admission date");
        }

        if (patient.isAdmitted()) {
            throw new InvalidPatientStateException(
                    "Cannot calculate billing: Patient is still admitted (no discharge date)");
        }
    }

    /**
     * Abstract hook method - subclasses must implement
     */
    protected abstract double getDailyRate(Ward ward);

    /**
     * Abstract hook method - subclasses must implement
     */
    protected abstract BillingPolicy selectPolicy(Patient patient);

    @Override
    public void execute() {
        System.out.println("Billing service is running...");
    }
}