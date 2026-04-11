package com.user.userextract.controller;

import com.user.userextract.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("https://user-frontend-ddg1.onrender.com")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all-users")
    public List<Map<String, Object>> getAllUsers() {
        return userService.getAllUsers();
    }
}