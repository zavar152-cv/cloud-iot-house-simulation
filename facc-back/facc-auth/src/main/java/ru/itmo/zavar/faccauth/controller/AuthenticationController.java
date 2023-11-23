package ru.itmo.zavar.faccauth.controller;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
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
import ru.itmo.zavar.faccauth.util.RoleConstants;
import ru.itmo.zavar.faccauth.util.SpringErrorMessage;

@Tag(name = "AuthenticationController", description = "Provides methods for authentication, role changing and token validation")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Generates new JWT token for provided credentials and returns user info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User is found and JWT token is generated"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @PostMapping("/signIn")
    public ResponseEntity<JwtDTO.Response.JwtDetails> signIn(@Valid @RequestBody UserDTO.Request.SignIn request) {
        try {
            JwtDTO.Response.JwtDetails response = authenticationService.signIn(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Operation(summary = "Creates new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "User exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "Role not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDTO.Request.SignUp request) {
        try {
            authenticationService.signUp(request.getUsername(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (EntityNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Operation(summary = "Grants " + RoleConstants.ADMIN + " role to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User granted " + RoleConstants.ADMIN + " role"),
            @ApiResponse(responseCode = "400", description = "User already has " + RoleConstants.ADMIN + " role",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @PutMapping("/grantAdmin")
    public ResponseEntity<?> grantAdmin(@Valid @RequestBody UserDTO.Request.ChangeRole request) {
        try {
            authenticationService.grantAdmin(request.getUsername());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (UsernameNotFoundException | EntityNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Operation(summary = "Revokes " + RoleConstants.ADMIN + " role from user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User lost " + RoleConstants.ADMIN + " role"),
            @ApiResponse(responseCode = "400", description = "User didn't have " + RoleConstants.ADMIN + " role",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @PutMapping("/revokeAdmin")
    public ResponseEntity<?> revokeAdmin(@Valid @RequestBody UserDTO.Request.ChangeRole request) {
        try {
            authenticationService.revokeAdmin(request.getUsername());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (UsernameNotFoundException | EntityNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Operation(summary = "Validates JWT token by name and token value")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token is valid",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.Response.UserDetails.class))}),
            @ApiResponse(responseCode = "400", description = "JWT token is not valid",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @GetMapping("/validateJwtToken")
    public ResponseEntity<UserDTO.Response.UserDetails> validateJwtToken(@Valid @RequestBody JwtDTO.Request.JwtValidation request) {
        try {
            boolean tokenValid = authenticationService.isTokenValid(request.getUsername(), request.getJwtToken());
            if (!tokenValid)
                throw new JwtException("JWT token is not valid");
            return ResponseEntity.ok(authenticationService.getUserDetailsByToken(request.getJwtToken()));
        } catch (UsernameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
