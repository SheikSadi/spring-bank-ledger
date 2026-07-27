package com.paypay.learn.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private String id;

    @Column(nullable = true)
    private String userName;

    @Column(nullable = true)
    private String firstName;

    
    @Column(nullable = true)
    private String lastName;

    
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String passwordHash;

    protected UserEntity() {}

    public UserEntity(
        String id,
        String userName,
        String email,
        String firstName,
        String lastName,
        String role,
        String passwordHash
    ) {
        this.id = id;
        this.email = email;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public static UserEntity fromDomain(User domain) {
        return new UserEntity(
            domain.id(),
            domain.userName(),
            domain.email(),
            domain.firstName(),
            domain.lastname(),
            domain.role(),
            domain.passwordHash()
        );
    }

    public User toDomain() {
        return new User(
            this.id,
            this.userName,
            this.email,
            this.firstName,
            this.lastName,
            this.role,
            this.passwordHash
        );
    }    

}
