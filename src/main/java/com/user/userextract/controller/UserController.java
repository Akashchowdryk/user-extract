package com.user.userextract.controller;

import com.user.userextract.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // ✅ FAST USERS
    @GetMapping("/all-users")
    public List<Map<String, Object>> getUsers() {
        return userService.getAllUsers();
    }

    // ✅ USER DETAILS WITH GEOFENCE
    @GetMapping("/user/{login}")
    public Map<String, Object> getUserDetails(@PathVariable String login) {
        return userService.getUserWithGeofence(login);
    }
}