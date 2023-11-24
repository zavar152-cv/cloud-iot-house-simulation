package ru.itmo.zavar.faccauth.service;

import ru.itmo.zavar.faccauth.entity.RoleEntity;

import java.util.Optional;

public interface RoleService {

    Optional<RoleEntity> findByName(String name);

    Optional<RoleEntity> getAdminRole();

    Optional<RoleEntity> getUserRole();

    RoleEntity saveRole(RoleEntity roleEntity);
}
