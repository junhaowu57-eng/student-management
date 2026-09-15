package com.example.student_management.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.student_management.utils.JwtUtil;


@Component 
public class JwtInterceptor implements HandlerInterceptor{
    @Override 
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception{
        String authHeader=request.getHeader("Authorization");
        if(authHeader==null||!authHeader.startsWith("Bearer ")){
            response.setStatus(401);
            return false;
        }
        String token=authHeader.substring(7);
        try{
            String username=JwtUtil.parseToken(token);
            request.setAttribute("username",username);
        }catch(Exception e){
            response.setStatus(401);
            return false;
        }
        return true;
    }
}
