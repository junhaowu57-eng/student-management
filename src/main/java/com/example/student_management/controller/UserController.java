package com.example.student_management.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.student_management.service.UserService;
import com.example.student_management.utils.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.student_management.User;
import com.example.student_management.common.Result;

@RestController 
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/register")
    public Result register(@Valid @RequestBody User user){
        return Result.success(userService.register(user));
    }
    
    @PostMapping("/login")
    public Result login(@Valid @RequestBody User user){
        User loginUser=userService.login(
            user.getUsername(),
            user.getPassword()
        );
        String token=JwtUtil.generateToken(loginUser.getUsername(),loginUser.getRole());
        return Result.success(token);
    }
}
