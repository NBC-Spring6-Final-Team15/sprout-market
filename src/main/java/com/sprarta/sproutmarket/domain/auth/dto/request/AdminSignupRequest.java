package com.sprarta.sproutmarket.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminSignupRequest {

    @NotBlank
    private String username;

    @NotBlank @Email
    private String email;

    private int authNumber;

    @NotBlank
    private String password;

    @NotBlank
    private String nickname;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String adminKey;
}