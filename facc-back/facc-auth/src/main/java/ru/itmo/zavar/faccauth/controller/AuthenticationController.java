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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

import java.util.List;
import java.util.NoSuchElementException;

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

    @Operation(summary = "Returns all users info")
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO.Response.UserDetails>> getAllUsers () {
        return ResponseEntity.ok(authenticationService.getAllUserDetails());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable @NotNull @Positive Long id) {
        try {
            authenticationService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Updates user's name")
    @PutMapping("/users/{id}/name")
    public ResponseEntity<?> updateUserName(@PathVariable @NotNull @Positive Long id, @Valid @RequestBody UserDTO.Request.ChangeName changeName) {
        try {
            authenticationService.updateUserName(id, changeName.getUsername());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Updates user's password")
    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> updateUserPassword(@PathVariable @NotNull @Positive Long id, @Valid @RequestBody UserDTO.Request.ChangePassword changePassword) {
        try {
            authenticationService.updateUserPassword(id, changePassword.getPassword());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
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
            authenticationService.grantAdmin(request.getId());
            log.info("Granted {} role to user with id {}", RoleConstants.ADMIN, request.getId());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "Granted {} role to user with id {}", RoleConstants.ADMIN, request.getId());
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
            authenticationService.revokeAdmin(request.getId());
            log.info("Revoked {} role from user with id {}", RoleConstants.ADMIN, request.getId());
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "Revoked {} role from user with id {}", RoleConstants.ADMIN, request.getId());
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
            @ApiResponse(responseCode = "401", description = "JWT token is not valid",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SpringErrorMessage.class))})})
    @GetMapping("/auth")
    public ResponseEntity<UserDTO.Response.UserDetails> validateJwtToken(@RequestHeader(name = "Authorization", required = false) @Valid @NotBlank String authorizationHeader) {
        try {
            if(authorizationHeader == null)
                throw new JwtException("Header 'Authorization' is not present");
            String jwt = authorizationHeader.substring(7);
            boolean tokenValid = authenticationService.isTokenValid(jwt);
            if (!tokenValid)
                throw new JwtException("JWT token is not valid");
            UserDTO.Response.UserDetails userDetailsByToken = authenticationService.getUserDetailsByToken(jwt);
            log.info("User has valid token {} and got details", jwt);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "AuthenticationService",
                    "User has valid token {} and got details", jwt);
            return userDetailsByToken.getRoles().contains(RoleConstants.ADMIN) ?
                    ResponseEntity.status(HttpStatus.OK).header("X_isAdmin", String.valueOf(userDetailsByToken.getRoles().contains(RoleConstants.ADMIN))).body(userDetailsByToken) :
                    ResponseEntity.status(HttpStatus.OK).body(userDetailsByToken);
        } catch (UsernameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (JwtException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }
}
