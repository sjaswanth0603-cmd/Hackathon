
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBedAlert implements BedObserver {
    private static final String LOG_FILE = "bed_alerts.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onBedStatusChanged(Ward ward, int freeBeds, int totalBeds) {
        int occupied = totalBeds - freeBeds;
        double occupancyPercent = (occupied / (double) totalBeds) * 100.0;

        if (freeBeds == 0 || occupancyPercent >= 90.0) {
            logAlert(ward, occupied, totalBeds, occupancyPercent);
        }
    }

    private void logAlert(Ward ward, int occupied, int totalBeds, double occupancyPercent) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            pw.printf("[%s] ALERT: %s ward - %d/%d beds occupied (%.1f%%)%n",
                    timestamp, ward, occupied, totalBeds, occupancyPercent);
        } catch (IOException e) {
            System.err.println("Failed to write bed alert to log: " + e.getMessage());
        }
    }
}