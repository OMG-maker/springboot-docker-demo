package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    public String generateToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
}

// package com.example.demo.security;

// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.KeySupplier;
// import java.util.Date;

// public class JwtUtil {
//     private static final KeySupplier HS256_KEY_SUPPLIER = Jwts.SIG.HS256;
//     private static final long EXPIRATION_TIME = 864_000_000; // 10 days

//     public static String generateToken(String username) {
//         return Jwts.builder()
//             .subject(username)
//             .issuedAt(new Date())
//             .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//             .signWith(HS256_KEY_SUPPLIER.key())
//             .compact();
//     }

//     public static String getUsernameFromToken(String token) {
//         return Jwts.parser()
//             .verifyWith(HS256_KEY_SUPPLIER.key())
//             .build()
//             .parseSignedClaims(token)
//             .getPayload()
//             .getSubject();
//     }
// }


