package com.example.student_management.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.example.student_management.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.web.method.HandlerMethod;
import com.example.student_management.annotation.RequireRole;
import com.example.student_management.common.Result;
import tools.jackson.databind.ObjectMapper;

@Component 
public class JwtInterceptor implements HandlerInterceptor{
    private final ObjectMapper objectMapper;
    public JwtInterceptor(ObjectMapper objectMapper){
        this.objectMapper=objectMapper;
    }

    /**
    * 统一返回拦截器中的错误信息
    * 避免 401、403 重复写 JSON 返回代码
    */
    private boolean writeError(
        HttpServletResponse response,
        int code,
        String message) throws Exception {

        // 设置 HTTP 状态码，例如 401 / 403
        response.setStatus(code);

        // 告诉前端返回的是 JSON，并使用 UTF-8 防止中文乱码
        response.setContentType("application/json;charset=UTF-8");

        // 创建项目统一的 Result 错误对象
        Result result = Result.error(code, message);

        // 将 Java 的 Result 对象转换成 JSON 字符串
        String json = objectMapper.writeValueAsString(result);

        // 把 JSON 写回客户端
        response.getWriter().write(json);

        // false 表示拦截请求，不再进入 Controller
        return false;
    }

    @Override 
    public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception{
        if("OPTIONS".equalsIgnoreCase(request.getMethod())){
            return true;
        }
        String authHeader=request.getHeader("Authorization");
        if(authHeader==null||!authHeader.startsWith("Bearer ")){
            return writeError(response, 401, "未登录或无效token");
        }
        String token=authHeader.substring(7);
        try{
            Claims claims=JwtUtil.parseToken(token);
            String username=claims.getSubject();
            String role =claims.get("role",String.class);
            
            request.setAttribute("role",role);
            request.setAttribute("username",username);
            // 判断当前请求最终要执行的是不是 Controller 方法
            if (handler instanceof HandlerMethod) {

                HandlerMethod handlerMethod = (HandlerMethod) handler;

                // 读取 Controller 方法上的 @RequireRole 注解
                RequireRole requireRole =handlerMethod.getMethodAnnotation(RequireRole.class);

                // 有注解才需要进行角色权限判断
                if (requireRole != null) {

                    // 获取接口要求的角色，例如 ADMIN
                    String requiredRole = requireRole.value();

                    // 当前用户角色和接口要求角色不一致，拒绝访问
                    if (!requiredRole.equals(role)) {
                        return writeError(response, 403, "没有权限");
                    }
                }
            }
        }catch(Exception e){
            return writeError(response, 401, "未登录或无效token");
        }
        return true;
    }
}
