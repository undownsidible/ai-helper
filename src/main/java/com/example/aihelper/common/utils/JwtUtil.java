package com.example.aihelper.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "aihelper";

    public static String createToken(Long userId){

        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(new Date(System.currentTimeMillis()+86400000))
                .sign(Algorithm.HMAC256(SECRET));
    }

}