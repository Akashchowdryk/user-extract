package com.user.userextract.dto;
import java.util.*;

public class BulkUpdateDTO {

    private List<String> logins;
    private Long reportingTo;

    public List<String> getLogins() {
        return logins;
    }

    public void setLogins(List<String> logins) {
        this.logins = logins;
    }

    public Long getReportingTo() {
        return reportingTo;
    }

    public void setReportingTo(Long reportingTo) {
        this.reportingTo = reportingTo;
    }
}
