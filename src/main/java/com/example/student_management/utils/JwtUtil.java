package com.example.student_management.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import com.example.student_management.enums.Role;
import io.jsonwebtoken.Claims;

public class JwtUtil {
    private static final SecretKey SECRET_KEY=
            Keys.hmacShaKeyFor(
                "student_management_secret_key_student_management_secret_key".getBytes()
            );
    public static String generateToken(String username,Role role){
        return Jwts.builder()
                .subject(username)
                .claim("role",role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+1000*60*60))
                .signWith(SECRET_KEY )
                .compact();
    }
    public static Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
