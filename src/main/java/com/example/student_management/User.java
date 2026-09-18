package com.example.student_management;

import jakarta.validation.constraints.NotBlank;
import com.example.student_management.enums.Role;

public class User {
    private Integer id;
    @NotBlank (message="用户名不能为空")
    private String username;
    @NotBlank (message="密码不能为空")
    private String password;
    private Role role;
    public Integer getId(){
        return id;
    }
    public void setId(Integer id){
        this.id=id;
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username=username;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password=password;
    }
    public Role getRole(){
        return role;
    }
    public void setRole(Role role){
        this.role=role;
    }
}
