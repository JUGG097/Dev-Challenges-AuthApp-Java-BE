package com.example.base.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubUserDetails {
    private String email;
    private String name;
    private String avatar_url;
}
