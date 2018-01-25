package com.karthikmlore.minipasswordmanager;

public class RecordData {

    private String username;
    private String password;
    private String notes;

    public RecordData(String username, String password, String notes) {
        this.username = username;
        this.password = password;
        this.notes = notes;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getNotes() {
        return notes;
    }
}
