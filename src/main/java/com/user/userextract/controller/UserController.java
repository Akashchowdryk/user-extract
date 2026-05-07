package com.user.userextract.controller;

import com.user.userextract.dto.*;
import com.user.userextract.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.user.userextract.dto.UserEditDTO;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.http.HttpStatus;

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
    @PutMapping("/edit-user")
    public ResponseEntity<?> updateUser(@RequestBody UserEditDTO dto) {
        try {
            Object response = userService.updateUser(dto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(Map.of(
                            "status", "FAILED",
                            "message", e.getMessage()
                    ));
        }
    }
 // ✅ ROLES
    @GetMapping("/roles")
    public Object getRoles() {
        return userService.getRoles();
    }

    // ✅ REPORTING USERS
    @GetMapping("/reporting-users")
    public Object getReportingUsers() {
        return userService.getReportingUsers();
    }

    // ✅ GEOFENCES
    @GetMapping("/geofences")
    public Object getGeofences() {
        return userService.getGeofences();
    }
    @PutMapping("/bulk-update-reporting")
    public ResponseEntity<?> bulkUpdate(@RequestBody BulkUpdateDTO dto) {
        return ResponseEntity.ok(userService.bulkUpdateReporting(dto));
    }
    @GetMapping("/hierarchy/root")
    public List<Map<String, Object>> getRootUsers() {
        return userService.getRootUsers();
    }

    @GetMapping("/hierarchy/children/{login}")
    public List<Map<String, Object>> getChildren(@PathVariable String login) {
        return userService.getChildren(login);
    }
   
}