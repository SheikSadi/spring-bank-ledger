package com.paypay.learn.ledger;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AdminController {

    @PostMapping("/admin/users")
    public ResponseEntity<?> createUser() {
        String body = "User created";
        URI location = URI.create("/admin/user/new-user");
        return ResponseEntity.created(location).body(body);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<?> getUsers() {
        String body = "Fetched users";
        return ResponseEntity.ok().body(body);
    }

}
