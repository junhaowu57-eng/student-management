package com.example.student_management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 这个注解只能加在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，方便拦截器通过反射读取
public @interface RequireRole {

    // 使用时写成 @RequireRole("ADMIN")
    String value();
}
