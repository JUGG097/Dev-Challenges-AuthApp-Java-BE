package com.example.base.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
    })
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @NotBlank
    private String email;
    private String password;
    private String bio;
    private String image;
    private String phoneNumber;
}
