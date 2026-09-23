package com.example.student_management.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.student_management.service.UserService;
import com.example.student_management.utils.JwtUtil;
import com.example.student_management.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.student_management.User;
import com.example.student_management.common.Result;
import com.example.student_management.dto.LoginDTO;
import com.example.student_management.dto.RegisterDTO;
<<<<<<< HEAD
=======
import com.example.student_management.vo.LoginVO;
>>>>>>> eba346e246de06ab3546280529d5ec1aa5859624

@RestController 
@RequestMapping("/users")
@Tag (name="用户管理")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService=userService;
    }

    @Operation (summary = "用户注册")
    @PostMapping("/register")
    public Result register(@Valid @RequestBody  RegisterDTO registerDTO){
        User user=new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        return Result.success(userService.register(user));
    }
    
    @Operation (summary = "用户登录")
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDTO loginDTO){
        User loginUser=userService.login(
            loginDTO.getUsername(),
            loginDTO.getPassword()
        );
        String token=JwtUtil.generateToken(loginUser.getUsername(),loginUser.getRole());
        LoginVO loginVO=new LoginVO(loginUser.getUsername(),loginUser.getRole(),token);
        return Result.success(loginVO);
    }
}
