package com.user.userextract.controller;

import com.user.userextract.dto.UserSummaryDTO;
import com.user.userextract.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*") // 🔥 allow frontend (React)
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ GET ALL USERS SUMMARY (FRONT PAGE)
    @GetMapping("/users-summary")
    public List<UserSummaryDTO> getUsersSummary() {
        return userService.getUsersSummary();
    }

    // ✅ GET PARTICULAR USER DETAILS (ON CLICK)
    @GetMapping("/user/{login}")
    public Map<String, Object> getUserDetails(@PathVariable String login) {
        return userService.getUserWithGeofence(login);
    }

    // ✅ GET DISTRICTS (MASTER GEOFENCES)
    @GetMapping("/districts")
    public List<Map<String, Object>> getDistricts() {
        return userService.getDistricts();
    }

    // ✅ GET BLOCKS BY DISTRICT
    @GetMapping("/blocks/{districtId}")
    public List<Map<String, Object>> getBlocks(@PathVariable String districtId) {
        return userService.getBlocksByDistrict(districtId);
    }
}