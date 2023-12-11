package ru.itmo.zavar.faccauth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {
    String extractUserName(String token);

    String generateToken(UserDetails userDetails);

    Date extractExpiration(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isTokenValid(String token);
}
