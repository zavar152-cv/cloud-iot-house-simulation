package ru.itmo.zavar.faccauth.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.itmo.zavar.faccauth.entity.UserEntity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface UserService {
    UserDetailsService userDetailsService();

    UserEntity saveUser(UserEntity userEntity);

    void deleteUser(Long id) throws NoSuchElementException;

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);

    List<UserEntity> findAll();
}
