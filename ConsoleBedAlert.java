

public class ConsoleBedAlert implements BedObserver {
    private static final double HIGH_OCCUPANCY_THRESHOLD = 90.0;

    @Override
    public void onBedStatusChanged(Ward ward, int freeBeds, int totalBeds) {
        int occupied = totalBeds - freeBeds;
        double occupancyPercent = (occupied / (double) totalBeds) * 100.0;

        if (freeBeds == 0) {
            System.out.println("\n⚠️  ALERT: " + ward + " ward is FULL! (" +
                    occupied + "/" + totalBeds + " beds)");
        } else if (occupancyPercent >= HIGH_OCCUPANCY_THRESHOLD) {
            System.out.println("\n⚠️  WARNING: " + ward + " ward high occupancy: " +
                    String.format("%.1f%%", occupancyPercent) + " (" + occupied + "/" + totalBeds + " beds)");
        }
    }
}