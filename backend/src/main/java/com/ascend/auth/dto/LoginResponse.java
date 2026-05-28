package com.ascend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private UUID id;
    private String username;
    private String email;
    private Integer level;
    private Long xp;
    private String league;
    private Boolean premium;
    private String avatarUrl;
}
