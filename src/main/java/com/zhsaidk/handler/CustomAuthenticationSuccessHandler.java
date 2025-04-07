package com.zhsaidk.handler;

import com.zhsaidk.database.entity.Token;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.UserRepository;
import com.zhsaidk.service.CookieService;
import com.zhsaidk.service.TokenService;
import com.zhsaidk.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            User user = userRepository.findUserByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            String accessToken = tokenService.generateAccessToken(user);
            Token token = tokenService.generateTokenByUser(user);
            tokenService.save(token);

            // Устанавливаем куки (accessToken передаем напрямую)
            cookieService.setCookies(response, accessToken, token.getRefreshToken());

            response.setStatus(HttpStatus.OK.value());
            response.sendRedirect(request.getContextPath() + "/hello");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed");
        }
    }
}