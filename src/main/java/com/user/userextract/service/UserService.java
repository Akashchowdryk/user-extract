package com.user.userextract.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private RestTemplate restTemplate;

    // 🔥 HARD CODE YOUR TOKEN HERE
    private final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaWJlcmlmeWluYyIsImF1dGgiOiJST0xFX0JBLFJPTEVfT0EsUk9MRV9QTEFOX0FETUlOLFJPTEVfUk9MTE9VVF9BRE1JTixST0xFX1JPTExPVVRfTUFOQUdFUixST0xFX1VTRVJfQURNSU4iLCJleHAiOjE3Nzc2Mjc3NTF9.FWiSwm1QAgBvPiDCJT2f0NaZOQHr6oGPo5Z12xvc_QW9XStX4WYkQB1zrm-fO73aV95WStvqgt-CPHFFi7vsDg";

    public List<Map<String, Object>> getAllUsersWithGeofence() {

        List<Map<String, Object>> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        while (true) {

            String url = "https://polycab.fiberify.com/api/users?page=" + page + "&size=" + size;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> users = response.getBody();

            if (users == null || users.isEmpty()) break;

            for (Map<String, Object> user : users) {

                String login = (String) user.get("login");

                try {
                    // 🔥 SECOND API CALL
                    String detailUrl = "https://sitpolycab.fiberify.com/api/users/" + login;

                    HttpHeaders detailHeaders = new HttpHeaders();
                    detailHeaders.set("Authorization", "Bearer " + token);

                    HttpEntity<String> detailEntity = new HttpEntity<>(detailHeaders);

                    ResponseEntity<Map> detailResponse = restTemplate.exchange(
                            detailUrl,
                            HttpMethod.GET,
                            detailEntity,
                            Map.class
                    );

                    Map<String, Object> detail = detailResponse.getBody();

                    if (detail != null && detail.containsKey("geofenceNames")) {
                        user.put("geofenceNames", detail.get("geofenceNames"));
                    } else {
                        user.put("geofenceNames", new ArrayList<>());
                    }

                } catch (Exception e) {
                    System.out.println("Error fetching geofence for: " + login);
                    user.put("geofenceNames", new ArrayList<>());
                }

                finalUsers.add(user);
            }

            page++;
        }

        return finalUsers;
    }
}