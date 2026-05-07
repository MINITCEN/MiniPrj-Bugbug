package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(nullable = false, length = 255)
    private String password;

    private String phoneNumber;
    private String address;

    @Column(nullable = false, length = 20)
    private String role;
}