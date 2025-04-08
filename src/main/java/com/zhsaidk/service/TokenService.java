package com.zhsaidk.service;

import com.zhsaidk.database.entity.Token;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    @Value("${ACCESS_TOKEN_EXPIRATION}")
    private final Integer ACCESS_TOKEN_EXPIRATION;  // 15 минут
    @Value("${REFRESH_TOKEN_EXPIRATION}")
    private final Integer REFRESH_TOKEN_EXPIRATION;  // 24 часа
    @Value("${JWT_SECRET}")
    private final String SECRET;
    @Value("${ISSUER}")
    private final String ISSUER;

    private final TokenRepository tokenRepository;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return generateToken(user, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, REFRESH_TOKEN_EXPIRATION);
    }

    private String generateToken(User user, long expiration) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .claim("role", user.getRole().name())
                .signWith(secretKey)
                .compact();
    }

    public Claims validateToken(String token) {
        log.info("Проверяется токен ...");
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Token generateTokenByUser(User user) {
        String refreshToken = generateRefreshToken(user);
        return Token.builder()
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    public Optional<Token> findByRefreshToken(String token) {
        return tokenRepository.findTokenByRefreshToken(token);
    }
}
