package com.example.student_management.config;

import com.example.student_management.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration 
public class WebConfig implements WebMvcConfigurer{
    private final JwtInterceptor jwtInterceptor;
    public WebConfig(JwtInterceptor jwtInterceptor){
        this.jwtInterceptor=jwtInterceptor;
    }

    @Override 
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/students/**");
    }
}
