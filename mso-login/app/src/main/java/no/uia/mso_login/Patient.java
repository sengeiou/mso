package no.uia.mso_login;

public class Patient {
    private int id;
    private String username;
    private String name;
    private String heartRate;

    public Patient(int id, String name, String heartRate) {
        this.id = id;
        this.name = name;
        this.heartRate = heartRate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHeartRate(String hr) {
        this.heartRate = hr;
    }

    public String getHeartRate() {
        return heartRate;
    }
}
