

public enum Ward {
    GENERAL("General Ward"),
    ICU("Intensive Care Unit"),
    PRIVATE("Private Ward");

    private final String description;

    Ward(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}