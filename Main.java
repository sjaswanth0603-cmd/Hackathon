

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static List<Patient> allPatients = new ArrayList<>();
    private static Map<Ward, List<Patient>> wardAllocations = new HashMap<>();
    private static Map<Ward, Double> wardRates = new HashMap<>();
    private static BedManager bedManager;
    private static BillingService billingService;
    private static int nextPatientId = 1;

    public static void main(String[] args) {
        System.out.println("=== Hospital Patient Record System ===\n");

        initializeSystem();
        loadData();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = getIntInput("Enter choice: ");

            switch (choice) {
                case 1:
                    admitPatient();
                    break;
                case 2:
                    dischargePatient();
                    break;
                case 3:
                    listPatients();
                    break;
                case 4:
                    showOccupancyAnalytics();
                    break;
                case 5:
                    saveAndExit();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.\n");
            }
        }

        scanner.close();
    }

    private static void initializeSystem() {
        // Initialize ward allocations
        for (Ward ward : Ward.values()) {
            wardAllocations.put(ward, new ArrayList<>());
        }

        // Initialize bed manager with capacities
        Map<Ward, Integer> totalBeds = new HashMap<>();
        totalBeds.put(Ward.GENERAL, 50);
        totalBeds.put(Ward.ICU, 20);
        totalBeds.put(Ward.PRIVATE, 30);

        bedManager = new BedManager(totalBeds);

        // Register observers
        bedManager.registerObserver(new ConsoleBedAlert());
        bedManager.registerObserver(new FileBedAlert());

        // Initialize billing service
        billingService = new StandardBillingServiceImpl();
    }

    private static void loadData() {
        loadRates();
        loadPatients();
    }

    private static void loadRates() {
        try (BufferedReader br = new BufferedReader(new FileReader("rates.cfg"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    Ward ward = Ward.valueOf(parts[0].trim());
                    double rate = Double.parseDouble(parts[1].trim());
                    wardRates.put(ward, rate);
                }
            }
            System.out.println("Loaded ward rates from rates.cfg");
        } catch (FileNotFoundException e) {
            System.out.println("rates.cfg not found. Using default rates.");
            wardRates.put(Ward.GENERAL, 1500.0);
            wardRates.put(Ward.ICU, 5000.0);
            wardRates.put(Ward.PRIVATE, 3000.0);
            createDefaultRatesFile();
        } catch (IOException e) {
            System.out.println("Error reading rates.cfg: " + e.getMessage());
        }
    }

    private static void createDefaultRatesFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("rates.cfg"))) {
            pw.println("GENERAL=1500");
            pw.println("ICU=5000");
            pw.println("PRIVATE=3000");
            System.out.println("Created default rates.cfg file");
        } catch (IOException e) {
            System.out.println("Could not create rates.cfg: " + e.getMessage());
        }
    }

    private static void loadPatients() {
        try (BufferedReader br = new BufferedReader(new FileReader("patients.csv"))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    int age = Integer.parseInt(parts[2].trim());
                    Ward ward = Ward.valueOf(parts[3].trim());
                    int bedNo = Integer.parseInt(parts[4].trim());
                    LocalDate admitDate = LocalDate.parse(parts[5].trim(), DATE_FORMATTER);
                    LocalDate dischargeDate = parts.length > 6 && !parts[6].trim().isEmpty()
                            ? LocalDate.parse(parts[6].trim(), DATE_FORMATTER) : null;

                    BillingPolicy policy = new StandardBillingPolicy();
                    Patient patient = new Patient(id, name, age, ward, bedNo, admitDate, policy);

                    if (dischargeDate != null) {
                        patient.setDischargeDate(dischargeDate);
                    } else {
                        wardAllocations.get(ward).add(patient);
                        bedManager.allocateBed(ward);
                    }

                    allPatients.add(patient);
                    if (id >= nextPatientId) {
                        nextPatientId = id + 1;
                    }
                }
            }
            System.out.println("Loaded " + allPatients.size() + " patients from patients.csv\n");
        } catch (FileNotFoundException e) {
            System.out.println("patients.csv not found. Starting with empty patient list.\n");
        } catch (IOException | BedUnavailableException e) {
            System.out.println("Error loading patients: " + e.getMessage() + "\n");
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Admit Patient");
        System.out.println("2. Discharge Patient");
        System.out.println("3. List Patients");
        System.out.println("4. Show Occupancy Analytics");
        System.out.println("5. Save & Exit");
    }

    private static void admitPatient() {
        System.out.println("\n--- Admit Patient ---");

        String name = getStringInput("Enter patient name: ");
        int age = getIntInput("Enter age: ");

        System.out.println("Available wards:");
        for (Ward ward : Ward.values()) {
            System.out.println("  - " + ward);
        }

        Ward ward = null;
        while (ward == null) {
            String wardStr = getStringInput("Enter ward: ").toUpperCase();
            try {
                ward = Ward.valueOf(wardStr);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid ward. Try again.");
            }
        }

        LocalDate admitDate = getDateInput("Enter admission date (yyyy-MM-dd) or press Enter for today: ");
        if (admitDate == null) {
            admitDate = LocalDate.now();
        }

        try {
            bedManager.allocateBed(ward);

            int bedNo = bedManager.getOccupiedBeds(ward);
            BillingPolicy policy = selectBillingPolicy(ward);

            Patient patient = new Patient(nextPatientId++, name, age, ward, bedNo, admitDate, policy);
            allPatients.add(patient);
            wardAllocations.get(ward).add(patient);

            System.out.println("\nPatient admitted successfully!");
            System.out.println(patient);

        } catch (BedUnavailableException e) {
            System.out.println("\nERROR: " + e.getMessage());
        }
    }

    private static BillingPolicy selectBillingPolicy(Ward ward) {
        if (ward == Ward.PRIVATE) {
            return new WeekendDiscountBillingPolicy();
        }
        return new StandardBillingPolicy();
    }

    private static void dischargePatient() {
        System.out.println("\n--- Discharge Patient ---");

        int patientId = getIntInput("Enter patient ID: ");
        Patient patient = findPatientById(patientId);

        if (patient == null) {
            System.out.println("Patient not found.");
            return;
        }

        if (!patient.isAdmitted()) {
            System.out.println("Patient is already discharged.");
            return;
        }

        LocalDate dischargeDate = getDateInput("Enter discharge date (yyyy-MM-dd) or press Enter for today: ");
        if (dischargeDate == null) {
            dischargeDate = LocalDate.now();
        }

        try {
            patient.setDischargeDate(dischargeDate);
            wardAllocations.get(patient.getWard()).remove(patient);
            bedManager.releaseBed(patient.getWard());

            // Calculate billing
            double charges = billingService.calculateCharges(patient);

            // Write to billing.txt
            writeBillingRecord(patient, charges);

            System.out.println("\nPatient discharged successfully!");
            System.out.println(patient);
            System.out.println("Total charges: ₹" + String.format("%.2f", charges));

        } catch (InvalidPatientStateException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void writeBillingRecord(Patient patient, double charges) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("billing.txt", true))) {
            double dailyRate = wardRates.getOrDefault(patient.getWard(), 0.0);
            long daysStayed = patient.getStayLengthInDays();

            pw.printf("%d, %s, %s, %d, %.2f, %.2f%n",
                    patient.getId(),
                    patient.getName(),
                    patient.getWard(),
                    daysStayed,
                    dailyRate,
                    charges);

            System.out.println("Billing record written to billing.txt");
        } catch (IOException e) {
            System.out.println("Error writing billing record: " + e.getMessage());
        }
    }

    private static void listPatients() {
        System.out.println("\n--- List Patients ---");
        System.out.println("1. All patients");
        System.out.println("2. Currently admitted");
        System.out.println("3. Discharged");

        int choice = getIntInput("Enter choice: ");

        List<Patient> patientsToShow = new ArrayList<>();

        switch (choice) {
            case 1:
                patientsToShow = allPatients;
                break;
            case 2:
                patientsToShow = allPatients.stream()
                        .filter(Patient::isAdmitted)
                        .collect(java.util.stream.Collectors.toList());
                break;
            case 3:
                patientsToShow = allPatients.stream()
                        .filter(p -> !p.isAdmitted())
                        .collect(java.util.stream.Collectors.toList());
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        if (patientsToShow.isEmpty()) {
            System.out.println("\nNo patients to display.");
        } else {
            System.out.println("\n" + patientsToShow.size() + " patient(s) found:\n");
            for (Patient p : patientsToShow) {
                System.out.println(p);
                System.out.println("---");
            }
        }
    }

    private static void showOccupancyAnalytics() {
        System.out.println("\n=== Occupancy Analytics ===\n");

        int totalBedsHospital = 0;
        int totalOccupiedHospital = 0;

        for (Ward ward : Ward.values()) {
            int total = bedManager.getTotalBeds(ward);
            int occupied = bedManager.getOccupiedBeds(ward);
            int free = total - occupied;
            double occupancyPercent = (occupied / (double) total) * 100;

            System.out.println("Ward: " + ward);
            System.out.println("  Total beds: " + total);
            System.out.println("  Occupied: " + occupied);
            System.out.println("  Free: " + free);
            System.out.printf("  Occupancy: %.2f%%%n%n", occupancyPercent);

            totalBedsHospital += total;
            totalOccupiedHospital += occupied;
        }

        double overallOccupancy = (totalOccupiedHospital / (double) totalBedsHospital) * 100;
        System.out.println("Overall Hospital Occupancy:");
        System.out.printf("  %d / %d beds (%.2f%%)%n%n",
                totalOccupiedHospital, totalBedsHospital, overallOccupancy);

        // Average length of stay
        List<Patient> dischargedPatients = allPatients.stream()
                .filter(p -> !p.isAdmitted())
                .collect(java.util.stream.Collectors.toList());

        if (!dischargedPatients.isEmpty()) {
            long totalDays = dischargedPatients.stream()
                    .mapToLong(Patient::getStayLengthInDays)
                    .sum();
            double avgStay = totalDays / (double) dischargedPatients.size();
            System.out.printf("Average Length of Stay: %.2f days%n", avgStay);
            System.out.println("  (Based on " + dischargedPatients.size() + " discharged patients)");
        } else {
            System.out.println("No discharged patients yet.");
        }
    }

    private static void saveAndExit() {
        System.out.println("\nSaving data...");

        // Save patients to patients.csv
        try (PrintWriter pw = new PrintWriter(new FileWriter("patients.csv"))) {
            pw.println("id,name,age,ward,bedNo,admitDate,dischargeDate");
            for (Patient p : allPatients) {
                pw.printf("%d,%s,%d,%s,%d,%s,%s%n",
                        p.getId(),
                        p.getName(),
                        p.getAge(),
                        p.getWard(),
                        p.getBedNumber(),
                        p.getAdmitDate().format(DATE_FORMATTER),
                        p.getDischargeDate() != null ? p.getDischargeDate().format(DATE_FORMATTER) : "");
            }
            System.out.println("Saved patients to patients.csv");
        } catch (IOException e) {
            System.out.println("Error saving patients: " + e.getMessage());
        }

        System.out.println("\nThank you for using Hospital Patient Record System!");
        System.out.println("Goodbye!");
    }

    private static Patient findPatientById(int id) {
        return allPatients.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static LocalDate getDateInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(input, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date.");
            return LocalDate.now();
        }
    }
}