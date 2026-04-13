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

    private final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaWJlcmlmeWluYyIsImF1dGgiOiJST0xFX0JBLFJPTEVfT0EsUk9MRV9QTEFOX0FETUlOLFJPTEVfUk9MTE9VVF9BRE1JTixST0xFX1JPTExPVVRfTUFOQUdFUixST0xFX1VTRVJfQURNSU4iLCJleHAiOjE3Nzc2Mjc3NTF9.FWiSwm1QAgBvPiDCJT2f0NaZOQHr6oGPo5Z12xvc_QW9XStX4WYkQB1zrm-fO73aV95WStvqgt-CPHFFi7vsDg";

    // ✅ USERS SUMMARY (FAST)
    public List<Map<String, Object>> getAllUsers() {

        List<Map<String, Object>> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        while (true) {

            String url = "https://sitpolycab.fiberify.com/api/users?page=" + page + "&size=" + size;

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

                // 🔥 CALL DETAIL API
                String detailUrl = "https://sitpolycab.fiberify.com/api/users/" + login;

                ResponseEntity<Map> detailRes = restTemplate.exchange(
                        detailUrl,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                Map<String, Object> detail = detailRes.getBody();

                Map<String, Object> summary = new HashMap<>();

                summary.put("login", user.get("login"));
                summary.put("name", user.get("firstName") + " " + user.get("lastName"));
                summary.put("phone", user.get("phone"));
                summary.put("activated", user.get("activated"));

                // 🔥 IMPORTANT (IDS)
                summary.put("geofenceIds",
                        detail.get("geofences") != null
                                ? (List<Integer>) detail.get("geofences")
                                : new ArrayList<>());

                // 🔥 FOR UI DISPLAY
                summary.put("geofenceNames", detail.get("geofenceNames"));

                summary.put("reportingTo",
                        detail.get("ownedBy") != null
                                ? ((List<Map<String, Object>>) detail.get("ownedBy"))
                                .stream().map(x -> (String) x.get("login")).toList()
                                : new ArrayList<>());

                finalUsers.add(summary);
            }

            page++;
        }

        return finalUsers;
    }

    // ✅ DISTRICTS
    public List<Map<String, Object>> getDistricts() {

        String url = "https://sitpolycab.fiberify.com/api/user-geofences-by-type-master";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );

        return response.getBody();
    }

    // ✅ BLOCKS
    public List<Map<String, Object>> getBlocks(Long id) {

        String url = "https://sitpolycab.fiberify.com/api/mini-geofences-by-masterGefenceId/" + id;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );

        return response.getBody();
    }
}