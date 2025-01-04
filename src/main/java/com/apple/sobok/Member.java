package com.apple.sobok;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Member {
    @Id
    String id;

    String username;
    String password;
    String displayName;
}
