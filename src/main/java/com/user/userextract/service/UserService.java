package com.user.userextract.service;

import com.user.userextract.dto.UserSummaryDTO;
import com.user.userextract.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.user.userextract.dto.UserEditDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.*;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
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

            String url = "https://sitpolycab.fiberify.com/api/users?page=" + page + "&size=" + size;

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
    public Object updateUser(UserEditDTO dto) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🔥 STEP 1: GET EXISTING USER (IMPORTANT)
        String getUrl = "https://sitpolycab.fiberify.com/api/users/" + dto.getLogin();

        HttpEntity<String> getEntity = new HttpEntity<>(headers);

        Map<String, Object> existingUser = restTemplate.exchange(
                getUrl,
                HttpMethod.GET,
                getEntity,
                Map.class
        ).getBody();

        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        // 🔥 STEP 2: UPDATE ONLY REQUIRED FIELDS

        existingUser.put("firstName", dto.getFirstName());
        existingUser.put("lastName", dto.getLastName());
        existingUser.put("email", dto.getEmail());
        existingUser.put("phone", dto.getPhone());
        existingUser.put("gpsimei", dto.getGpsimei());
        if (dto.getActivated() != null) {
            existingUser.put("activated", dto.getActivated());
        }

        // ✅ authorities
        existingUser.put("authorities", dto.getAuthorities());

        // ✅ geofences
        List<Map<String, Long>> geoList = new ArrayList<>();
        if (dto.getGeofences() != null) {
            for (Long id : dto.getGeofences()) {
                geoList.add(Map.of("id", id));
            }
        }
        existingUser.put("geofences", geoList);

        // ✅ reporting
        if (dto.getReportingTo() != null) {
            existingUser.put("ownedBy",
                    List.of(Map.of("id", dto.getReportingTo()))
            );
        }

        // 🔥 STEP 3: PUT FULL OBJECT

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(existingUser, headers);

        String putUrl = "https://sitpolycab.fiberify.com/api/users";

        System.out.println("🔥 FINAL FULL PAYLOAD: " + existingUser);

        ResponseEntity<Object> response = restTemplate.exchange(
                putUrl,
                HttpMethod.PUT,
                entity,
                Object.class
        );

        return response.getBody();
    }
 // ================================
 // ✅ ROLES
 // ================================
 public Object getRoles() {
     return callGetApi("https://sitpolycab.fiberify.com/api/configs/Roles");
 }

 // ================================
 // ✅ REPORTING USERS
 // ================================
 public Object getReportingUsers() {
     return callGetApi("https://sitpolycab.fiberify.com/api/reporting-users");
 }

 // ================================
 // ✅ GEOFENCES
 // ================================
 public Map<String, Object> getGeofences() {

	    List<Map<String, Object>> all =
	            (List<Map<String, Object>>) callGetApi(
	                    "https://sitpolycab.fiberify.com/api/master-mini-geofences"
	            );

	    List<Map<String, Object>> masters = new ArrayList<>();
	    List<Map<String, Object>> minis = new ArrayList<>();

	    if (all != null) {
	        for (Map<String, Object> g : all) {

	            String type = (String) g.get("geofenceType");

	            if ("MASTER".equalsIgnoreCase(type)) {
	                masters.add(g);
	            } else if ("MINI".equalsIgnoreCase(type)) {
	                minis.add(g);
	            }
	        }
	    }

	    Map<String, Object> result = new HashMap<>();
	    result.put("masters", masters);
	    result.put("minis", minis);

	    return result;
	}

 // ================================
 // 🔥 COMMON API CALL METHOD
 // ================================
 private Object callGetApi(String url) {

     HttpHeaders headers = new HttpHeaders();
     headers.set("Authorization", "Bearer " + token);

     HttpEntity<String> entity = new HttpEntity<>(headers);

     return restTemplate.exchange(
             url,
             HttpMethod.GET,
             entity,
             Object.class
     ).getBody();
 }
 public Object bulkUpdateReporting(BulkUpdateDTO dto) {

	    List<String> logins = dto.getLogins();
	    Long reportingId = dto.getReportingTo();

	    for (String login : logins) {

	        // fetch user
	    	Map<String, Object> user = (Map<String, Object>) callGetApi(
	    		    "https://sitpolycab.fiberify.com/api/users/" + login
	    		);

	        // update reporting
	    	Map<String, Object> reporting = new HashMap<>();
	    	reporting.put("id", reportingId);

	    	List<Map<String, Object>> ownedBy = new ArrayList<>();
	    	ownedBy.add(reporting);

	    	user.put("ownedBy", ownedBy);

	        // call PUT API
	        callPutApi(
	            "https://sitpolycab.fiberify.com/api/users",
	            user
	        );
	    }

	    return Map.of(
	        "status", "SUCCESS",
	        "updatedUsers", logins
	    );
	}
 private void callPutApi(String url, Object body) {

	    HttpHeaders headers = new HttpHeaders();
	    headers.set("Authorization", "Bearer " + token);
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    HttpEntity<Object> entity = new HttpEntity<>(body, headers);

	    restTemplate.exchange(
	        url,
	        HttpMethod.PUT,
	        entity,
	        Object.class
	    );
	}
 public List<Map<String, Object>> getRootUsers() {

	    List<Map<String, Object>> users = getAllUsers();
	    List<Map<String, Object>> roots = new ArrayList<>();

	    for (Map<String, Object> u : users) {

	        String reportingTo = (String) u.get("reportingTo");

	        if (reportingTo == null || reportingTo.isEmpty()) {
	            u.put("hasChildren", hasChildren(users, (String) u.get("login")));
	            roots.add(u);
	        }
	    }

	    return roots;
	}
 public List<Map<String, Object>> getChildren(String login) {

	    List<Map<String, Object>> users = getAllUsers();

	    List<Map<String, Object>> children = new ArrayList<>();

	    for (Map<String, Object> u : users) {

	        String reportingTo = (String) u.get("reportingTo");

	        if (login.equals(reportingTo)) {

	            u.put("hasChildren", hasChildren(users, (String) u.get("login")));
	            children.add(u);
	        }
	    }

	    return children;
	}
 private boolean hasChildren(List<Map<String, Object>> users, String login) {

	    for (Map<String, Object> u : users) {
	        String reportingTo = (String) u.get("reportingTo");

	        if (login.equals(reportingTo)) {
	            return true;
	        }
	    }

	    return false;
	}
 public List<Map<String, Object>> getAllUsers() {

	    List<UserSummaryDTO> dtos = getUsersSummary();

	    List<Map<String, Object>> users = new ArrayList<>();

	    for (UserSummaryDTO dto : dtos) {

	        Map<String, Object> map = new HashMap<>();

	        map.put("login", dto.getLogin());
	        map.put("reportingTo", dto.getReportingTo());

	        users.add(map);
	    }

	    return users;
	}
}