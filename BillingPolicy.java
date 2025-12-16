

public interface BillingPolicy {
    double computeTotal(double dailyRate, long daysStayed);
    String getPolicyName();
}