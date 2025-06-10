package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final List<User> users = new ArrayList<>();

    public UserService() {
        // 預設兩個用戶
        users.add(new User("worker1", "pass", "worker"));
        users.add(new User("manager1", "pass", "manager"));
    }

    public User authenticate(String username, String password, String role) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username)
                        && u.getPassword().equals(password)
                        && u.getRole().equals(role))
                .findFirst()
                .orElse(null);
    }
} 
