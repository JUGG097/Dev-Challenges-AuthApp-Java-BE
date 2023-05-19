package com.example.base.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Nullable
    private String name;
    @Nullable
    private String bio;
    @Nullable
    private String image;
    @Nullable
    private String phoneNumber;
}
