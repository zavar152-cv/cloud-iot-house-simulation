package ru.itmo.zavar.faccauth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChangeRoleRequest(@NotBlank @Size(min = 5, max = 25) String username, @NotBlank String role) {
}
