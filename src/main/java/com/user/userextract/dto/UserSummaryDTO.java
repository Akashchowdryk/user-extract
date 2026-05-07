package com.user.userextract.dto;

import java.util.List;

public class UserSummaryDTO {

    private String login;
    private String name;
    private String phone;
    private boolean activated;
    private String reportingTo;
    private List<String> roles;
    private List<String> geofenceNames;
    private String version;
    private Long id;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public boolean isActivated() {
		return activated;
	}
	public void setActivated(boolean activated) {
		this.activated = activated;
	}
	public String getReportingTo() {
		return reportingTo;
	}
	public void setReportingTo(String reportingTo) {
		this.reportingTo = reportingTo;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<String> getGeofenceNames() {
		return geofenceNames;
	}
	public void setGeofenceNames(List<String> geofenceNames) {
		this.geofenceNames = geofenceNames;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

    // getters & setters
}