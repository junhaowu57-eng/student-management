package com.example.student_management;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class Student {
    private Integer id;
    @NotBlank (message="姓名不能为空")
    private String name;
    @Min (value=1,message="年龄不能小于1")
    private Integer age;
    private String gender;
    private String email;
    public Integer getId(){
        return id;
    }
    public void setId(Integer id){
        this.id=id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public Integer getAge(){
        return age;
    }
    public void setAge(Integer age){
        this.age=age;
    }
    public String getGender(){
        return gender;
    }
    public void setGender(String gender){
        this.gender=gender;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email=email;
    }
}
