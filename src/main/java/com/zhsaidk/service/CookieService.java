package com.zhsaidk.service;

import com.zhsaidk.database.entity.Token;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {
    private final Integer ACCESS_TOKEN_MAX_AGE=3600;
    private final Integer REFRESH_TOKEN_MAX_AGE=3600;

    public void setCookies(HttpServletResponse response, String access_token, String refresh_token){
        response.addCookie(createCookie("access_token", access_token, ACCESS_TOKEN_MAX_AGE));
        response.addCookie(createCookie("refresh_token", refresh_token, REFRESH_TOKEN_MAX_AGE));
    }

    public void clearAllCookies(HttpServletResponse response){
        response.addCookie(clearCookie("access_token"));
        response.addCookie(clearCookie("refresh_token"));
    }

    private Cookie clearCookie(String cookieName){
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public Cookie createCookie(String cookieName, String token, Integer expires){
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(expires);
        return cookie;
    }
}
