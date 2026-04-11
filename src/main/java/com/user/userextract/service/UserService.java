package com.user.userextract.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "https://sitpolycab.fiberify.com/api/users";

    // 🔐 Paste your Bearer token here
    private final String TOKEN = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmaWJlcmlmeWluYyIsImF1dGgiOiJST0xFX0JBLFJPTEVfT0EsUk9MRV9QTEFOX0FETUlOLFJPTEVfUk9MTE9VVF9BRE1JTixST0xFX1JPTExPVVRfTUFOQUdFUixST0xFX1VTRVJfQURNSU4iLCJleHAiOjE3Nzc2Mjc3NTF9.FWiSwm1QAgBvPiDCJT2f0NaZOQHr6oGPo5Z12xvc_QW9XStX4WYkQB1zrm-fO73aV95WStvqgt-CPHFFi7vsDg";

    public List<Map<String, Object>> getAllUsers() {

        List<JsonNode> allUsers = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        int page = 0;
        int size = 50;
        boolean hasMore = true;

        while (hasMore) {

            String url = BASE_URL
                    + "?page=" + page
                    + "&size=" + size
                    + "&sort=last_modified_date,desc";

            try {

                // 🔐 Headers
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", TOKEN);
                headers.set("Accept", "application/json");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                String.class
                        );

                JsonNode root = mapper.readTree(response.getBody());

                // ✅ API returns direct array
                JsonNode content = root;

                System.out.println("PAGE: " + page + " SIZE: " + content.size());

                if (content.size() == 0) {
                    hasMore = false;
                } else {
                    content.forEach(allUsers::add);
                    page++;
                }

            } catch (Exception e) {
                e.printStackTrace();
                hasMore = false;
            }
        }
        List<Map<String, Object>> cleanUsers = new ArrayList<>();

        for (JsonNode user : allUsers) {
            Map<String, Object> map = mapper.convertValue(user, Map.class);
            cleanUsers.add(map);
        }

        return cleanUsers;
    }
}