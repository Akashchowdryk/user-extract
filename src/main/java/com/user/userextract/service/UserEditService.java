package com.user.userextract.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class UserEditService {

    private final String BASE_URL = "https://sitpolycab.fiberify.com/api";

    @Autowired
    private RestTemplate restTemplate;

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ⚠️ ADD TOKEN HERE IF REQUIRED
        headers.set("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaWJlcmlmeWluYyIsImF1dGgiOiJST0xFX0JBLFJPTEVfT0EsUk9MRV9QTEFOX0FETUlOLFJPTEVfUk9MTE9VVF9BRE1JTixST0xFX1JPTExPVVRfTUFOQUdFUixST0xFX1VTRVJfQURNSU4iLCJleHAiOjE3Nzc2Mjc3NTF9.FWiSwm1QAgBvPiDCJT2f0NaZOQHr6oGPo5Z12xvc_QW9XStX4WYkQB1zrm-fO73aV95WStvqgt-CPHFFi7vsDg");

        return headers;
    }

    // ✅ ROLES
    public JsonNode getRoles() {
        return callGet("/configs/Roles");
    }

    // ✅ REPORTING USERS
    public JsonNode getReportingUsers() {
        return callGet("/reporting-users");
    }

    // ✅ GROUPS
    public JsonNode getGroups() {
        return callGet("/user-groups");
    }

    // ✅ UPDATE USER
    public ResponseEntity<String> updateUser(String payload) {

    	try {
            HttpHeaders headers = getHeaders();

            ObjectMapper mapper = new ObjectMapper();
            

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/users",
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("ERROR: " + e.getMessage());
        }
    }

    // 🔁 COMMON GET METHOD
    private JsonNode callGet(String url) {
        try {
        	HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        	ResponseEntity<String> response = restTemplate.exchange(
        		    BASE_URL + url,
        		    HttpMethod.GET,
        		    entity,
        		    String.class
        		);

        		ObjectMapper mapper = new ObjectMapper();
        		return mapper.readTree(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}