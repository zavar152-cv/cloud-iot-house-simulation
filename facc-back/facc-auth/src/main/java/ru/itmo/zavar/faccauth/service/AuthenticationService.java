package ru.itmo.zavar.faccauth.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.itmo.zavar.faccauth.dto.JwtDTO;
import ru.itmo.zavar.faccauth.dto.UserDTO;

public interface AuthenticationService {
    void signUp(String username, String password) throws IllegalArgumentException, EntityNotFoundException;

    JwtDTO.Response.JwtDetails signIn(String username, String password) throws IllegalArgumentException;

    void grantAdmin(String username) throws IllegalArgumentException, UsernameNotFoundException, EntityNotFoundException;

    void revokeAdmin(String username) throws IllegalArgumentException, UsernameNotFoundException, EntityNotFoundException;

    boolean isTokenValid(String username, String token) throws UsernameNotFoundException;

    boolean isTokenValid(String token);

    UserDTO.Response.UserDetails getUserDetailsByToken(String token);
}
