package com.user.userextract.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.user.userextract.service.*;
import com.fasterxml.jackson.databind.JsonNode;
@RestController
@RequestMapping("/api/edit")
@CrossOrigin("*")
public class UserEditController {

    @Autowired
    private UserEditService service;

    // ✅ ROLES
    @GetMapping("/roles")
    public JsonNode getRoles() {
        return service.getRoles();
    }

    // ✅ REPORTING USERS
    @GetMapping("/reporting")
    public JsonNode getReportingUsers() {
        return service.getReportingUsers();
    }

    // ✅ USER GROUPS (optional)
    @GetMapping("/groups")
    public JsonNode getGroups() {
        return service.getGroups();
    }

    // ✅ UPDATE USER
    @PutMapping("/user")
    public ResponseEntity<String> updateUser(@RequestBody String payload) {
        return service.updateUser(payload);
    }
}