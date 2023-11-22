package ru.itmo.zavar.faccauth.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.itmo.zavar.faccauth.dto.JwtDTO;
import ru.itmo.zavar.faccauth.dto.UserDTO;

public interface AuthenticationService {
    void addUser(String username, String password) throws IllegalArgumentException;

    JwtDTO.Response.JwtDetails signIn(String username, String password) throws IllegalArgumentException;

    void grantAdmin(String username) throws IllegalArgumentException;

    void revokeAdmin(String username) throws IllegalArgumentException;

    boolean isTokenValid(String username, String token) throws UsernameNotFoundException;

    UserDTO.Response.UserDetails getUserDetailsByToken(String token);
}
