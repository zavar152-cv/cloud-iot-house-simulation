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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.faccauth.dto.JwtDTO;
import ru.itmo.zavar.faccauth.dto.UserDTO;
import ru.itmo.zavar.faccauth.service.AuthenticationService;
import ru.itmo.zavar.faccauth.service.CloudLoggingService;
import ru.itmo.zavar.faccauth.util.RoleConstants;
import ru.itmo.zavar.faccauth.util.SpringErrorMessage;
import yandex.cloud.api.logging.v1.LogEntryOuterClass;

@Tag(name = "AuthenticationController", description = "Provides methods for authentication, role changing and token validation")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AuthenticationController")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final CloudLoggingService cloudLoggingService;

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
            log.info("User {} with id {} logged in", response.getUsername(), response.getId());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "User {} with id {} logged in", response.getUsername(), response.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Operation(summary = "Creates new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "409", description = "User exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "Role not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDTO.Request.SignUp request) {
        try {
            authenticationService.signUp(request.getUsername(), request.getPassword());
            log.info("User {} created", request.getUsername());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "User {} created", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (EntityNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Operation(summary = "Grants " + RoleConstants.ADMIN + " role to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User granted " + RoleConstants.ADMIN + " role"),
            @ApiResponse(responseCode = "409", description = "User already has " + RoleConstants.ADMIN + " role",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @PutMapping("/grantAdmin")
    public ResponseEntity<?> grantAdmin(@Valid @RequestBody UserDTO.Request.ChangeRole request) {
        try {
            authenticationService.grantAdmin(request.getUsername());
            log.info("Granted {} role to user {}", RoleConstants.ADMIN, request.getUsername());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "Granted {} role to user {}", RoleConstants.ADMIN, request.getUsername());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (UsernameNotFoundException | EntityNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @Operation(summary = "Revokes " + RoleConstants.ADMIN + " role from user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User lost " + RoleConstants.ADMIN + " role"),
            @ApiResponse(responseCode = "409", description = "User didn't have " + RoleConstants.ADMIN + " role",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @PutMapping("/revokeAdmin")
    public ResponseEntity<?> revokeAdmin(@Valid @RequestBody UserDTO.Request.ChangeRole request) {
        try {
            authenticationService.revokeAdmin(request.getUsername());
            log.info("Revoked {} role from user {}", RoleConstants.ADMIN, request.getUsername());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "Revoked {} role from user {}", RoleConstants.ADMIN, request.getUsername());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
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
            UserDTO.Response.UserDetails userDetailsByToken = authenticationService.getUserDetailsByToken(request.getJwtToken());
            log.info("User {} has valid token {} and got details", request.getUsername(), request.getJwtToken());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "User {} has valid token {} and got details", request.getUsername(), request.getJwtToken());
            return ResponseEntity.ok(userDetailsByToken);
        } catch (UsernameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
