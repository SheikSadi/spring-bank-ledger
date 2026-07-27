package com.paypay.learn.ledger;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;


@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {

        List<User> users = userService.getUsers();

        return ResponseEntity.ok().body(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {

        User user = userService.createUser(request);
        
        URI location = URI.create("/users/" + user.id());
        
        return ResponseEntity.created(location).body(
            CreateUserResponse.from(user)
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(
        @PathVariable String id
    ) {

        Optional<User> user = userService.findUserById(id);

        return user
            .map(u -> ResponseEntity.ok().body(u))
            .orElse(ResponseEntity.notFound().build())
        ;
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
        @PathVariable String id
    ) {

        Optional<User> user = userService.deleteUser(id);

        return user
            .map(u -> ResponseEntity.ok().body(u))
            .orElse(ResponseEntity.notFound().build())
        ;
    }

}
