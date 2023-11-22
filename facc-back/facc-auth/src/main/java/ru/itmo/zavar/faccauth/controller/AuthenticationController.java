package ru.itmo.zavar.faccauth.controller;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.faccauth.dto.JwtDTO;
import ru.itmo.zavar.faccauth.dto.UserDTO;
import ru.itmo.zavar.faccauth.service.AuthenticationService;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/signIn")
    public ResponseEntity<JwtDTO.Response.JwtDetails> signIn(@Valid @RequestBody UserDTO.Request.SignIn request) {
        try {
            JwtDTO.Response.JwtDetails response = authenticationService.signIn(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDTO.Request.SignUp request) {
        try {
            authenticationService.addUser(request.getUsername(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PostMapping("/grantAdmin")
    public ResponseEntity<?> grantAdmin(@Valid @RequestBody UserDTO.Request.ChangeRole request) {
        try {
            authenticationService.grantAdmin(request.getUsername());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PostMapping("/revokeAdmin")
    public ResponseEntity<?> revokeAdmin(@Valid @RequestBody UserDTO.Request.ChangeRole request) {
        try {
            authenticationService.revokeAdmin(request.getUsername());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/validateJwtToken")
    public ResponseEntity<UserDTO.Response.UserDetails> validateJwtToken(@Valid @RequestBody JwtDTO.Request.JwtValidation request) {
        try {
            boolean tokenValid = authenticationService.isTokenValid(request.getUsername(), request.getJwtToken());
            if(!tokenValid)
                throw new JwtException("JWT token is not valid");
            return ResponseEntity.ok(authenticationService.getUserDetailsByToken(request.getJwtToken()));
        } catch (UsernameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
