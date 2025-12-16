
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StandardBillingServiceImpl extends BillingService {
    private Map<Ward, Double> rateMap;

    public StandardBillingServiceImpl() {
        this.rateMap = new HashMap<>();
        loadRatesFromFile();
    }

    private void loadRatesFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader("rates.cfg"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    Ward ward = Ward.valueOf(parts[0].trim());
                    double rate = Double.parseDouble(parts[1].trim());
                    rateMap.put(ward, rate);
                }
            }
        } catch (IOException e) {
            // Use default rates if file not found
            rateMap.put(Ward.GENERAL, 1500.0);
            rateMap.put(Ward.ICU, 5000.0);
            rateMap.put(Ward.PRIVATE, 3000.0);
        }
    }

    @Override
    protected double getDailyRate(Ward ward) {
        return rateMap.getOrDefault(ward, 1500.0);
    }

    @Override
    protected BillingPolicy selectPolicy(Patient patient) {
        // Use patient's assigned billing policy
        if (patient.getBillingPolicy() != null) {
            return patient.getBillingPolicy();
        }

        // Default policy based on ward
        if (patient.getWard() == Ward.PRIVATE) {
            return new WeekendDiscountBillingPolicy();
        }

        return new StandardBillingPolicy();
    }
}