package com.MoneyLog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/api/test/protected")
    public String protectedEndpoint() {
        return "인증 성공!";
    }
}
