

public class WeekendDiscountBillingPolicy implements BillingPolicy {
    private static final double LONG_STAY_DISCOUNT = 0.10; // 10% discount
    private static final int LONG_STAY_THRESHOLD = 7; // days

    @Override
    public double computeTotal(double dailyRate, long daysStayed) {
        double total = dailyRate * daysStayed;

        // Apply discount for long stays
        if (daysStayed >= LONG_STAY_THRESHOLD) {
            total *= (1.0 - LONG_STAY_DISCOUNT);
        }

        return total;
    }

    @Override
    public String getPolicyName() {
        return "Weekend/Long Stay Discount Billing";
    }

    @Override
    public String toString() {
        return getPolicyName() + " (10% off for 7+ days)";
    }
}