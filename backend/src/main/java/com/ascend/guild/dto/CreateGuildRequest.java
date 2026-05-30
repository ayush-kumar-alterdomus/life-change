package com.ascend.guild.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuildRequest {

    @NotBlank(message = "Guild name is required")
    @Size(min = 3, max = 100, message = "Guild name must be between 3 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Guild type is required")
    @Pattern(regexp = "PUBLIC|PRIVATE|PREMIUM", message = "Type must be PUBLIC, PRIVATE, or PREMIUM")
    private String type;
}
