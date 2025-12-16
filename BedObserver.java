

public interface BedObserver {
    void onBedStatusChanged(Ward ward, int freeBeds, int totalBeds);
}