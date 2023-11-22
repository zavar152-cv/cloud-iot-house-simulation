package ru.itmo.zavar.faccauth.dto.response;

import lombok.Builder;

@Builder
public record JwtAuthenticationResponse(String token) {
}
