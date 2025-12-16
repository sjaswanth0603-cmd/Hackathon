


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Patient extends Person {
    private Ward ward;
    private int bedNumber;
    private LocalDate admitDate;
    private LocalDate dischargeDate;
    private BillingPolicy billingPolicy;

    public Patient(int id, String name, int age, Ward ward, int bedNumber,
                   LocalDate admitDate, BillingPolicy billingPolicy) {
        super(id, name, age);
        this.ward = ward;
        this.bedNumber = bedNumber;
        this.admitDate = admitDate;
        this.dischargeDate = null;
        this.billingPolicy = billingPolicy;
    }

    public boolean isAdmitted() {
        return dischargeDate == null;
    }

    public long getStayLengthInDays() {
        if (admitDate == null) {
            return 0;
        }

        LocalDate endDate = dischargeDate != null ? dischargeDate : LocalDate.now();
        long days = ChronoUnit.DAYS.between(admitDate, endDate);
        return Math.max(1, days);
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public int getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(int bedNumber) {
        this.bedNumber = bedNumber;
    }

    public LocalDate getAdmitDate() {
        return admitDate;
    }

    public void setAdmitDate(LocalDate admitDate) {
        this.admitDate = admitDate;
    }

    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public BillingPolicy getBillingPolicy() {
        return billingPolicy;
    }

    public void setBillingPolicy(BillingPolicy billingPolicy) {
        this.billingPolicy = billingPolicy;
    }

    @Override
    public String toString() {
        return String.format(
                "Patient ID: %d%n" +
                        "Name: %s%n" +
                        "Age: %d%n" +
                        "Ward: %s%n" +
                        "Bed Number: %d%n" +
                        "Admit Date: %s%n" +
                        "Discharge Date: %s%n" +
                        "Status: %s%n" +
                        "Days Stayed: %d",
                getId(), getName(), getAge(), ward, bedNumber,
                admitDate,
                dischargeDate != null ? dischargeDate.toString() : "N/A",
                isAdmitted() ? "Admitted" : "Discharged",
                getStayLengthInDays()
        );
    }
}