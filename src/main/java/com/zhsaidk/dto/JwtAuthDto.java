package com.zhsaidk.dto;

import lombok.Value;

@Value
public class JwtAuthDto {
    String username;
    String password;
}
