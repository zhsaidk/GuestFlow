package com.zhsaidk.util;

import com.zhsaidk.database.entity.Token;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.service.CookieService;
import com.zhsaidk.service.TokenService;
import com.zhsaidk.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final TokenService tokenService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Map<String, String> cookies = extractCookies(request);

            if (cookies.containsKey(ACCESS_TOKEN)) {
                try {
                    String accessToken = cookies.get(ACCESS_TOKEN);
                    Claims claims = tokenService.validateToken(accessToken);
                    authenticateUser(claims.getSubject(), request);
                } catch (JwtException e) {
                    log.warn("Проблемы с access токеном: {}", e.getMessage());

                    if (cookies.containsKey(REFRESH_TOKEN)) {
                        try {
                            String refreshToken = cookies.get(REFRESH_TOKEN);
                            Claims claims = tokenService.validateToken(refreshToken);

                            Optional<Token> storedToken = tokenService.findByRefreshToken(refreshToken);

                            if (storedToken.isEmpty()) {
                                log.warn("Refresh token not found or invalidated");
                                cookieService.clearAllCookies(response);
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("Refresh token otozvan!");
                                return;
                            }


                            User user = userService.getUserByEmail(claims.getSubject());
                            String newAccessToken = tokenService.generateAccessToken(user);

                            cookieService.setCookies(response, newAccessToken, refreshToken);
                            authenticateUser(user.getEmail(), request);
                        } catch (JwtException ex) {
                            log.warn("Refresh token also invalid: {}", ex.getMessage());
                            cookieService.clearAllCookies(response);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("Invalid or expired refresh token");
                            return;
                        }
                    } else {
                        cookieService.clearAllCookies(response);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Access token expired and no refresh token present");
                        return;
                    }
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal server error");
        }
    }

    private Map<String, String> extractCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Map.of();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> ACCESS_TOKEN.equals(cookie.getName()) || REFRESH_TOKEN.equals(cookie.getName()))
                .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
    }

    private void authenticateUser(String email, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Исключить фильтр для определенных путей, например, /login и /register
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/login") || path.equals("/register");
    }
}