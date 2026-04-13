package com.user.userextract.controller;

import com.user.userextract.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // 🔥 NEW API
    @GetMapping("/users-summary")
    public List<Map<String, Object>> getUsersSummary() {
        return service.getUsersSummary();
    }
}