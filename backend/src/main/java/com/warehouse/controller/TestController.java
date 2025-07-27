package com.warehouse.controller;

import org.springframework.web.bind.annotation.*;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello! 服务器运行正常";
    }

    @GetMapping("/time")
    public String currentTime() {
        return "当前时间: " + java.time.LocalDateTime.now();
    }
}
