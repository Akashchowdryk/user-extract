package com.user.userextract.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

@Service
public class UserService {

    @Autowired
    private RestTemplate restTemplate;

    private final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaWJlcmlmeWluYyIsImF1dGgiOiJST0xFX0JBLFJPTEVfT0EsUk9MRV9QTEFOX0FETUlOLFJPTEVfUk9MTE9VVF9BRE1JTixST0xFX1JPTExPVVRfTUFOQUdFUixST0xFX1VTRVJfQURNSU4iLCJleHAiOjE3Nzc2Mjc3NTF9.FWiSwm1QAgBvPiDCJT2f0NaZOQHr6oGPo5Z12xvc_QW9XStX4WYkQB1zrm-fO73aV95WStvqgt-CPHFFi7vsDg";

    private HttpEntity<String> getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    // 🚀 FAST USERS SUMMARY (PARALLEL)
    public List<Map<String, Object>> getAllUsers() {

        List<Map<String, Object>> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

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

                futures.add(executor.submit(() -> {

                    try {
                        String detailUrl = "https://sitpolycab.fiberify.com/api/users/" + login;

                        ResponseEntity<Map> detailRes = restTemplate.exchange(
                                detailUrl,
                                HttpMethod.GET,
                                getEntity(),
                                Map.class
                        );

                        Map<String, Object> detail = detailRes.getBody();

                        Map<String, Object> summary = new HashMap<>();

                        summary.put("login", detail.get("login"));
                        summary.put("name", detail.get("firstName") + " " + detail.get("lastName"));
                        summary.put("phone", detail.get("phone"));
                        summary.put("activated", detail.get("activated"));

                        summary.put("geofenceIds",
                                detail.get("geofences") != null
                                        ? (List<Integer>) detail.get("geofences")
                                        : new ArrayList<>());

                        summary.put("geofenceNames", detail.get("geofenceNames"));

                        summary.put("reportingTo",
                                detail.get("ownedBy") != null
                                        ? ((List<Map<String, Object>>) detail.get("ownedBy"))
                                        .stream().map(x -> (String) x.get("login")).toList()
                                        : new ArrayList<>());

                        return summary;

                    } catch (Exception e) {
                        return null;
                    }
                }));
            }

            page++;
        }

        for (Future<Map<String, Object>> f : futures) {
            try {
                Map<String, Object> user = f.get();
                if (user != null) finalUsers.add(user);
            } catch (Exception ignored) {}
        }

        executor.shutdown();

        return finalUsers;
    }

    // ✅ FIX: USER DETAILS (USED BY FRONTEND CLICK)
    public Map<String, Object> getUserDetails(String login) {

        String url = "https://sitpolycab.fiberify.com/api/users/" + login;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                getEntity(),
                Map.class
        );

        return response.getBody();
    }

    // ✅ DISTRICTS
    public List<Map<String, Object>> getDistricts() {
        String url = "https://sitpolycab.fiberify.com/api/user-geofences-by-type-master";

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                getEntity(),
                List.class
        );

        return response.getBody();
    }

    // ✅ BLOCKS
    public List<Map<String, Object>> getBlocks(Long id) {
        String url = "https://sitpolycab.fiberify.com/api/mini-geofences-by-masterGefenceId/" + id;

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                getEntity(),
                List.class
        );

        return response.getBody();
    }
}