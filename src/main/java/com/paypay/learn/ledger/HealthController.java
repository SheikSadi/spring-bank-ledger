package com.paypay.learn.ledger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {
    @GetMapping("/ping")
    String ping() {
        return "pong";
    }
}
