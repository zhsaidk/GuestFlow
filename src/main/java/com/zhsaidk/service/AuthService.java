package com.zhsaidk.service;

import com.zhsaidk.database.entity.Role;
import com.zhsaidk.database.entity.Token;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.TokenRepository;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.JwtAuthDto;
import com.zhsaidk.dto.JwtResponse;
import com.zhsaidk.dto.TokenDto;
import com.zhsaidk.exception.AppError;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService{
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    public ResponseEntity<?> createAuthToken(JwtAuthDto authDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDto.getUsername(), authDto.getPassword())
            );
        } catch (BadCredentialsException exception) {
            return new ResponseEntity<>(
                    new AppError(HttpStatus.UNAUTHORIZED.value(), "Некорректный логин или пароль"),
                    HttpStatus.UNAUTHORIZED
            );
        }

        User user = userRepository.findUserByEmail(authDto.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String accessToken = tokenService.generateAccessToken(user);
        Token token = tokenService.generateTokenByUser(user);

        tokenRepository.save(token);
        userRepository.save(user);

        return ResponseEntity.ok(new TokenDto(accessToken, token.getRefreshToken()));
    }

    public ResponseEntity<?> refreshToken(String refreshToken) {
        try {
            Claims claims = tokenService.validateToken(refreshToken);
            String email = claims.getSubject();

            User user = userRepository.findUserByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            if (tokenService.findByRefreshToken(refreshToken).isEmpty()) {
                return new ResponseEntity<>(
                        new AppError(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token"),
                        HttpStatus.UNAUTHORIZED
                );
            }

            String newAccessToken = tokenService.generateAccessToken(user);
            return ResponseEntity.ok(new TokenDto(newAccessToken, refreshToken));

        } catch (Exception e) {
            return new ResponseEntity<>(
                    new AppError(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token"),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @PostConstruct
    public void init(){

        if (userRepository.findUserByEmail("zhavokhir02@gmail.com").isEmpty()){
            User user = User.builder()
                    .firstName("Zhavokhir")
                    .lastName("Saitkulov")
                    .email("zhavokhir02@gmail.com")
                    .password("{noop}123")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(user);
        }
        else {
            log.info("Данные уже существует");
        }
    }
}
