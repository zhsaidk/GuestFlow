package com.zhsaidk.dto;

import lombok.Value;

@Value
public class TokenDto {
    String accessToken;
    String refreshToken;
}
