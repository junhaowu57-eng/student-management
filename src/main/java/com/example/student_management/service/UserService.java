package com.example.student_management.service;

import com.example.student_management.mapper.UserMapper;
import org.springframework.stereotype.Service;
import com.example.student_management.User;
import com.example.student_management.exception.BusinessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service 
public class UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
    public UserService(UserMapper userMapper){
        this.userMapper=userMapper;
    }
    public int register(User user){
        User existingUser=userMapper.findByUsername(user.getUsername());
        if(existingUser!=null){
            throw new BusinessException(400, "用户名已存在");
        }

        String encodePassword =passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        return userMapper.insert(user);
    }
    public User login(String username,String password){
        User user=userMapper.findByUsername(username);
        if(user==null){
            throw new BusinessException(400, "用户名或密码错误");
        }
        boolean matched=passwordEncoder.matches(password, user.getPassword());
        if(!matched){
            throw new BusinessException(400, "用户名或密码错误");
        }

        return user;
    }
}
