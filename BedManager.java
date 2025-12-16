
import java.util.*;

public class BedManager {
    private Map<Ward, Integer> totalBeds;
    private Map<Ward, Integer> occupiedBeds;
    private List<BedObserver> observers;

    public BedManager(Map<Ward, Integer> totalBeds) {
        this.totalBeds = new HashMap<>(totalBeds);
        this.occupiedBeds = new HashMap<>();
        this.observers = new ArrayList<>();

        // Initialize occupied beds to 0
        for (Ward ward : totalBeds.keySet()) {
            occupiedBeds.put(ward, 0);
        }
    }

    public void registerObserver(BedObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BedObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Ward ward) {
        int total = totalBeds.get(ward);
        int occupied = occupiedBeds.get(ward);
        int free = total - occupied;

        for (BedObserver observer : observers) {
            observer.onBedStatusChanged(ward, free, total);
        }
    }

    public void allocateBed(Ward ward) throws BedUnavailableException {
        int total = totalBeds.get(ward);
        int occupied = occupiedBeds.get(ward);

        if (occupied >= total) {
            throw new BedUnavailableException(
                    "No beds available in " + ward + " ward. All " + total + " beds are occupied.");
        }

        occupiedBeds.put(ward, occupied + 1);
        notifyObservers(ward);
    }

    public void releaseBed(Ward ward) {
        int occupied = occupiedBeds.get(ward);
        if (occupied > 0) {
            occupiedBeds.put(ward, occupied - 1);
            notifyObservers(ward);
        }
    }

    public int getTotalBeds(Ward ward) {
        return totalBeds.get(ward);
    }

    public int getOccupiedBeds(Ward ward) {
        return occupiedBeds.get(ward);
    }

    public int getFreeBeds(Ward ward) {
        return totalBeds.get(ward) - occupiedBeds.get(ward);
    }

    public double getOccupancyPercentage(Ward ward) {
        int total = totalBeds.get(ward);
        int occupied = occupiedBeds.get(ward);
        return (occupied / (double) total) * 100.0;
    }
}