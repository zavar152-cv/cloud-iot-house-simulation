package ru.itmo.zavar.faccauth.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.faccauth.dto.JwtDTO;
import ru.itmo.zavar.faccauth.dto.UserDTO;
import ru.itmo.zavar.faccauth.entity.RoleEntity;
import ru.itmo.zavar.faccauth.entity.UserEntity;
import ru.itmo.zavar.faccauth.service.AuthenticationService;
import ru.itmo.zavar.faccauth.service.JwtService;
import ru.itmo.zavar.faccauth.service.RoleService;
import ru.itmo.zavar.faccauth.service.UserService;
import ru.itmo.zavar.faccauth.util.RoleConstants;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    @Transactional
    public void init() {
        Optional<RoleEntity> optionalUserRoleEntity = roleService.getAdminRole();
        RoleEntity userRole = optionalUserRoleEntity.orElseGet(() -> roleService.saveRole(new RoleEntity(1L, RoleConstants.USER)));
        Optional<RoleEntity> optionalAdminRoleEntity = roleService.getAdminRole();
        RoleEntity adminRole = optionalAdminRoleEntity.orElseGet(() -> roleService.saveRole(new RoleEntity(0L, RoleConstants.ADMIN)));
        if (userService.findByUsername(adminUsername).isEmpty()) {
            UserEntity admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(userRole, adminRole)).build();
            userService.saveUser(admin);
        }
    }

    @Override
    public void addUser(String username, String password) throws IllegalArgumentException {
        Optional<RoleEntity> roleUser = roleService.getUserRole();
        if (roleUser.isEmpty()) {
            throw new IllegalArgumentException("Role not found");
        }
        var user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser.get())).build();
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User exists");
        } else {
            userService.saveUser(user);
        }
    }

    @Override
    public JwtDTO.Response.JwtDetails signIn(String username, String password) throws IllegalArgumentException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        var user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtService.generateToken(user);
        Date date = jwtService.extractExpiration(token);
        return new JwtDTO.Response.JwtDetails(user.getId(), token, date, user.getUsername(), user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public void grantAdmin(String username) throws IllegalArgumentException {
        Optional<UserEntity> optionalUserEntity = userService.findByUsername(username);
        UserEntity userEntity = optionalUserEntity.orElseThrow(() -> new IllegalArgumentException("User not found"));
        Optional<RoleEntity> optionalRoleEntity = roleService.getAdminRole();
        RoleEntity roleEntity = optionalRoleEntity.orElseThrow(() -> new IllegalArgumentException("Role not found"));
        if(!userEntity.getRoles().contains(roleEntity)) {
            userEntity.getRoles().add(roleEntity);
            userService.saveUser(userEntity);
        } else {
            throw new IllegalArgumentException("User already has " + RoleConstants.ADMIN + " role");
        }
    }

    @Override
    public void revokeAdmin(String username) throws IllegalArgumentException {
        Optional<UserEntity> optionalUserEntity = userService.findByUsername(username);
        UserEntity userEntity = optionalUserEntity.orElseThrow(() -> new IllegalArgumentException("User not found"));
        Optional<RoleEntity> optionalRoleEntity = roleService.getAdminRole();
        RoleEntity roleEntity = optionalRoleEntity.orElseThrow(() -> new IllegalArgumentException("Role not found"));
        if(userEntity.getRoles().contains(roleEntity)) {
            userEntity.getRoles().remove(roleEntity);
            userService.saveUser(userEntity);
        } else {
            throw new IllegalArgumentException("User didn't have " + RoleConstants.ADMIN + " role");
        }
    }

    @Override
    public boolean isTokenValid(String username, String token) throws UsernameNotFoundException {
        UserDetails userDetails = userService.userDetailsService()
                .loadUserByUsername(username);
        return jwtService.isTokenValid(token, userDetails);
    }

    @Override
    public UserDTO.Response.UserDetails getUserDetailsByToken(String token) {
        String username = jwtService.extractUserName(token);
        UserEntity userDetails = (UserEntity) userService.userDetailsService()
                .loadUserByUsername(username);
        return new UserDTO.Response.UserDetails(userDetails.getId(), userDetails.getUsername(),
                userDetails.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toUnmodifiableSet()));
    }
}
