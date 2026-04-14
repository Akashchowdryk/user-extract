package com.user.userextract.service;

import com.user.userextract.dto.UserSummaryDTO;
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

    // 🔥 THREAD POOL
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // CACHE
    private List<UserSummaryDTO> cachedUsers = new ArrayList<>();
    private long lastFetchTime = 0;
    private final long CACHE_DURATION = 5 * 60 * 1000;

    // =========================================
    // ✅ USERS SUMMARY (FAST + ENRICHED)
    // =========================================
    public List<UserSummaryDTO> getUsersSummary() {

        long now = System.currentTimeMillis();

        if (!cachedUsers.isEmpty() && (now - lastFetchTime) < CACHE_DURATION) {
            return cachedUsers;
        }

        List<UserSummaryDTO> finalUsers = new ArrayList<>();

        int page = 0;
        int size = 50;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        while (true) {

            String url = "https://polycab.fiberify.com/api/users?page=" + page + "&size=" + size;

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            List<Map<String, Object>> users = response.getBody();
            if (users == null || users.isEmpty()) break;

            // 🔥 PARALLEL DETAIL CALLS (LIMITED)
            List<CompletableFuture<UserSummaryDTO>> futures = new ArrayList<>();

            for (Map<String, Object> user : users) {

                futures.add(CompletableFuture.supplyAsync(() -> {

                    try {
                        String login = (String) user.get("login");

                        UserSummaryDTO dto = new UserSummaryDTO();

                        dto.setLogin(login);
                        dto.setName(user.get("firstName") + " " + user.get("lastName"));
                        dto.setPhone((String) user.get("phone"));
                        dto.setActivated((Boolean) user.get("activated"));
                        dto.setRoles((List<String>) user.get("authorities"));
                        dto.setVersion((String) user.get("applicationVersion"));

                        // 🔥 DETAIL API (IMPORTANT)
                        String detailUrl = "https://sitpolycab.fiberify.com/api/users/" + login;

                        ResponseEntity<Map> detailRes = restTemplate.exchange(
                                detailUrl,
                                HttpMethod.GET,
                                entity,
                                Map.class
                        );

                        Map<String, Object> detail = detailRes.getBody();

                        if (detail != null) {

                            // ✅ reportingTo
                            List<Map<String, Object>> ownedBy =
                                    (List<Map<String, Object>>) detail.get("ownedBy");

                            if (ownedBy != null && !ownedBy.isEmpty()) {
                                dto.setReportingTo((String) ownedBy.get(0).get("login"));
                            }

                            // ✅ geofences
                            dto.setGeofenceNames(
                                    (List<String>) detail.get("geofenceNames")
                            );
                        }

                        return dto;

                    } catch (Exception e) {
                        return null;
                    }

                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            for (CompletableFuture<UserSummaryDTO> f : futures) {
                try {
                    UserSummaryDTO dto = f.get();
                    if (dto != null) finalUsers.add(dto);
                } catch (Exception ignored) {}
            }

            page++;
        }

        cachedUsers = finalUsers;
        lastFetchTime = now;

        return finalUsers;
    }

    // =========================================
    // USER DETAILS
    // =========================================
    public Map<String, Object> getUserWithGeofence(String login) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://sitpolycab.fiberify.com/api/users/" + login;

        return restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
    }

    // =========================================
    // DISTRICTS (CACHED)
    // =========================================
    private List<Map<String, Object>> cachedDistricts = new ArrayList<>();
    private long districtFetchTime = 0;

    public List<Map<String, Object>> getDistricts() {

        long now = System.currentTimeMillis();

        if (!cachedDistricts.isEmpty() && (now - districtFetchTime) < CACHE_DURATION) {
            return cachedDistricts;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://sitpolycab.fiberify.com/api/user-geofences-by-type-master";

        List<Map<String, Object>> data = restTemplate.exchange(
                url, HttpMethod.GET, entity, List.class
        ).getBody();

        cachedDistricts = data;
        districtFetchTime = now;

        return data;
    }

    // =========================================
    // BLOCKS
    // =========================================
    public List<Map<String, Object>> getBlocksByDistrict(String districtId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://sitpolycab.fiberify.com/api/mini-geofences-by-masterGefenceId/" + districtId;

        return restTemplate.exchange(url, HttpMethod.GET, entity, List.class).getBody();
    }
}