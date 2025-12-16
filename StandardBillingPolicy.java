

public class StandardBillingPolicy implements BillingPolicy {

    @Override
    public double computeTotal(double dailyRate, long daysStayed) {
        return dailyRate * daysStayed;
    }

    @Override
    public String getPolicyName() {
        return "Standard Billing";
    }

    @Override
    public String toString() {
        return getPolicyName();
    }
}