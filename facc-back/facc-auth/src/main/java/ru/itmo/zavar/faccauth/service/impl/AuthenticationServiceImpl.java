package ru.itmo.zavar.faccauth.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.faccauth.entity.RoleEntity;
import ru.itmo.zavar.faccauth.entity.UserEntity;
import ru.itmo.zavar.faccauth.service.AuthenticationService;
import ru.itmo.zavar.faccauth.service.JwtService;
import ru.itmo.zavar.faccauth.service.RoleService;
import ru.itmo.zavar.faccauth.service.UserService;
import ru.itmo.zavar.faccauth.util.RoleConstants;

import java.util.Optional;
import java.util.Set;

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
        if (roleService.getUserRole().isEmpty())
            roleService.saveRole(new RoleEntity(2L, RoleConstants.USER));
        Optional<RoleEntity> optionalRoleEntity = roleService.getAdminRole();
        RoleEntity adminRole = optionalRoleEntity.orElseGet(() -> roleService.saveRole(new RoleEntity(0L, RoleConstants.ADMIN)));
        if (userService.findByUsername("admin").isEmpty()) {
            UserEntity admin = UserEntity.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole)).build();
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
    public String signIn(String username, String password) throws IllegalArgumentException {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        var user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return jwtService.generateToken(user);
    }

    @Override
    public void changeRole(String username, String role) {
        Optional<UserEntity> optionalUserEntity = userService.findByUsername(username);
        UserEntity userEntity = optionalUserEntity.orElseThrow();
        Optional<RoleEntity> optionalRoleEntity = roleService.findByName(role);
        RoleEntity roleEntity = optionalRoleEntity.orElseThrow();
        userEntity.getRoles().clear();
        userEntity.getRoles().add(roleEntity);
        userService.saveUser(userEntity);
    }
}
