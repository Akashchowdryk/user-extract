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

    // 🚀 FAST SUMMARY API
    public List<Map<String, Object>> getUsersSummary() {

        List<Map<String, Object>> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        while (true) {

            String url = "https://polycab.fiberify.com/api/users?page=" + page + "&size=" + size;

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

                        // ✅ reportingTo
                        List<Map<String, Object>> ownedBy =
                                (List<Map<String, Object>>) detail.get("ownedBy");

                        if (ownedBy != null && !ownedBy.isEmpty()) {
                            summary.put("reportingTo", ownedBy.get(0).get("login"));
                        } else {
                            summary.put("reportingTo", "-");
                        }

                        return summary;

                    } catch (Exception e) {
                        Map<String, Object> fallback = new HashMap<>();
                        fallback.put("login", login);
                        fallback.put("activated", "ERROR");
                        fallback.put("reportingTo", "ERROR");
                        return fallback;
                    }
                }));
            }

            page++;
        }

        // 🔥 collect results
        for (Future<Map<String, Object>> future : futures) {
            try {
                finalUsers.add(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        return finalUsers;
    }
}