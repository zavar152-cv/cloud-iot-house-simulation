package ru.itmo.zavar.faccauth.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.itmo.zavar.faccauth.dto.JwtDTO;
import ru.itmo.zavar.faccauth.dto.UserDTO;

import java.util.List;
import java.util.NoSuchElementException;

public interface AuthenticationService {
    void signUp(String username, String password) throws IllegalArgumentException, EntityNotFoundException;

    JwtDTO.Response.JwtDetails signIn(String username, String password) throws IllegalArgumentException;

    List<UserDTO.Response.UserDetails> getAllUserDetails();

    void grantAdmin(String username) throws IllegalArgumentException, UsernameNotFoundException, EntityNotFoundException;

    void revokeAdmin(String username) throws IllegalArgumentException, UsernameNotFoundException, EntityNotFoundException;

    void updateUserName(Long id, String newUsername) throws NoSuchElementException;

    void updateUserPassword(Long id, String newPassword) throws NoSuchElementException, IllegalArgumentException;

    boolean isTokenValid(String username, String token) throws UsernameNotFoundException;

    boolean isTokenValid(String token);

    UserDTO.Response.UserDetails getUserDetailsByToken(String token);
}
