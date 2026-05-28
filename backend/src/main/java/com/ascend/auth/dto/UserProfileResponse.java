package com.ascend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private Integer level;
    private Long xp;
    private String league;
    private Boolean premium;
    private Boolean hardMode;
    private String timezone;
    private LocalDateTime createdAt;
}
