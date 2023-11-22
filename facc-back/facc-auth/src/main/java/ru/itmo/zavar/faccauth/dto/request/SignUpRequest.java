package ru.itmo.zavar.faccauth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.itmo.zavar.faccauth.security.ValidPassword;

public record SignUpRequest(@NotBlank @Size(min = 5, max = 25) String username, @ValidPassword @NotBlank String password) {
}
