package com.user.userextract.service;

import com.user.userextract.dto.UserSummaryDTO;
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

    // =========================================
    // 🔥 CACHE VARIABLES
    // =========================================
    private List<UserSummaryDTO> cachedUsers = new ArrayList<>();
    private long lastFetchTime = 0;

    private List<Map<String, Object>> cachedDistricts = new ArrayList<>();
    private long districtFetchTime = 0;

    // ⏱ 5 minutes cache
    private final long CACHE_DURATION = 5 * 60 * 1000;

    // =========================================
    // ✅ USERS SUMMARY (FAST VERSION)
    // =========================================
    public List<UserSummaryDTO> getUsersSummary() {

        long currentTime = System.currentTimeMillis();

        // ✅ RETURN CACHE IF AVAILABLE
        if (!cachedUsers.isEmpty() && (currentTime - lastFetchTime) < CACHE_DURATION) {
            System.out.println("Returning cached users...");
            return cachedUsers;
        }

        System.out.println("Fetching fresh users...");

        List<UserSummaryDTO> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        while (true) {

            String url = "https://sitpolycab.fiberify.com/api/users?page=" + page + "&size=" + size;

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> users = response.getBody();

            if (users == null || users.isEmpty()) break;

            for (Map<String, Object> user : users) {

                UserSummaryDTO dto = new UserSummaryDTO();

                // BASIC DATA ONLY (FAST)
                dto.setLogin((String) user.get("login"));
                dto.setName(user.get("firstName") + " " + user.get("lastName"));
                dto.setPhone((String) user.get("phone"));
                dto.setActivated((Boolean) user.get("activated"));

                // ROLES
                dto.setRoles((List<String>) user.get("authorities"));

                // VERSION
                dto.setVersion((String) user.get("applicationVersion"));

                // 🔥 OPTIONAL SAFE FIELDS (avoid null issues)
                dto.setReportingTo((String) user.getOrDefault("reportingTo", null));

                finalUsers.add(dto);
            }

            page++;
        }

        // ✅ SAVE CACHE
        cachedUsers = finalUsers;
        lastFetchTime = currentTime;

        return finalUsers;
    }

    // =========================================
    // ✅ USER DETAILS (ON CLICK)
    // =========================================
    public Map<String, Object> getUserWithGeofence(String login) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://sitpolycab.fiberify.com/api/users/" + login;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }

    // =========================================
    // ✅ DISTRICTS (CACHED)
    // =========================================
    public List<Map<String, Object>> getDistricts() {

        long currentTime = System.currentTimeMillis();

        if (!cachedDistricts.isEmpty() && (currentTime - districtFetchTime) < CACHE_DURATION) {
            System.out.println("Returning cached districts...");
            return cachedDistricts;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://sitpolycab.fiberify.com/api/user-geofences-by-type-master";

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );

        cachedDistricts = response.getBody();
        districtFetchTime = currentTime;

        return cachedDistricts;
    }

    // =========================================
    // ✅ BLOCKS BY DISTRICT
    // =========================================
    public List<Map<String, Object>> getBlocksByDistrict(String districtId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://sitpolycab.fiberify.com/api/mini-geofences-by-masterGefenceId/" + districtId;

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
        );

        return response.getBody();
    }
}