package com.zhsaidk.http.rest;

import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.dto.JwtAuthDto;
import com.zhsaidk.dto.JwtResponse;
import com.zhsaidk.exception.AppError;
import com.zhsaidk.service.AuthService;
import com.zhsaidk.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class AuthRestController {
    private final AuthService authService;

    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody JwtAuthDto authDto){
        return authService.createAuthToken(authDto);
    };

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody JwtResponse jwtResponse) {
        return authService.refreshToken(jwtResponse.getToken());
    }
}





















