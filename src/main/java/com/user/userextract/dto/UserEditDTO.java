package com.user.userextract.dto;

import java.util.List;

public class UserEditDTO {

    private Long id;
    private String login;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gpsimei;

    private List<String> authorities;
    private List<Long> geofences;

    private Long reportingTo;

    // 🔥 GETTERS & SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGpsimei() {
        return gpsimei;
    }

    public void setGpsimei(String gpsimei) {
        this.gpsimei = gpsimei;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public List<Long> getGeofences() {
        return geofences;
    }

    public void setGeofences(List<Long> geofences) {
        this.geofences = geofences;
    }

    public Long getReportingTo() {
        return reportingTo;
    }

    public void setReportingTo(Long reportingTo) {
        this.reportingTo = reportingTo;
    }
}