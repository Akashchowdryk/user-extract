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

    // 🔥 TOKEN
    private final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaWJlcmlmeWluYyIsImF1dGgiOiJST0xFX0JBLFJPTEVfT0EsUk9MRV9QTEFOX0FETUlOLFJPTEVfUk9MTE9VVF9BRE1JTixST0xFX1JPTExPVVRfTUFOQUdFUixST0xFX1VTRVJfQURNSU4iLCJleHAiOjE3Nzc2Mjc3NTF9.FWiSwm1QAgBvPiDCJT2f0NaZOQHr6oGPo5Z12xvc_QW9XStX4WYkQB1zrm-fO73aV95WStvqgt-CPHFFi7vsDg";

    // ✅ HEADERS COMMON METHOD
    private HttpEntity<String> getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    // ✅ EXISTING (NO CHANGE)
    public List<Map<String, Object>> getAllUsers() {

        List<Map<String, Object>> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        while (true) {

            String url = "https://sitpolycab.fiberify.com/api/users?page=" + page + "&size=" + size;

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    getEntity(),
                    List.class
            );

            List<Map<String, Object>> users = response.getBody();

            if (users == null || users.isEmpty()) break;

            finalUsers.addAll(users);
            page++;
        }

        return finalUsers;
    }

    // ✅ EXISTING (NO CHANGE)
    public Map<String, Object> getUserWithGeofence(String login) {

        String url = "https://sitpolycab.fiberify.com/api/users/" + login;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                getEntity(),
                Map.class
        );

        return response.getBody();
    }

    // 🚀 NEW METHOD (🔥 MAIN OPTIMIZATION)
    public List<Map<String, Object>> getUsersSummary() {

        List<Map<String, Object>> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        while (true) {

            String url = "https://sitpolycab.fiberify.com/api/users?page=" + page + "&size=" + size;

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    getEntity(),
                    List.class
            );

            List<Map<String, Object>> users = response.getBody();

            if (users == null || users.isEmpty()) break;

            for (Map<String, Object> user : users) {

                String login = (String) user.get("login");

                try {
                    // 🔥 CALL DETAIL API
                    String detailUrl = "https://sitpolycab.fiberify.com/api/users/" + login;

                    ResponseEntity<Map> detailResponse = restTemplate.exchange(
                            detailUrl,
                            HttpMethod.GET,
                            getEntity(),
                            Map.class
                    );

                    Map<String, Object> detail = detailResponse.getBody();

                    Map<String, Object> summary = new HashMap<>();

                    summary.put("id", detail.get("id"));
                    summary.put("login", detail.get("login"));

                    String name =
                            (detail.get("firstName") != null ? detail.get("firstName") : "") + " " +
                            (detail.get("lastName") != null ? detail.get("lastName") : "");

                    summary.put("name", name.trim());
                    summary.put("email", detail.get("email"));
                    summary.put("activated", detail.get("activated"));

                    // ✅ reportingTo (ownedBy → login)
                    List<Map<String, Object>> ownedBy =
                            (List<Map<String, Object>>) detail.get("ownedBy");

                    if (ownedBy != null && !ownedBy.isEmpty()) {
                        summary.put("reportingTo", ownedBy.get(0).get("login"));
                    } else {
                        summary.put("reportingTo", "-");
                    }

                    finalUsers.add(summary);

                } catch (Exception e) {

                    // ⚠️ fallback if error
                    Map<String, Object> fallback = new HashMap<>();
                    fallback.put("id", user.get("id"));
                    fallback.put("login", login);
                    fallback.put("name", user.get("firstName") + " " + user.get("lastName"));
                    fallback.put("email", user.get("email"));
                    fallback.put("activated", "ERROR");
                    fallback.put("reportingTo", "ERROR");

                    finalUsers.add(fallback);
                }
            }

            page++;
        }

        return finalUsers;
    }
}