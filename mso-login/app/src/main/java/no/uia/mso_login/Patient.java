package no.uia.mso_login;

public class Patient {
    private int id;
    private String username;
    private String patientName;
    private String heartRate;
    private boolean request = false;

    public Patient(int id, String username, String patientName, String heartRate) {
        this.id = id;
        this.username = username;
        this.patientName = patientName;
        this.heartRate = heartRate;
    }

    public int getId() {
        return id;
    }

    public boolean getRequest() {return request; }

    public void setRequest(boolean r) {this.request = r;}

    public String getName() {
        return patientName;
    }

    public String getUsername() {
        return username;
    }

    public void setName(String name) {
        this.patientName = name;
    }

    public void setHeartRate(String hr) {
        this.heartRate = hr;
    }

    public String getHeartRate() {
        return heartRate;
    }
}
