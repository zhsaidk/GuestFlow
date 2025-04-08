package com.zhsaidk.exception;

import lombok.Data;
import lombok.Value;

import java.util.Date;

@Data
public class AppError {
    Integer status;
    String message;
    Date timestamp;

    public AppError(Integer status, String message){
        this.status = status;
        this.message = message;
        timestamp = new Date();
    }
}
