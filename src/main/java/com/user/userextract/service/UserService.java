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

    // 🔥 YOUR TOKEN
    private final String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaWJlcmlmeWluYyIsImF1dGgiOiJST0xFX0JBLFJPTEVfT0EsUk9MRV9QTEFOX0FETUlOLFJPTEVfUk9MTE9VVF9BRE1JTixST0xFX1JPTExPVVRfTUFOQUdFUixST0xFX1VTRVJfQURNSU4iLCJleHAiOjE3Nzc2Mjc3NTF9.FWiSwm1QAgBvPiDCJT2f0NaZOQHr6oGPo5Z12xvc_QW9XStX4WYkQB1zrm-fO73aV95WStvqgt-CPHFFi7vsDg";

    // =====================================================
    // ✅ USERS SUMMARY (MAIN API)
    // =====================================================
    public List<UserSummaryDTO> getUsersSummary() {

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

                String login = (String) user.get("login");

                // 🔥 CALL DETAIL API
                String detailUrl = "https://sitpolycab.fiberify.com/api/users/" + login;

                ResponseEntity<Map> detailResponse = restTemplate.exchange(
                        detailUrl,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

                Map<String, Object> detail = detailResponse.getBody();

                UserSummaryDTO dto = new UserSummaryDTO();

                // BASIC
                dto.setLogin(login);
                dto.setName(user.get("firstName") + " " + user.get("lastName"));
                dto.setPhone((String) user.get("phone"));
                dto.setActivated((Boolean) user.get("activated"));

                // ✅ ROLES
                dto.setRoles((List<String>) user.get("authorities"));

                // ✅ VERSION
                dto.setVersion((String) user.get("applicationVersion"));

                // ✅ REPORTING TO
                if (detail != null) {
                    List<Map<String, Object>> ownedBy =
                            (List<Map<String, Object>>) detail.get("ownedBy");

                    if (ownedBy != null && !ownedBy.isEmpty()) {
                        dto.setReportingTo((String) ownedBy.get(0).get("login"));
                    }
                }

                // ✅ GEOFENCES
                if (detail != null) {
                    dto.setGeofenceNames(
                            (List<String>) detail.get("geofenceNames")
                    );
                }

                finalUsers.add(dto);
            }

            page++;
        }

        return finalUsers;
    }

    // =====================================================
    // ✅ USER DETAILS (ON CLICK)
    // =====================================================
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

    // =====================================================
    // ✅ DISTRICTS (MASTER GEOFENCES)
    // =====================================================
    public List<Map<String, Object>> getDistricts() {

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

        return response.getBody();
    }

    // =====================================================
    // ✅ BLOCKS BY DISTRICT
    // =====================================================
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