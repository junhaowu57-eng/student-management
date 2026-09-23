package com.example.student_management.vo;

import com.example.student_management.enums.Role;

public class LoginVO {
    private String username;
    private Role role;
    private String token;
    public LoginVO(String username,Role role,String token){
        this.username=username;
        this.role=role;
        this.token=token;
    }
    public String getUsername(){
        return username;
    }
    public Role getRole(){
        return role;
    }
    public String getToken(){
        return token;
    }
}
