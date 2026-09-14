package com.example.student_management.mapper;

import com.example.student_management.User;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
@Mapper 
public interface UserMapper {
    @Select ("SELECT*FROM user WHERE username=#{username}")
    User findByUsername(String username);
    @Insert ("INSERT INTO user(username,password) VALUES(#{username},#{password})")
    int insert(User user);
}
