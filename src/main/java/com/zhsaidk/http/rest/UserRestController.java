package com.zhsaidk.http.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class UserRestController {

    @GetMapping
    public String hello(){
        return "Hello";
    }
}
