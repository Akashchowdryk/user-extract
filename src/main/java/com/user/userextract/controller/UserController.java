package com.user.userextract.controller;

import com.user.userextract.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users-summary")
    public List<Map<String, Object>> getUsers() {
        return userService.getAllUsers();
    }

    // 🔥 FIXED (THIS WAS MISSING / WRONG)
    @GetMapping("/user/{login}")
    public Map<String, Object> getUser(@PathVariable String login) {
        return userService.getUserDetails(login);
    }

    @GetMapping("/districts")
    public List<Map<String, Object>> getDistricts() {
        return userService.getDistricts();
    }

    @GetMapping("/blocks/{id}")
    public List<Map<String, Object>> getBlocks(@PathVariable Long id) {
        return userService.getBlocks(id);
    }
}